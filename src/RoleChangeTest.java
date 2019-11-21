import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RoleChangeTest {
	List<Leader> leaders = new ArrayList<Leader>();
	List<Member> members = new ArrayList<Member>();
	
	double communicationtime = 0.0;
	int roleChangeCount = 0;
	int activeReject = 0;
	int activeReject2 = 0;
	int selectReject = 0;
	int roleReject = 0;
	
	//---------------------------------------------------------------------------------------
	
	void initialize(Sfmt rnd){
		List<Grid> grid = new ArrayList<Grid>();
		for(int i=0;i<51;i++){
			for(int j=0;j<51;j++){
				Grid g = new Grid(i,j);
				grid.add(g);
			}
		}
		
		Collections.shuffle(grid, new Random(13));
		
		for(int i=0;i<500;i++){
			int p = rnd.NextInt(2);
			if(p == 0){
				Leader leader = new Leader(rnd);
				leader.setPosition(grid.get(i).x, grid.get(i).y);
				leaders.add(leader);
			}else if(p == 1){
				Member member = new Member(rnd);
				member.setPosition(grid.get(i).x, grid.get(i).y);
				members.add(member);
			}
		}
		
		for(int i=0;i<leaders.size();i++){
			Leader leader = leaders.get(i);
			for(int j=0;j<members.size();j++){
				Member member = members.get(j);
				leader.setdistance(member);
			}
			for(int j=0;j<leaders.size();j++){
				Leader ld = leaders.get(j);
				leader.setdistance(ld);
			}
		}
		for(int i=0;i<members.size();i++){
			Member member = members.get(i);
			for(int j=0;j<leaders.size();j++){
				Leader leader = leaders.get(j);
				member.setdistance(leader);
			}
			for(int j=0;j<members.size();j++){
				Member mem = members.get(j);
				member.setdistance(mem);
			}
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	void selectRole(Sfmt rnd, int tick){
		List<Leader> newLeaders = new ArrayList<Leader>();
		List<Member> newMembers = new ArrayList<Member>();
	
		for(int i = 0;i<leaders.size();i++){
			Leader ld = leaders.get(i);
			if(ld.getPhase() == 0){
				int ep = eGreedy(rnd);
				if(ep == 0){
					if(ld.lE > ld.mE){
						newLeaders.add(ld);
					}else if(ld.lE < ld.mE){
						roleChangeCount++;
						Member mem = new Member(ld);
						mem.lastSelectTick = tick;
						newMembers.add(mem);
					}else{
						int p = rnd.NextInt(2);
						if(p == 0){
							newLeaders.add(ld);
						}else if(p == 1){
							roleChangeCount++;
							Member mem = new Member(ld);
							mem.lastSelectTick = tick;
							newMembers.add(mem);
						}	
					}
				}else if(ep == 1){
					int p = rnd.NextInt(2);
					if(p == 0){
						newLeaders.add(ld);
					}else if(p == 1){
						roleChangeCount++;
						Member mem = new Member(ld);
						mem.lastSelectTick = tick;
						newMembers.add(mem);
					}
				}
			}else{
				newLeaders.add(ld);
			}
		}
		
		for(int i = 0;i<members.size();i++){
			Member mem = members.get(i);
			if(mem.getPhase() == 0){
				int ep = eGreedy(rnd);
				if(ep == 0){
					if(mem.lE < mem.mE){
						newMembers.add(mem);
					}else if(mem.lE > mem.mE){
						roleChangeCount++;
						Leader ld = new Leader(mem); 
						newLeaders.add(ld);
					}else{
						int p = (int)rnd.NextUnif() * 2;
						if(p == 0){
							roleChangeCount++;
							Leader leader = new Leader(mem);
							newLeaders.add(leader);
						}else if(p == 1){
							newMembers.add(mem);
						}
					}
				}else if(ep == 1){
					int p = (int)rnd.NextUnif() * 2;
					if(p == 0){
						roleChangeCount++;
						Leader leader = new Leader(mem);
						newLeaders.add(leader);
					}else if(p == 1){
						newMembers.add(mem);
					}
				}
			}else{
				newMembers.add(mem);
			}
		}
		leaders = newLeaders;
		members = newMembers;
	}
	
	//---------------------------------------------------------------------------------------
	
	void update(Environment e){
		
		for(int i=0;i<leaders.size();i++){
			Leader leader = leaders.get(i);
			leader.deagent.clear();
			for(int j=0;j<members.size();j++){
				Member member = members.get(j);
				if(leader.getthreshold() < leader.de[member.getmyid()]){
					leader.adddeagent(member);
				}
				leader.reducede(member.getmyid());
			}
			leader.updatedeagent();
		}
		for(int i=0;i<members.size();i++){
			Member member = members.get(i);
			member.deagent.clear();
			for(int j=0;j<leaders.size();j++){
				Leader leader = leaders.get(j);
				if(member.getthreshold() < member.de[leader.getmyid()]){
					member.adddeagent(leader);
				}
				member.reducede(leader.getmyid());
			}
			member.updatedeagent();
		}
		e.decrementdelay();
		communicationtime += e.checkdelay(leaders,members);
	}
	
	//---------------------------------------------------------------------------------------
	
	public void run(){
		Environment e = new Environment();
		Sfmt rnd = new Sfmt(17/*seed*/);
		initialize(rnd);
		
		PrintWriter pw = null;
		try{
			FileWriter fw = new FileWriter("test2.csv", false); 
            pw = new PrintWriter(new BufferedWriter(fw));
            pw.print("tick");
        	pw.print(",");
        	pw.print("excution task");
        	pw.println();
		}catch(IOException ex){
			System.out.println(ex);
		}
		PrintWriter pw1 = null;
		try{
			FileWriter fw1 = new FileWriter("communicationtime.csv", false); 
            pw1 = new PrintWriter(new BufferedWriter(fw1));
            pw1.print("tick");
        	pw1.print(",");
        	pw1.print("communication time");
        	pw1.println();
		}catch(IOException ex){
			System.out.println(ex);
		}
		int excutiontask = 0;
		int wastetask = 0;
		int sumOfExcutionTime = 0;
		int numOfExcutingTask = 0;
		//e.addTask(5/*mu*/, rnd);
		for(int tick=0;tick<20001;tick++){
			System.out.println(leaders.size());
			System.out.println(members.size());
			System.out.println("tick: " + tick);
			Random r = new Random(13);
			Collections.shuffle(leaders, r);
			Collections.shuffle(members, r);
			e.addTask(rnd.NextPoisson(5)/*mu*/, rnd);
				
			//リーダの行動
			for(int i=0;i<leaders.size();i++){
				Leader ld = leaders.get(i);
				roleReject += ld.getRejectMessages().size();
				ld.sendRejectMessage(e);
				switch(ld.getPhase()){
				case 0:
					if(!e.TaskisEmpty()){//タスクがあれば
						//タスクを取得
						Task task = e.pushTask();
						//System.out.println("Subtask size is " + task.getSubTasks().size());
						//候補メンバーに送るメッセージを決める(e-greedy法)
						int p = eGreedy(rnd);
						List<MessagetoMember> messagestomember = null;
						if(p == 0){
							messagestomember = ld.selectmember(members, task);
						}else if(p == 1){
							System.out.println("Epsilon");
							messagestomember = ld.selectrandommember(members, task);
						}
						
						//候補メンバーが足りなければタスクは破棄
						if(messagestomember == null){
							//System.out.println("waste task due to lack of member " + ld.getmyid());
							wastetask++;
							break;
						}
						for (int j=0; j<messagestomember.size(); j++){
							
							System.out.println("send message from Leader " + messagestomember.get(j).getfrom().getmyid() + " to Member " + messagestomember.get(j).getto().getmyid() + " " + messagestomember.get(j).getsubtask() + " delay " + messagestomember.get(j).getdelay());
						}
						
						//メッセージを送る
						for(int j=0;j<messagestomember.size();j++){
							ld.sendmessagetomember(messagestomember.get(j), e);
						}
						ld.setphase(1);
					}else{
						ld.updateE(0, false);
					}
					break;
				case 1:
					//メッセージの返信を見て
					if(ld.waitreply() == 0){
						//全部返信がきててアロケーションできるなら
						ld.taskallocate(e);
						ld.setphase(2);
						
						//excutiontask++;
					}else if(ld.waitreply() == 1){
						//全部返信がきててアロケーションできないなら
						ld.failallocate(e);
						System.out.println("waste task due to allocation " + ld.getmyid());
						wastetask++;
						ld.setphase(0);
						ld.clearall();
						
					}
					break;
				
				case 2:
					ld.reduceexcutiontime();
					if(ld.checkmyexcution() == 0){
						//System.out.println("finish my task");
						
						if(ld.checkexcution() == 0){
							ld.setphase(0);
							excutiontask++;
							ld.clearall();
						}
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
						int p = eGreedy(rnd);
						MessagetoMember decide = null;
						if(p == 0){
							decide = mem.decideMessage(messages);
						}else if(p == 1){
							System.out.println("Epsilon");
							decide = mem.decideRandomMessage(messages,rnd);
						}
						//来てるメッセージ全ての返信をする
						mem.sendreplymessages(e, decide, messages);	
						//受理したメッセージがあれば次のphaseへ
						if(decide != null){
							selectReject += messages.size() - 1;
							//System.out.println("member next phase 1" + mem.getmyid());
							mem.setcondition(true);
							mem.setphase(1);
							mem.lastSelectTick = tick;
						}else{
							selectReject += messages.size();
							if(tick - mem.lastSelectTick > mem.thresholdV){
								mem.updateE(1, false);
								mem.lastSelectTick = tick;
							}
						}
						//メッセージ集合を初期化
						mem.clearmessages();
					}else{
						if(tick - mem.lastSelectTick > mem.thresholdV){
							mem.updateE(1, false);
							mem.lastSelectTick = tick;
						}
					}
					break;
				case 1:
					//allocationまち
					List<MessagetoMember> messages = mem.getmessages();
					activeReject += messages.size();
					mem.sendreplymessages(e, null, messages);	
					mem.clearmessages();
					
					MessagetoMember messagetom;
					if((messagetom = mem.gettaskmessage()) != null){
						if(messagetom.taskisallocated()){
							System.out.println("taskexcuting" + mem + " " + mem.getmyid());
							mem.taskexcution(messagetom);
							mem.setcondition(false);
							mem.setphase(2);
							sumOfExcutionTime += mem.getExcutiontime();
							numOfExcutingTask++;
						}else{
							System.out.println("task is not allocated " + mem.getmyid());
							mem.setphase(0);
							mem.setcondition(false);
							mem.clearall();
						}
					}
					break;
				case 2:
					/*
					List<MessagetoMember> messages2 = mem.getmessages();
					activeReject2 += messages2.size();
					mem.sendreplymessages(e, null, messages2);	
					mem.clearmessages();
					*/
					
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
					break;
				}
				
			}	
			selectRole(rnd, tick);
			update(e);
			
			if(tick % 100 == 0){
				pw.print(tick);
	        	pw.print(",");
	        	pw.print(excutiontask);
	        	pw.print(",");
	        	pw.print((double)sumOfExcutionTime / numOfExcutingTask);
	        	pw.println();
	        	sumOfExcutionTime = 0;
				numOfExcutingTask = 0;
				excutiontask = 0;
			}
			if(tick % 100 == 0){
				pw1.print(tick);
	        	pw1.print(",");
	        	pw1.print(communicationtime / 100);	
	        	pw1.println();
	        	communicationtime = 0;
			}
		}
		System.out.println(wastetask);
		pw.close();
		pw1.close();
		System.out.println("leader :");
		for(int i=0;i<leaders.size();i++){
			System.out.println(leaders.get(i).getmyid() + " " + leaders.get(i).getPhase());
		}
		System.out.println("member :");
		for(int i=0;i<members.size();i++){
			System.out.println(members.get(i).getmyid() + " " + members.get(i).getPhase());
		}
		System.out.println("role change count " + roleChangeCount);
		System.out.println("active reject count " + activeReject);
		System.out.println("active reject2 count " + activeReject2);
		System.out.println("select reject count " + selectReject);
		System.out.println("role reject count " + roleReject);
		
		for(int i=0;i<leaders.size();i++){
			Collections.sort(leaders, new LeaderIdComparator());
			Leader leader = leaders.get(i);
			if(!leader.deagent.isEmpty()){
				System.out.println("number of leader " + leader.getmyid() + " deagent is " + leader.deagent.size());
				for(int j=0;j<leader.deagent.size();j++){
					Agent agent = leader.deagent.get(j);
					System.out.println("agent " + agent.getmyid() + " de = " + leader.de[agent.getmyid()]);
				}
			}
		}
		
		for(int i=0;i<members.size();i++){
			Collections.sort(members, new MemberIdComparator());
			Member member = members.get(i);
			if(!member.deagent.isEmpty()){
				System.out.println("number of member " + member.getmyid() + " deagent is " + member.deagent.size() + " " + member.averageOfCapability());
				for(int j=0;j<member.deagent.size();j++){
					Agent agent = member.deagent.get(j);
					System.out.println("agent " + agent.getmyid() + " de = " + member.de[agent.getmyid()]);
				}
			}
		}
		
		/*
		for(int i=0;i<400;i++){
			Member member = members.get(i);
			System.out.println("member " + member.getmyid() + " last active " + member.lasttick + " " +member.isactive());
		}
		*/
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public int eGreedy(Sfmt rnd) {
		int A;
        int randNum = rnd.NextInt(101);
        if (randNum <= 0.05 * 100.0) {
        	//eの確率
			A = rnd.NextInt(2);
        } else {
        	//(1-e)の確率
        	A = 0;
        }
        return A;
	}
}
