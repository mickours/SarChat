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
    private LogicalClock msgToAckLogicalClock;
    private User sender;
    
    public AckMulticastMessage(TextMessage textMsg, User sender) {
        msgToAckLogicalClock = textMsg.getLc();
        this.sender = sender;
    }

    public LogicalClock getMsgToAckLogicalClock() {
        return msgToAckLogicalClock;
    }

    public User getSender() {
        return sender;
    }
    
}
