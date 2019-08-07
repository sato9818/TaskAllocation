import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
		
		Collections.shuffle(grid, new Random(13));
		
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
			Leader leader = leaders.get(i);
			leader.deagent.clear();
			for(int j=0;j<400;j++){
				Member member = members.get(j);
				if(leader.getthreshold() < leader.de[member.getmyid()]){
					leader.adddeagent(member);
				}
				leader.reducede(member.getmyid());
			}
			leader.updatedeagent();
		}
		for(int i=0;i<400;i++){
			Member member = members.get(i);
			member.deagent.clear();
			for(int j=0;j<100;j++){
				Leader leader = leaders.get(j);
				if(member.getthreshold() < member.de[leader.getmyid()]){
					member.adddeagent(leader);
				}
				member.reducede(leader.getmyid());
			}
			member.updatedeagent();
		}
		e.checkdelay();
		e.decrementdelay();
		
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
		
		int excutiontask = 0;
		
		for(int tick=0;tick<200;tick++){
			System.out.println("tick: " + tick);
			Random r = new Random(7);
			Collections.shuffle(leaders, r);
			Collections.shuffle(members, r);
			e.addTask(2/*mu*/, rnd);
			
			/*
			Task ts = e.pushTask();
			System.out.println(ts.getsubtasksize());
			List<SubTask> sb = ts.getSubTasks();
			for (int i=0; i<sb.size(); ++i){
				SubTask sb1 = sb.get(i);
			    System.out.println(sb1.getutility() + "(" + sb1.reqCapa[0] + ", " + sb1.reqCapa[1] + ", " + sb1.reqCapa[2] + ")");
			}
			*/
			if(tick == 127){
				
				for(int i=0;i<members.size();i++){
					if(members.get(i).getmyid() == 180)
					System.out.println(members.get(i).getmyid() + " " + members.get(i).isactive() + " " + members.get(i).getPhase() + " " + members.get(i).excutiontime);	
				}
				/*
				for(int i=0;i<leaders.size();i++){
					System.out.println(leaders.get(i).getPhase());
				}
				*/
			}
			
			
			
			//リーダの行動
			
			for(int i=0;i<leaders.size();i++){
				Leader ld = leaders.get(i);
				switch(ld.getPhase()){
				case 0:
					if(!e.TaskisEmpty()){//タスクがあれば
						//タスクを取得
						Task task = e.pushTask();
						//候補メンバーに送るメッセージを決める
						List<MessagetoMember> messagestomember = ld.selectmember(members, task);
						
						if(messagestomember == null){
							break;
						}
						System.out.println(task.getSubTasks().size());
						
						for (int j=0; j<messagestomember.size(); j++){
							System.out.println("send message from Leader " + messagestomember.get(j).getfrom().getmyid() + " to Member " + messagestomember.get(j).getto().getmyid() + " " + messagestomember.get(j).getsubtask() + " delay " + messagestomember.get(j).getdelay());
						}
						//メッセージを送る
						for(int j=0;j<messagestomember.size();j++){
							ld.sendmessagetomember(messagestomember.get(j), e);
						}
						
						ld.setphase(1);
					}
					break;
				case 1:
					//メッセージの返信を見て
					if(ld.waitreply() == 0){
						//アロケーション成功
						System.out.println("success taskallocation");
						ld.taskallocate(e);
						ld.setphase(2);
					}else if(ld.waitreply() == 1){
						//アロケーション失敗
						ld.failallocate(e);
						ld.setphase(0);
						ld.clearall();
					}
					break;
				case 2:
					if(ld.checkexcution() == 0){
						ld.setphase(0);
						ld.clearall();
						excutiontask++;
						System.out.println("success " + ld.getmyid());
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
					if(mem.havemessage()){//メッセージが来ていたら
						//来てるメッセージを取得
						List<MessagetoMember> messages = mem.getmessages();
						
						//メッセージから受理するメッセージを選ぶ
						MessagetoMember decide = mem.decideMessage(messages);
						//受理したメッセージがあれば次のphaseへ
						if(decide != null){
							//System.out.println("member next phase");
							mem.setphase(1);
						}
						//来てるメッセージ全ての返信をする
						mem.sendreplymessages(e, decide, messages);	
						mem.clearmessages();
					}
					break;
				case 1:
					//excution開始
					if(mem.checkexcution(e) == 0){
						mem.setphase(0);
						mem.clearall();
					}else if(mem.checkexcution(e) == 1){
						//System.out.println("no task");
						mem.setphase(0);
						mem.clearall();
					}
					mem.reduceexcutiontime();
					break;
				}
			}	
			update(e);
			
		}	
		System.out.println(excutiontask);
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
