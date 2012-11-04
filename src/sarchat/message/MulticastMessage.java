/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat.message;

import java.io.Serializable;
import sarchat.User;

/**
 *
 * @author Michael Mercier <michael_mercier@orange.fr>
 */
public class MulticastMessage implements Message, Serializable {
    private int clock;
    private User sender;

    public MulticastMessage(int clock, User sender) {
        this.clock = clock;
        this.sender = sender;
    }

    public int getClock() {
        return clock;
    }
    
    public User getSender() {
        return sender;
    }

    @Override
    public String toString() {
        return "MulticastMessage{" + "clock=" + clock + ", sender=" + sender + '}';
    }
    
    
}
