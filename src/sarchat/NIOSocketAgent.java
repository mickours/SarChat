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
public abstract class NIOSocketAgent {


    protected HashMap<User, SocketChannel> userSocketChannelMap;
    protected Selector selector;
    protected ServerSocketChannel serverSocket;

    private final ByteBuffer lengthByteBuffer = ByteBuffer.wrap(new byte[4]);
    private ByteBuffer dataByteBuffer = null;
    private boolean readLength = true;

    public NIOSocketAgent() {
        selector = null;
        userSocketChannelMap = new HashMap<>();
        try {
            selector = Selector.open();
        } catch (IOException ex) {
            Logger.getLogger(NIOSocketAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void createServerSocket(int port) throws IOException {
        serverSocket = null;
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().setReuseAddress(true);
            serverSocket.socket().bind(new InetSocketAddress(port));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select(5);
                for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) {
                    SelectionKey key = i.next();
                    i.remove();
                    if (key.isConnectable()) {
                        receivedConnectable(key);
                    }
                    if (key.isAcceptable()) {
                        receivedAcceptable(key);
                    }
                    if (key.isReadable()) {
                        receivedReadable(key);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Socket failure: " + e.getMessage());
        } finally {
            try {
                selector.close();
                serverSocket.socket().close();
                serverSocket.close();
                System.exit(0);
            } catch (Exception e) {
                // do nothing - server failed
            }
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
                Message ret = null;
                ByteArrayInputStream bais = new ByteArrayInputStream(dataByteBuffer.array());
                ObjectInputStream ois = new ObjectInputStream(bais);
                try{
                    ret = (Message) ois.readObject();
                }catch(IOException e){
                    //Nothing for now ...
                }finally{
                    // clean up
                    bais.close();
                    ois.close();
                    dataByteBuffer = null;
                    readLength = true;
                    return ret;
                }
            }
        }
        return null;
    }

    public void sendMessage(User sendTo, Message msg) throws IOException {
        SocketChannel socketWhereToSend = userSocketChannelMap.get(sendTo);
        if (socketWhereToSend != null && socketWhereToSend.isConnected()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int i = 0; i < 4; i++) {
                baos.write(0);
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(msg);
            }
            final ByteBuffer wrap = ByteBuffer.wrap(baos.toByteArray());
            wrap.putInt(0, baos.size() - 4);

            socketWhereToSend.write(wrap);
//            System.out.println("SEND TO "+sendTo.name+"\t" + msg);
        }
    }

    protected User getUserFromSocket(SocketChannel socketFrom){
        for (User user : userSocketChannelMap.keySet()){
            SocketChannel sock = userSocketChannelMap.get(user);
            if (sock.equals(socketFrom)){
                return user;
            }
        }
        return null;
    }


    protected abstract void receivedConnectable(SelectionKey key)throws IOException;
    protected abstract void receivedAcceptable(SelectionKey key)throws IOException;
    protected abstract void receivedReadable(SelectionKey key)throws Exception;

}
