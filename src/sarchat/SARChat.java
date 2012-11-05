/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Michael Mercier <michael_mercier@orange.fr>
 */
public class SARChat implements PeerEventListener {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args[0].equalsIgnoreCase("Server")) {
            try {
                System.out.println("SARChat Server is running...");
                new Server().createServerSocket(Server.serverPort);
            } catch (IOException ex) {
                Logger.getLogger(SARChat.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (args.length == 2) {
            final Peer peer = new Peer(args[0], Arrays.asList(args[1].split(";")));
            try {
                peer.sendJoinGroupMessage();
                System.out.println("Peer " + peer + " is running and try to join " + args[0]);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            peer.createListenSocket();
                        } catch (IOException ex) {
                            Logger.getLogger(SARChat.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                //SARChat chatUI = new SARChat(peer);
                /* Set the Nimbus look and feel */
                //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
                GUI gui = new GUI(peer);
                gui.setVisible(true);
                gui.setLocationRelativeTo(null);
                thread.start();
            } catch (IOException ex) {
                Logger.getLogger(SARChat.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Usage: \nserver\nto launc a server");
            System.out.println("UserName UserName;A;B");
            System.out.println("to launch a peer and join the group with A and B");
        }
    }
    private final Scanner sc;
    private Peer peer;

    private SARChat(Peer peer) {
        sc = new Scanner(System.in);
        this.peer = peer;
        peer.setListener(this);
    }

    private void chat() {
        System.out.println("talk:");
        String str = sc.nextLine();
        try {
            peer.sendTextMessage(str);
        } catch (IOException ex) {
            System.out.println("Error while sending your message: \"" + str + "\"");
        }
    }

    @Override
    public void groupIsReady(GroupTable group) {
        System.out.println("The group " + group + " is ready");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    chat();
                }
            }
        }).start();
    }

    @Override
    public void messageDelivered(String message, User sender) {
        System.out.println("Message from: " + sender.name);
        System.out.println(message);
    }

    @Override
    public void peerDown(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void peerUp(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void burstStopAnotherUser() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
