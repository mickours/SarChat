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
            this.group = new GroupTable(group);
        }
    }
    
    LinkedList<Tuple> msgQ = new LinkedList<>();
    
    public Tuple insertMessage(TextMessage msg,GroupTable group){
        for (Tuple tuple : msgQ) {
            if (tuple.msg.getClock() == msg.getClock() && tuple.msg.getSender().equals(msg.getSender())){
                return tuple;
            }
        }
        int ind=0;
        while (msgQ.size() > ind && msgQ.get(ind).msg.getClock() <= msg.getClock() ){
            ind++;
        }
        msgQ.add(ind, new Tuple(msg,msg.getSender(),group));
        return msgQ.get(ind);
    }
    
    public Tuple getHeadMessage(){
        return msgQ.pop();
    }
    
    public boolean ackReceived(User ackFrom, TextMessage txtMsg, GroupTable group) {
        for (Tuple tuple : msgQ) {
            if (tuple.sender.equals(txtMsg.getSender()) && txtMsg.getClock() == tuple.msg.getClock()){
                tuple.group.remove(ackFrom);
                return tuple.group.isEmpty();
            }
        }
        Tuple entry = insertMessage(txtMsg,group);
        entry.group.remove(ackFrom);
        return entry.group.isEmpty();
    }
    
}
