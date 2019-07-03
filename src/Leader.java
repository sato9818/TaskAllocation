import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Leader extends Agent{
	private int phase = 0;
	private int waittime = 0;
	private List<Member> preteam = new ArrayList<Member>();
	
	Leader(){
		super();
	}
	
	public int getPhase(){
		return phase;
	}
	
	public void changephase(){
		phase++;
	}
	
	public void selectmember(Task t, List<Member> members){
		for(int i=0;i<t.getsubtasknum();i++){
			SubTask[] subtasks = t.getSubTasks();
			Arrays.sort(subtasks, new SubUtilityComparator());
			//sort member
			Collections.sort(members, new Comparator<Member>(){
				public int compare(Member m1, Member m2) {
					 return de[m1.myid] > de[m2.myid] ? -1 : 1;
				}
			});
			
		}
	}
}
