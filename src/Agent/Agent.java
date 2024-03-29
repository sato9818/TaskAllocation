package agent;

import static shared.Constants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import analysis.Analyzer;
import environment.Area;
import environment.Environment;
import message.Message;
import message.MessageType;
import task.SubTask;

public class Agent {
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
	public List<Agent> deAgents = new ArrayList<Agent>();
	//各サブタスクtypeに応じた信頼エージェント
	public HashMap<Integer, List<Agent>> specificDeAgentsMap = new HashMap<Integer, List<Agent>>();
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
	
	final protected Environment environment;
	
	protected int sumQueueSize = 0;
	protected int failureOrFinishedmessage = 0;
	public double leaderDependabilityDegreeThreshold = INITAIL_LEADER_DEPENDABLITY_DEGREE_THRESHOLD;
	public double memberDependabilityDegreeThreshold = INITAIL_MEMBER_DEPENDABLITY_DEGREE_THRESHOLD;
	public double epsilonForLeader = INITIAL_EPSILON;
	public double epsilonForMember = INITIAL_EPSILON;
	int rejectedSubtasks = 0;
	int wastedSubtasks = 0;
	int executedSubtasks = 0;
	int queueSizeSum = 0;
	int failedSubtasks = 0;
	int suceededSubtasks = 0;
	double lastSuccessRate = 0.0;
	int allocatedSubTasks = 0;
	int unallocatedSubtasks = 0;
	double lastAllocatedRate = 0;

	
	
	
	//集計用------------------------------------------------------------------------------------
	public static int refusedTask[] = new int[NUM_OF_AGENT];
	public static int allocatedSubTask[] = new int[NUM_OF_AGENT];
	public static int leaderCount[] = new int[NUM_OF_AGENT];
	public static int memberCount[] = new int[NUM_OF_AGENT];
	public static int ownedSubtask[][] = new int[NUM_OF_AGENT][EXPERIMENTAL_DURATION];
	
	//---------------------------------------------------------------------------------------
	
	Agent(Area area, int x, int y, int id, Environment e){
		environment = e;
		setCapacity();
		initializeDependability();
		gridX = x;
		gridY = y;
		this.area = area;
		myId = id;
	}
	
	//---------------------------------------------------------------------------------------
	
