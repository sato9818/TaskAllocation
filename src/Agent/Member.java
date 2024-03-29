package agent;
import static shared.Constants.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import analysis.Analyzer;
import environment.Area;
import environment.Environment;
import message.Message;
import message.MessageType;
import random.Sfmt;
import task.SubTask;
import util.Calculator;

public class Member extends Agent{
	
	//このメンバにきたメッセージの集合
	private List<Message> solicitationMessages = new ArrayList<Message>();
	//処理が終了したメッセージ
	private Message finishMessage = null; 
	
	HashMap<Integer, Integer> allocateTimeMap = new HashMap<Integer, Integer>();
	
	Queue<Message> messageQueue = new ArrayDeque<Message>();
	
	private List<Message> preSubTasks = new ArrayList<Message>();
	
	private int expectedTasks = 0;
	private int inactiveTime = 0;
	private boolean roleChangable = false;
	
	//List<Member> deagent = new ArrayList<Member>();
	
	//---------------------------------------------------------------------------------------
	
	public Member(Area area, int x, int y, int id, Environment e){
		super(area, x, y, id, e);
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
			if(mySubTask == null){
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
		mySubTask = allocationMessage.getSubTask(); 
		int et = getExcutingTime(mySubTask);	
		remainingTime = et;
		finishMessage = new Message(MessageType.FINISH, this, allocationMessage.from(), allocationMessage.getSubTask(), et, messageQueue.size());
		Analyzer.waitingTime[this.getArea().getId()][tick] += tick - allocateTimeMap.get(finishMessage.getSubTask().getTaskId());
		executeTask(tick);
	}
	
	//---------------------------------------------------------------------------------------
	
	public void replyMessages(){
		if(CNP_MODE == true){
			for(int i=0;i<solicitationMessages.size();i++){
				Message message = solicitationMessages.get(i);
				SubTask subTask = decideSubTask(message);
				Message acceptedMessage = new Message(MessageType.ACCEPTANCE, this, message.from(), subTask, true);
				allMessages.add(acceptedMessage);
			}
			solicitationMessages.clear();
		}else{
			int p = Calculator.eGreedy(environment.rnd, this.epsilonForMember);
			while(!solicitationMessages.isEmpty()){
				boolean decide = true;
				Message message = null;
				if(p == 0){
					message = decideMessage(solicitationMessages, null);
					if(this.isReciprocity()){
						if(!deAgents.contains(message.from())){
							decide = false;
						}
					}
				}else if(p == 1){
					message = decideMessage(solicitationMessages, environment.rnd);
				}
				
				// if(COOPERATIVE && messageQueue.size() + expectedTasks >= SUB_TASK_QUEUE_SIZE){
				// 	decide = false;
				// }
				
				if(messageQueue.size() == SUB_TASK_QUEUE_SIZE){
					decide = false;
				}
				
				if(decide){
					expectedTasks++;
				}
				sendReplyMessages(message, decide);
				solicitationMessages.remove(message);
			}
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void replyCnpMessages(){
		
		
	}
	
	//---------------------------------------------------------------------------------------
	
	
	public Message decideMessage(List<Message> messages, Sfmt rnd){
		if(rnd != null){
			int messageSize = messages.size();
			int p = (int)(rnd.NextUnif() * messageSize);
			Message message = messages.get(p);
			return message;
		}else{
			sortMessagesByMemberDE(messages);
			Message message = messages.get(0);
			return message;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public SubTask decideSubTask(Message message){
		List<SubTask> subTasks = message.getSubTasks();
		double max = 0;
		SubTask confSubTask = null; 
		for(int i=0;i<subTasks.size();i++){
			SubTask subTask = subTasks.get(i);
			double utility;
			if(max < (utility = (double)subTask.getutility() / getExcutingTime(subTask))){
				max = utility;
				confSubTask = subTask;
			}
		}
		return confSubTask;
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
			Message acceptedMessage = new Message(MessageType.ACCEPTANCE, this, solicitationMessage.from(), solicitationMessage.getSubTask(), true); 
			allMessages.add(acceptedMessage);
		}else{
			Message rejectedMessage = new Message(MessageType.ACCEPTANCE, this, solicitationMessage.from(), solicitationMessage.getSubTask(), false); 
			allMessages.add(rejectedMessage);
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void executeTask(int tick){
		remainingTime--;
		if(remainingTime == 0){
			finishMessage.setQueueSize(messageQueue.size());
			allMessages.add(finishMessage);
			mySubTask = null;
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
		case CNP_SOLICITATION:
			solicitationMessages.add(message);
			break;
		case ALLOCATION:
			expectedTasks--;
			if(message.getSubTask() == null){
				updateRoleEvaluation(false);
				updateDependablity(message, false);
				unallocatedSubtasks++;
			}else{
				updateRoleEvaluation(true);
				updateDependablity(message, true);
				preSubTasks.add(message);
				updateThreshold(message.from());
				allocatedSubTasks++;
				allocatedSubTask[getMyId()]++;
			}
			break;
		case COLLAPSE_TEAM:
			SubTask subTask = message.getSubTask();
			if(mySubTask != null){
				if(subTask.getTaskId() == mySubTask.getTaskId()){
					remainingTime = 0;
					mySubTask = null;
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
			updateDependablity(message, false, 0);
			notifyFailure(message,tick);
			rejectedSubtasks++;
			break;
		case FINISH:
			finishMemberSubTask(message, tick);
			break;
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	
	private void chooseSubTasks(int tick){
		sortMessagesByMemberDE(preSubTasks);
//		System.out.println("ID: " + getMyId());
		for(int i = 0;i<preSubTasks.size();i++){
			Message message = preSubTasks.get(i);
//			System.out.println("de: " + de[message.from().getMyId()]);
			if(messageQueue.size() >= SUB_TASK_QUEUE_SIZE){
				allMessages.add(new Message(MessageType.REFUSE, this, message.from(), message.getSubTask()));
				
				refusedTask[getMyId()]++;
				Analyzer.rejectedTask[this.getArea().getId()][tick]++;
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
//			delta = 1.0 / (double)(this.getdistance(message.from().getMyId()) * 2 + getExcutingTime(message.getSubTask()));
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
		return remainingTime;
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