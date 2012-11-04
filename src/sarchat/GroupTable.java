/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat;

import java.net.InetAddress;
import java.util.HashSet;

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
    
}
