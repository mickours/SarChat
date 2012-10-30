/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat.message;

/**
 *
 * @author mickours
 */
public class TextMessage extends MulticastMessage{


    private String message;
    
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return " lc=" +getLc() + " message=" + message;
    }
    
    
}
