/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat;

import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;

/**
 *
 * @author Michael Mercier <michael_mercier@orange.fr>
 */
public class GUI extends javax.swing.JFrame implements PeerEventListener {

    private Peer peer;

    /**
     * Creates new form GUI
     */
    public GUI(Peer peer) {
        this.peer = peer;
        peer.setListener(this);
        initComponents();
        for (User user : peer.getMyGroup()) {
            model.addElement(user.name);
        }
        NameLabel.setText(peer.getMyName());
       // chatMessageTextArea.setEditable(false);
        chatMessageTextArea.setText("Connecting...\n");
        sendMessageTextArea.setEnabled(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        JoinDialog = new javax.swing.JDialog();
        jPanel1 = new javax.swing.JPanel();
        AddLabel = new javax.swing.JLabel();
        JoinButton = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        containersGroup = new javax.swing.JPanel();
        personAvailablePanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        groupList = new javax.swing.JList();
        TitleBoxGroup = new javax.swing.JLabel();
        NameLabel = new javax.swing.JLabel();
        containersDialog = new javax.swing.JPanel();
        chatMessageScrollPane = new javax.swing.JScrollPane();
        chatMessageTextArea = new javax.swing.JTextArea();
        containersWrite = new javax.swing.JPanel();
        SendButton = new javax.swing.JButton();
        BurstToggleButton = new javax.swing.JToggleButton();
        sendMessageTextArea = new javax.swing.JTextField();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        AddLabel.setText("Créer un groupe : ");

        JoinButton.setText("Join a group");
        JoinButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JoinButtonActionPerformed(evt);
            }
        });

        jTextField1.setText("Put group's name and user's name");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(AddLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(121, 121, 121)
                .addComponent(JoinButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AddLabel))
                .addGap(28, 28, 28)
                .addComponent(JoinButton)
                .addContainerGap(133, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout JoinDialogLayout = new javax.swing.GroupLayout(JoinDialog.getContentPane());
        JoinDialog.getContentPane().setLayout(JoinDialogLayout);
        JoinDialogLayout.setHorizontalGroup(
            JoinDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        JoinDialogLayout.setVerticalGroup(
            JoinDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SARChat");
        setMinimumSize(new java.awt.Dimension(400, 200));
        setPreferredSize(new java.awt.Dimension(500, 300));

        containersGroup.setBackground(new java.awt.Color(199, 234, 236));

        personAvailablePanel.setBackground(new java.awt.Color(199, 234, 236));

        model = new DefaultListModel();
        groupList.setBackground(new java.awt.Color(199, 234, 236));
        groupList.setModel(model);
        jScrollPane3.setViewportView(groupList);

        TitleBoxGroup.setText("Available person");

        javax.swing.GroupLayout personAvailablePanelLayout = new javax.swing.GroupLayout(personAvailablePanel);
        personAvailablePanel.setLayout(personAvailablePanelLayout);
        personAvailablePanelLayout.setHorizontalGroup(
            personAvailablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(personAvailablePanelLayout.createSequentialGroup()
                .addGroup(personAvailablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TitleBoxGroup))
                .addGap(0, 26, Short.MAX_VALUE))
        );
        personAvailablePanelLayout.setVerticalGroup(
            personAvailablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, personAvailablePanelLayout.createSequentialGroup()
                .addComponent(TitleBoxGroup)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE))
        );

        NameLabel.setText("Name");

        javax.swing.GroupLayout containersGroupLayout = new javax.swing.GroupLayout(containersGroup);
        containersGroup.setLayout(containersGroupLayout);
        containersGroupLayout.setHorizontalGroup(
            containersGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containersGroupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containersGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(personAvailablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(containersGroupLayout.createSequentialGroup()
                        .addComponent(NameLabel)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        containersGroupLayout.setVerticalGroup(
            containersGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containersGroupLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(NameLabel)
                .addGap(18, 18, 18)
                .addComponent(personAvailablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        containersDialog.setBackground(java.awt.Color.white);
        containersDialog.setForeground(java.awt.Color.white);

        chatMessageTextArea.setEditable(false);
        chatMessageScrollPane.setViewportView(chatMessageTextArea);

        javax.swing.GroupLayout containersDialogLayout = new javax.swing.GroupLayout(containersDialog);
        containersDialog.setLayout(containersDialogLayout);
        containersDialogLayout.setHorizontalGroup(
            containersDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containersDialogLayout.createSequentialGroup()
                .addComponent(chatMessageScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                .addContainerGap())
        );
        containersDialogLayout.setVerticalGroup(
            containersDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chatMessageScrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        containersWrite.setBackground(java.awt.Color.white);
        containersWrite.setForeground(java.awt.Color.white);

        SendButton.setMnemonic('\n');
        SendButton.setText("Send");
        SendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SendButtonActionPerformed(evt);
            }
        });

        BurstToggleButton.setText("Burst");
        BurstToggleButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                BurstToggleButtonItemStateChanged(evt);
            }
        });
        BurstToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BurstToggleButtonActionPerformed(evt);
            }
        });

        sendMessageTextArea.setName(""); // NOI18N
        sendMessageTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sendMessageTextAreaKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout containersWriteLayout = new javax.swing.GroupLayout(containersWrite);
        containersWrite.setLayout(containersWriteLayout);
        containersWriteLayout.setHorizontalGroup(
            containersWriteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containersWriteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sendMessageTextArea)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(containersWriteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(SendButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(BurstToggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                .addContainerGap())
        );
        containersWriteLayout.setVerticalGroup(
            containersWriteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, containersWriteLayout.createSequentialGroup()
                .addGap(0, 12, Short.MAX_VALUE)
                .addGroup(containersWriteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SendButton)
                    .addComponent(sendMessageTextArea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(BurstToggleButton)
                .addContainerGap())
        );

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        openMenuItem.setMnemonic('o');
        openMenuItem.setText("Open");
        fileMenu.add(openMenuItem);

        saveMenuItem.setMnemonic('s');
        saveMenuItem.setText("Save");
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setMnemonic('a');
        saveAsMenuItem.setText("Save As ...");
        saveAsMenuItem.setDisplayedMnemonicIndex(5);
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('e');
        editMenu.setText("Edit");

        cutMenuItem.setMnemonic('t');
        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setMnemonic('y');
        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setMnemonic('p');
        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setMnemonic('d');
        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");

        contentsMenuItem.setMnemonic('c');
        contentsMenuItem.setText("Contents");
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(containersGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(containersWrite, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(containersDialog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(containersGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(containersDialog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(containersWrite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void JoinButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JoinButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_JoinButtonActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void sendMessageTextAreaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sendMessageTextAreaKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            actionSend();
        }
    }//GEN-LAST:event_sendMessageTextAreaKeyPressed

    private void BurstToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BurstToggleButtonActionPerformed
       if (peer.isInBurst()){
           peer.stopBurst();
       }
       else{
           peer.startBurst();
       }
    }//GEN-LAST:event_BurstToggleButtonActionPerformed

    private void BurstToggleButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_BurstToggleButtonItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            BurstToggleButton.setText("Stop Burst");
        } else if (evt.getStateChange() == ItemEvent.DESELECTED) {
            BurstToggleButton.setText("Burst");
        }
    }//GEN-LAST:event_BurstToggleButtonItemStateChanged

    private void SendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SendButtonActionPerformed
        actionSend();
    }//GEN-LAST:event_SendButtonActionPerformed

    private void actionSend() {
        String textTape;
        textTape = sendMessageTextArea.getText();
        try {
            peer.sendTextMessage(textTape);
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        sendMessageTextArea.setText("");
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel AddLabel;
    private javax.swing.JToggleButton BurstToggleButton;
    private javax.swing.JButton JoinButton;
    private javax.swing.JDialog JoinDialog;
    private javax.swing.JLabel NameLabel;
    private javax.swing.JButton SendButton;
    private javax.swing.JLabel TitleBoxGroup;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JScrollPane chatMessageScrollPane;
    private javax.swing.JTextArea chatMessageTextArea;
    private javax.swing.JPanel containersDialog;
    private javax.swing.JPanel containersGroup;
    private javax.swing.JPanel containersWrite;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JList groupList;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JPanel personAvailablePanel;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JTextField sendMessageTextArea;
    // End of variables declaration//GEN-END:variables
    private DefaultListModel model;

    @Override
    public void groupIsReady(GroupTable group) {
        chatMessageTextArea.append("The group is ready!\n");
        sendMessageTextArea.setEnabled(true);
    }

    @Override
    public void messageDelivered(String message, User sender) {
        chatMessageTextArea.append(sender.name + " : " + message + "\n");
        chatMessageTextArea.setCaretPosition(chatMessageTextArea.getDocument().getLength());
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
