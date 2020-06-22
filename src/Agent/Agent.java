package Agent;

import static Constants.Constants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Environment.Area;
import Environment.Environment;
import Message.Message;
import Random.Sfmt;
import Task.SubTask;

public class Agent {
	//固有番号をつけるためのもの
	static int num = 0;
	
	
	//定数------------------------------------------------------------------------------------
	
	//座標
	private final int gridX;
	private final int gridY;
	//自分のエリア
	protected final Area area;
	//固有番号
	private final int myId;
	
	//変数------------------------------------------------------------------------------------
	
	//リソース
	int capacity[] = new int[TYPES_OF_RESOURCE];
	//リソースの平均値
	private double capave = 0.0;
	//信頼度
	double de[] = new double[NUM_OF_AGENT];
	//送ったメッセージリスト
	protected List<Message> allMessages = new ArrayList<Message>();
	//フェイズ
	protected int phase = 0;
	//信頼エージェントのリスト
	protected List<Agent> deAgents = new ArrayList<Agent>();
	//各エージェントとの距離
	private int distance[] = new int[NUM_OF_AGENT];
	//届いたメッセージの置き場
	protected List<Message> post = new ArrayList<Message>();
	//役割適正値
	protected double leaderEvaluation = 0.5;
	protected double memberEvaluation = 0.5;
	//タスクidとタスクを処理しているメンバのリストのmap
	HashMap<Integer, List<Agent>> memberListMap = new HashMap<Integer, List<Agent>>();
	//タスクidとそれを割り当てた時間のMap
	HashMap<Integer, Integer> executionTimeMap = new HashMap<Integer, Integer>();
	//互恵主義か合理主義か
	protected boolean reciprocityAction = false;
	
	//集計用------------------------------------------------------------------------------------
	
