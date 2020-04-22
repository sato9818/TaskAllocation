package Environment;
import java.util.ArrayDeque;
import java.util.Queue;

import Random.Sfmt;
import Task.Task;

public class Area {
	static int num = 0;
	
	private Queue<Task> taskQueue = new ArrayDeque<Task>();
	private final int minX;
	private final int maxX;
	private final int minY;
	private final int maxY;
	private final int workload;
	private final int id;
	
	
	Area(int workload, int minX, int minY, int maxX, int maxY){
		this.workload = workload;
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		id = num;
		num++;
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
	
	public void addTask(Sfmt rnd){
		for(int i=0;i<rnd.NextPoisson(workload);i++){
			Task task = new Task(rnd);
			taskQueue.add(task);
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
	
	public int getId(){
		return id;
	}
	
	//---------------------------------------------------------------------------------------
	@Override
	public String toString(){
		return "X: " + minX + " ~ " + maxX + "\n" +
				"Y: " + minY + " ~ " + maxY + "\n" +
				"Workload: " + workload + "\n" +
				"Queue Size: " + taskQueue.size();
	}
}
