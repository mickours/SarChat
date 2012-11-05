/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat.message;

import sarchat.User;

/**
 *
 * @author Michael Mercier <michael_mercier@orange.fr>
 */
public class BurstMessage extends MulticastMessage{

    public BurstMessage(int clock, User sender) {
        super(clock, sender);
    }
    
}
