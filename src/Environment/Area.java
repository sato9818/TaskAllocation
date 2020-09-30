package Environment;
import java.util.ArrayDeque;
import java.util.Queue;

import Random.Sfmt;
import Task.Task;

import static Constants.Constants.*;

public class Area {
	static int num = 0;
	public static int overflowedTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	
	private Queue<Task> taskQueue = new ArrayDeque<Task>();
	private final int minX;
	private final int maxX;
	private final int minY;
	private final int maxY;
	private double workload;
	private final int id;
	
	
	private int taskCount = 0; 
	
	
	Area(double workload, int minX, int minY, int maxX, int maxY){
		this.workload = workload;
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		id = num;
		num++;
		if(num == NUM_OF_AREA){
			num = 0;
		}
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
	
	public void addTask(int tick){
		int p = Environment.rnd.NextPoisson(workload);
		taskCount += p;
		for(int i=0;i<p;i++){
			Task task = new Task();
			if(taskQueue.size() > TASK_QUEUE_SIZE){
				overflowedTask[getId()][tick]++;
			}else{
				taskQueue.add(task);
			}
			
		}
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
