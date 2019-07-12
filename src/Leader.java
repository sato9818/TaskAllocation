import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Leader extends Agent{
	private int phase = 0;
	private int waittime = 0;
	
	
	Leader(Sfmt rnd){
		super(rnd);
	}
	
	public int getPhase(){
		return phase;
	}
	
	public void changephase(){
		phase++;
	}
	
	public HashMap<Member, SubTask> selectmember(List<SubTask> subtasks, List<Member> members){
		HashMap<Member, SubTask> preteam = new HashMap<Member, SubTask>();
		List<SubTask> confsubtask = new ArrayList<SubTask>();
		
		
		Collections.sort(subtasks, new SubUtilityComparator());
		//sort member
		Collections.sort(members, new Comparator<Member>(){
			public int compare(Member m1, Member m2) {
				 return de[m1.getmyid()] > de[m2.getmyid()] ? -1 : 1;
			}
		});
		for(int i=0;i<2/*N_d*/;i++){
			int k = 0;
			int subtasksize = subtasks.size();
			for(int j=0;j<subtasksize;j++){
				SubTask subtask = subtasks.get(j);
				if(!confsubtask.contains(subtask)){
					Member member = members.get(k++);
					preteam.put(member, subtask);
					
					if(deagent.contains(member)){
						confsubtask.add(subtask);
					}
				}
				//System.out.println("confsubtasksize:" + confsubtask.size());
			}
		}
		return preteam;
	}
}