	public static int executedTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int executedSubTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int wastedTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static double waitingTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static double executedTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static double allExecutedTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int allocationMemberCount[][][] = new int[NUM_OF_AREA][NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int tookTask = 0;
	public static int finishSubTask = 0;
	
	//---------------------------------------------------------------------------------------
	
	Agent(Area area, int x, int y){
		setCapacity();
		initializeDependability();
		gridX = x;
		gridY = y;
		this.area = area;
		myId = num;
		num++;
		if(num == NUM_OF_AGENT){
			num = 0;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	Agent(Agent agent){
		myId = agent.getMyId();
		capacity = agent.capacity;
		area = agent.getArea();
		de = agent.de;
		gridX = agent.getPositionX();
		gridY = agent.getPositionY();
		deAgents = agent.deAgents;
		capave = agent.capave;
		distance = agent.distance;
		leaderEvaluation = agent.leaderEvaluation;
		memberEvaluation = agent.memberEvaluation;
		memberListMap = agent.memberListMap;
		executionTimeMap = agent.executionTimeMap;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void readMessage(Message message, int tick){
	}
		
	//---------------------------------------------------------------------------------------
	
	public List<Agent> sortMemberDeAgent(List<Agent> agents){
		for (int i = 0; i < agents.size() - 1; i++) {
            for (int j = agents.size() - 1; j > i; j--) {
                if (de[agents.get(j - 1).getMyId()] < de[agents.get(j).getMyId()]) {
                    Collections.swap(agents,j-1,j);
                }
            }
        }
		return agents;
	}
	
	//---------------------------------------------------------------------------------------
	
	public List<Message> sortMemberDeMessage(List<Message> messages){
		for (int i = 0; i < messages.size() - 1; i++) {
            for (int j = messages.size() - 1; j > i; j--) {
                if (de[messages.get(j - 1).from().getMyId()] < de[messages.get(j).from().getMyId()]) {
                    Collections.swap(messages,j-1,j);
                }
            }
        }
		return messages;
	}
	
	//---------------------------------------------------------------------------------------
	
	public List<Message> sendMessages(){
		return allMessages;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void clearMessages(){
		allMessages.clear();
	}
	
	
	//---------------------------------------------------------------------------------------
	
	public void setdistance(Agent agent){
		int dis = (int)Math.ceil((double)manhattan(this.getPositionX(), agent.getPositionX(), this.getPositionY(), agent.getPositionY()) / 200 * 10/**/ );
		distance[agent.getMyId()] = dis;
	}
	
	//---------------------------------------------------------------------------------------
	
	double euclid(double x1, double x2, double y1, double y2) {
		double d = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
		return d;
	}	
	
	//---------------------------------------------------------------------------------------
	
	private int manhattan(int x1, int x2, int y1, int y2){
		int x = Math.abs(x1-x2);
		int y = Math.abs(y1-y2);
		return x + y;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getdistance(int id){
		return distance[id];
	}
	
	//---------------------------------------------------------------------------------------
	
	private void initializeDependability(){
		for(int i=0;i<NUM_OF_AGENT;i++){
			if(i != this.getMyId()){
				de[i] = 0.5;
			}
		}
	}
	//---------------------------------------------------------------------------------------
	
	public Area getArea(){
		return area;
	}
	
	//---------------------------------------------------------------------------------------
	
	private void setCapacity(){
		while(capacity[0] == 0 && capacity[1] == 0 && capacity[2] == 0){
			int p = 0;
			for(int i=0;i<3;i++){
				capacity[i] = 1 + Environment.rnd.NextInt(5);
				capave += (double)capacity[i];
				if(capacity[i] != 0){
					p++;
				}
			}
			capave /= (double)p;
		}
	}
	//---------------------------------------------------------------------------------------
	
	public void finishMemberSubTask(Message message, int tick){
		Agent member = message.from();
		SubTask subTask = message.getSubTask();
		int taskId = subTask.getTaskId();
		List<Agent> executingMember = memberListMap.get(taskId);
		executingMember.remove(member);
		int startTick = executionTimeMap.get(taskId);
		updateDependablity(message,true,tick - startTick);
		executedSubTask[this.getArea().getId()][tick]++;
		allExecutedTime[this.getArea().getId()][tick] += tick - startTick;
		executedTime[this.getArea().getId()][tick] += message.getExecutedTime();
		if(executingMember.isEmpty()){
			memberListMap.remove(taskId);
			executedTask[this.getArea().getId()][tick]++;
		}
	}
	
	
	//---------------------------------------------------------------------------------------
	
	public void updateDependablity(Message message, boolean success, int executedTime){
		double delta = 0.0;
		if(success){
			delta = (double)message.getSubTask().getutility() 
			/  //---------------------------------------------------------------
					(double)(executedTime) ;
			
//				delta = (double)message.getSubTask().getutility() / (this.getdistance(message.from().getMyId()) * 2 + getExcutingTime(message.getSubTask())) ;
//			System.out.println("excutiontime " + getExcutingTime(message.getSubTask()));
//				delta = 1;
//			System.out.println(executedTime);
			
		}
		this.de[message.from().getMyId()] = 
					(1.0 - 0.01/**/) * this.de[message.from().getMyId()] 
					+ 0.01 * delta;
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getExcutingTime(SubTask s){
		int et = 0;
		for(int i=0;i<TYPES_OF_RESOURCE;i++){
			int a = (int)Math.ceil((double)s.getcapacity(i) / capacity[i]);
			if(et < a ){
				et = a;
			}
		}
		return et;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getPositionX(){
		return gridX;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getPositionY(){
		return gridY;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void reducede(int id){
		de[id] = Math.max(de[id]-0.000002, 0.0);
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getMyId(){
		return myId;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getPhase(){
		return phase;
	}
	
	//---------------------------------------------------------------------------------------
	
	public double averageOfCapability(){
		return capave;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void adddeagent(Agent agent){
		deAgents.add(agent);
	}
	
	//---------------------------------------------------------------------------------------
	
	public List<Agent> sortagent(List<Agent> agents){
		for (int i = 0; i < agents.size() - 1; i++) {
            for (int j = agents.size() - 1; j > i; j--) {
                if (de[agents.get(j - 1).getMyId()] < de[agents.get(j).getMyId()]) {
                    Collections.swap(agents,j-1,j);
                }
            }
        }
		return agents;
	}
	
	//---------------------------------------------------------------------------------------
	
	public double getLeaderEvaluation(){
		return leaderEvaluation;
	}
	
	//---------------------------------------------------------------------------------------
	
	public double getMemberEvaluation(){
		return memberEvaluation;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void clearDependablityAgent(){
		deAgents.clear();
	}

	//---------------------------------------------------------------------------------------
	
	public double getDependablity(int id){
		return de[id];
	}
	//---------------------------------------------------------------------------------------
	
	public boolean isReciprocity(){
		return reciprocityAction;
	}

	//---------------------------------------------------------------------------------------
	
	public int eGreedy() {
		int A;
        int randNum = Environment.rnd.NextInt(101);
        if (randNum <= EPSILON * 100.0) {
        	//eの確率
			A = Environment.rnd.NextInt(2);
        } else {
        	//(1-e)の確率
        	A = 0;
        }
        return A;
	}
}
