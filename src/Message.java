
public class Message {
	private SubTask subtask;
	private int delay;
	Message(SubTask s){
		subtask = s;
	}
	
	public int getdelay(){
		return delay;
	}
	
	public SubTask getsubtask(){
		return subtask;
	}
}
