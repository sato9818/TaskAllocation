package message;

import java.util.List;

import agent.Agent;
import task.SubTask;

public class Message {
	private SubTask subtask;
	private List<SubTask> subtasks;
	private int delay;
	private MessageType type;
	private Agent from;
	private Agent to;
	private boolean acceptance;
	private int executedTime;
	private int queueSize;
	
	//---------------------------------------------------------------------------------------
	//SOLICITATION
	//ALLOCATION
	//REFUSE
	//COLLAPSE_TEAM
	
	public Message(MessageType type, Agent from, Agent to, SubTask subtask){
		setDelay(from, to);
		this.type = type;
		this.from = from;
		this.to = to;
		this.subtask = subtask;
	}
	//---------------------------------------------------------------------------------------
	//ACCEPTANCE
	public Message(MessageType type, Agent from, Agent to, SubTask subtask, boolean acceptance){
		setDelay(from, to);
		this.type = type;
		this.from = from;
		this.to = to;
		this.subtask = subtask;
		this.acceptance = acceptance;
	}
	
	
	//---------------------------------------------------------------------------------------
	//FINISH
	public Message(MessageType type, Agent from, Agent to, SubTask subtask, int executedTime, int queueSize){
		setDelay(from, to);
		this.type = type;
		this.from = from;
		this.to = to;
		this.subtask = subtask;
		this.executedTime = executedTime;
		this.queueSize = queueSize;
	}
	
	//---------------------------------------------------------------------------------------
	//CNP_SOLICITATION
	public Message(MessageType type, Agent from, Agent to, List<SubTask> subtasks){
		setDelay(from, to);
		this.type = type;
		this.from = from;
		this.to = to;
		this.subtasks = subtasks;
	}
	
	//---------------------------------------------------------------------------------------
	@Override
	public String toString(){
		switch(type){
		case ACCEPTANCE:
			return "Message\nType: " + type + "\n" + 
			"From: " + from.getMyId() + "\n" + 
			"To  : " + to.getMyId() + "\n" +
			"SubTask: " + subtask + "\n" +
			"Accept: " + acceptance;
			
		default:
			return "Message\nType: " + type + "\n" + 
			"From: " + from.getMyId() + "\n" + 
			"To  : " + to.getMyId() + "\n" +
			"SubTask: " + subtask;
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public SubTask getSubTask(){
		return subtask;
	}
	
	//---------------------------------------------------------------------------------------
	
	public List<SubTask> getSubTasks(){
		return subtasks;
	}
	
	//---------------------------------------------------------------------------------------
	
	public MessageType getType(){
		return type;
	}
	//---------------------------------------------------------------------------------------
	
	public Agent from(){
		return from;
	}
	
	//---------------------------------------------------------------------------------------
	
	public Agent to(){
		return to;
	}
	
	//---------------------------------------------------------------------------------------
	
	public boolean isAccepted(){
		return acceptance;
	}
	
	//---------------------------------------------------------------------------------------
	
	private void setDelay(Agent from, Agent to){
		delay = from.getdistance(to.getMyId());
	}
	
	
	//---------------------------------------------------------------------------------------
	
	public boolean isDelivered(){
		delay--;
		if(delay == 0){
			return true;
		}else{
			return false;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getExecutedTime(){
		return executedTime;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getQueueSize(){
		return queueSize;
	}
	
	// 
	
	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}
}
