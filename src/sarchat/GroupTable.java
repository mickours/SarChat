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
     * Recupere l'user qui a l'adresse ip donnee
     * @param ip cherchee
     * @return l'user correspondant, null si aucun
     */
    public User getUser(InetAddress ip){
        for (User user : this){
            if(user.ip.equals(ip)){
                return user;                
            }
        }
        return null;
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
    public boolean userJoin(String userName, InetAddress ip) {
        boolean groupComplete = true;
        for (User user : this) {
            if (user.name.equals(userName)){
                user.ip = ip;
                if (user.port == -1){
                    //TODO : revoir cette focntion
                }
            }
            if (user.ip == null){
                groupComplete = false;
            }
        }
        return groupComplete; 
    }
    
}
