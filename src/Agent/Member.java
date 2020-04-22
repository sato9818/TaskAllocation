package Agent;
import static Constants.Constants.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import Environment.Area;
import Message.Message;
import Random.Sfmt;
import Task.SubTask;

public class Member extends Agent{
	
	//このメンバにきたメッセージの集合
	private List<Message> solicitationMessages = new ArrayList<Message>();
	//処理が終了したメッセージ
	private Message finishMessage = null; 
	
	HashMap<Integer, Integer> allocateTimeMap = new HashMap<Integer, Integer>();
	
	private int excutingTime;
	
	Queue<Message> taskQueue = new ArrayDeque<Message>();
	
	SubTask excutingTask = null;
	
	private int expectedTasks = 0;
	private int inactiveTime = 0;
	private boolean roleChangable = false;
	
	//List<Member> deagent = new ArrayList<Member>();
	
	//---------------------------------------------------------------------------------------
	
	public Member(Sfmt rnd, Area area, int x, int y){
		super(rnd, area, x, y);
	}
	
	//---------------------------------------------------------------------------------------
	
	public Member(Leader ld){
		super(ld);
	}
	
	//---------------------------------------------------------------------------------------
	public void act(int tick){
		
		
		switch(phase){
		case INACTIVE:
			if(!solicitationMessages.isEmpty()){//メッセージが来ていたら
				phase = ACTIVE;
				inactiveTime = 0;
				roleChangable = false;
			}else{
				inactiveTime++;
				if(inactiveTime > INACTIVE_THRESHOLD){
					updateRoleEvaluation(false);
					roleChangable = true;
				}
				break;
			}
		case ACTIVE:
				//メッセージから受理するメッセージを選ぶ
			if(!solicitationMessages.isEmpty()){
				while(!solicitationMessages.isEmpty()){
					boolean decide = true;
					int p = eGreedy(rnd);
					Message message = null;
					if(p == 0){
						message = decideMessage(solicitationMessages, null);
					}else if(p == 1){
						message = decideMessage(solicitationMessages, rnd);
						
					}
					if(expectedTasks + taskQueue.size() > 4){
						decide = false;
					}
					if(decide){
						expectedTasks++;
					}
					sendReplyMessages(message, decide);
					solicitationMessages.remove(message);
				}
				//ないと思うが受理したタスクがなかったらinactiveへ
				if(expectedTasks == 0){
					phase = INACTIVE;
				}
				break;
			}else{
				phase = EXECUTING_TASK;
			}
		case EXECUTING_TASK:
			if(excutingTask == null){
				if(!taskQueue.isEmpty()){
					Message allocationMessage = taskQueue.poll();
					excutingTask = allocationMessage.getSubTask(); 
					int et = getExcutingTime(excutingTask);	
					excutingTime = et;
					finishMessage = new Message(FINISH, this, allocationMessage.from(), allocationMessage.getSubTask(), et);
				}else{
					if(expectedTasks > 0){
						phase = ACTIVE;
					}else if(expectedTasks == 0){
						phase = INACTIVE;
					}
				}
			}else{
				executeTask(tick);
			}
			break;
		}
//		System.out.println(phase);
	}
	
