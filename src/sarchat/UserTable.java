/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sarchat;

import java.net.InetAddress;
import java.util.List;

/**
 *
 * @author Michael Mercier <michael_mercier@orange.fr>
 */
public class UserTable {
    private class triplet{
        public String name;
        public InetAddress ip;
        public int port;
    }

    private List<triplet> table;
}
