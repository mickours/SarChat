/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat.message;

import sarchat.User;

/**
 *
 * @author mickours
 */
public class TextMessage extends MulticastMessage{


    private String message;

    public TextMessage(String textTape, int clock, User sender ) {
        super(clock, sender);
        message = textTape;
    }
    
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return " lc=" +getClock() + " message=" + message;
    }
    
    
}
