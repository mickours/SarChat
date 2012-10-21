/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat.message;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;
import sarchat.GroupTable;

/**
 *
 * @author mickours
 */
public class JoinMessage extends UnicastMessage{
    private String userName;
    private GroupTable group;
    private InetAddress ip;

    public JoinMessage(String userName, GroupTable group) {
        this.userName = userName;
        this.group = group;
    }

    public String getUserName() {
        return userName;
    }

    public GroupTable getGroup() {
        return group;
    }

    public InetAddress getIp() {
        return ip;
    }
}
