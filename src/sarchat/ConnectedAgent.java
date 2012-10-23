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
    protected User me;
    private HashMap<User, SocketChannel> mapUserSocket;
    private Selector selector;

    public void createListenSocket(int port) throws IOException {
        selector = null;
        ServerSocketChannel server = null;
        try {
            selector = Selector.open();
            server = ServerSocketChannel.open();
            server.socket().bind(new InetSocketAddress(port));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select();
                for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) {
                    SelectionKey key = i.next();
                    i.remove();
                    if (key.isConnectable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        socketChannel.finishConnect();
                        socketChannel.register(selector, SelectionKey.OP_READ);


                        //Adresse de celui qui envoie
                        InetAddress ipSender = ((InetSocketAddress) ((SocketChannel) key.channel()).getRemoteAddress())
                                .getAddress();

                        // Ici deux cas : soit on a établi la connection avec le serveur,
                        // et il faut envoyer un join...
                        // soit on se connecte avec un autre peer
                        if (group.getUser(ipSender).name.equals("server")) {
                            //TODO : Envoyer join au serveur
                            //Revoir séparation des classes ??
                        } else {
                            //Connecté à un autre peer
                        }
                    }
                    if (key.isAcceptable()) {
                        // accept connection
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.socket().setTcpNoDelay(true);
                        client.register(selector, SelectionKey.OP_READ);

                        //Adresse de celui qui envoie
                        InetAddress ipSender = ((InetSocketAddress) ((SocketChannel) key.channel()).getRemoteAddress())
                                .getAddress();
                        //On recupère l'user correspondant dans le groupe
                        User sender = group.getUser(ipSender);
                        if (sender != null) {
                            //L'user est dans le groupe, on l'ajoute à la list user/socket
                            mapUserSocket.put(sender, client);
                        } else { //On ne connait pas la personne qui se connecte : on est sur le serveur ! 
                            //On s'attend donc à recevoir un join qui nous permettra de mettre la personne dans la liste...
                        }
                    }
                    if (key.isReadable()) {
                        // ...read messages...
                        
                        
                        //Solution... buffer temporaires pour chaque socketchannel
                        //en attente de recevoir la totalité de l'objet...
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("Server failure: " + e.getMessage());
        } finally {
            try {
                selector.close();
                server.socket().close();
                server.close();
                //stopped();
            } catch (Exception e) {
                // do nothing - server failed
            }
        }
    }

    public abstract void messageReceived(User from, Message msg);

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
