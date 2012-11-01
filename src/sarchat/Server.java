package sarchat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import sarchat.message.AckMessage;
import sarchat.message.JoinMessage;
import sarchat.message.Message;
import sarchat.message.UnicastMessage;

/**
 *
 * @author Michael Mercier <michael_mercier@orange.fr>
 */
public class Server extends ConnectedAgent {

    // The host:port combination to listen on
    public static final int serverPort = 9999;

    public void createServerSocket(int port) throws IOException {
        ServerSocketChannel server = null;
        Message msgReceived;
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
                        //Normalement pas besoin pour server :
                        // Il accepte les connections mais ne fait pas de demandes !
                    }
                    if (key.isAcceptable()) {
                        // accept connection
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.socket().setTcpNoDelay(true);
                        client.register(selector, SelectionKey.OP_READ);
                    }
                    if (key.isReadable()) {
                        // ...read messages...
                        msgReceived = null;
                        
                        //Attente active de la reception de l'intégralité de l'objet
                        while (msgReceived == null){
                             msgReceived = readMsg(key);
                        }
                        messageReceived((SocketChannel) key.channel(), msgReceived);
                    }
                }
            }
        } catch (Throwable e) {
            System.out.println("Error : "+e.toString());
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

    public void messageReceived(SocketChannel socketFrom, Message msg) {
        
        //Adresse de celui qui envoie
        InetAddress ipSender = null;
        try {
            ipSender = ((InetSocketAddress) socketFrom.getRemoteAddress())
        .getAddress();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //check if the message is correct
        assert (msg != null);
        assert (msg instanceof UnicastMessage);

        Set<User> waitForAck = new HashSet<User>();
        System.out.println("Server received:\n" + msg);
        //JOIN Received
        if (msg instanceof JoinMessage) {
            JoinMessage joinMsg = (JoinMessage) msg;
            if (group == null) {
                group = new GroupTable(joinMsg.getGroup());
            }
            String userFromName = joinMsg.getUserName();
            User userFrom = group.getUser(userFromName);
            if(!mapUserSocket.containsValue(socketFrom)){
                mapUserSocket.put(userFrom, socketFrom);
            }
            if (ipSender!=null && group.userJoin(userFromName, ipSender)) {
                //the group is complete
                for (Iterator<User> it = group.iterator(); it.hasNext();) {
                    User user = it.next();
                    try {
                        sendMessage(user, new JoinMessage(group));
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    waitForAck.add(user);
                }
            }
        }
        //ACK Received
        if (msg instanceof AckMessage) {
            try {
                // TODO : voir si on change le send en mode socketchannel...
                sendMessage(socketFrom, new AckMessage());
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            waitForAck.remove(mapUserSocket.get(socketFrom));
            if (waitForAck.isEmpty()) {
                for (Iterator<User> it = group.iterator(); it.hasNext();) {
                    User user = it.next();
                    try {
                        sendMessage(user, new AckMessage());
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
}
