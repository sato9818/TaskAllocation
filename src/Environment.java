import java.util.ArrayDeque;
import java.util.Queue;

public class Environment {
	private Queue<Task> taskqueue = new ArrayDeque<>();
	
	
	public void addTask(int mu, Sfmt rnd){
		for(int i=0;i<mu;i++){
			Task task = new Task(rnd);
			taskqueue.add(task);
		}
	}
	
	public boolean TaskisEmpty(){
		return taskqueue.isEmpty();
	}
	
	public Task pushTask(){
		return taskqueue.poll();
	}
}
