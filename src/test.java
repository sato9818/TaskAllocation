import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class test {
	List<Leader> leaders = new ArrayList<Leader>();
	
	List<Member> members = new ArrayList<Member>();
	
	void initialize(Sfmt rnd){
		for(int i=0;i<100;i++){
			Leader leader = new Leader(rnd);
			leaders.add(leader);
		}
		for(int i=0;i<400;i++){
			Member member = new Member(rnd);
			members.add(member);
		}
		
	}
	
	public void run(){
		Environment e = new Environment();
		Sfmt rnd = new Sfmt(7);
		initialize(rnd);
		/*
		for (int i=0; i<leaders.size(); ++i){
			Leader ld = leaders.get(i);
		    System.out.println(ld.getmyid() + "(" + ld.capacity[0] + ", " + ld.capacity[1] + ", " + ld.capacity[2] + ")");
		}
		for (int i=0; i<members.size(); ++i){
			Member mem = members.get(i);
			System.out.println(mem.getmyid() + "(" + mem.capacity[0] + ", " + mem.capacity[1] + ", " + mem.capacity[2] + ")");
		}
		*/
		
		for(int tick=0;tick<1;tick++){
			
			Collections.shuffle(leaders);
			Collections.shuffle(members);
			e.addTask(1/*mu*/, rnd);
			
			/*
			Task ts = e.pushTask();
			System.out.println(ts.getsubtasksize());
			List<SubTask> sb = ts.getSubTasks();
			for (int i=0; i<sb.size(); ++i){
				SubTask sb1 = sb.get(i);
			    System.out.println(sb1.getutility() + "(" + sb1.reqCapa[0] + ", " + sb1.reqCapa[1] + ", " + sb1.reqCapa[2] + ")");
			}
			*/
			
			
			
			//リーダの行動
			
			for(int i=0;i<leaders.size();i++){
				Leader ld = leaders.get(i);
				switch(ld.getPhase()){
				case 0:
					if(!e.TaskisEmpty()){
						Task t = e.pushTask();
						System.out.println(t.getsubtasksize());
						ld.selectmember(t.getSubTasks(), members);
						System.out.println(t.getsubtasksize());
						ld.changephase();
					}
					break;
				}
			}	
		}	
	}
	/*
	}
	
	
	for(100)
	Leader.getTask()
	Leader.selectmember()
	or
	leader.waitforreply
	or
	leader.allocation
	
	for(400)
	member.waitforselection
	or
	member.replytoleader
	or
	member.waitforsubtask
	or
	member.excute()
	*/
}
