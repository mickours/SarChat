package sarchat;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
    
    @Override
    public void messageReceived(User from, Message msg) {
        //check if the message is correct
        assert (msg != null);
        assert (msg instanceof UnicastMessage);
        
        Set<User> waitForAck = new HashSet<User>();
        System.out.println("Server received:\n"+msg);
        //JOIN Received
        if (msg instanceof JoinMessage){
            JoinMessage joinMsg = (JoinMessage) msg;
            if (group == null){
                group = new GroupTable(joinMsg.getGroup());
            }
            if (group.userJoin(joinMsg.getUserName(),from.ip)){
                //the group is complete
                for (Iterator<User> it = group.iterator(); it.hasNext();) {
                    User user = it.next();
                    sendMessage(user,new JoinMessage(null, group));
                    waitForAck.add(user);
                }
            }
        }
        //ACK Received
        if (msg instanceof AckMessage){
            sendMessage(from, new AckMessage());
            waitForAck.remove(from);
            if (waitForAck.isEmpty()){
                for (Iterator<User> it = group.iterator(); it.hasNext();) {
                    User user = it.next();
                    sendMessage(user,new AckMessage());
                }
            }
        }
    }
    
}
