/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat.message;

import java.io.Serializable;

/**
 *
 * @author Michael Mercier <michael_mercier@orange.fr>
 */
public class MulticastMessage implements Message, Serializable {
    private LogicalClock lc;

    public LogicalClock getLc() {
        return lc;
    }

    public void setLc(LogicalClock lc) {
        this.lc = lc;
    }
    
    
}
