package sarchat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import sarchat.message.AckMessage;
import sarchat.message.AckMulticastMessage;
import sarchat.message.BurstMessage;
import sarchat.message.HeartBeatMessage;
import sarchat.message.JoinMessage;
import sarchat.message.LogicalClock;
import sarchat.message.Message;
import sarchat.message.MessageToDeliverQueue;
import sarchat.message.MessageToDeliverQueue.Tuple;
import sarchat.message.MulticastMessage;
import sarchat.message.TextMessage;
import sarchat.message.UnicastMessage;
import sarchat.utils.ConnectionHelper;

public class Peer extends NIOSocketAgent {

    private LogicalClock myClock;
    private MessageToDeliverQueue msgQueue;
    private User server;
    private User me;
    private GroupTable myGroup;
    public static final Logger log = Logger.getAnonymousLogger();
    Timer joinTimer = new Timer(true);
    final long serverTimout = 10000;//10s
    private PeerEventListener listener;
    private boolean inBurst;
    //Pour udp heartbeat
    private ByteBuffer dataByteBufferUDP;
    private DatagramChannel dataChan;
    private HashMap<User, Timer> mapUserTimer;
    private long heartBeatTimeout = 10000;
    private long timeBetweenHeartBeats = 1000;
    private Timer heartBeatTimer;

    public Peer(String myName, final List<String> groupToJoin) {
        super();
        myGroup = new GroupTable();
        heartBeatTimer = new Timer(true);
        for (String userName : groupToJoin) {
            myGroup.add(new User(userName));
        }
        int port = ConnectionHelper.getAvailablePort();

        try {
            me = new User(myName, port);

            server = new User("server", InetAddress.getLocalHost(), Server.serverPort);
            //Se connecter au serveur
            initConnection(server);
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
            assert false;
        }
        //init multicast transmition
        msgQueue = new MessageToDeliverQueue();
        myClock = new LogicalClock();
    }

    public void createListenSocket() throws IOException {
        createServerSocket(me.port);
    }

