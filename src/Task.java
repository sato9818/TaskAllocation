
public class Task {
	private SubTask[] subtasks;
	int utility = 0;
	
	Task(Sfmt rnd){
		int numOfSubtask;
		numOfSubtask = 3 + (int)(rnd.NextUnif()*4);
		subtasks = new SubTask[numOfSubtask]; 
		
		for(int i=0;i<subtasks.length;i++){
			utility +=subtasks[i].getutility();
		}
		
	}
	
	public int getsubtasknum(){
		return subtasks.length;
	}
	public SubTask[] getSubTasks(){
		return subtasks;
	}
}
