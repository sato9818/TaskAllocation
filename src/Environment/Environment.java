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
import agent.LeaderState;
import agent.Role;
import message.Message;
import random.Sfmt;

public class Environment {
	
	private List<Agent> leaders = new ArrayList<Agent>();
	private List<Agent> members = new ArrayList<Agent>();
	private List<Agent> agents = new ArrayList<Agent>();
	private List<Area> areas = new ArrayList<Area>();
	private HashMap<Integer, List<Message>> mailBoxes = new HashMap<Integer, List<Message>>();
	
	public Sfmt rnd;
	public Random r;
	private int taskID = 0;
	private int areaID = 0;
	private int agentID = 0;
	public int tick;
	
	static public int countMembers[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	static public int countLeaders[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	static public double leaderDependableAgents[][][] = new double[TYPES_OF_RESOURCE][NUM_OF_AREA][EXPERIMENTAL_DURATION];
	static public double memberDependableAgents[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	static public int reciprocalMembers[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	static public int reciprocalLeaders[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	static public double avgSubTaskQueue[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	static public double avgLeaderThreshold[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	static public double avgMemberThreshold[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	static public double communicationTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	static public double countSentMessages[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	
	//agentから集計用------------------------------------------------------------------------------------
	//処理したタスク数
	static public int executedTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	//処理したサブタスク数
	static public int executedSubTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	//チームが組めなかったことによるサブタスク破棄
	static public int wastedTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	//途中で断られてチームが解散になったことによるタスク失敗
	static public int rejectedTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	static public double waitingTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	static public double executedTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	static public double allExecutedTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	static public int allocationMemberCount[][][] = new int[NUM_OF_AREA][NUM_OF_AREA][EXPERIMENTAL_DURATION];
	
	static public int overflowedTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];

	
	
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
		
		for(int i=0;i<NUM_OF_VERTICAL_DIVISION;i++){
			for(int j=0;j<NUM_OF_HORIZONTAL_DIVISION;j++){
				Area area = new Area(WORKLOADS[areaID], i*divX, j*divY, (i+1) * divX - 1, (j+1) * divY - 1, areaID);
				areas.add(area);
				areaID++;
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
			Area area = identifyArea(x, y);
			area.addAgent();
			if(p == 0){
				Agent leader = new Agent(area, x, y, this, agentID++);
				leader.setRole(Role.LEADER);
				leaders.add(leader);
				agents.add(leader);
			}else if(p == 1){
				Agent member = new Agent(area, x, y, this, agentID++);
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
	
	public void run(int t){
		tick = t;
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
			taskID = areas.get(i).addTask(tick, taskID, rnd);
			if(taskID > 1000000) {
				taskID = 0;
			}
		}

		if(!THRESHOLD_FIXED){
			updateAgentsThreshold();
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
		aggregateAgentData();
	}
	
	private void updateAgentsThreshold(){
		for(Agent agent : agents){
			agent.updateThreshold();
		}
	}
	
	synchronized private void aggregateAgentData() {
		for(Agent agent: agents) {
			executedTask[agent.getArea().getId()][tick] += agent.executedTask;
			executedSubTask[agent.getArea().getId()][tick] += agent.executedSubTask;
			wastedTask[agent.getArea().getId()][tick] += agent.wastedTask;
			rejectedTask[agent.getArea().getId()][tick] += agent.rejectedTask;
			waitingTime[agent.getArea().getId()][tick] += agent.waitingTime;
			executedTime[agent.getArea().getId()][tick] += agent.executedTime;
			allExecutedTime[agent.getArea().getId()][tick] += agent.allExecutedTime;
			for(Area area: areas) {
				allocationMemberCount[agent.getArea().getId()][area.getId()][tick] += agent.allocationMemberCount[area.getId()];
			}
			agent.clearVariablesForAnalize();
		}
		
		for(Area area: areas) {
			overflowedTask[area.getId()][tick] += area.overflowedTask[tick];
		}
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
	
	synchronized private void countAgents(){
		for(int i=0;i<leaders.size();i++){
			Agent leader = leaders.get(i);
			countLeaders[leader.getArea().getId()][tick]++;
			avgLeaderThreshold[leader.getArea().getId()][tick] += leader.leaderDependabilityDegreeThreshold;
			for(int type=0;type<TYPES_OF_RESOURCE;type++) {
				leaderDependableAgents[type][leader.getArea().getId()][tick] += leader.specificDeAgentsMap.get(type).size();
			}
			if(leader.isReciprocity() == true){
				reciprocalLeaders[leader.getArea().getId()][tick]++;
			}
		}
		for(int i=0;i<members.size();i++){
			Agent member = members.get(i);
			avgSubTaskQueue[member.getArea().getId()][tick] += (double)member.getSubTaskQueueSize();
			countMembers[member.getArea().getId()][tick]++;
			avgMemberThreshold[member.getArea().getId()][tick] += member.memberDependabilityDegreeThreshold;
			memberDependableAgents[member.getArea().getId()][tick] += member.getDeAgents().size();
			if(member.isReciprocity() == true){
				reciprocalMembers[member.getArea().getId()][tick]++;
			}
		}
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
				if(leader.leaderDependabilityDegreeThreshold < leader.getLeaderDependablity(agent.getMyId())){
					leader.adddeagent(agent);
				}
				for(int k=0;k<3;k++){
					if(leader.leaderDependabilityDegreeThreshold < leader.getLeaderSpecificDependablity(k, agent.getMyId())){
						leader.addSpecificDeAgents(k, agent);
//						if(!dependableAgents.contains(agent)){//値を重複させない
//							dependableAgents.add(agent);
//						}
						leader.selectLeaderAttitude(true);
					}
				}
			}
			leader.selectLeaderAttitude();
		}
		for(int i=0;i<members.size();i++){
			Agent member = members.get(i);
			member.clearDependablityAgent();
			for(int j=0;j<agents.size();j++){
				Agent agent = agents.get(j);
				if(member.memberDependabilityDegreeThreshold < member.getMemberDependablity(agent.getMyId())){
					member.adddeagent(agent);
				}
			}
			member.selectMemberAttitude();
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
