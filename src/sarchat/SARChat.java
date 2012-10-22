/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Mercier <michael_mercier@orange.fr>
 */
public class SARChat {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args[0].equalsIgnoreCase("Server")){
            try {
                new Server().createListenSocket(Server.serverPort);
            } catch (IOException ex) {
                Logger.getLogger(SARChat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else if (args.length == 2){
                Peer peer = new Peer(args[0]);
                peer.joinGroup(Arrays.asList(args[1].split(";")));
        }else{
            System.out.println("Usage: \nserver\nto launc a server");
            System.out.println("UserName UserName;A;B");
            System.out.println("to launch a peer and join the group with A and B");
        }
    }
}
