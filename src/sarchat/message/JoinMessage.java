/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat.message;

import java.net.InetAddress;
import sarchat.GroupTable;
import sarchat.User;

/**
 *
 * @author mickours
 */
public class JoinMessage extends UnicastMessage{
    private User sender;
    private GroupTable group;

    public JoinMessage(GroupTable group) {
        this.sender = null;
        this.group = group;
    }

    public JoinMessage(User user, GroupTable group) {
        this.sender = new User(user);
        this.group = group;
    }

    public String getUserName() {
        return sender.name;
    }

    public GroupTable getGroup() {
        return group;
    }

    public InetAddress getIp() {
        return sender.ip;
    }
}
