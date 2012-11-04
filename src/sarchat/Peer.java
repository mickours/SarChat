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
        System.out.println("CONNEXION: "+me.name+" with "+connectMeWith.name);
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
    public void sendTextMessage(TextMessage msg) throws IOException {
        for (User user : myGroup) {
            msg.setLc(myClock.incrementClock());
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
        System.out.println("Peer " + me.name + " received:\n\t" + msg);

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
            LogicalClock msgClock = textMsg.getLc();
            myClock.updateClock(msgClock);
            msgQueue.insertMessage(textMsg, from, myGroup);
            for (User u : myGroup) {
                try {
                    sendMessage(u, new AckMulticastMessage(textMsg, from));
                } catch (IOException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            }
        } else if (msg instanceof AckMulticastMessage) {
            AckMulticastMessage ack = (AckMulticastMessage) msg;
            LogicalClock lc = ack.getMsgToAckLogicalClock();
            if (msgQueue.ackReceived(from, ack.getSender(), lc)) {
                Tuple toDeliver = msgQueue.getHeadMessage();
                log.log(Level.INFO, "{0}{1}", new Object[]{toDeliver.sender, toDeliver.toString()});
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
            listener.messageDelivered(null, server);
    }

    @Override
    protected void receivedConnectable(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.finishConnect();
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    @Override
    protected void receivedAcceptable(SelectionKey key) throws IOException {
        // accept connection
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
        
        messageReceived(getUserFromSocket((SocketChannel) key.channel()), msgReceived);
    }

    private void connectToGroupMembers() {
        List<User> toConnectWith = myGroup.getUserToConnectWith(me);
        for (User user : toConnectWith) {
            initConnection(user);
        }
    }
}
