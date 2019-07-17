import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Member extends Agent{
	List<SubTask> temposubtask = new ArrayList<SubTask>();
	int taskexcution;
	
	Member(Sfmt rnd){
		super(rnd);
	}
	
	public void addtemposubtask(SubTask s){
		temposubtask.add(s);
	}
	
	private SubTask decidesubtask(){
		SubTask decide = temposubtask.get(0);
		for(int i = 0;i<temposubtask.size();i++){
			SubTask subtask = temposubtask.get(i);
			if(de[subtask.getfrom().getmyid()] > de[decide.getfrom().getmyid()]){
				decide = subtask;
			}else if(deagent.contains(subtask.getfrom()) && !deagent.contains(decide.getfrom())){
				decide = subtask;
			}
		}
		return decide;
	}
	
	public void sentmessage(SubTask subtask){
		SubTask decide = decidesubtask();
		for(int i=0;i<temposubtask.size();i++){
			SubTask st = temposubtask.get(i);
		
		}
	}
	
	
}
