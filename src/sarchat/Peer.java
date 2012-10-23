
package sarchat;

import java.io.IOException;
import java.net.InetAddress;
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

    User server;
    Timer joinTimer = new Timer(true);
    final long serverTimout = 10000;//10s

    public Peer(String myName) {
        int port = ConnectionHelper.getAvailablePort();        
        
        try {
            createListenSocket(port);
            me = new User(myName, port);
            server = new User("server", InetAddress.getLocalHost(), Server.serverPort);
            
            //Se connecter au serveur
            this.initConnection(server);
           
        } catch (Exception ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
        }
    }
    
    public void joinGroup(final List<String> groupToJoin) throws IOException {
        GroupTable grpTable = new GroupTable();
        for (String userName : groupToJoin) {
            grpTable.add(new User(userName));
        }
        sendMessage(server, new JoinMessage(me, grpTable));
        //set timout
        joinTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    joinGroup(groupToJoin);
                } catch (IOException ex) {
                    Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, serverTimout);
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