    //Initiate a connection with another user or the server
    private void initConnection(User connectMeWith) {
//        System.out.println(me.name + "CONNEXION: " + me.name + " with " + connectMeWith.name);
        if (userSocketChannelMap.get(connectMeWith) == null) {
            try {
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                socketChannel.socket().setTcpNoDelay(true);
                socketChannel.connect(new InetSocketAddress(connectMeWith.ip, connectMeWith.port));
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
                userSocketChannelMap.put(connectMeWith, socketChannel);
            } catch (IOException ex) {
                Logger.getLogger(NIOSocketAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * send the text message "msg" to the user specified in "sendTo"
     */
    public void sendTextMessage(String string) throws IOException {
        TextMessage msg = new TextMessage(string, myClock.incrementClock(), me);
        for (User user : myGroup) {
            sendMessage(user, msg);
        }
    }

    public void sendJoinGroupMessage() throws IOException {
        sendMessage(server, new JoinMessage(me, myGroup));
        //set timout
        joinTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendJoinGroupMessage();
                } catch (IOException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            }
        }, serverTimout);
    }

    void sendBurstMessage() throws IOException {
        BurstMessage msg = new BurstMessage(myClock.incrementClock(), me);
        for (User user : myGroup) {
            sendMessage(user, msg);
        }
    }

    public boolean isInBurst() {
        return inBurst;
    }

    public void startBurst() {
        inBurst = true;
        doBurst();
        System.out.println("BURST START");
    }

    public void stopBurst() {
        inBurst = false;
    }

    private void doBurst() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int var = 0;
                while (inBurst) {
                    try {
                        sendTextMessage(Integer.toString(var++));
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.out.println("BURST STOP");
            }
        }).start();
    }

    public void messageReceived(User from, Message msg) {
        assert (msg != null);
      //  System.out.println(me.name + " RECEIVED\t" + msg);

        if (msg instanceof UnicastMessage) {
            try {
                handleInitMessage(msg);
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        } else if (msg instanceof MulticastMessage) {
            handleChatMessage(from, msg);
        }
    }

    private void handleInitMessage(Message msg) throws IOException {
        //JOIN
        if (msg instanceof JoinMessage) {
            joinTimer.cancel();
            joinTimer = null;
            joinTimer = new Timer(true);

            JoinMessage joinMsg = (JoinMessage) msg;
            myGroup = joinMsg.getGroup();

            sendMessage(server, new AckMessage());

            //set timout
            joinTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        sendMessage(server, new AckMessage());
                    } catch (IOException ex) {
                        log.log(Level.SEVERE, null, ex);
                    }
                }
            }, serverTimout);
        }

        //ACK
        if (msg instanceof AckMessage) {
            joinTimer.cancel();
            connectToGroupMembers();
            //initHeartBeats();
            fireGroupIsReadyEvent();
        }
    }

    private void handleChatMessage(User from, Message msg) {
        if (msg instanceof AckMulticastMessage) {
            AckMulticastMessage ack = (AckMulticastMessage) msg;
            if (msgQueue.ackReceived(from, ack.getMulticastMsg(), myGroup)) {
                Tuple toDeliver;
                while ((toDeliver = msgQueue.getToDeliverMessage()) != null) {
                    if (toDeliver.msg instanceof TextMessage) {
                        fireMessageDeliveredEvent(((TextMessage) toDeliver.msg).getMessage(), toDeliver.msg.getSender());
                    }
                    else if (toDeliver.msg instanceof BurstMessage){
                        fireBurstMessageEvent();
                    }
//                    System.out.println("DELIVER " + toDeliver.msg);
                    //log.log(Level.INFO, "{0}{1}", new Object[]{toDeliver.sender, toDeliver.toString()});
                }
            }
        }
        else if (msg instanceof MulticastMessage) {
            MulticastMessage mcMsg = (MulticastMessage) msg;
            //update clock and store msg in a queue
            int msgClock = mcMsg.getClock();
            if (msgClock < 0){
                return;
            }
            myClock.updateClock(msgClock);
            msgQueue.insertMessage(mcMsg, myGroup);
            for (User u : myGroup) {
                try {
                    sendMessage(u, new AckMulticastMessage(mcMsg, me));
                } catch (IOException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void setListener(PeerEventListener listener) {
        this.listener = listener;
    }

    private void fireGroupIsReadyEvent() {
        if (listener != null) {
            listener.groupIsReady(myGroup);
        }
    }

    private void fireMessageDeliveredEvent(String message, User sender) {
        listener.messageDelivered(message, sender);
    }

    private void fireBurstMessageEvent(){
        listener.burst();
    }

    @Override
    protected void receivedConnectable(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.finishConnect();
        socketChannel.register(selector, SelectionKey.OP_READ);
        sendMessage(getUserFromSocket(socketChannel), new MulticastMessage(-1, me));
    }

    @Override
    protected void receivedAcceptable(SelectionKey key) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.socket().setTcpNoDelay(true);
        client.register(selector, SelectionKey.OP_READ);
    }

    @Override
    protected void receivedReadable(SelectionKey key) throws Exception {
        Message msgReceived = null;

        while (msgReceived == null) {
            msgReceived = readMsg(key);
        }
        SocketChannel channel = (SocketChannel) key.channel();
        if (msgReceived instanceof MulticastMessage) {
            User sender = ((MulticastMessage) msgReceived).getSender();
            sender.ip = ((InetSocketAddress) channel.getRemoteAddress())
                    .getAddress();
            userSocketChannelMap.put(sender, channel);
        }
        messageReceived(getUserFromSocket(channel), msgReceived);
    }

    //TODO : A revoiir completement ??
    private HeartBeatMessage readHBM(DatagramChannel socket) throws IOException, ClassNotFoundException {
        dataByteBufferUDP = ByteBuffer.allocate(4500);
        socket.receive(dataByteBufferUDP);

        //TODO : Verifier qu'on a reçu au moins 4...

        int tailleMsg = dataByteBufferUDP.getInt(0);
        //TODO : verifier qu'on a reçu taille  + 4...
        ByteArrayInputStream bais = new ByteArrayInputStream(dataByteBufferUDP.array());
        //TODO séparer pour récupérer que ce que l'on veut...
        ObjectInputStream ois = new ObjectInputStream(bais);
        final HeartBeatMessage ret = (HeartBeatMessage) ois.readObject();
        return ret;
    }

    private void sendHBM() {
        if (dataChan != null) {
            for (User user : myGroup.getAllOtherThanMe(me)) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    for (int i = 0; i < 4; i++) {
                        baos.write(0);
                    }
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(new HeartBeatMessage(me));
                    oos.close();
                    final ByteBuffer wrap = ByteBuffer.wrap(baos.toByteArray());
                    wrap.putInt(0, baos.size() - 4);
                    dataChan.send(wrap, new InetSocketAddress(user.ip, user.port + 1));
                } catch (IOException ex) {
                    Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        heartBeatTimer.schedule((new TimerTask() {
            @Override
            public void run() {
                sendHBM();
            }
        }), timeBetweenHeartBeats);
    }

    private void connectToGroupMembers() {
        List<User> toConnectWith = myGroup.getUserToConnectWith(me);
        for (User user : toConnectWith) {
            initConnection(user);
        }
    }

    private void prepareTimer(final User user) {
        Timer t = new Timer(true);
        mapUserTimer.put(user, t);
        t.schedule((new TimerTask() {
            @Override
            public void run() {
                //TODO : user est mort
                System.out.println("TimerMouru");
            }
        }), heartBeatTimeout);
    }

    public String getMyName() {
        return me.name;
    }

    public GroupTable getMyGroup() {
        return myGroup;
    }

    private void initMapUserTimer() {
        mapUserTimer = new HashMap<>();
        for (User user : this.myGroup.getAllOtherThanMe(me)) {
            prepareTimer(user);
        }
    }

    private void initDatagramChannel() throws IOException {
        dataChan = DatagramChannel.open();
        dataChan.socket().setReuseAddress(true);
        dataChan.socket().bind(new InetSocketAddress(me.port + 1));
        dataChan.configureBlocking(false);
        dataChan.register(selector, SelectionKey.OP_READ);
    }

    private void initHeartBeats() throws IOException {
        initDatagramChannel();
        initMapUserTimer();
        sendHBM();
    }
}
