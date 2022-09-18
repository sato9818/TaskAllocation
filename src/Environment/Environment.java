package Environment;
import static Constants.Constants.*;

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

import Agent.Agent;
import Agent.Role;
import Agent.LeaderState;
import Message.Message;
import Random.Sfmt;

public class Environment {
	
	private List<Agent> leaders = new ArrayList<Agent>();
	private List<Agent> members = new ArrayList<Agent>();
	private List<Agent> agents = new ArrayList<Agent>();
	private List<Area> areas = new ArrayList<Area>();
	private HashMap<Integer, List<Message>> mailBoxes = new HashMap<Integer, List<Message>>();
	public static Sfmt rnd;
	public static Random r;
	public static int tick;
	
	public static double communicationTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static double countSentMessages[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int countMembers[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int countLeaders[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int reciprocityMembers[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int reciprocityLeaders[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static double avgSubTaskQueue[] = new double[EXPERIMENTAL_DURATION];

	
	
	//---------------------------------------------------------------------------------------
	
	public Environment(int seed){
		rnd = new Sfmt(seed);
		r = new Random(seed);
		generateAreas();
		generateAgents();
		for(int i=0;i<NUM_OF_AGENT;i++){
			mailBoxes.put(agents.get(i).getMyId(), new ArrayList<Message>());
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	private void generateAreas(){
		int divX = GRID_X / NUM_OF_VERTICAL_DIVISION;
		int divY = GRID_Y / NUM_OF_HORIZONTAL_DIVISION;
		
		int count = 0;
		for(int i=0;i<NUM_OF_VERTICAL_DIVISION;i++){
			for(int j=0;j<NUM_OF_HORIZONTAL_DIVISION;j++){
				Area area = new Area(WORKLOADS[count], i*divX, j*divY, (i+1) * divX - 1, (j+1) * divY - 1);
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
				Agent leader = new Agent(identifyArea(x, y), x, y);
				leader.setRole(Role.LEADER);
				leaders.add(leader);
				agents.add(leader);
			}else if(p == 1){
				Agent member = new Agent(identifyArea(x, y), x, y);
				member.setRole(Role.MEMBER);
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
		Environment.tick = tick;
		System.out.println("tick: " + tick + "---------------------------------------------------------------------------------------------------");
//		System.out.println("# leader: " + leaders.size());
//		System.out.println("# member: " + members.size());
//		System.out.println("# agent: " + agents.size());
		
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
			areas.get(i).addTask(tick);
		}
		if(RECIPROCITY){
			updateDependablityAgent();
		}
		agentsGetMessages();
		
		Collections.shuffle(leaders, r);
		Collections.shuffle(members, r);
		Collections.shuffle(agents, r);
		
		
		leadersAct();
		membersAct();
		collectMessages();
		changeAgentRole();
		countAgents();
		decreaseDependability();
	}
	
	//---------------------------------------------------------------------------------------
	
	private void agentsGetMessages(){
		for(int i=0;i<leaders.size();i++){
			Agent leader = leaders.get(i);
			
			List<Message> messageList = mailBoxes.get(leader.getMyId());
			Iterator<Message> it = messageList.iterator();
			while(it.hasNext()){
				Message message = it.next();
				if(message.isDelivered()){
					leader.readMessageAsLeader(message);
					communicationTime[message.from().getArea().getId()][tick] += message.from().getdistance(message.to().getMyId());
					countSentMessages[message.from().getArea().getId()][tick]++;
					it.remove();
				}
			}
		}
		
		for(int i=0;i<members.size();i++){
			Agent member = members.get(i);
			
			List<Message> messageList = mailBoxes.get(member.getMyId());
			Iterator<Message> it = messageList.iterator();
			while(it.hasNext()){
				Message message = it.next();
				if(message.isDelivered()){
					member.readMessageAsMember(message);
					communicationTime[message.from().getArea().getId()][tick] += message.from().getdistance(message.to().getMyId());
					countSentMessages[message.from().getArea().getId()][tick]++;
					it.remove();
				}
			}
		}
	}
	
	//---------------------------------------------------------------------------------------

	private void leadersAct(){
		for(int i=0;i<leaders.size();i++){
			Agent leader = leaders.get(i);
			if(tick >= TIME_TO_RESET_DE){
				List<Agent> mainAgents = new ArrayList<Agent>();
				for(int j = 0;j<agents.size();j++){
					if(leader.getMainMemberIds().contains(agents.get(j).getMyId())){
						mainAgents.add(agents.get(j));
					}
				}
				leader.act(mainAgents);
			}else{
				leader.act(agents);
			}
				
		}
	}
	
	//---------------------------------------------------------------------------------------

	private void membersAct(){
		for(int i=0;i<members.size();i++){
			Agent member = members.get(i);
			member.act();
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	private void collectMessages(){
		for(int i=0;i<agents.size();i++){
			Agent agent = agents.get(i);
			List<Message> messages = agent.sendMessages();
			for(int j=0;j<messages.size();j++){
				mailBoxes.get(messages.get(j).to().getMyId()).add(messages.get(j));
			}
			agent.clearMessages();
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	private void changeAgentRole(){
		for(int i = 0;i<leaders.size();i++){
			Agent leader = leaders.get(i);
			if(leader.getLeaderState() != LeaderState.SELECT_MEMBER){
				continue;
			}

			int ep = eGreedy();
			if(ep == 0){
				if(leader.getLeaderEvaluation() < leader.getMemberEvaluation()){
					leaders.remove(leader);
					leader.setRole(Role.MEMBER);
					members.add(leader);
				}else if(leader.getLeaderEvaluation() == leader.getMemberEvaluation()){ 
					int p = rnd.NextInt(2);
					if(p == 0){
						leaders.remove(leader);
						leader.setRole(Role.MEMBER);
						members.add(leader);
					}	
				}
			}else if(ep == 1){
				int p = rnd.NextInt(2);
				if(p == 0){
					leaders.remove(leader);
					leader.setRole(Role.MEMBER);
					members.add(leader);
				}
			}

		}
		
		for(int i = 0;i<members.size();i++){
			Agent member = members.get(i);
			if(!member.roleChangable()){
				continue;
			}
			int ep = eGreedy();
			if(ep == 0){
				if(member.getLeaderEvaluation() > member.getMemberEvaluation()){
					members.remove(member);
					member.setRole(Role.LEADER);
					leaders.add(member);
				}else if(member.getLeaderEvaluation() > member.getMemberEvaluation()){
					int p = (int)rnd.NextInt(2);
					if(p == 0){
						members.remove(member);
						member.setRole(Role.LEADER);
						leaders.add(member);
					}
				}
			}else if(ep == 1){
				int p = (int)rnd.NextInt(2);
				if(p == 0){
					members.remove(member);
					member.setRole(Role.LEADER);
					leaders.add(member);
				}
			}

		}
	}
	
	//---------------------------------------------------------------------------------------
	
	private void countAgents(){
		for(int i=0;i<leaders.size();i++){
			Agent leader = leaders.get(i);
			countLeaders[leader.getArea().getId()][tick]++;
			if(leader.isReciprocity() == true){
				reciprocityLeaders[leader.getArea().getId()][tick]++;
			}else{
//				System.out.println("not reciprocity");
			}
			
		}
		double buf = 0;
		for(int i=0;i<members.size();i++){
			Agent member = members.get(i);
			buf += (double)member.getSubTaskQueueSize();
			countMembers[member.getArea().getId()][tick]++;
			if(member.isReciprocity() == true){
				reciprocityMembers[member.getArea().getId()][tick]++;
			}else{
//				System.out.println("not reciprocity");
			}
		}
		avgSubTaskQueue[tick] += buf / members.size();
	}
	
	//---------------------------------------------------------------------------------------
	
	private void updateDependablityAgent(){
		for(int i=0;i<leaders.size();i++){
			Agent leader = leaders.get(i);
			leader.clearDependablityAgent();
			leader.clearSpecificDependablityAgent();
			leader.selectLeaderAttitude(false);
			List<Agent> dependableAgents = new ArrayList<Agent>();
			for(int j=0;j<agents.size();j++){
				Agent agent = agents.get(j);
				if(LEADER_DEPENDABLITY_DEGREE_THRESHOLD < leader.getLeaderDependablity(agent.getMyId())){
					leader.adddeagent(agent);
				}
				for(int k=0;k<3;k++){
					if(LEADER_DEPENDABLITY_DEGREE_THRESHOLD < leader.getLeaderSpecificDependablity(k, agent.getMyId())){
						leader.addSpecificDeAgents(k, agent);
//						if(!dependableAgents.contains(agent)){//値を重複させない
//							dependableAgents.add(agent);
//						}
						leader.selectLeaderAttitude(true);
					}
				}
			}
//			leader.selectAction();
		}
		for(int i=0;i<members.size();i++){
			Agent member = members.get(i);
			member.clearDependablityAgent();
			for(int j=0;j<agents.size();j++){
				Agent agent = agents.get(j);
				if(MEMBER_DEPENDABLITY_DEGREE_THRESHOLD < member.getMemberDependablity(agent.getMyId())){
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
	
	public void exportAgentConnection(){
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
	
	public void exportForCytoscape(){
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
