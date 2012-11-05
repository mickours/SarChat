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
    private MulticastMessage textMsg;

    public AckMulticastMessage(MulticastMessage textMsg, User sender ) {
        super(-1, sender);
        this.textMsg = textMsg;

    }

    public MulticastMessage getMulticastMsg() {
        return textMsg;
    }

    @Override
    public String toString() {
        return "AckMulticastMessage{" + "textMsg=" + textMsg + "} from " +getSender();
    }

}
