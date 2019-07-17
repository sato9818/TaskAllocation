import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Environment {
	private Queue<Task> taskqueue = new ArrayDeque<>();
	private List<MessagetoLeader> messagelisttoleader = new ArrayList<MessagetoLeader>();
	private List<MessagetoMember> messagelisttomember = new ArrayList<MessagetoMember>();
	
	
	public void addTask(int mu, Sfmt rnd){
		for(int i=0;i<mu;i++){
			Task task = new Task(rnd);
			taskqueue.add(task);
		}
	}
	
	public boolean TaskisEmpty(){
		return taskqueue.isEmpty();
	}
	
	public Task pushTask(){
		return taskqueue.poll();
	}
	
	public void addmessagetoleader(MessagetoLeader m){
		messagelisttoleader.add(m);
	}
	
	public void addmessagetomember(MessagetoMember m){
		messagelisttomember.add(m);
	}
	
	public void checkdelay(){
		for(int i=0;i<messagelisttoleader.size();i++){
			MessagetoLeader message = messagelisttoleader.get(i);
			if(message.getdelay() == 0){
				sendmessagetoleader(message);
				messagelisttoleader.remove(i);
				i--;
			}
		}
		for(int i=0;i<messagelisttomember.size();i++){
			MessagetoMember message = messagelisttomember.get(i);
			if(message.getdelay() == 0){
				sendmessagetomember(message);
				messagelisttomember.remove(i);
				i--;
			}
		}
	}
	
	public void sendmessagetoleader(MessagetoLeader message){
		message.getto().getmessage(message);
	}
	
	public void sendmessagetomember(MessagetoMember massage){
		
	}
	
	public void deletemessage(){
		
	}
	
}
