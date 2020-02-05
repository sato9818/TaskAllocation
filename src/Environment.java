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
	private Queue<Task> taskqueue0 = new ArrayDeque<Task>();
	private Queue<Task> taskqueue1 = new ArrayDeque<Task>();
	private Queue<Task> taskqueue2 = new ArrayDeque<Task>();
	private Queue<Task> taskqueue3 = new ArrayDeque<Task>();
	private Queue<Task> taskqueue4 = new ArrayDeque<Task>();
	private Queue<Task> taskqueue5 = new ArrayDeque<Task>();
	private Queue<Task> taskqueue6 = new ArrayDeque<Task>();
	private Queue<Task> taskqueue7 = new ArrayDeque<Task>();
	private Queue<Task> taskqueue8 = new ArrayDeque<Task>();
	private List<MessagetoLeader> messagelisttoleader = new ArrayList<MessagetoLeader>();
	private List<MessagetoMember> messagelisttomember = new ArrayList<MessagetoMember>();
	int numOfMessage = 0;
	int sumOfDelay = 0;
	
	
	//---------------------------------------------------------------------------------------
	
	Environment(){
	}
	
	//---------------------------------------------------------------------------------------
	
	
	public void addTask(int mu, Sfmt rnd){
		for(int i=0;i<mu;i++){
			Task task = new Task(rnd);
			taskqueue.add(task);
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public int addTask(int mu, Sfmt rnd, int type){
		int wastetask = 0;
		switch(type){
		case 0:
			if(taskqueue0.size() + mu > 1000){
				wastetask = taskqueue0.size() + mu - 1000;
				mu = mu - wastetask;
			}
			for(int i=0;i<mu;i++){
				Task task = new Task(rnd);
				taskqueue0.add(task);
			}
			break;
		case 1:
			if(taskqueue1.size() + mu > 1000){
				wastetask = taskqueue1.size() + mu - 1000;
				mu = mu - wastetask;
			}
			for(int i=0;i<mu;i++){
				Task task = new Task(rnd);
				taskqueue1.add(task);
			}
			break;
		case 2:
			if(taskqueue2.size() + mu > 1000){
				wastetask = taskqueue2.size() + mu - 1000;
				mu = mu - wastetask;
			}
			for(int i=0;i<mu;i++){
				Task task = new Task(rnd);
				taskqueue2.add(task);
			}
			break;
		case 3:
			if(taskqueue3.size() + mu > 1000){
				wastetask = taskqueue3.size() + mu - 1000;
				mu = mu - wastetask;
			}
			for(int i=0;i<mu;i++){
				Task task = new Task(rnd);
				taskqueue3.add(task);
			}
			break;
		case 4:
			if(taskqueue4.size() + mu > 1000){
				wastetask = taskqueue4.size() + mu - 1000;
				mu = mu - wastetask;
			}
			for(int i=0;i<mu;i++){
				Task task = new Task(rnd);
				taskqueue4.add(task);
			}
			break;
		case 5:
			if(taskqueue5.size() + mu > 1000){
				wastetask = taskqueue5.size() + mu - 1000;
				mu = mu - wastetask;
			}
			for(int i=0;i<mu;i++){
				Task task = new Task(rnd);
				taskqueue5.add(task);
			}
			break;
		case 6:
			if(taskqueue6.size() + mu > 1000){
				wastetask = taskqueue6.size() + mu - 1000;
				mu = mu - wastetask;
			}
			for(int i=0;i<mu;i++){
				Task task = new Task(rnd);
				taskqueue6.add(task);
			}
			break;
		case 7:
			if(taskqueue7.size() + mu > 1000){
				wastetask = taskqueue7.size() + mu - 1000;
				mu = mu - wastetask;
			}
			for(int i=0;i<mu;i++){
				Task task = new Task(rnd);
				taskqueue7.add(task);
			}
			break;
		case 8:
			if(taskqueue8.size() + mu > 1000){
				wastetask = taskqueue8.size() + mu - 1000;
				mu = mu - wastetask;
			}
			for(int i=0;i<mu;i++){
				Task task = new Task(rnd);
				taskqueue8.add(task);
			}
			break;
		}
		return wastetask;
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public boolean TaskisEmpty(){
		return taskqueue.isEmpty();
	}
	
	//---------------------------------------------------------------------------------------
	
	public boolean TaskisEmpty(int area){
		switch(area){
		case 0:
			return taskqueue0.isEmpty();
		case 1:
			return taskqueue1.isEmpty();
		case 2:
			return taskqueue2.isEmpty();
		case 3:
			return taskqueue3.isEmpty();
		case 4:
			return taskqueue4.isEmpty();
		case 5:
			return taskqueue5.isEmpty();
		case 6:
			return taskqueue6.isEmpty();
		case 7:
			return taskqueue7.isEmpty();
		case 8:
			return taskqueue8.isEmpty();
		default:
			return false;
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public Task pushTask(){
		return taskqueue.poll();
	}
	
	//---------------------------------------------------------------------------------------
	
	public Task pushTask(int area){
		switch(area){
		case 0:
			return taskqueue0.poll();
		case 1:
			return taskqueue1.poll();
		case 2:
			return taskqueue2.poll();
		case 3:
			return taskqueue3.poll();
		case 4:
			return taskqueue4.poll();
		case 5:
			return taskqueue5.poll();
		case 6:
			return taskqueue6.poll();
		case 7:
			return taskqueue7.poll();
		case 8:
			return taskqueue8.poll();		
		default:
			return null;
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public void addmessagetoleader(MessagetoLeader m){
		messagelisttoleader.add(m);
	}
	
	//---------------------------------------------------------------------------------------
	
	public void addmessagetomember(MessagetoMember m){
		messagelisttomember.add(m);
	}
	
	//---------------------------------------------------------------------------------------
	
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
	
	//---------------------------------------------------------------------------------------
	
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
	
	//---------------------------------------------------------------------------------------
	
	public void sendmessagetoleader(MessagetoLeader message){
		message.getto().getmessage(message);
	}
	
	//---------------------------------------------------------------------------------------
	
	public void sendmessagetomember(MessagetoMember message){
		message.getto().getmessage(message);
	}
	
	//---------------------------------------------------------------------------------------
	
	public double[] checkdelay(List<Leader> leaders, List<Member> members){
		final int area = 9;
		int areaMessageCount[] = new int[area];
		int areaDelaySum[] = new int[area];
		double areaAveCommu[] = new double[area];
		
		for(int i=0;i<messagelisttoleader.size();i++){
			MessagetoLeader message = messagelisttoleader.get(i);
			if(message.getdelay() == 0){
				sendmessagetoleader(message,leaders,members);
				areaMessageCount[message.getfrom().getArea()]++;
				areaDelaySum[message.getfrom().getArea()] += message.getdistance();
				messagelisttoleader.remove(i);
				i--;
			}
		}
		for(int i=0;i<messagelisttomember.size();i++){
			MessagetoMember message = messagelisttomember.get(i);
			if(message.getdelay() == 0){
				sendmessagetomember(message,leaders,members);
				areaMessageCount[message.getfrom().getArea()]++;
				areaDelaySum[message.getfrom().getArea()] += message.getdistance();
				messagelisttomember.remove(i);
				i--;
			}
		}
		for(int i=0;i<area;i++){
			areaAveCommu[i] = (double)areaDelaySum[i] / areaMessageCount[i];
		}
		
		return areaAveCommu;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void sendmessagetomember(MessagetoMember message, List<Leader> leaders, List<Member> members){
		//System.out.println("message type " + message.gettype() + " from " + message.getfrom().getmyid() + " to " + message.getto().getmyid() + " " + message.taskisallocated());
		for(int i=0;i<members.size();i++){
			Member member = members.get(i);
			if(member.getmyid() == message.getto().getmyid()){
				member.getmessage(message);
			}
		}
		for(int i=0;i<leaders.size();i++){
			Leader leader = leaders.get(i);
			if(leader.getmyid() == message.getto().getmyid() && message.gettype() == 0){
				leader.getmessage(new MessagetoLeader(message.getfrom(), message.getto(), 3));
			}
		}
		//message.getto().getmessage(message);
	}
	
	//---------------------------------------------------------------------------------------
	
	public void sendmessagetoleader(MessagetoLeader message, List<Leader> leaders, List<Member> members){
		//System.out.println("message type " + message.gettype() + " from " + message.getfrom().getmyid() + " to " + message.getto().getmyid() + " " + message.taskisallocated());
		for(int i=0;i<members.size();i++){
			Member member = members.get(i);
			if(member.getmyid() == message.getto().getmyid() && message.gettype() == 1){
				member.getmessage(new MessagetoMember(message.getfrom(), member,message.getsubtask()));
			}
		}
		for(int i=0;i<leaders.size();i++){
			Leader leader = leaders.get(i);
			if(leader.getmyid() == message.getto().getmyid()){
				leader.getmessage(message);
			}
		}
		//message.getto().getmessage(message);
	}
	
	//---------------------------------------------------------------------------------------
	
}
