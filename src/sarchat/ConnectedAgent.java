/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
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
    
    private final ByteBuffer lengthByteBuffer = ByteBuffer.wrap(new byte[4]);
    private ByteBuffer dataByteBuffer = null;
    private boolean readLength = true;

    public ConnectedAgent() {
        selector = null;
        mapUserSocket = new HashMap<User, SocketChannel>();
        try {
            selector = Selector.open();
        } catch (IOException ex) {
            Logger.getLogger(ConnectedAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Message readMsg(SelectionKey key) throws IOException, ClassNotFoundException {
        SocketChannel socket = (SocketChannel) key.channel();

        if (readLength) {
            socket.read(lengthByteBuffer);
            if (lengthByteBuffer.remaining() == 0) {
                readLength = false;
                dataByteBuffer = ByteBuffer.allocate(lengthByteBuffer.getInt(0));
                lengthByteBuffer.clear();
            }
        } else {
            socket.read(dataByteBuffer);
            if (dataByteBuffer.remaining() == 0) {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(dataByteBuffer.array()));
                final Message ret = (Message) ois.readObject();
                // clean up
                dataByteBuffer = null;
                readLength = true;
                return ret;
            }
        }
        return null;
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
    
    public void sendMessage(SocketChannel socketWhereToSend, Message msg) throws IOException {
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