	//---------------------------------------------------------------------------------------
	
	
	public Message decideMessage(List<Message> messages, Sfmt rnd){
		Message decide = null;
		if(rnd != null){
			int messageSize = messages.size();
			int p = (int)(rnd.NextUnif() * messageSize);
			decide = messages.get(p);
			return decide;
		}
		for(int i=0;i<messages.size();i++){
			Message message = messages.get(i);
			Agent leader = message.from();
			if(deagent.isEmpty()){
				if(decide == null){ 
					decide = message;
				}else{
					//信頼度高いリーダーを優先
					if(de[leader.getMyId()] > de[decide.from().getMyId()]){
						decide = message;
					}
				}	
			}else{
				if(deagent.contains(leader) && !deagent.contains(decide)){
					decide = message;
				}else if(deagent.contains(leader) && deagent.contains(decide)){
					if(de[leader.getMyId()] > de[decide.from().getMyId()]){
						decide = message;
					}
				}else if(!deagent.contains(leader) && !deagent.contains(decide)){
					decide = null;
				}
			}
		}
		return decide;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void updateRoleEvaluation(boolean success){
		double delta;
		if(success){
			delta = 1.0;
		}else{
			delta = 0.0;
		}
		memberEvaluation = (1.0 - LEARNING_RATE) * memberEvaluation + LEARNING_RATE * delta; 
	}
		
	//---------------------------------------------------------------------------------------
	
	public void sendReplyMessages(Message solicitationMessage, boolean decide){
		if(decide){
			Message acceptedMessage = new Message(ACCEPTANCE, this, solicitationMessage.from(), solicitationMessage.getSubTask(), true); 
			allMessages.add(acceptedMessage);
		}else{
			Message rejectedMessage = new Message(ACCEPTANCE, this, solicitationMessage.from(), solicitationMessage.getSubTask(), false); 
			allMessages.add(rejectedMessage);
		}
	}
	
	
	//---------------------------------------------------------------------------------------
	
	public int getExcutingTime(SubTask s){
		int et = 0;
		for(int i=0;i<TYPES_OF_RESOURCE;i++){
			int a = (int)Math.ceil((double)s.getcapacity(i) / capacity[i]);
			if(et < a ){
				et = a;
			}
		}
		return et;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void executeTask(int tick){
		excutingTime--;
		if(excutingTime == 0){
			finishSubTask++;
			allMessages.add(finishMessage);
			waitingTime[this.getArea().getId()][tick] += tick - allocateTimeMap.get(finishMessage.getSubTask().getTaskId());
			excutingTask = null;
			finishMessage = null;
			if(taskQueue.size() > 0 || expectedTasks > 0){
				phase = ACTIVE;
			}else{
				phase = INACTIVE;
				roleChangable = true;
			}
		}
	}
	
	//---------------------------------------------------------------------------------------
	@Override
	public void readMessage(Message message, int tick){
		//System.out.println(message);
		switch(message.getType()){
		case SOLICITATION:
			solicitationMessages.add(message);
			break;
		case ALLOCATION:
			expectedTasks--;
			if(message.getSubTask() == null){
				updateRoleEvaluation(false);
				updateDependablity(message, false);
			}else{
				updateRoleEvaluation(true);
				updateDependablity(message, true);
				taskQueue.add(message);
				allocateTimeMap.put(message.getSubTask().getTaskId(), tick);
			}
			break;
		case FINISH:
			finishMemberSubTask(message, tick);
			break;
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public void updateDependablity(Message message, boolean success){
		double delta = 0.0;
		if(success){
//			delta = (double)message.getsubtask().getutility() / (message.getdistance() * 2 + excutiontime);
			//System.out.println("excutiontime " + excutiontime);
			delta = 1 / (this.getdistance(message.from().getMyId()) * 2 + excutingTime);
//			delta = 1;
		}
		this.de[message.from().getMyId()] = 
					(1.0 - LEARNING_RATE/**/) * this.de[message.from().getMyId()] 
					+ LEARNING_RATE * delta;
		//System.out.println("de[" + message.getfrom().getmyid() + "] = " + this.de[message.getfrom().getmyid()]);
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public boolean roleChangable(){
		return roleChangable;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getExcutiontime(){
		return excutingTime;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void updatedeagent(){
		if(MEMBER_DEPENDABLITY_AGENT_THRESHOLD < deagent.size()){
			deagent = sortagent(deagent);
			List<Agent> buf = new ArrayList<Agent>();
			for(int i=0;i<MEMBER_DEPENDABLITY_AGENT_THRESHOLD;i++){
				buf.add(deagent.get(i));
			}
			deagent = buf;
		}
	}
	
	
	//---------------------------------------------------------------------------------------

	public boolean haveMessages(){
		return !solicitationMessages.isEmpty();
	}
	
	//---------------------------------------------------------------------------------------
	
	public void clearall(){
		finishMessage = null;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void clearmessages(){
		solicitationMessages.clear();
	}
	
	//---------------------------------------------------------------------------------------
}