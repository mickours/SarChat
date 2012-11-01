package sarchat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
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

public class Peer extends ConnectedAgent {

    private LogicalClock myClock;
    private MessageToDeliverQueue msgQueue;
    private User server;
    private User me;
    public static final Logger log = Logger.getAnonymousLogger();
    Timer joinTimer = new Timer(true);
    final long serverTimout = 10000;//10s
    private HashMap<SocketChannel, User> mapSocketUser;

    public Peer(String myName, final List<String> groupToJoin) {
        super();
        group = new GroupTable();
        mapSocketUser = new HashMap<SocketChannel, User>();
        for (String userName : groupToJoin) {
            group.add(new User(userName));
        }
        int port = ConnectionHelper.getAvailablePort();

        try {
            me = new User(myName, port);

            server = new User("server", InetAddress.getLocalHost(), Server.serverPort);
            //Se connecter au serveur
            initConnection(server);
            createListenSocket(port);
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
            assert false;
        }
        //init multicast transmition
        msgQueue = new MessageToDeliverQueue();
        myClock = new LogicalClock();
    }
    
    //Initiate a connection with another user or the server
    public void initConnection(User connectMewith) {
        if (mapUserSocket.get(connectMewith) == null) {
            try {
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                socketChannel.socket().setTcpNoDelay(true);
                socketChannel.connect(new InetSocketAddress(connectMewith.ip, connectMewith.port));
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
                mapUserSocket.put(connectMewith, socketChannel);
                mapSocketUser.put(socketChannel, connectMewith);
            } catch (IOException ex) {
                Logger.getLogger(ConnectedAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //Sinon : deja connecte
    }
    
    /**
     * send the text message "msg" to the user specified in "sendTo"
     */
    public void sendTextMessage(User sendTo, TextMessage msg){
        //inform the GUI
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void joinGroup() throws IOException {
        sendMessage(server, new JoinMessage(me, group));
        //set timout
        joinTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    joinGroup();
                } catch (IOException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            }
        }, serverTimout);
    }

    private void createListenSocket(int port) throws IOException {
        ServerSocketChannel serverSocket = null;
        Message msgReceived;
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(port));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select();
                for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) {
                    SelectionKey key = i.next();
                    i.remove();
                    if (key.isConnectable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        socketChannel.finishConnect();
                        socketChannel.register(selector, SelectionKey.OP_READ);


                        //Adresse de celui qui envoie
                        InetAddress ipSender = ((InetSocketAddress) ((SocketChannel) key.channel()).getRemoteAddress())
                                .getAddress();

                        // Ici deux cas : soit on a établi la connection avec le serveur,
                        // et il faut envoyer un join...
                        // soit on se connecte avec un autre peer
                        if (this.server.ip.equals(ipSender)) {
                            //On rejoind le groupe...
                            this.joinGroup();
                        }
                    }
                    if (key.isAcceptable()) {
                        // accept connection
                        SocketChannel client = serverSocket.accept();
                        client.configureBlocking(false);
                        client.socket().setTcpNoDelay(true);
                        client.register(selector, SelectionKey.OP_READ);

                        //Adresse de celui qui envoie
                        InetAddress ipSender = ((InetSocketAddress) ((SocketChannel) key.channel()).getRemoteAddress())
                                .getAddress();
                        //On recupère l'user correspondant dans le groupe
                        User sender = group.getUser(ipSender);
                        if (sender != null) {
                            //L'user est dans le groupe, on l'ajoute à la list user/socket
                            mapUserSocket.put(sender, client);
                            mapSocketUser.put(client, sender);
                        }
                    }
                    if (key.isReadable()) {
                        // ...read messages...
                        msgReceived = null;
                        
                        //Attente active de la reception de l'intégralité de l'objet
                        while (msgReceived == null){
                             msgReceived = readMsg(key);
                        }
                        messageReceived(mapSocketUser.get((SocketChannel) key.channel()), msgReceived);
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("Peer failure: " + e.getMessage());
        } finally {
            try {
                selector.close();
                serverSocket.socket().close();
                serverSocket.close();
                //stopped();
            } catch (Exception e) {
                // do nothing - server failed
            }
        }
    }
    
    public void messageReceived(User from, Message msg) {
        assert (msg != null);
        System.out.println("Peer " + me.name + " received:\n" + msg);

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
            group = joinMsg.getGroup();
//            boolean error;
//            do{
//            try {
//                createListenSocket(me.port);
//                error = false;
//            } catch (IOException ex) {
//                log.log(Level.SEVERE, null, ex);
//                error = true;
//            }
//            }while(error);
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
            chatIsReady();
        }
    }

    private void handleChatMessage(User from, Message msg) {
        if (msg instanceof TextMessage) {
            TextMessage textMsg = (TextMessage) msg;
            //update clock and store msg in a queue
            LogicalClock msgClock = textMsg.getLc();
            myClock.updateClock(msgClock);
            msgQueue.insertMessage(textMsg, from, group);
            for (User u : group) {
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
                deliverMessage(toDeliver.msg.getMessage(), toDeliver.sender);
            }
        } else {
            assert false;
        }
    }

    private void chatIsReady() {
        //inform the GUI
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void deliverMessage(String message, User sender) {
        //inform the GUI
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
