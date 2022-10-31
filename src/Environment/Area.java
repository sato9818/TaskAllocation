package environment;
import java.util.ArrayDeque;
import java.util.Queue;

import analysis.Analyzer;
import random.Sfmt;
import task.Task;

import static shared.Constants.*;

public class Area {
	private Queue<Task> taskQueue = new ArrayDeque<Task>();
	private final int minX;
	private final int maxX;
	private final int minY;
	private final int maxY;
	private double workload;
	private final int id;
	
	
	private int taskCount = 0; 
	
	
	Area(double workload, int minX, int minY, int maxX, int maxY, int id){
		this.workload = workload;
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		this.id = id;
	}
	
	//---------------------------------------------------------------------------------------
	
	public boolean inArea(int x, int y){
		if(x >= minX && x <= maxX && y >= minY && y <= maxY){
			return true;
		}else{
			return false;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public int addTask(Sfmt rnd, int tick, int taskID){
		int p = rnd.NextPoisson(workload);
		taskCount += p;
		for(int i=0;i<p;i++){
			Task task = new Task(rnd, taskID++);
			if(taskQueue.size() > TASK_QUEUE_SIZE){
				Analyzer.overflowedTask[getId()][tick]++;
			}else{
				taskQueue.add(task);
			}
		}
		return taskID;
	}
	
	//---------------------------------------------------------------------------------------
	
	public boolean taskIsEmpty(){
		return taskQueue.isEmpty();
	}
	
	//---------------------------------------------------------------------------------------
		
	public Task pushTask(){
		return taskQueue.poll();
	}
	
	//---------------------------------------------------------------------------------------
	
	public void changeWorkload(double workload){
		this.workload = workload;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getId(){
		return id;
	}
	
	//---------------------------------------------------------------------------------------
	@Override
	public String toString(){
		return "X: " + minX + " ~ " + maxX + "\n" +
				"Y: " + minY + " ~ " + maxY + "\n" +
				"Workload: " + workload + "\n" +
				"Queue Size: " + taskQueue.size() + "\n" +
				"Produce Task: " + taskCount;
	}
}
