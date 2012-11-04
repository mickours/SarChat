/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Michael Mercier <michael_mercier@orange.fr>
 */
public class GroupTable extends HashSet<User> {

    public GroupTable(GroupTable group) {
        super(group.size());
        for (User user : group){
            add(new User(user));
        }
    }

    GroupTable() {
        super();
    }
    
    public User getUser(String name){
        for (User user : this){
            if(user.name.equals(name)){
                return user;                
            }
        }
        return null;
    }

    /**
     * set the user name, ip and port in the group
     * @return true if all the users have reach the group
     */
    public boolean userJoin(User toJoin) {
        boolean groupComplete = true;
        for (User user : this) {
            if (user.name.equals(toJoin.name)){
                user.ip = toJoin.ip;
                user.port = toJoin.port;
            }
            if (user.ip == null || user.port == -1){
                groupComplete = false;
            }
        }
        return groupComplete; 
    }

    List<User> getUserToConnectWith(User me) {
        List<User> underMeList = new LinkedList();
        for (User user : this) {
            if (user.name.compareTo(me.name) >= 0){
                underMeList.add(user);
            }
        }
        return underMeList;
    }
    
}
