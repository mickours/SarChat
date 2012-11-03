package sarchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
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
public class Server extends NIOSocketAgent {

    // The host:port combination to listen on
    public static final int serverPort = 9999;
    private GroupTable group;
    
    private Set<User> waitForAck = new HashSet<>();

    public void messageReceived(User userFrom, Message msg) {
        //check if the message is correct
        assert (msg != null);
        assert (msg instanceof UnicastMessage);


        System.out.println("Server received:\n\t" + msg);
        //JOIN Received
        if (msg instanceof JoinMessage) {
            JoinMessage joinMsg = (JoinMessage) msg;
            if (group == null) {
                group = new GroupTable(joinMsg.getGroup());
            }
            
            if (group.userJoin(userFrom)) {
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
            waitForAck.remove(userFrom);
          
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

    @Override
    protected void receivedConnectable(SelectionKey key) {
        //Do nothing
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

        //Attente active de la reception de l'intégralité de l'objet
        while (msgReceived == null){
             msgReceived = readMsg(key);
        }
        
        if (msgReceived instanceof JoinMessage) {
            SocketChannel channel = (SocketChannel)key.channel();
            User sender = ((JoinMessage)msgReceived).getUser();
            sender.ip = ((InetSocketAddress) channel.getRemoteAddress())
                .getAddress();
            userSocketChannelMap.put(sender, channel);
        }
        messageReceived(getUserFromSocket((SocketChannel) key.channel()), msgReceived);
    }
}
