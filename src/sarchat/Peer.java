package sarchat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import sarchat.message.AckMessage;
import sarchat.message.AckMulticastMessage;
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

    public Peer(String myName, final List<String> groupToJoin) {
        super();
        myGroup = new GroupTable();
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
    
    public void createListenSocket() throws IOException{
        createServerSocket(me.port);
    }

    //Initiate a connection with another user or the server
    private void initConnection(User connectMeWith) {
        System.out.println(me.name+"CONNEXION: "+me.name+" with "+connectMeWith.name);
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

    public void messageReceived(User from, Message msg) {
        assert (msg != null);
        System.out.println(me.name+" RECEIVED\t" + msg);

        if (msg instanceof UnicastMessage) {
            try {
                handleInitMessage(msg);
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        } else if (msg instanceof MulticastMessage) {
            handleChatMessage(from, msg);
        } else {
            assert false;
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
            fireGroupIsReadyEvent();
        }
    }

    private void handleChatMessage(User from, Message msg) {
        if (msg instanceof TextMessage) {
            TextMessage textMsg = (TextMessage) msg;
            //update clock and store msg in a queue
            int msgClock = textMsg.getClock();
            myClock.updateClock(msgClock);
            msgQueue.insertMessage(textMsg, myGroup);
            for (User u : myGroup) {
                try {
                    sendMessage(u, new AckMulticastMessage(textMsg, me));
                } catch (IOException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            }
        } else if (msg instanceof AckMulticastMessage) {
            AckMulticastMessage ack = (AckMulticastMessage) msg;
            if (msgQueue.ackReceived(from, ack.getTextMsg(),myGroup)) {
                Tuple toDeliver = msgQueue.getHeadMessage();
                //log.log(Level.INFO, "{0}{1}", new Object[]{toDeliver.sender, toDeliver.toString()});
                fireMessageDeliveredEvent(toDeliver.msg.getMessage(), toDeliver.sender);
            }
        } else {
            assert false;
        }
    }
    
    public void setListener(PeerEventListener listener){
        this.listener = listener;
    }
    
    private void fireGroupIsReadyEvent(){
        if (listener != null){
            listener.groupIsReady(myGroup);
        }
    }
    
    private void fireMessageDeliveredEvent(String message, User sender) {
        listener.messageDelivered(message, sender);
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
        
        if (msgReceived instanceof MulticastMessage) {
            SocketChannel channel = (SocketChannel)key.channel();
            User sender = ((MulticastMessage)msgReceived).getSender();
            sender.ip = ((InetSocketAddress) channel.getRemoteAddress())
                .getAddress();
            userSocketChannelMap.put(sender, channel);
        }
        messageReceived(getUserFromSocket((SocketChannel) key.channel()), msgReceived);
    }

    private void connectToGroupMembers() {
        List<User> toConnectWith = myGroup.getUserToConnectWith(me);
        for (User user : toConnectWith) {
            initConnection(user);
        }
    }

    String getMyName() {
        return me.name;
    }

    GroupTable getMyGroup() {
        return myGroup;
    }
}
