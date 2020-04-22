package Message;
import static Constants.Constants.ACCEPTANCE;
import static Constants.Constants.FINISH;
import static Constants.Constants.SOLICITATION;

import Agent.Agent;
import Task.SubTask;

public class Message {
	private SubTask subtask;
	private int delay;
	private int type;
	private Agent from;
	private Agent to;
	private boolean acceptance;
	private int executedTime;
	
	//---------------------------------------------------------------------------------------
	//SOLICITATION
	//ALLOCATION
	
	public Message(int type, Agent from, Agent to, SubTask subtask){
		setDelay(from, to);
		this.type = type;
		this.from = from;
		this.to = to;
		this.subtask = subtask;
	}
	//---------------------------------------------------------------------------------------
	//ACCEPTANCE
	public Message(int type, Agent from, Agent to, SubTask subtask, boolean acceptance){
		setDelay(from, to);
		this.type = type;
		this.from = from;
		this.to = to;
		this.subtask = subtask;
		this.acceptance = acceptance;
	}
	
	//---------------------------------------------------------------------------------------
	//FINISH
	public Message(int type, Agent from, Agent to, SubTask subtask, int executedTime){
		setDelay(from, to);
		this.type = type;
		this.from = from;
		this.to = to;
		this.subtask = subtask;
		this.executedTime = executedTime;
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
	
	public int getType(){
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
}
