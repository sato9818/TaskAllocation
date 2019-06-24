
public class Task {
	SubTask subtasks[];
	
	Task(Sfmt rnd){
		int numOfSubtask;
		numOfSubtask = 3 + (int)(rnd.NextUnif()*4);
		subtasks = new SubTask[numOfSubtask]; 
	}
}
