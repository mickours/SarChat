package sarchat.message;

import sarchat.User;

public class HeartBeatMessage extends UnicastMessage{
    private User sender;
    
    public HeartBeatMessage(User sender){
        this.sender = sender;
    }
    
    public User getSender() {
        return sender;
    }
    
}
