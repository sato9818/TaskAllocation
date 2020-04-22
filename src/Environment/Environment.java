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
import Agent.Leader;
import Agent.Member;
import Message.Message;
import Random.Sfmt;

public class Environment {
	
	private List<Leader> leaders = new ArrayList<Leader>();
	private List<Member> members = new ArrayList<Member>();
	private List<Agent> agents = new ArrayList<Agent>();
	private List<Area> areas = new ArrayList<Area>();
	private HashMap<Integer, List<Message>> agentIdToMessageList = new HashMap<Integer, List<Message>>();
	Sfmt rnd;
	
	public static double communicationTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static double countSentMessages[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int countMembers[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int countLeaders[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	
	
	//---------------------------------------------------------------------------------------
	
	public Environment(Sfmt rnd, int seed){
		this.rnd = rnd;
		generateAreas();
		generateAgents(seed);
		for(int i=0;i<NUM_OF_AGENT;i++){
			agentIdToMessageList.put(agents.get(i).getMyId(), new ArrayList<Message>());
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	private void generateAreas(){
		int divX = GRID_X / NUM_OF_VERTICAL_DIVISION;
		int divY = GRID_Y / NUM_OF_HORIZONTAL_DIVISION;
		
		
		for(int i=0;i<NUM_OF_VERTICAL_DIVISION;i++){
			for(int j=0;j<NUM_OF_HORIZONTAL_DIVISION;j++){
				double p = rnd.NextUnif();
				int workload = 2;
//				if(p < 1.0 / 3){
//					workload = LOW_WORKLOAD;
//				}else if(p < 2.0 / 3){
//					workload = MODERATE_WORKLOAD;
//				}else{
//					workload = HIGH_WORKLOAD;
//				}
				Area area = new Area(workload, i*divX, j*divY, (i+1) * divX - 1, (j+1) * divY - 1);
				areas.add(area);
			}
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	
	private void generateAgents(int seed){
		
		List<Grid> grid = new ArrayList<Grid>();
		for(int i=0;i<GRID_X;i++){
			for(int j=0;j<GRID_Y;j++){
				Grid g = new Grid(i,j);
				grid.add(g);
			}
		}
		
		Collections.shuffle(grid, new Random(seed));
		
		for(int i=0;i<NUM_OF_AGENT;i++){
			int x = grid.get(i).x;
			int y = grid.get(i).y;
			int p = rnd.NextInt(2);
			if(p == 0){
				Leader leader = new Leader(rnd, identifyArea(x, y), x, y);
				leaders.add(leader);
				agents.add(leader);
			}else if(p == 1){
				Member member = new Member(rnd, identifyArea(x, y), x, y);
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
	
	public void run(int tick, Random r){
		System.out.println("tick: " + tick + "---------------------------------");
		System.out.println("Leader: " + leaders.size());
		System.out.println("Member: " + members.size());
		System.out.println(Agent.tookTask);
		System.out.println(Agent.finishSubTask);
		
		for(int i=0;i<areas.size();i++){
			areas.get(i).addTask(rnd);
		}
		
		updateDependablityAgent();
		agentsGetMessages(tick);
		
		Collections.shuffle(leaders, r);
		Collections.shuffle(members, r);
		Collections.shuffle(agents, r);
		
		
		leadersAct(tick);
		membersAct(tick);
		collectMessages();
		changeRole();
		countAgents(tick);
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
					communicationTime[message.from().getArea().getId()][tick] += message.from().getdistance(message.to().getMyId());
					countSentMessages[message.from().getArea().getId()][tick]++;
					it.remove();
				}
			}
		}
	}
	
	//---------------------------------------------------------------------------------------

	private void leadersAct(int tick){
		
		for(int i=0;i<leaders.size();i++){
			Leader leader = leaders.get(i);
			leader.act(agents, tick);
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
	
	private void changeRole(){
		List<Leader> newLeaders = new ArrayList<Leader>();
		List<Member> newMembers = new ArrayList<Member>();
	
		for(int i = 0;i<leaders.size();i++){
			Leader ld = leaders.get(i);
			if(ld.getPhase() == SELECT_MEMBER){
				int ep = eGreedy(rnd);
				if(ep == 0){
					if(ld.getLeaderEvaluation() > ld.getMemberEvaluation()){
						newLeaders.add(ld);
					}else if(ld.getLeaderEvaluation() < ld.getMemberEvaluation()){
						Member mem = new Member(ld);
						newMembers.add(mem);
					}else{
						int p = rnd.NextInt(2);
						if(p == 0){
							newLeaders.add(ld);
						}else if(p == 1){
							Member mem = new Member(ld);
							newMembers.add(mem);
						}	
					}
				}else if(ep == 1){
					int p = rnd.NextInt(2);
					if(p == 0){
						newLeaders.add(ld);
					}else if(p == 1){
						Member mem = new Member(ld);
						newMembers.add(mem);
					}
				}
			}else{
				newLeaders.add(ld);
			}
		}
		
		for(int i = 0;i<members.size();i++){
			Member mem = members.get(i);
			if(mem.roleChangable()){
				int ep = eGreedy(rnd);
				if(ep == 0){
					if(mem.getLeaderEvaluation() < mem.getMemberEvaluation()){
						newMembers.add(mem);
					}else if(mem.getLeaderEvaluation() > mem.getMemberEvaluation()){
						Leader ld = new Leader(mem); 
						newLeaders.add(ld);
					}else{
						int p = (int)rnd.NextUnif() * 2;
						if(p == 0){
							Leader leader = new Leader(mem);
							newLeaders.add(leader);
						}else if(p == 1){
							newMembers.add(mem);
						}
					}
				}else if(ep == 1){
					int p = (int)rnd.NextUnif() * 2;
					if(p == 0){
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
		agents.clear();
		agents.addAll(leaders);
		agents.addAll(members);
	}
	
	//---------------------------------------------------------------------------------------
	
	private void countAgents(int tick){
		for(int i=0;i<leaders.size();i++){
			countLeaders[leaders.get(i).getArea().getId()][tick]++;
		}
		for(int i=0;i<members.size();i++){
			countMembers[members.get(i).getArea().getId()][tick]++;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	private void updateDependablityAgent(){
		for(int i=0;i<leaders.size();i++){
			Leader leader = leaders.get(i);
			leader.clearDependablityAgent();
			for(int j=0;j<members.size();j++){
				Member member = members.get(j);
				if(LEADER_DEPENDABLITY_DEGREE_THRESHOLD < leader.getDependablity(member.getMyId())){
					leader.adddeagent(member);
				}
				leader.reducede(member.getMyId());
			}
			leader.updatedeagent();
		}
		for(int i=0;i<members.size();i++){
			Member member = members.get(i);
			member.clearDependablityAgent();
			for(int j=0;j<leaders.size();j++){
				Leader leader = leaders.get(j);
				if(MEMBER_DEPENDABLITY_DEGREE_THRESHOLD < member.getDependablity(leader.getMyId())){
					member.adddeagent(leader);
				}
				member.reducede(leader.getMyId());
			}
			member.updatedeagent();
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void printArea(){
		for(int j=0;j<NUM_OF_AREA;j++){
			System.out.println(areas.get(j));
    	}
	}
	
	
	//---------------------------------------------------------------------------------------
	
	public int eGreedy(Sfmt rnd) {
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
