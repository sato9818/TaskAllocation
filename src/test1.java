import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class test1 {
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
		for(int i=0;i<leaders.size();i++){
			Leader leader = leaders.get(i);
			for(int j=0;j<members.size();j++){
				Member member = members.get(j);
				leader.setdistance(member);
			}
		}
		for(int i=0;i<members.size();i++){
			Member member = members.get(i);
			for(int j=0;j<leaders.size();j++){
				Leader leader = leaders.get(j);
				member.setdistance(leader);
			}
		}
	}
	
	void update(Environment e){
		e.decrementdelay();
		e.checkdelay();
	}
	
	public void run(){
		Environment e = new Environment();
		Sfmt rnd = new Sfmt(13/*seed*/);
		initialize(rnd);
		File file = new File("test.txt");
		PrintWriter pw = null;
		try{
			pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		}catch(IOException ex){
			System.out.println(ex);
		}
		
		int excutiontask = 0;
		int wastetask = 0;
		e.addTask(1/*mu*/, rnd);
		for(int tick=0;tick<101;tick++){
			System.out.println("tick: " + tick);
			Random r = new Random(7);
			Collections.shuffle(leaders, r);
			Collections.shuffle(members, r);
			
			
			
			
			//リーダの行動
			
			for(int i=0;i<leaders.size();i++){
				Leader ld = leaders.get(i);
				switch(ld.getPhase()){
				case 0:
					if(!e.TaskisEmpty()){//タスクがあれば
						//タスクを取得
						Task task = e.pushTask();
						//近い１００体を選ぶ。
						List<Member> mems = ld.selectMemberCNP(members, task);
						//メッセージを作る
						for(int memi=0;memi<mems.size();memi++){
							Member mem = mems.get(memi);
							MessagetoMember mtom = new MessagetoMember(ld, mem, task);
							e.addmessagetomember(mtom);
						}
						ld.setphase(1);
					}
					break;
				case 1:
					//メッセージの返信を見て
					if(ld.waitReplyCNP() == 0){
						//全部返信がきててアロケーションできるなら
						ld.taskallocateCNP(e);
						ld.setphase(0);
						ld.clearallCNP();
						excutiontask++;
					}else if(ld.waitReplyCNP() == 1){
						//全部返信がきててアロケーションできないなら
						ld.failallocateCNP(e);
						System.out.println("waste task due to allocation " + ld.getmyid());
						wastetask++;
						ld.setphase(0);
						ld.clearallCNP();
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
						//それぞれのメッセージから入札するサブタスクを選ぶ
						SubTask decide = null;
						for(int messagei=0;messagei<messages.size();messagei++){
							MessagetoMember mtom = messages.get(messagei);
							List<SubTask> subtasks = mtom.gettask().getSubTasks();
							for(int subtaski=0;subtaski<subtasks.size();subtaski++){
								SubTask subtask = subtasks.get(subtaski);
								if(decide == null){
									decide = subtask;
								}else{
									double upsdecide = (double)decide.getutility() / mem.setexcutiontime(decide);
									double ups = (double)subtask.getutility() / mem.setexcutiontime(subtask);
									if(upsdecide < ups){
										decide = subtask;
									}
								}
							}
						}
						for(int messagei=0;messagei<messages.size();messagei++){
							MessagetoMember mtom = messages.get(messagei);
							if(mtom.gettask().getSubTasks().contains(decide)){
								mem.sendreplymessagesCNP(e, decide, mtom);
							}else{
								mem.sendreplymessagesCNP(e, null, mtom);
							}
						}
						//入札したサブタスクがあれば次のphaseへ
						if(decide != null){
							System.out.println("member " + mem.getmyid() + " decide subtask " + decide);
							mem.setcondition(true);
							mem.setphase(1);
						}
						//きてたメッセージを初期化
						mem.clearmessages();
					}
					break;
				case 1:
					//allocationまち
					List<MessagetoMember> messages = mem.getmessages();
					for(int messagei=0;messagei<messages.size();messagei++){
						MessagetoMember mtom = messages.get(messagei);
						mem.sendreplymessagesCNP(e, null, mtom);	
					}
					mem.clearmessages();
					
					MessagetoMember messagetom;
					if((messagetom = mem.gettaskmessage()) != null){
						if(messagetom.taskisallocated()){
							mem.taskexcution(messagetom);
							mem.setphase(2);
						}else{
							mem.setphase(0);
							mem.setcondition(false);
							mem.clearall();
						}
					}
					break;
				case 2:
					mem.reduceexcutiontime();
					if(mem.checkexcution(e) == 0){
						//System.out.println("member next phase 0" + mem.getmyid());
						mem.setphase(0);
						mem.clearall();
						mem.setcondition(false);
					}else if(mem.checkexcution(e) == 1){
						//System.out.println("no task");
						//System.out.println("member next phase 0" + mem.getmyid());
						mem.setphase(0);
						mem.clearall();
						mem.setcondition(false);
					}
				}
				
			}	
			update(e);
			if(tick % 100 == 0){
				pw.println(excutiontask);
				excutiontask = 0;
			}
		}
		System.out.println(wastetask);
		pw.close();
	}
	

}
