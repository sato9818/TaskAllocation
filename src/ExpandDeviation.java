import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import Agent.Agent;
import Agent.Leader;
import Agent.Member;
import Comparator.LeaderIdComparator;
import Comparator.MemberIdComparator;
import Environment.Environment;
import Environment.Grid;
import Random.Sfmt;
import Task.Task;

public class ExpandDeviation {
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
		for(int i=0;i<99;i++){
			for(int j=0;j<99;j++){
				Grid g = new Grid(i,j);
				grid.add(g);
			}
		}
		
		Collections.shuffle(grid, new Random(13));
		
		for(int i=0;i<1000;i++){
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
				if(leader.getthreshold() < leader.de[member.getMyId()]){
					leader.adddeagent(member);
				}
				leader.reducede(member.getMyId());
			}
			leader.updatedeagent();
		}
		for(int i=0;i<members.size();i++){
			Member member = members.get(i);
			member.deagent.clear();
			for(int j=0;j<leaders.size();j++){
				Leader leader = leaders.get(j);
				if(member.getthreshold() < member.de[leader.getMyId()]){
					member.adddeagent(leader);
				}
				member.reducede(leader.getMyId());
			}
			member.updatedeagent();
		}
		e.decrementdelay();
		communicationtime += e.checkdelay(leaders,members);
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public void run(){
		Environment e = new Environment();
		Sfmt rnd = new Sfmt(13/*seed*/);
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
		PrintWriter pw2 = null;
		try{
			FileWriter fw2 = new FileWriter("Agents.csv", false); 
            pw2 = new PrintWriter(new BufferedWriter(fw2));
            pw2.print("tick");
        	pw2.print(",");
        	pw2.print("leader");
        	pw2.print(",");
        	pw2.print("member");
        	pw2.println();
		}catch(IOException ex){
			System.out.println(ex);
		}
		
		int excutiontask = 0;
		int wastetask = 0;
		int sumOfExcutionTime = 0;
		int numOfExcutingTask = 0;
		//e.addTask(5/*mu*/, rnd);
		for(int tick=0;tick<50001;tick++){
			System.out.println(leaders.size());
			System.out.println(members.size());
			System.out.println("tick: " + tick);
			Random r = new Random(13);
			Collections.shuffle(leaders, r);
			Collections.shuffle(members, r);
			
			e.addTask(rnd.NextPoisson(19)/*mu*/, rnd);
			
//			e.addTask(rnd.NextPoisson(1)/*mu*/, rnd, 0);
//			e.addTask(rnd.NextPoisson(2)/*mu*/, rnd, 1);
//			e.addTask(rnd.NextPoisson(2)/*mu*/, rnd, 2);
//			e.addTask(rnd.NextPoisson(4)/*mu*/, rnd, 3);
//			e.addTask(rnd.NextPoisson(1)/*mu*/, rnd, 4);
//			e.addTask(rnd.NextPoisson(1)/*mu*/, rnd, 5);
//			e.addTask(rnd.NextPoisson(2)/*mu*/, rnd, 6);
//			e.addTask(rnd.NextPoisson(4)/*mu*/, rnd, 7);
//			e.addTask(rnd.NextPoisson(2)/*mu*/, rnd, 8);
	
			//リーダの行動
			for(int i=0;i<leaders.size();i++){
				Leader ld = leaders.get(i);
				roleReject += ld.getRejectMessages().size();
				ld.sendRejectMessage(e);
				switch(ld.getPhase()){
				case 0:
					//int area = ld.getArea();
					if(!e.TaskisEmpty()){//タスクがあれば
						//タスクを取得
						Task task = e.pushTask();
						//System.out.println("Subtask size is " + task.getSubTasks().size());
						//候補メンバーに送るメッセージを決める(e-greedy法)
						int p = eGreedy(rnd);
						List<MessagetoMember> messagestomember = null;
						if(p == 0){
							messagestomember = ld.selectMember(members, task);
						}else if(p == 1){
							//System.out.println("Epsilon");
							messagestomember = ld.selectRandomMember(members, task);
						}
						
						//候補メンバーが足りなければタスクは破棄
						if(messagestomember == null){
							//System.out.println("waste task due to lack of member " + ld.getmyid());
							wastetask++;
							break;
						}
						
//						for (int j=0; j<messagestomember.size(); j++){
//							
//							System.out.println("send message from Leader " + messagestomember.get(j).getfrom().getmyid() + " to Member " + messagestomember.get(j).getto().getmyid() + " " + messagestomember.get(j).getsubtask() + " delay " + messagestomember.get(j).getdelay());
//						}
						
						//メッセージを送る
						for(int j=0;j<messagestomember.size();j++){
							ld.sendMessage(messagestomember.get(j), e);
						}
						ld.setphase(1);
					}else{
						ld.updateE(0, false);
					}
					break;
				case 1:
					//メッセージの返信を見て
					if(ld.waitReply() == 0){
						//全部返信がきててアロケーションできるなら
						int time = ld.taskallocate(e);
						ld.setphase(0);
						ld.clearall();
						//excutiontask++;
					}else if(ld.waitReply() == 1){
						//全部返信がきててアロケーションできないなら
						ld.failAllocate(e);
						//System.out.println("waste task due to allocation " + ld.getmyid());
						wastetask++;
						ld.setphase(0);
						ld.clearall();
					}
					break;
				}
				
			}	
			
			//メンバの行動
			for(int i=0;i<members.size();i++){
				Member mem = members.get(i);
				if(mem.excutingTask != null){
					mem.setphase(1);
				}else if(mem.haveMessages()){
					mem.setphase(0);
				}else if(mem.taskQueue.size() != 0){
					mem.setphase(1);
				}else{
					mem.setphase(2);
				}
				switch(mem.getPhase()){
				case 0:
					if(mem.haveMessages()){//メッセージが来ていたら
						//来てるメッセージを取得
						List<MessagetoMember> messages = mem.getMessages();
						//メッセージから受理するメッセージを選ぶ
						int flag = 0;
						while(!messages.isEmpty()){
							boolean decide = true;
							int p = eGreedy(rnd);
							MessagetoMember mtom = null;
							if(p == 0){
								mtom = mem.decideMessage(messages);
							}else if(p == 1){
								System.out.println("Epsilon");
								mtom = mem.decideRandomMessage(messages,rnd);
								System.out.println(mtom);
							}
							if(mem.expectedTasks + mem.taskQueue.size() > 4){
								decide = false;
							}
							//来てるメッセージ全ての返信をする
							if(decide){
								mem.expectedTasks++;
								flag = 1;
							}
							mem.sendReplyMessages(e, mtom, decide);
							messages.remove(mtom);
						}
						//受理したメッセージがあれば次のphaseへ
						if(flag == 1){
							selectReject += messages.size() - 1;
							mem.messageAgreeCount++;
							//System.out.println("member next phase 1" + mem.getmyid());
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
					int et = mem.taskexcution(e);
					sumOfExcutionTime = sumOfExcutionTime + et;
					if(et > 0){
						numOfExcutingTask++;
					}
					break;
				}
				if(mem.messageAgreeCount == mem.receiveTaskMessageCount){
					mem.setcondition(false);
				}else{
					mem.setcondition(true);
				}
				if(!mem.isactive() && mem.taskQueue.size() == 0 && mem.excutingTask == null){
					mem.setphase(0);
				}else{
					mem.setphase(1);
				}
			}	
			selectRole(rnd, tick);
			update(e);
			excutiontask += calcuExecutedTask();
			
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
			if(tick % 100 == 0){
				pw2.print(tick);
	        	pw2.print(",");
	        	pw2.print(leaders.size());
	        	pw2.print(",");
	        	pw2.print(members.size());
	        	pw2.println();
			}
		}
		System.out.println(wastetask);
		pw.close();
		pw1.close();
		pw2.close();
		System.out.println("leader :");
		for(int i=0;i<leaders.size();i++){
			System.out.println(leaders.get(i).getMyId() + " " + leaders.get(i).getPhase());
		}
		System.out.println("member :");
		for(int i=0;i<members.size();i++){
			System.out.println(members.get(i).getMyId() + " " + members.get(i).getPhase());
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
				System.out.println("number of leader " + leader.getMyId() + " deagent is " + leader.deagent.size());
				for(int j=0;j<leader.deagent.size();j++){
					Agent agent = leader.deagent.get(j);
					System.out.println("agent " + agent.getMyId() + " de = " + leader.de[agent.getMyId()]);
				}
			}
		}
		
		for(int i=0;i<members.size();i++){
			Collections.sort(members, new MemberIdComparator());
			Member member = members.get(i);
			if(!member.deagent.isEmpty()){
				System.out.println("number of member " + member.getMyId() + " deagent is " + member.deagent.size() + " " + member.averageOfCapability());
				for(int j=0;j<member.deagent.size();j++){
					Agent agent = member.deagent.get(j);
					System.out.println("agent " + agent.getMyId() + " de = " + member.de[agent.getMyId()]);
				}
			}
		}
		printAgentGrid(leaders,members);
		
		
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public int calcuExecutedTask(){
		int executedTask = 0;
		for(int i=0;i<leaders.size();i++){
			Leader leader = leaders.get(i);
			executedTask += leader.getCountExecutedTask();
		}
		for(int i=0;i<members.size();i++){
			Member member = members.get(i);
			executedTask += member.getCountExecutedTask();
		}
		return executedTask;
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
	
	public void printAgentGrid(List<Leader> leaders, List<Member> members){
		try {
            //出力先を作成する
            FileWriter fw = new FileWriter("AgentGrid.csv", false); 
            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
            pw.print("role");
        	pw.print(",");
        	pw.print("x");
        	pw.print(",");
        	pw.print("y");
        	pw.println();
            for(int i=0;i<leaders.size();i++){
            	Leader leader = leaders.get(i);
            	pw.print("Leader");
            	pw.print(",");
            	pw.print(leader.getPositionX());
            	pw.print(",");
            	pw.print(leader.getPositionY());
            	pw.println();
            }
            for(int i=0;i<members.size();i++){
            	Member member = members.get(i);
            	pw.print("Member");
            	pw.print(",");
            	pw.print(member.getPositionX());
            	pw.print(",");
            	pw.print(member.getPositionY());
            	pw.println();
            }
            pw.close();
		}catch (IOException ex) {
            //例外時処理
            ex.printStackTrace();
        }
	}
}
