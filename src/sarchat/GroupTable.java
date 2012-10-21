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
    private static int currentPort = 10000;

    public GroupTable(GroupTable group) {
        super(group.size());
        for (User user : group){
            add(new User(user));
        }
    }

    GroupTable() {
        super();
    }

    /**
     * set the user name, ip and port in the group
     * @return true if all the users have reach the group
     */
    public boolean userJoin(String userName, InetAddress ip) {
        boolean groupComplete = true;
        for (User user : this) {
            if (user.name.equals(userName)){
                user.ip = ip;
                if (user.port == -1){
                    user.port = generatePort();
                }
            }
            if (user.ip == null){
                groupComplete = false;
            }
        }
        return groupComplete;
        
    }

    private int generatePort() {
        return currentPort++;
    }    

    int getMyPort(String name) {
        for (User user : this) {
            if (user.name.equals(name)){
                return user.port;
            }
        }
        throw new RuntimeException("the port should be set");
    }
    
}
