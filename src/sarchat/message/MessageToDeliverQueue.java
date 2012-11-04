package sarchat.message;

import java.util.LinkedList;
import sarchat.GroupTable;
import sarchat.User;

public class MessageToDeliverQueue {

    
    public class Tuple{
       public TextMessage msg;
       public GroupTable group;
       public User sender;

        public Tuple(TextMessage msg, User sender,GroupTable group) {
            this.sender = sender;
            this.msg = msg;
            this.group = group;
        }
    }
    
    LinkedList<Tuple> msgQ = new LinkedList<>();
    
    public void insertMessage(TextMessage msg,User from,GroupTable group){
        int ind=0;
        while (msgQ.size() > ind && msgQ.get(ind).msg != null 
                && msgQ.get(ind).msg.getClock() < msg.getClock() ){
            ind++;
        }
        msgQ.add(ind, new Tuple(msg,from,group));
    }
    
    public Tuple getHeadMessage(){
        return msgQ.pop();
    }
    
    public boolean ackReceived(User ackFrom, User sender, int lc) {
        for (Tuple tuple : msgQ) {
            if (tuple.sender.equals(sender) && lc == tuple.msg.getClock()){
                tuple.group.remove(ackFrom);
                return tuple.group.isEmpty();
            }
        }
        throw new RuntimeException("the ack from "+ackFrom+" for the message "+lc+":"+sender+" failed");
    }
    
}
