package sarchat;

import java.io.Serializable;
import java.net.InetAddress;

/**
 *
 * @author Michael Mercier <michael_mercier@orange.fr>
 */
public class User implements Serializable{
    public String name;
    public InetAddress ip;
    public int port = -1;

    public User(String userName) {
        this.name = userName;
    }

    public User(String name, InetAddress ip, int port) {
        this.name=name;
        this.ip = ip;
        this.port = port;
    }

    public User(String name, int port) {
        this.name = name;
        this.port = port;
    }

    public User(User user) {
        this.name = user.name;
        this.ip = user.ip;
        this.port = user.port;
    }
}
