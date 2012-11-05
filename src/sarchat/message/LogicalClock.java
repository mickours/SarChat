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
public class LogicalClock implements Serializable {

    private int clock = 0;

    public int getClock() {
        return clock;
    }

    public int incrementClock() {
        return clock++;
    }

    public void updateClock(int msgClock) {
        clock = Math.max(clock, msgClock) + 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LogicalClock other = (LogicalClock) obj;
        if (this.clock != other.clock) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.clock;
        return hash;
    }
}
