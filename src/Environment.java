import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Environment {
	private Queue<Task> taskqueue = new ArrayDeque<Task>();
	private List<MessagetoLeader> messagelisttoleader = new ArrayList<MessagetoLeader>();
	private List<MessagetoMember> messagelisttomember = new ArrayList<MessagetoMember>();
	int numOfMessage = 0;
	int sumOfDelay = 0;
	
	Environment(){
	}
	
	
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
	
	public void decrementdelay(){
		for(int i=0;i<messagelisttoleader.size();i++){
			MessagetoLeader message = messagelisttoleader.get(i);
			message.decreasedelay();
		}
		for(int i=0;i<messagelisttomember.size();i++){
			MessagetoMember message = messagelisttomember.get(i);
			message.decreasedelay();
		}
	}
	
	public double checkdelay(){
		
		for(int i=0;i<messagelisttoleader.size();i++){
			MessagetoLeader message = messagelisttoleader.get(i);
			if(message.getdelay() == 0){
				sendmessagetoleader(message);
				numOfMessage++;
				sumOfDelay += message.getdistance();
				messagelisttoleader.remove(i);
				i--;
			}
		}
		for(int i=0;i<messagelisttomember.size();i++){
			MessagetoMember message = messagelisttomember.get(i);
			if(message.getdelay() == 0){
				sendmessagetomember(message);
				numOfMessage++;
				sumOfDelay += message.getdistance();
				messagelisttomember.remove(i);
				i--;
			}
		}
		double aveOfCommu = (double)sumOfDelay / numOfMessage;
		return aveOfCommu;
	}
	
	public void sendmessagetoleader(MessagetoLeader message){
		message.getto().getmessage(message);
	}
	
	public void sendmessagetomember(MessagetoMember message){
		message.getto().getmessage(message, this);
	}

	
}
