package sarchat;

import java.util.EventListener;


public interface PeerEventListener extends EventListener{
    public void groupIsReady(GroupTable group);
    public void messageDelivered(String message, User sender);
    public void peerDown(User user);
    public void peerUp(User user);
    public void burst();
}
