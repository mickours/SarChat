package sarchat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import sarchat.message.AckMessage;
import sarchat.message.JoinMessage;
import sarchat.message.Message;
import sarchat.message.MulticastMessage;
import sarchat.message.UnicastMessage;
import sarchat.utils.ConnectionHelper;

/**
 *
 * @author mickours
 */
public class Peer extends ConnectedAgent {

    private User server;
    private User me;
    private List<String> groupToJoin;
    Timer joinTimer = new Timer(true);
    final long serverTimout = 10000;//10s

    public Peer(String myName,final List<String> groupToJoin) {
        super();
        this.groupToJoin = groupToJoin;
        group = new GroupTable();
        for (String userName : groupToJoin) {
            group.add(new User(userName));
        }
        int port = ConnectionHelper.getAvailablePort();

        try {
            me = new User(myName, port);

            server = new User("server", InetAddress.getLocalHost(), Server.serverPort);
            //Se connecter au serveur
            this.initConnection(server);
            createListenSocket(port);
        } catch (Exception ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
        }
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
                    Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, serverTimout);
    }

    public void createListenSocket(int port) throws IOException {
        ServerSocketChannel server = null;
        try {
            server = ServerSocketChannel.open();
            server.socket().bind(new InetSocketAddress(port));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
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
                        if(this.server.ip.equals(ipSender)){
                            //On rejoind le groupe...
                            this.joinGroup();
                        }
                    }
                    if (key.isAcceptable()) {
                        // accept connection
                        SocketChannel client = server.accept();
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
                        } else { //On ne connait pas la personne qui se connecte : on est sur le serveur ! 
                            //On s'attend donc à recevoir un join qui nous permettra de mettre la personne dans la liste...
                        }
                    }
                    if (key.isReadable()) {
                        // ...read messages...
                        //Solution... buffer temporaires pour chaque socketchannel
                        //en attente de recevoir la totalité de l'objet...
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("Server failure: " + e.getMessage());
        } finally {
            try {
                selector.close();
                server.socket().close();
                server.close();
                //stopped();
            } catch (Exception e) {
                // do nothing - server failed
            }
        }
    }

    @Override
    public void messageReceived(User from, Message msg) {
        assert (msg != null);
        System.out.println("Peer " + me.name + " received:\n" + msg);

        if (msg instanceof UnicastMessage) {
            try {
                handleInitMessage(msg);
            } catch (IOException ex) {
                Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
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
            JoinMessage joinMsg = (JoinMessage) msg;
            group = joinMsg.getGroup();
            boolean error;
//            do{
            try {
                createListenSocket(me.port);
                error = false;
            } catch (IOException ex) {
                Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                error = true;
            }
//            }while(error);
            sendMessage(server, new AckMessage());
            //set timout
            joinTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        sendMessage(server, new AckMessage());
                    } catch (IOException ex) {
                        Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
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
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void chatIsReady() {
        //inform the GUI
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
