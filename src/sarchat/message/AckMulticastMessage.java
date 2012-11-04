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
public class AckMulticastMessage extends MulticastMessage{
    private TextMessage textMsg;
    
    public AckMulticastMessage(TextMessage textMsg, User sender ) {
        super(-1, sender);
        this.textMsg = textMsg;
        
    }

    public TextMessage getTextMsg() {
        return textMsg;
    }

    @Override
    public String toString() {
        return "AckMulticastMessage{" + "textMsg=" + textMsg + '}';
    }

}
