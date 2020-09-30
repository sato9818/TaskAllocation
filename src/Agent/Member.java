package Agent;
import static Constants.Constants.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import Environment.Area;
import Environment.Environment;
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
	
	Queue<Message> messageQueue = new ArrayDeque<Message>();
	
	private List<Message> preSubTasks = new ArrayList<Message>();
	
	SubTask excutingTask = null;
	
	private int expectedTasks = 0;
	private int inactiveTime = 0;
	private boolean roleChangable = false;
	
	//List<Member> deagent = new ArrayList<Member>();
	
	//---------------------------------------------------------------------------------------
	
	public Member(Area area, int x, int y){
		super(area, x, y);
	}
	
	//---------------------------------------------------------------------------------------
	
	public Member(Leader ld){
		super(ld);
	}
	
	//---------------------------------------------------------------------------------------
	public void act(int tick){
		chooseSubTasks(tick);
		memberCount[getMyId()]++;
		ownedSubtask[getMyId()][tick] += messageQueue.size();
		
		switch(phase){
		case INACTIVE:
			if(!solicitationMessages.isEmpty()){//メッセージが来ていたら
				replyMessages();
				phase = ACTIVE;
				inactiveTime = 0;
				roleChangable = false;
			}else{
				inactiveTime++;
				if(inactiveTime > INACTIVE_THRESHOLD){
					updateRoleEvaluation(false);
					roleChangable = true;
					inactiveTime = 0;
				}
			}
			break;
		case ACTIVE:
			if(!messageQueue.isEmpty()){
				startToExecuteTask(tick);
				phase = EXECUTING_TASK;
			}else{
				if(!solicitationMessages.isEmpty()){
					replyMessages();
				}else{
					if(expectedTasks > 0){
						phase = ACTIVE;
					}else{
						phase = INACTIVE;
						roleChangable = true;
					}
				}
			}
			
			break;
		case EXECUTING_TASK:
			if(excutingTask == null){
				if(!solicitationMessages.isEmpty()){
					replyMessages();
					phase = ACTIVE;
				}else{
					if(!messageQueue.isEmpty()){
						startToExecuteTask(tick);
						phase = EXECUTING_TASK;
					}else{
						if(expectedTasks > 0){
							phase = ACTIVE;
						}else{
							phase = INACTIVE;
							roleChangable = true;
						}
					}
				}
			}else{
				executeTask(tick);
			}
			break;
		}
		
	}
	//---------------------------------------------------------------------------------------
	
	public void startToExecuteTask(int tick){
		Message allocationMessage = messageQueue.poll();
		excutingTask = allocationMessage.getSubTask(); 
		int et = getExcutingTime(excutingTask);	
		excutingTime = et;
		finishMessage = new Message(FINISH, this, allocationMessage.from(), allocationMessage.getSubTask(), et);
		waitingTime[this.getArea().getId()][tick] += tick - allocateTimeMap.get(finishMessage.getSubTask().getTaskId());
		executeTask(tick);
	}
	
	//---------------------------------------------------------------------------------------
	
	public void replyMessages(){
		int p = eGreedy();
		
		while(!solicitationMessages.isEmpty()){
			boolean decide = true;
			Message message = null;
			if(p == 0){
				message = decideMessage(solicitationMessages, null);
				if(this.isReciprocity()){
					if(!deAgents.contains(message.from())){
						decide = false;
					}
					if(expectedTasks + messageQueue.size() < SUB_TASK_QUEUE_SIZE - deAgents.size()){
						decide = true;
					}
				}
			}else if(p == 1){
				message = decideMessage(solicitationMessages, Environment.rnd);
			}
			
//			if(expectedTasks + taskQueue.size() ){
//				decide = false;
//			}
			if(decide){
				expectedTasks++;
			}
			sendReplyMessages(message, decide);
			solicitationMessages.remove(message);
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	
	public Message decideMessage(List<Message> messages, Sfmt rnd){
		if(rnd != null){
			int messageSize = messages.size();
			int p = (int)(rnd.NextUnif() * messageSize);
			Message message = messages.get(p);
			return message;
		}else{
			messages = sortMessageByMemberDe(messages);
			Message message = messages.get(0);
//			for(int i=0;i<messages.size();i++){
//				System.out.println("member: " + getMyId() + " " + memberDe[messages.get(i).from().getMyId()]);
//			}
			return message;
		}
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
	
	public void executeTask(int tick){
		excutingTime--;
		if(excutingTime == 0){
			finishSubTask++;
			allMessages.add(finishMessage);
			
			excutingTask = null;
			finishMessage = null;
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
				preSubTasks.add(message);
				allocatedSubTask[getMyId()]++;
			}
			break;
		case COLLAPSE_TEAM:
			SubTask subTask = message.getSubTask();
			if(excutingTask != null){
				if(subTask.getTaskId() == excutingTask.getTaskId()){
					excutingTime = 0;
					excutingTask = null;
					phase = ACTIVE;
				}
			}else{
				for(Message m : messageQueue){
					if(m.getSubTask().getTaskId() == subTask.getTaskId()){
						messageQueue.remove(m);
						break;
					}
				}
			}
			break;
		case REFUSE:
//			System.out.println(getMyId());
//			System.out.println(executionTimeMap);
			updateDependablity(message, false, 0);
			notifyFailure(message,tick);
			break;
		case FINISH:
			finishMemberSubTask(message, tick);
			break;
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	
	private void chooseSubTasks(int tick){
		preSubTasks = sortMessageByMemberDe(preSubTasks);
//		System.out.println("ID: " + getMyId());
		for(int i = 0;i<preSubTasks.size();i++){
			Message message = preSubTasks.get(i);
//			System.out.println("de: " + de[message.from().getMyId()]);
			if(messageQueue.size() >= SUB_TASK_QUEUE_SIZE){
				allMessages.add(new Message(REFUSE, this, message.from(), message.getSubTask()));
				
				refusedTask[getMyId()]++;
				rejectedTask[this.getArea().getId()][tick]++;
			}else{
				messageQueue.add(message);
				allocateTimeMap.put(message.getSubTask().getTaskId(), tick);
			}
		}
		preSubTasks.clear();
	}
	
	//---------------------------------------------------------------------------------------
	
	public void updateDependablity(Message message, boolean success){
		double delta = 0.0;
		if(success){
			delta = (double)message.getSubTask().getutility() / (double)(this.getdistance(message.from().getMyId()) * 2 + getExcutingTime(message.getSubTask()));
//			System.out.println("excutiontime " + this.getdistance(message.from().getMyId()));
//			delta = 1.0 / (this.getdistance(message.from().getMyId()) * 2 + getExcutingTime(message.getSubTask()));
//			delta = 1;
		}
		this.memberDe[message.from().getMyId()] = 
					(1.0 - LEARNING_RATE/**/) * this.memberDe[message.from().getMyId()] 
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
	
	public void selectAction(){
		if(MEMBER_DEPENDABLITY_AGENT_THRESHOLD <= deAgents.size()){
			reciprocityAction = true;
		}else{
			reciprocityAction = false;
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
	public int getSubTaskQueueSize(){
		return messageQueue.size();
	}
}