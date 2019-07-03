import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class test {
	List<Leader> leaders = new ArrayList<Leader>();
	
	List<Member> members = new ArrayList<Member>();
	
	void initialize(){
		for(int i=0;i<100;i++){
			Leader leader = new Leader();
			leaders.add(leader);
		}
		for(int i=0;i<400;i++){
			Member member = new Member();
			members.add(member);
		}
		
	}
	
	public void run(){
		Environment e = new Environment();
		Sfmt rnd = new Sfmt(7);
		initialize();
		
		for(int tick=0;tick<10000;tick++){
			
			Collections.shuffle(leaders);
			Collections.shuffle(members);
			e.addTask(2, rnd);
			
			//リーダの行動
			for(int i=0;i<leaders.size();i++){
				Leader ld = leaders.get(i);
				switch(ld.getPhase()){
				case 0:
					if(!e.TaskisEmpty()){
						ld.selectmember(e.pushTask(), members);
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
