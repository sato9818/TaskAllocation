import java.util.ArrayDeque;
import java.util.Queue;

public class Environment {
	Queue<Task> queueOfTask = new ArrayDeque<>();
	private int[][] grid = new int[50][50];
	
	public void addTask(int mu, Sfmt rnd){
		for(int i=0;i<mu;i++){
			Task task = new Task(rnd);
			queueOfTask.add(task);
		}
	}
}
