package Task;
import java.util.ArrayList;
import java.util.List;

import Random.Sfmt;

public class Task {
	static int num = 0;
	private int id;
	private List<SubTask> subtasks = new ArrayList<SubTask>();
	
	private int utility = 0;
	
	//---------------------------------------------------------------------------------------
	
	public Task(Sfmt rnd){
		int numOfSubtask;
		numOfSubtask = 3 + rnd.NextInt(4);
		if(num == 1000000){
			num = 0;
		}
		id=num;
		num++;
		for(int i=0;i<numOfSubtask;i++){
			SubTask subtask = new SubTask(rnd, id);
			subtasks.add(subtask);
			utility +=subtask.getutility();
		}
		 
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getsubtasksize(){
		return subtasks.size();
	}
	
	//---------------------------------------------------------------------------------------
	
	public List<SubTask> getSubTasks(){
		return subtasks;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getutility(){
		return utility;
	}
	
	//---------------------------------------------------------------------------------------
}
