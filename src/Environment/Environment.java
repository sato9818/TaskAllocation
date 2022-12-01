package environment;
import static shared.Constants.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import agent.Agent;
import agent.Leader;
import agent.Member;
import analysis.Analyzer;
import message.Message;
import random.Sfmt;

public class Environment {
	
	private List<Leader> leaders = new ArrayList<Leader>();
	private List<Member> members = new ArrayList<Member>();
	private List<Agent> agents = new ArrayList<Agent>();
	private List<Area> areas = new ArrayList<Area>();
	private HashMap<Integer, List<Message>> agentIdToMessageList = new HashMap<Integer, List<Message>>();
	private int agentID = 0;
	private int areaID = 0;
	private int taskID = 0;
	public Sfmt rnd;
	public Random r;
	
	//---------------------------------------------------------------------------------------
	
	public Environment(int seed){
		rnd = new Sfmt(seed);
		r = new Random(seed);
		generateAreas();
		generateAgents();
		for(int i=0;i<NUM_OF_AGENT;i++){
			agentIdToMessageList.put(agents.get(i).getMyId(), new ArrayList<Message>());
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	private void generateAreas(){
		int divX = GRID_X / NUM_OF_VERTICAL_DIVISION;
		int divY = GRID_Y / NUM_OF_HORIZONTAL_DIVISION;
		
		int count = 0;
		for(int i=0;i<NUM_OF_VERTICAL_DIVISION;i++){
			for(int j=0;j<NUM_OF_HORIZONTAL_DIVISION;j++){
				Area area = new Area(WORKLOADS[count], i*divX, j*divY, (i+1) * divX - 1, (j+1) * divY - 1, areaID++);
				areas.add(area);
				count++;
			}
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	
	private void generateAgents(){
		
		List<Grid> grid = new ArrayList<Grid>();
		for(int i=0;i<GRID_X;i++){
			for(int j=0;j<GRID_Y;j++){
				Grid g = new Grid(i,j);
				grid.add(g);
			}
		}
		
		Collections.shuffle(grid, r);
		
		for(int i=0;i<NUM_OF_AGENT;i++){
			int x = grid.get(i).x;
			int y = grid.get(i).y;
			int p = rnd.NextInt(2);
			if(p == 0){
				Leader leader = new Leader(identifyArea(x, y), x, y, agentID++, this);
				leaders.add(leader);
				agents.add(leader);
			}else if(p == 1){
				Member member = new Member(identifyArea(x, y), x, y, agentID++, this);
				members.add(member);
				agents.add(member);
			}
		}
		
		for(int i=0;i<agents.size();i++){
			Agent agent = agents.get(i);
			for(int j=i+1;j<agents.size();j++){
				Agent agent2 = agents.get(j);
				agent.setdistance(agent2);
				agent2.setdistance(agent);
			}
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	private Area identifyArea(int x, int y){
		for(int i=0;i<areas.size();i++){
			Area area = areas.get(i);
			if(area.inArea(x, y)){
				return area;
			}
		}
		System.out.println("area not exist.");
		System.exit(1);
		return null;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void run(int tick){
//		System.out.println("tick: " + tick + "---------------------------------------------------------------------------------------------------");
		
		if(tick == CHANGE_WORKLOAD_TIME){
			changeAreaWorkload(HIGH_WORKLOAD, areas.get(0));
		}
		if(tick == RESTORE_WORKLOAD_TIME){
			changeAreaWorkload(MODERATE_WORKLOAD, areas.get(0));
		}
		if(tick >= TIME_TO_RESET_DE && tick % 100 == 0){
			resetDeAndSetBetterAgents();
		}
		
		for(int i=0;i<areas.size();i++){
			taskID = areas.get(i).addTask(rnd, tick, taskID);
		}
		if(RECIPROCITY){
			updateDependablityAgent();
		}
		agentsGetMessages(tick);
		
		Collections.shuffle(leaders, r);
		Collections.shuffle(members, r);
		Collections.shuffle(agents, r);
		
		
		leadersAct(tick);
		membersAct(tick);
		collectMessages();
		if(!THRESHOLD_FIXED) updateAgentsThreshold(tick);
		countAgents(tick);
		changeRole(tick);
		decreaseDependability();
	}
	
	public void updateAgentsThreshold(int tick) {
		for(Agent agent : agents) {
			agent.updateThreshold(tick);
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	private void agentsGetMessages(int tick){
		for(int i=0;i<agents.size();i++){
			Agent agent = agents.get(i);
			
			List<Message> messageList = agentIdToMessageList.get(agent.getMyId());
			Iterator<Message> it = messageList.iterator();
			while(it.hasNext()){
				Message message = it.next();
				if(message.isDelivered()){
					agent.readMessage(message, tick);
					Analyzer.communicationTime[message.from().getArea().getId()][tick] += message.from().getdistance(message.to().getMyId());
					Analyzer.countSentMessages[message.from().getArea().getId()][tick]++;
					it.remove();
				}
			}
		}
	}
	
	//---------------------------------------------------------------------------------------

	private void leadersAct(int tick){
		
		for(int i=0;i<leaders.size();i++){
			Leader leader = leaders.get(i);
			if(tick >= TIME_TO_RESET_DE){
				List<Agent> mainAgents = new ArrayList<Agent>();
				for(int j = 0;j<agents.size();j++){
					if(leader.getMainMemberIds().contains(agents.get(j).getMyId())){
						mainAgents.add(agents.get(j));
					}
				}
				leader.act(mainAgents, tick);
			}else{
				leader.act(agents, tick);
			}
				
		}
	}
	
	//---------------------------------------------------------------------------------------

	private void membersAct(int tick){
		
		for(int i=0;i<members.size();i++){
			Member member = members.get(i);
			member.act(tick);
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	private void collectMessages(){
		for(int i=0;i<agents.size();i++){
			Agent agent = agents.get(i);
			List<Message> messages = agent.sendMessages();
			for(int j=0;j<messages.size();j++){
				agentIdToMessageList.get(messages.get(j).to().getMyId()).add(messages.get(j));
			}
			agent.clearMessages();
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	private void changeRole(int tick){
		List<Leader> newLeaders = new ArrayList<Leader>();
		List<Member> newMembers = new ArrayList<Member>();
	
		for(int i = 0;i<leaders.size();i++){
			Leader ld = leaders.get(i);
			if(ld.getPhase() == SELECT_MEMBER){
				int ep = eGreedy();
				if(ep == 0){
					if(ld.getLeaderEvaluation() > ld.getMemberEvaluation()){
						newLeaders.add(ld);
					}else if(ld.getLeaderEvaluation() < ld.getMemberEvaluation()){
						Member mem = new Member(ld);
						newMembers.add(mem);
						Analyzer.roleChangeCount[ld.getArea().getId()][tick]++;
					}else{
						int p = rnd.NextInt(2);
						if(p == 0){
							newLeaders.add(ld);
						}else if(p == 1){
							Member mem = new Member(ld);
							newMembers.add(mem);
							Analyzer.roleChangeCount[ld.getArea().getId()][tick]++;
						}	
					}
				}else if(ep == 1){
					int p = rnd.NextInt(2);
					if(p == 0){
						newLeaders.add(ld);
					}else if(p == 1){
						Member mem = new Member(ld);
						newMembers.add(mem);
						Analyzer.roleChangeCount[ld.getArea().getId()][tick]++;
					}
				}
			}else{
				newLeaders.add(ld);
			}
		}
		
		for(int i = 0;i<members.size();i++){
			Member mem = members.get(i);
			if(mem.roleChangable()){
				int ep = eGreedy();
				if(ep == 0){
					if(mem.getLeaderEvaluation() < mem.getMemberEvaluation()){
						newMembers.add(mem);
					}else if(mem.getLeaderEvaluation() > mem.getMemberEvaluation()){
						Leader ld = new Leader(mem); 
						newLeaders.add(ld);
						Analyzer.roleChangeCount[mem.getArea().getId()][tick]++;
					}else{
						int p = (int)rnd.NextInt(2);
						if(p == 0){
							Leader leader = new Leader(mem);
							newLeaders.add(leader);
							Analyzer.roleChangeCount[mem.getArea().getId()][tick]++;
						}else if(p == 1){
							newMembers.add(mem);
						}
					}
				}else if(ep == 1){
					int p = (int)rnd.NextInt(2);
					if(p == 0){
						Leader leader = new Leader(mem);
						newLeaders.add(leader);
						Analyzer.roleChangeCount[mem.getArea().getId()][tick]++;
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
		agents.clear();
		agents.addAll(leaders);
		agents.addAll(members);
	}
	
	//---------------------------------------------------------------------------------------
	
	private void countAgents(int tick){
		for(int i=0;i<leaders.size();i++){
			Leader leader = leaders.get(i);
			Analyzer.countLeaders[leader.getArea().getId()][tick]++;
			for(int subtaskType=0;subtaskType<TYPES_OF_RESOURCE;subtaskType++) {
				Analyzer.leaderDependableAgents[subtaskType][leader.getArea().getId()][tick] += leader.specificDeAgentsMap.get(subtaskType).size();
			}
			
			if(leader.isReciprocity() == true){
				Analyzer.reciprocalLeaders[leader.getArea().getId()][tick]++;
			}
			Analyzer.leaderAverageThreshold[leader.getArea().getId()][tick] += leader.leaderDependabilityDegreeThreshold;
		}
		for(int i=0;i<members.size();i++){
			Member member = members.get(i);
			Analyzer.subTaskQueueSum[member.getArea().getId()][tick] += member.getSubTaskQueueSize();
			Analyzer.memberSubtaskQueueHist[member.getSubTaskQueueSize()][member.getArea().getId()][tick]++;
			Analyzer.countMembers[member.getArea().getId()][tick]++;
			if(member.isReciprocity() == true){
				Analyzer.reciprocalMembers[member.getArea().getId()][tick]++;
			}
			Analyzer.memberDependableAgents[member.getArea().getId()][tick] += member.deAgents.size();
			Analyzer.memberAverageThreshold[member.getArea().getId()][tick] += member.memberDependabilityDegreeThreshold;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	private void updateDependablityAgent(){
		
		for(int i=0;i<leaders.size();i++){
			Leader leader = leaders.get(i);
			leader.clearDependablityAgent();
			leader.clearSpecificDependablityAgent();
			leader.selectAction(false);
			List<Agent> dependableAgents = new ArrayList<Agent>();
			for(int j=0;j<agents.size();j++){
				Agent agent = agents.get(j);
				if(leader.leaderDependabilityDegreeThreshold < leader.getLeaderDependablity(agent.getMyId())){
					leader.adddeagent(agent);
				}
				for(int k=0;k<3;k++){
					if(leader.leaderDependabilityDegreeThreshold < leader.getLeaderSpecificDependablity(k, agent.getMyId())){
						leader.addSpecificDeAgents(k, agent);
//						if(!dependableAgents.contains(agent)){//値を重複させない
//							dependableAgents.add(agent);
//						}
						leader.selectAction(true);
					}
				}
			}
			// leader.selectAction();
		}
		for(int i=0;i<members.size();i++){
			Member member = members.get(i);
			member.clearDependablityAgent();
			for(int j=0;j<agents.size();j++){
				Agent agent = agents.get(j);
				if(member.memberDependabilityDegreeThreshold < member.getMemberDependablity(agent.getMyId())){
					member.adddeagent(agent);
				}
			}
			member.selectAction();
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	private void decreaseDependability(){
		for(int j=0;j<agents.size();j++){
			Agent agent1 = agents.get(j);
			for(int i=0;i<agents.size();i++){
				Agent agent2 = agents.get(i);
				agent1.reducede(agent2.getMyId());
			}
    	}
	}
	
	public void resetDeAndSetBetterAgents(){
		for(int j=0;j<agents.size();j++){
			Agent agent = agents.get(j);
			agent.setMainAgents(new ArrayList<Agent>(agents));
//			agent.resetDe();
    	}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void printArea(){
		for(int j=0;j<NUM_OF_AREA;j++){
			System.out.println(areas.get(j));
    	}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void printDeAgent(){
		//0:0.3以下 1:0.3~0.4 2:0.4~0.5 3:0.5~0.6 4: 0.6~0.7 5:0.7~0.8 6:0.8以上
//		int[][] range = new int[500][7];
//		for(int j=0;j<agents.size();j++){
//			int count = 0;
//
//			for(int i=0;i<agents.size();i++){
//				double de = agents.get(j).getDependablity(i);
//				if(de < 0.3){
//					range[agents.get(j).getMyId()][0] ++;
//				}else if(de < 0.4){
//					range[agents.get(j).getMyId()][1] ++;
//				}else if(de < 0.5){
//					range[agents.get(j).getMyId()][2] ++;
//				}else if(de < 0.6){
//					range[agents.get(j).getMyId()][3] ++;
//				}else if(de < 0.7){
//					range[agents.get(j).getMyId()][4] ++;
//				}else if(de < 0.8){
//					range[agents.get(j).getMyId()][5] ++;
//				}else{
//					range[agents.get(j).getMyId()][6] ++;
//				}
//					
//			}
//			
//			for(int i=0;i<7;i++){
//				System.out.println(agents.get(j).getMyId() + ": [" + i + "] " + range[agents.get(j).getMyId()][i]);
//			}
//			
//    	}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void exportAllocatedSubTask(boolean reciprocity){
		try{
			FileWriter fw;
			if(RECIPROCITY){
				fw = new FileWriter("csv/ReciprocityAgentInfo.csv", false);
			}else{
				fw = new FileWriter("csv/RationalAgentInfo.csv", false);
			}
			PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
            pw.print("Agent ID");
            pw.print(",");
        	pw.print("Area ID");
            pw.print(",");
        	pw.print("x");
        	pw.print(",");
        	pw.print("y");
        	for(int i=0;i<TYPES_OF_RESOURCE;i++){
        		pw.print(",");
            	pw.print("Resource: " + (i+1));
        	}
        	pw.print(",");
        	pw.print("Resouce Average");
        	pw.print(",");
        	pw.print("Leader Evaluation");
        	pw.print(",");
        	pw.print("Member Evaluation");
        	pw.print(",");
        	pw.print("Allocated Subtasks");
        	pw.print(",");
        	pw.print("Refused Subtasks");
        	pw.print(",");
        	pw.print("Member Count");
        	pw.print(",");
        	pw.print("Leader Count");
        	pw.println();
        	for(int i=0;i<agents.size();i++){
    			Agent agent = agents.get(i);
    			pw.print(agent.getMyId());
    			pw.print(",");
            	pw.print(agent.getArea().getId());
                pw.print(",");
            	pw.print(agent.getPositionX());
            	pw.print(",");
            	pw.print(agent.getPositionY());
            	for(int j=0;j<TYPES_OF_RESOURCE;j++){
            		pw.print(",");
                	pw.print(agent.getCapacity(j));
            	}
            	pw.print(",");
            	pw.print(agent.averageOfCapability());
            	pw.print(",");
            	pw.print(agent.getLeaderEvaluation());
            	pw.print(",");
            	pw.print(agent.getMemberEvaluation());
            	pw.print(",");
            	pw.print(Agent.allocatedSubTask[agent.getMyId()]);
            	pw.print(",");
            	pw.print(Agent.refusedTask[agent.getMyId()]);
            	pw.print(",");
            	pw.print(Agent.memberCount[agent.getMyId()]);
            	pw.print(",");
            	pw.print(Agent.leaderCount[agent.getMyId()]);
            	
            	pw.println();
    		}
        	pw.close();
		}catch(IOException ex){
			System.out.println(ex);
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public void exportOwnedSubTask(boolean reciprocity){
		try{
			FileWriter fw;
			if(RECIPROCITY){
				fw = new FileWriter("csv/ReciprocityAgentOwnedSubTask.csv", false);
			}else{
				fw = new FileWriter("csv/RationalAgentOwnedSubTask.csv", false);
			}
			PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
            pw.print("tick");
  
        	for(int i=0;i<agents.size();i++){
        		pw.print(",");
            	pw.print(agents.get(i).getMyId());
        	}
        	
        	pw.println();
        	
        	
    		for(int i=0;i<EXPERIMENTAL_DURATION - 1;i++){
    			if((i+1) % 100 == 0){
    				pw.print(i+1);
    			}
    			for(int j=0;j<agents.size();j++){
        			Agent agent = agents.get(j);
        			Agent.ownedSubtask[agent.getMyId()][i+1] += Agent.ownedSubtask[agent.getMyId()][i];
        			if((i+1) % 100 == 0){
    	    			pw.print(",");
    	            	pw.print(String.format("%.2f", (double)(Agent.ownedSubtask[agent.getMyId()][i+1] - Agent.ownedSubtask[agent.getMyId()][i-99]) / 100));
        			}
        		}
    			if((i+1) % 100 == 0){
    				pw.println();
    			}
    			
    		}
        	pw.close();
		}catch(IOException ex){
			System.out.println(ex);
		}
		
	}
	//---------------------------------------------------------------------------------------
	
	public void exportAgentConnection(int tick){
		try{
			FileWriter fw;
			fw = new FileWriter("csv/ReciprocityAgentConnection"+ tick +".csv", false);
			PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
            pw.print("AgentID");
            pw.print(",");
            pw.print("Cap");
            pw.print(",");
            pw.print("Role");
            pw.print(",");
            pw.print("Count");
            pw.print(",");
            pw.print("TO");
            pw.print(",");
            pw.print("Cap");
            pw.print(",");
            pw.print("Distance");
            
        	pw.println();
    		for(int i=0;i<agents.size();i++){
    			Agent agent = agents.get(i);
				pw.print(agent.getMyId());
				pw.print(",");
				pw.print(String.format("%.2f",agent.averageOfCapability()));
				pw.print(",");
				pw.print(agent.getClass());
				List<Agent> deAgents = agent.getDeAgents();
				pw.print(",");
				pw.print(deAgents.size());
    			for(int j=0;j<deAgents.size();j++){
        			Agent deAgent = deAgents.get(j);
        			pw.print(",");
	            	pw.print(deAgent.getMyId());
	            	pw.print(",");
	            	pw.print(String.format("%.2f", deAgent.averageOfCapability()));
	            	pw.print(",");
	            	pw.print(agent.getdistance(deAgent.getMyId()));
        		}
    			pw.println();
    		}
        	pw.close();
		}catch(IOException ex){
			System.out.println(ex);
		}
		
	}	
	
	//---------------------------------------------------------------------------------------
	
	public void exportForCytoscape(int tick){
		try{
			FileWriter fw;
			fw = new FileWriter("csv/ReciprocityAgentForCytoscape"+ tick +".csv", false);
			PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
            pw.print("FROM");
            pw.print(",");
            pw.print("Cap");
            pw.print(",");
            pw.print("Role");
            pw.print(",");
            pw.print("TO");
            pw.print(",");
            pw.print("Distance");
            
        	pw.println();
    		for(int i=0;i<agents.size();i++){
    			Agent agent = agents.get(i);
    			List<Agent> deAgents = agent.getDeAgents();
    			for(int j=0;j<deAgents.size();j++){
    				Agent deAgent = deAgents.get(j);
					pw.print(agent.getMyId());
					pw.print(",");
					pw.print(String.format("%.2f",agent.averageOfCapability()));
					pw.print(",");
					pw.print(agent.getClass());
        			pw.print(",");
	            	pw.print(deAgent.getMyId());
	            	pw.print(",");
	            	pw.print(agent.getdistance(deAgent.getMyId()));
	            	pw.println();
        		}
    		}
        	pw.close();
		}catch(IOException ex){
			System.out.println(ex);
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public void changeAllAreaWorkload(double workload){
		for(int i=0;i<areas.size();i++){
			areas.get(i).changeWorkload(workload);
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void changeAreaWorkload(double workload, Area area){
		area.changeWorkload(workload);
	}
	
	
	//---------------------------------------------------------------------------------------
	
	public int eGreedy() {
		int A;
        int randNum = rnd.NextInt(101);
        if (randNum <= EPSILON * 100.0) {
        	//eの確率
			A = rnd.NextInt(2);
        } else {
        	//(1-e)の確率
        	A = 0;
        }
        return A;
	}
	
}
