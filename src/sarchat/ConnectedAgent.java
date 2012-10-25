/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import sarchat.message.Message;

/**
 *
 * @author Michael Mercier <michael_mercier@orange.fr>
 */
public abstract class ConnectedAgent {

    protected GroupTable group;
    protected HashMap<User, SocketChannel> mapUserSocket;
    protected Selector selector;
    
    public ConnectedAgent(){
        selector = null;
        mapUserSocket = new HashMap<User, SocketChannel>();
        try {
            selector = Selector.open();
        } catch (IOException ex) {
            Logger.getLogger(ConnectedAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public abstract void messageReceived(User from, Message msg);
    
    public Message readMsg(){
        return null;
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
            } catch (IOException ex) {
                Logger.getLogger(ConnectedAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //Sinon : deja connecte
    }

    public void sendMessage(User sendTo, Message msg) throws IOException {
        SocketChannel socketWhereToSend = mapUserSocket.get(sendTo);
        if (socketWhereToSend != null && socketWhereToSend.isConnected()) {
            
            /////////////////////////////////////////////////////////////////
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int i = 0; i < 4; i++) {
                baos.write(0);
            }
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(msg);
            oos.close();
            final ByteBuffer wrap = ByteBuffer.wrap(baos.toByteArray());
            wrap.putInt(0, baos.size() - 4);
            /////////////////////////////////////////////////////////////////
            //On a mis l'objet et sa taille dans le byte buffer et on envoie le tout !
            socketWhereToSend.write(wrap);
        } else { //pas de connection vers ce type la ou pas finie d'etrre etablie
            
        }
    }
}