	Agent(Agent agent){
		unallocatedSubtasks = agent.unallocatedSubtasks;
		lastAllocatedRate = agent.lastAllocatedRate;
		allocatedSubTasks = agent.allocatedSubTasks;
		failedSubtasks = agent.failedSubtasks;
		suceededSubtasks = agent.suceededSubtasks;
		epsilonForLeader = agent.epsilonForLeader;
		epsilonForMember = agent.epsilonForMember;
		environment = agent.environment;
		myId = agent.getMyId();
		capacity = agent.capacity;
		area = agent.getArea();
		leaderDe = agent.leaderDe;
		specificLeaderDe = agent.specificLeaderDe;
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
		leaderDependabilityDegreeThreshold = agent.leaderDependabilityDegreeThreshold;
		memberDependabilityDegreeThreshold = agent.memberDependabilityDegreeThreshold;
		executedSubtasks = agent.executedSubtasks;
		queueSizeSum = agent.queueSizeSum;
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
	
	protected void updateThreshold(Agent agent) {
		if(THRESHOLD_FIXED) return;
		if(agent.leaderDependabilityDegreeThreshold > 0.6) {
			memberDependabilityDegreeThreshold = 1.0;
		} else {
			memberDependabilityDegreeThreshold = 0.1;
		}
	}

	public void decreaseEpsilon(int tick){
		// epsilonForLeader = epsilonForLeader * EPSILON_DECAY_RATE;
		// epsilonForMember = epsilonForMember * EPSILON_DECAY_RATE;
		// Reward based epsilon greedy
		if(tick % 100 != 0) return;
		decreaseLeaderEpsilon();
		decreaseMemberEpsilon();
	}
	private void decreaseLeaderEpsilon() {
		if(suceededSubtasks == 0 && failedSubtasks == 0) return;
		double currentSuccessRate = (double) suceededSubtasks / (double) (suceededSubtasks + failedSubtasks);
		if(lastSuccessRate <= currentSuccessRate) {
			epsilonForLeader = epsilonForLeader - 0.0005;
		} else {
			epsilonForLeader = epsilonForLeader + 0.0005;
		}
		lastSuccessRate = currentSuccessRate;
		suceededSubtasks = 0; 
		failedSubtasks = 0;
		epsilonForLeader = Math.max(epsilonForLeader, 0.0);
	}

	private void decreaseMemberEpsilon() {
		if(allocatedSubTasks == 0 && unallocatedSubtasks == 0) return;
		double currentAllocatedRate = (double) allocatedSubTasks / (double) (allocatedSubTasks + unallocatedSubtasks);
		if(lastAllocatedRate <= currentAllocatedRate) {
			epsilonForMember = epsilonForMember - 0.001;
		} else {
			epsilonForMember = epsilonForMember + 0.001;
		}
		lastAllocatedRate = (double) allocatedSubTasks / (double) (allocatedSubTasks + unallocatedSubtasks);
		allocatedSubTasks = 0;
		unallocatedSubtasks = 0;
		epsilonForMember = Math.max(epsilonForMember, 0.0);
	}
	
	public void updateThreshold(int tick) {
		if(tick % 100 == 0) {
			// leaderDependabilityDegreeThreshold = -0.1 * ((double)queueSizeSum / executedSubtasks) + 1.5;
			if((double)queueSizeSum / executedSubtasks >= 1.2) {
				leaderDependabilityDegreeThreshold = 0.1;
			}else if((double)queueSizeSum / executedSubtasks >= 0.5) {
				leaderDependabilityDegreeThreshold = 0.5;
			}else {
				leaderDependabilityDegreeThreshold = 1.0;
			}
			queueSizeSum = 0;
			executedSubtasks = 0;
		}
		// leaderDependabilityDegreeThreshold += wastedSubtasks * LEADER_THRESHOLD_INCREASING_RATE;
		// leaderDependabilityDegreeThreshold = Math.max(leaderDependabilityDegreeThreshold - LEADER_THRESHOLD_DECREASING_RATE, 0);

		// memberDependabilityDegreeThreshold += rejectedSubtasks * MEMBER_THRESHOLD_INCREASING_RATE;
		// memberDependabilityDegreeThreshold = Math.max(memberDependabilityDegreeThreshold - MEMBER_THRESHOLD_DECREASING_RATE, 0);
		rejectedSubtasks = 0;
		wastedSubtasks = 0;
	}
	
	public void sortAgentBySpecificLeaderDE(List<Agent> agents, int idx) {
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
		int dis = (int)Math.ceil((double)manhattan(this.getPositionX(), agent.getPositionX(), this.getPositionY(), agent.getPositionY()) / 100 * 5);
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
				capacity[i] = 1 + environment.rnd.NextInt(5);
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
		suceededSubtasks++;
		
		List<Agent> executingMember = memberListMap.get(taskId);
		if(executingMember == null) return;
		executingMember.remove(member);
		int startTick = executionTimeMap.get(taskId);
		updateDependablity(message,true,tick - startTick);
		Analyzer.executedSubTask[this.getArea().getId()][tick]++;
		Analyzer.allExecutedTime[this.getArea().getId()][tick] += tick - startTick;
		Analyzer.executedTime[this.getArea().getId()][tick] += message.getExecutedTime();
		executedSubtasks++;
		Analyzer.subtaskQueueSizeFromLeaderPerspective[this.getArea().getId()][tick] += message.getQueueSize();
		queueSizeSum += message.getQueueSize();
		if(executingMember.isEmpty()){
			memberListMap.remove(taskId);
			Analyzer.executedTask[this.getArea().getId()][tick]++;
			Analyzer.taskCompletionTime[this.getArea().getId()][tick] += tick - startTick;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	protected void notifyFailure(Message message, int tick){
		List<Agent> members = memberListMap.get(message.getSubTask().getTaskId());
		if(members == null) return;
		Agent betrayal = message.from();
		sumQueueSize += SUB_TASK_QUEUE_SIZE;
		failureOrFinishedmessage++;
		failedSubtasks++;
		members.remove(betrayal);
		executingMembers.remove(betrayal);
		for(int i=0;i<members.size();i++){
			allMessages.add(new Message(MessageType.COLLAPSE_TEAM, this, members.get(i), message.getSubTask()));
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
					(double)(executedTime);
			
//				delta = (double)message.getSubTask().getutility() / (this.getdistance(message.from().getMyId()) * 2 + getExcutingTime(message.getSubTask())) ;
//			System.out.println("excutiontime " + getExcutingTime(message.getSubTask()));
				// delta = 1;
//			System.out.println(executedTime);
			
		}else{
			if(message.getType() == MessageType.REFUSE){
				delta = - (double)message.getSubTask().getutility();
			}
		}
		this.leaderDe[message.from().getMyId()] = 
				(1.0 - LEARNING_RATE) * this.leaderDe[message.from().getMyId()] 
				+ LEARNING_RATE * delta;
		if(subTask != null)
		if(subTask.getType() >= 0) this.specificLeaderDe[subTask.getType()][message.from().getMyId()] 
				= (1.0 - LEARNING_RATE) * this.specificLeaderDe[subTask.getType()][message.from().getMyId()] 
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
}
