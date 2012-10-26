/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat.message;

/**
 *
 * @author Michael Mercier <michael_mercier@orange.fr>
 */
public class LogicalClock {
    private int clock;

    public int getClock() {
        return clock;
    }

    public void incrementClock(){
        clock++;
    }

    public void updateClock(LogicalClock msgClock){
        clock = Math.max(clock, msgClock.getClock())+1;
    }
}
