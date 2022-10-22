package agent;

import static shared.Constants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import environment.Area;
import environment.Environment;
import message.Message;
import random.Sfmt;
import task.SubTask;

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
//	double de[] = new double[NUM_OF_AGENT];
	double leaderDe[] = new double[NUM_OF_AGENT];
	//各サブタスクtypeに応じた信頼度
	double specificLeaderDe[][] = new double[TYPES_OF_RESOURCE][NUM_OF_AGENT];
	double memberDe[] = new double[NUM_OF_AGENT];
	//送ったメッセージリスト
	protected List<Message> allMessages = new ArrayList<Message>();
	//フェイズ
	protected int phase = 0;
	//信頼エージェントのリスト
	protected List<Agent> deAgents = new ArrayList<Agent>();
	//各サブタスクtypeに応じた信頼エージェント
	protected HashMap<Integer, List<Agent>> specificDeAgentsMap = new HashMap<Integer, List<Agent>>();
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
	//サブタスクの残り時間
	protected int remainingTime = 0;
	//自分の処理しているサブタスク
	protected SubTask mySubTask = null;
	//距離2位内にいるエージェントのID
	protected List<Agent> nearAgents = new ArrayList<Agent>();
	
	protected List<Integer> mainMemberIds = new ArrayList<Integer>();
	
	protected List<Integer> subMemberIds = new ArrayList<Integer>();
	
	//タスクを渡してまだ終了報告が返ってきていないメンバ
	protected List<Agent> executingMembers = new ArrayList<Agent>();
	
	protected int sumQueueSize = 0;
	protected int failureOrFinishedmessage = 0;
	
	
	
	//集計用------------------------------------------------------------------------------------
	//処理したタスク数
	public static int executedTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	//処理したサブタスク数
	public static int executedSubTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	//チームが組めなかったことによるサブタスク破棄
	public static int wastedTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	//途中で断られてチームが解散になったことによるタスク失敗
	public static int rejectedTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static double waitingTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static double executedTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static double allExecutedTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int allocationMemberCount[][][] = new int[NUM_OF_AREA][NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int refusedTask[] = new int[NUM_OF_AGENT];
	public static int finishSubTask = 0;
	public static int allocatedSubTask[] = new int[NUM_OF_AGENT];
	public static int leaderCount[] = new int[NUM_OF_AGENT];
	public static int memberCount[] = new int[NUM_OF_AGENT];
	public static int ownedSubtask[][] = new int[NUM_OF_AGENT][EXPERIMENTAL_DURATION];
	
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
		leaderDe = agent.leaderDe;
		memberDe = agent.memberDe;
		gridX = agent.getPositionX();
		gridY = agent.getPositionY();
		deAgents = agent.deAgents;
		specificDeAgentsMap = agent.specificDeAgentsMap;
		capave = agent.capave;
		distance = agent.distance;
		leaderEvaluation = agent.leaderEvaluation;
		memberEvaluation = agent.memberEvaluation;
		memberListMap = agent.memberListMap;
		executionTimeMap = agent.executionTimeMap;
		mainMemberIds = agent.mainMemberIds;
		subMemberIds = agent.subMemberIds;
		sumQueueSize = agent.sumQueueSize;
		failureOrFinishedmessage = agent.failureOrFinishedmessage;
	}
	
	
	public void readMessage(Message message, int tick){
	}
	
	public void sortAgentsByLeaderDE(List<Agent> agents) {
		Collections.sort(agents, new Comparator<Agent>() {
		    @Override
		    public int compare(Agent a1, Agent a2) {
		        return Double.compare(leaderDe[a2.getMyId()], leaderDe[a1.getMyId()]);
		    }
		});
	}
	
	public void mergeSortAgentBySpecificLeaderDE(List<Agent> agents, int idx) {
		Collections.sort(agents, new Comparator<Agent>() {
		    @Override
		    public int compare(Agent a1, Agent a2) {
		        return Double.compare(specificLeaderDe[idx][a2.getMyId()], specificLeaderDe[idx][a1.getMyId()]);
		    }
		});
	}

	public void sortMessagesByMemberDE(List<Message> messages) {
		Collections.sort(messages, new Comparator<Message>() {
		    @Override
		    public int compare(Message m1, Message m2) {
		        return Double.compare(memberDe[m2.from().getMyId()], memberDe[m1.from().getMyId()]);
		    }
		});
	}
	
	public void resetDe(){
		for(int i=0;i<NUM_OF_AGENT;i++){
			leaderDe[i] = 0.0;
			memberDe[i] = 0.0;
		}
	}
	
	public void setMainAgents(List<Agent> agents){
		int mainMemberSize;
		if(failureOrFinishedmessage == 0){
			mainMemberSize = NUM_OF_AGENT - 1;
		}else{
			double averageQueue = (double)sumQueueSize / failureOrFinishedmessage;
			mainMemberSize = 50 + (int)(NUM_OF_AGENT * (averageQueue / MEMBER_DEPENDABLITY_AGENT_THRESHOLD));
			if(mainMemberSize >= NUM_OF_AGENT){
				mainMemberSize = NUM_OF_AGENT - 1;
			}
		}

		System.out.println(mainMemberSize);
		
		if(mainMemberIds.size() < mainMemberSize){
			List<Agent> allAgents = new ArrayList<Agent>(agents);
			for(int i = 0;i<agents.size();i++){
				Agent agent = agents.get(i);
				if(mainMemberIds.contains(agent.getMyId())){
					allAgents.remove(agent);
				}
			}
			sortAgentsByLeaderDE(allAgents);
			for(int i = 0;i < mainMemberSize-mainMemberIds.size();i++){
				Agent agent = allAgents.get(i);
				mainMemberIds.add(agent.getMyId());
			}
		}else if(mainMemberIds.size() > mainMemberSize){
			List<Agent> allAgents = new ArrayList<Agent>();
			for(int i = 0;i<agents.size();i++){
				Agent agent = agents.get(i);
				if(mainMemberIds.contains(agent.getMyId())){
					allAgents.add(agent);
				}
			}
			sortAgentsByLeaderDE(allAgents);
			for(int i = 0;i < mainMemberIds.size()-mainMemberSize;i++){
				Agent agent = allAgents.get(allAgents.size() - i - 1);
				mainMemberIds.remove(Integer.valueOf(agent.getMyId()));
			}
		}
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
				leaderDe[i] = 0.0;
				memberDe[i] = 0.0;
				for(int j=0;j<3;j++){
					specificLeaderDe[j][i] = 0.0;
				}
			}
		}
		for(int i=0;i<3;i++){
			specificDeAgentsMap.put(i, new ArrayList<Agent>());
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
//				capacity[i] = 3;
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
		sumQueueSize += message.getQueueSize();
		failureOrFinishedmessage++;
		
		List<Agent> executingMember = memberListMap.get(taskId);
		if(executingMember == null) return;
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
	
	protected void notifyFailure(Message message, int tick){
		List<Agent> members = memberListMap.get(message.getSubTask().getTaskId());
		if(members == null) return;
		Agent betrayal = message.from();
		sumQueueSize += SUB_TASK_QUEUE_SIZE;
		failureOrFinishedmessage++;
		members.remove(betrayal);
		executingMembers.remove(betrayal);
		for(int i=0;i<members.size();i++){
			allMessages.add(new Message(COLLAPSE_TEAM, this, members.get(i), message.getSubTask()));
		}
		memberListMap.remove(message.getSubTask().getTaskId());
		
	}
	
	
	//---------------------------------------------------------------------------------------
	
	public void updateDependablity(Message message, boolean success, int executedTime){
		double delta = 0.0;
		SubTask subTask = message.getSubTask();
		if(success){
			delta = (double)subTask.getutility() 
			/  //---------------------------------------------------------------
					(double)(executedTime) ;
			
//				delta = (double)message.getSubTask().getutility() / (this.getdistance(message.from().getMyId()) * 2 + getExcutingTime(message.getSubTask())) ;
//			System.out.println("excutiontime " + getExcutingTime(message.getSubTask()));
//				delta = 1;
//			System.out.println(executedTime);
			
		}else{
			if(message.getType() == REFUSE){
				delta = - (double)message.getSubTask().getutility();
			}
		}
		this.leaderDe[message.from().getMyId()] = 
				(1.0 - LEARNING_RATE/**/) * this.leaderDe[message.from().getMyId()] 
				+ LEARNING_RATE * delta;
		if(subTask != null)
		if(subTask.getType() >= 0) this.specificLeaderDe[subTask.getType()][message.from().getMyId()] 
				= (1.0 - LEARNING_RATE/**/) * this.specificLeaderDe[subTask.getType()][message.from().getMyId()] 
				+ LEARNING_RATE * delta;
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getExcutingTime(SubTask s){
		int et = 0;
		for(int i=0;i<TYPES_OF_RESOURCE;i++){
			int a = (int)Math.ceil((double)s.getcapacity(i) / capacity[i]);
			if(et < a){
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
		leaderDe[id] = Math.max(leaderDe[id]-0.000002, 0.0);
		for(int j=0;j<3;j++){
			Math.max(specificLeaderDe[j][id]-0.000002, 0.0);
		}
		memberDe[id] = Math.max(memberDe[id]-0.000002, 0.0);
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
	
	public double getLeaderDependablity(int id){
		return leaderDe[id];
	}
	
	//---------------------------------------------------------------------------------------
	
	public double getLeaderSpecificDependablity(int num, int id){
		return specificLeaderDe[num][id];
	}
	
	//---------------------------------------------------------------------------------------
	
	public double getMemberDependablity(int id){
		return memberDe[id];
	}
	//---------------------------------------------------------------------------------------
	
	public boolean isReciprocity(){
		return reciprocityAction;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getCapacity(int num){
		return capacity[num];
	}
	
	//---------------------------------------------------------------------------------------
	
	public List<Agent> getDeAgents(){
		return deAgents;
	}
	
	public List<Integer> getMainMemberIds(){
		return mainMemberIds;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void addSpecificDeAgents(int num, Agent agent){
		specificDeAgentsMap.get(num).add(agent);
	}
	
	//---------------------------------------------------------------------------------------
	
	public void clearSpecificDependablityAgent(){
		for(int i=0;i<3;i++){
			specificDeAgentsMap.get(i).clear();
		}
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
