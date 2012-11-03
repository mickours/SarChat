package sarchat;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.name);
        hash = 71 * hash + Objects.hashCode(this.ip);
        hash = 71 * hash + this.port;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.ip, other.ip)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        return true;
    }
    
    

    @Override
    public String toString() {
        return "User{" + "name=" + name + ", ip=" + ip + ", port=" + port + '}';
    }
}
