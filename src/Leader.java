import java.util.ArrayList;
import java.util.List;

public class Leader extends Agent{
	private Task task;
	private int phase = 0;
	private int waittime = 0;
	private List<Member> preteam = new ArrayList<Member>();
	
	Leader(){
		super();
	}
	
	public void setTask(Task t){
		task = t;
	}
	
	public int getPhase(){
		return phase;
	}
	
	public void changephase(){
		phase++;
	}
	
	public void selectmember(){
		
	}
}
