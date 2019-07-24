import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class test {
	List<Leader> leaders = new ArrayList<Leader>();
	
	List<Member> members = new ArrayList<Member>();
	
	void initialize(Sfmt rnd){
		List<Grid> grid = new ArrayList<Grid>();
		
		for(int i=0;i<50;i++){
			for(int j=0;j<50;j++){
				Grid g = new Grid(i,j);
				grid.add(g);
			}
		}
		
		Collections.shuffle(grid);
		
		for(int i=0;i<100;i++){
			Leader leader = new Leader(rnd);
			leader.setPosition(grid.get(i).x, grid.get(i).y);
			leaders.add(leader);
		}
		for(int i=100;i<500;i++){
			Member member = new Member(rnd);
			member.setPosition(grid.get(i).x, grid.get(i).y);
			members.add(member);
		}
		
	}
	
	void update(Environment e){
		for(int i=0;i<100;i++){
			for(int j=0;j<500;j++){
				leaders.get(i).reducede(j);
			}
		}
		for(int i=0;i<400;i++){
			for(int j=0;j<500;j++){
				members.get(i).reducede(j);
			}
		}
		e.decrementdelay();
		e.checkdelay();
	}
	
	public void run(){
		Environment e = new Environment();
		Sfmt rnd = new Sfmt(7/*seed*/);
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
						ld.setTask(e.pushTask());
						
						//System.out.println(t.getsubtasksize());
						ld.selectmember(members,e);
						//System.out.println(t.getsubtasksize());
						
						ld.setphase(1);
					}
					break;
				case 1:
					if(ld.checkallocation() == 0){
						ld.setphase(2);
						ld.taskallocate(e);
					}else if(ld.checkallocation() == 1){
						ld.failallocate(e);
						ld.setphase(0);
						ld.clearall();
					}
					break;
				case 2:
					if(ld.checkexcution() == 0){
						ld.setphase(0);
						ld.clearall();
					}else if(ld.checkexcution() == 1){
						
					}
					break;
				}
				
					
			}	
			
			//メンバの行動
			for(int i=0;i<members.size();i++){
				Member mem = members.get(i);
				switch(mem.getPhase()){
				case 0:
					if(mem.havemessage()){
						mem.decideSubtask();
						mem.setphase(1);
					}else{
						break;
					}
					
					break;
				case 1:
					mem.sendreplymessages(e);
				case 2:
					if(mem.checkexcution(e) == 0){
						mem.setphase(0);
						mem.clearall();
					}
					mem.reduceexcutiontime();
				}
			}	
			update(e);
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
