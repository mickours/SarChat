package sarchat.utils;

import java.io.IOException;
import java.net.ServerSocket;


public class ConnectionHelper {
    
    public static int getAvailablePort(){
        int port = -1;
        try {
            ServerSocket socket = new ServerSocket(0);
            port = socket.getLocalPort();
            //Pour pouvoir reutiliser le port si close pas fait
            socket.setReuseAddress(true);
            socket.close();
        } catch (IOException ex) {
            //Ne rien faire, on renverra -1
        }
        return port;
    }
    
}
