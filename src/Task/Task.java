package task;
import java.util.ArrayList;
import java.util.List;

import random.Sfmt;
import environment.Environment;
import static shared.Constants.*;

public class Task {
	private List<SubTask> subtasks = new ArrayList<SubTask>();
	
	private int utility = 0;
	
	
	
	//---------------------------------------------------------------------------------------
	
	public Task(Sfmt rnd, int id){
		int numOfSubtask;
		numOfSubtask = BASIC_SUBTASKS + rnd.NextInt(SUBTASK_FLUCTUATION+1);
		for(int i=0;i<numOfSubtask;i++){
			SubTask subtask = new SubTask(id, rnd);
			subtasks.add(subtask);
			utility += subtask.getutility();
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
