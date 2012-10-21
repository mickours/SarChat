/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

/**
 *
 * @author mickours
 */
public class Peer extends ConnectedAgent{
    User server;
    Timer joinTimer = new Timer(true);
    final long serverTimout = 10000;//10s

    public Peer(String myName){
        me = new User(myName);
        try {
            server = new User(InetAddress.getLocalHost(), Server.serverPort);
        } catch (Exception ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
            assert false;
        }
    }
    
    public void joinGroup(final List<String> groupToJoin){
        GroupTable grpTable = new GroupTable();
        for (String userName : groupToJoin) {
            grpTable.add(new User(userName));
        }
        sendMessage(server, new JoinMessage(me.name, grpTable));
        //set timout
        joinTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                joinGroup(groupToJoin);
            }
        }, serverTimout);
    }
    
    @Override
    public void messageReceived(User from, Message msg) {
        assert (msg != null);
        System.out.println("Peer "+me.name+" received:\n"+msg);
        
        if(msg instanceof UnicastMessage){
            handleInitMessage(msg);
        }
        else if(msg instanceof MulticastMessage){
            handleChatMessage(from,msg);
        }
        else {
            assert false;
        }
    }

    private void handleInitMessage(Message msg) {
        //JOIN
        if (msg instanceof JoinMessage){
            joinTimer.cancel();
            JoinMessage joinMsg = (JoinMessage) msg;
            group = joinMsg.getGroup();
            boolean error;
//            do{
                try {
                    createListenSocket(group.getMyPort(me.name));
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
                    sendMessage(server, new AckMessage());
                }
            }, serverTimout);
        }
        //ACK
        if (msg instanceof AckMessage){
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
