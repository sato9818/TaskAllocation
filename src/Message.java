
public class Message {
	private SubTask subtask;
	protected int delay;
	private int distance;
	private int type;
	
	Message(SubTask s, int ty){
		subtask = s;
		type = ty;
	}
	
	public int getdelay(){
		return delay;
	}
	
	public SubTask getsubtask(){
		return subtask;
	}
	
	public int gettype(){
		return type;
	}
	
	protected void setdelay(Agent from, Agent to){
		delay = manhattan(from.getPositionx(), to.getPositionx(), from.getPositiony(), to.getPositiony()) / 5/**/ + 1;
		distance = delay;
	}
	
	public void decreasedelay(){
		delay--;
	}
	
	private int manhattan(int x1, int x2, int y1, int y2){
		return Math.abs(x1-x2) + Math.abs(y1-y2);
	}
	
	public int getdistance(){
		return distance;
	}
}
