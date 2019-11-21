
public class Message {
	private SubTask subtask;
	protected int delay;
	private int distance;
	private int type;
	
	//---------------------------------------------------------------------------------------
	
	Message(SubTask s, int ty){
		subtask = s;
		type = ty;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getdelay(){
		return delay;
	}
	
	//---------------------------------------------------------------------------------------
	
	public SubTask getsubtask(){
		return subtask;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int gettype(){
		return type;
	}
	
	//---------------------------------------------------------------------------------------
	
	protected void setdelay(Agent from, Agent to){
		delay = from.getdistance(to.getmyid());
		distance = delay;
	}
	
	//---------------------------------------------------------------------------------------

	
	public void decreasedelay(){
		delay--;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getdistance(){
		return distance;
	}
	
	//---------------------------------------------------------------------------------------
}
