import java.util.ArrayList;
import java.util.List;

public class Task {
	private List<SubTask> subtasks = new ArrayList<SubTask>();
	
	private int utility = 0;
	
	Task(Sfmt rnd){
		int numOfSubtask;
		numOfSubtask = 3 + rnd.NextInt(4); 
		
		for(int i=0;i<numOfSubtask;i++){
			SubTask subtask = new SubTask(rnd);
			subtasks.add(subtask);
			utility +=subtask.getutility();
		}
		
	}
	
	public int getsubtasksize(){
		return subtasks.size();
	}
	public List<SubTask> getSubTasks(){
		return subtasks;
	}
	public int getutility(){
		return utility;
	}
}
