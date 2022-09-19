package Agent;

import static Constants.Constants.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import Comparator.SubUtilityComparator;
import Environment.Area;
import Environment.Environment;
import Message.Message;
import Message.MessageType;
import Random.Sfmt;
import Task.SubTask;
import Task.Task;

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
	protected MemberState memberState = MemberState.INACTIVE;
	protected LeaderState leaderState = LeaderState.SELECT_MEMBER;
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
	
	public Role role = Role.LEADER;
	
	
	//-----------------------------------リーダー用インスタンス変数-------------------------------------------
	//処理すべきサブタスクのリスト
	private List<SubTask> subTasksList = new ArrayList<SubTask>();
	//メッセージを送ったメンバのIDリスト
	private List<Integer> preMembers = new ArrayList<Integer>();
	//受理してくれたメンバーのリスト
	private List<Agent> acceptMembers = new ArrayList<Agent>();
	//タスクを処理しているメンバのリスト
	private List<Agent> membersExcuting = new ArrayList<Agent>();
	//サブタスクとそれを処理するメンバのリスト
	private HashMap<SubTask, Agent> team = new HashMap<SubTask, Agent>();
	
	int time = 0;
	
	//-----------------------------------メンバ用インスタンス変数-------------------------------------------
	//このメンバにきたメッセージの集合
	private List<Message> solicitationMessages = new ArrayList<Message>();
	//処理が終了したメッセージ
	private Message finishMessage = null; 
	
	HashMap<Integer, Integer> allocateTimeMap = new HashMap<Integer, Integer>();
	
	Queue<Message> messageQueue = new ArrayDeque<Message>();
	
	private List<Message> preSubTasks = new ArrayList<Message>();
	
	private int expectedTasks = 0;
	private int inactiveTime = 0;
	private boolean roleChangable = false;
	
	
	
	
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
	
	public Agent(Area area, int x, int y){
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
	
	public void setRole(Role role){
		this.role = role;
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
	
	public void finishMemberSubTask(Message message){
		Agent member = message.from();
		SubTask subTask = message.getSubTask();
		int taskId = subTask.getTaskId();
		int tick = Environment.tick;
		sumQueueSize += message.getQueueSize();
		failureOrFinishedmessage++;
		
		List<Agent> executingMember = memberListMap.get(taskId);
		if(executingMember == null) return;
		executingMember.remove(member);
		int startTick = executionTimeMap.get(taskId);
		updateDependablity(message,true, tick - startTick);
		executedSubTask[this.getArea().getId()][tick]++;
		allExecutedTime[this.getArea().getId()][tick] += tick - startTick;
		executedTime[this.getArea().getId()][tick] += message.getExecutedTime();
		if(executingMember.isEmpty()){
			memberListMap.remove(taskId);
			executedTask[this.getArea().getId()][tick]++;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	protected void notifyFailure(Message message){
		List<Agent> members = memberListMap.get(message.getSubTask().getTaskId());
		if(members == null) return;
		Agent betrayal = message.from();
		sumQueueSize += SUB_TASK_QUEUE_SIZE;
		failureOrFinishedmessage++;
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
					(double)(executedTime) ;
			
//				delta = (double)message.getSubTask().getutility() / (this.getdistance(message.from().getMyId()) * 2 + getExcutingTime(message.getSubTask())) ;
//			System.out.println("excutiontime " + getExcutingTime(message.getSubTask()));
//				delta = 1;
//			System.out.println(executedTime);
			
		}else{
			if(message.getType() == MessageType.REFUSE){
				delta = - (double)message.getSubTask().getutility();
			}
		}
		this.leaderDe[message.from().getMyId()] = 
				(1.0 - LEARNING_RATE/**/) * this.leaderDe[message.from().getMyId()] 
				+ LEARNING_RATE * delta;
		if(subTask != null)
		if(subTask.getType() > 0) this.specificLeaderDe[subTask.getType()-1][message.from().getMyId()] 
				= (1.0 - LEARNING_RATE/**/) * this.specificLeaderDe[subTask.getType()-1][message.from().getMyId()] 
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
	
	public LeaderState getLeaderState(){
		return leaderState;
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
	
	//---------------------------------------------------------------------------------------
	public void act(){
		int tick = Environment.tick;
		chooseSubTasks();
		memberCount[getMyId()]++;
		ownedSubtask[getMyId()][tick] += messageQueue.size();
		
		switch(memberState){
		case INACTIVE:
			if(!solicitationMessages.isEmpty()){//メッセージが来ていたら
				replyMessages();
				memberState = MemberState.ACTIVE;
				inactiveTime = 0;
				roleChangable = false;
			}else{
				inactiveTime++;
				if(inactiveTime > INACTIVE_THRESHOLD){
					updateMemberEvaluation(false);
					roleChangable = true;
					inactiveTime = 0;
				}
			}
			break;
		case ACTIVE:
			if(!messageQueue.isEmpty()){
				startToExecuteTask();
				memberState = MemberState.EXECUTING_TASK;
			}else{
				if(!solicitationMessages.isEmpty()){
					replyMessages();
				}else{
					if(expectedTasks > 0){
						memberState = MemberState.ACTIVE;
					}else{
						memberState = MemberState.INACTIVE;
						roleChangable = true;
					}
				}
			}
			
			break;
		case EXECUTING_TASK:
			if(mySubTask == null){
				if(!solicitationMessages.isEmpty()){
					replyMessages();
					memberState = MemberState.ACTIVE;
				}else{
					if(!messageQueue.isEmpty()){
						startToExecuteTask();
						memberState = MemberState.EXECUTING_TASK;
					}else{
						if(expectedTasks > 0){
							memberState = MemberState.ACTIVE;
						}else{
							memberState = MemberState.INACTIVE;
							roleChangable = true;
						}
					}
				}
			}else{
				executeTask();
			}
			break;
		}
		
	}
	//---------------------------------------------------------------------------------------
	
	public void startToExecuteTask(){
		int tick = Environment.tick;
		Message allocationMessage = messageQueue.poll();
		mySubTask = allocationMessage.getSubTask(); 
		int et = getExcutingTime(mySubTask);	
		remainingTime = et;
		finishMessage = new Message(MessageType.FINISH, this, allocationMessage.from(), allocationMessage.getSubTask(), et, messageQueue.size());
		waitingTime[this.getArea().getId()][tick] += tick - allocateTimeMap.get(finishMessage.getSubTask().getTaskId());
		executeTask();
	}
	
	//---------------------------------------------------------------------------------------
	
	public void replyMessages(){
		if(CNP_MODE == true){
			for(int i=0;i<solicitationMessages.size();i++){
				Message message = solicitationMessages.get(i);
				SubTask subTask = decideSubTask(message);
				Message acceptedMessage = new Message(MessageType.ACCEPTANCE, this, message.from(), subTask, true);
				allMessages.add(acceptedMessage);
			}
			solicitationMessages.clear();
		}else{
			int p = eGreedy();
			while(!solicitationMessages.isEmpty()){
				boolean decide = true;
				Message message = null;
				if(p == 0){
					message = decideMessage(solicitationMessages, null);
					if(this.isReciprocity()){
						if(!deAgents.contains(message.from())){
							decide = false;
						}
						if(expectedTasks + messageQueue.size() < SUB_TASK_QUEUE_SIZE - deAgents.size()){
							decide = true;
						}
					}
				}else if(p == 1){
					message = decideMessage(solicitationMessages, Environment.rnd);
				}
				
//					if(expectedTasks + taskQueue.size() ){
//						decide = false;
//					}
				if(decide){
					expectedTasks++;
				}

				sendReplyMessages(message, decide);
				solicitationMessages.remove(message);
			}
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	
	public Message decideMessage(List<Message> messages, Sfmt rnd){
		Message message = null;
		if(rnd != null){
			int messageSize = messages.size();
			int p = (int)(rnd.NextUnif() * messageSize);
			message = messages.get(p);
		}else{
			sortMessagesByMemberDE(messages);
			message = messages.get(0);
//				for(int i=0;i<messages.size();i++){
//					System.out.println("member: " + getMyId() + " " + memberDe[messages.get(i).from().getMyId()]);
//				}
		}
		
		return message;
	}
	
	//---------------------------------------------------------------------------------------
	
	public SubTask decideSubTask(Message message){
		List<SubTask> subTasks = message.getSubTasks();
		double max = 0;
		SubTask confSubTask = null; 
		for(int i=0;i<subTasks.size();i++){
			SubTask subTask = subTasks.get(i);
			double utility;
			if(max < (utility = (double)subTask.getutility() / getExcutingTime(subTask))){
				max = utility;
				confSubTask = subTask;
			}
		}
		return confSubTask;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void updateMemberEvaluation(boolean success){
		double delta;
		if(success){
			delta = 1.0;
		}else{
			delta = 0.0;
		}
		memberEvaluation = (1.0 - LEARNING_RATE) * memberEvaluation + LEARNING_RATE * delta; 
	}
		
	//---------------------------------------------------------------------------------------
	
	public void sendReplyMessages(Message solicitationMessage, boolean decide){
		if(decide){
			Message acceptedMessage = new Message(MessageType.ACCEPTANCE, this, solicitationMessage.from(), solicitationMessage.getSubTask(), true); 
			allMessages.add(acceptedMessage);
		}else{
			Message rejectedMessage = new Message(MessageType.ACCEPTANCE, this, solicitationMessage.from(), solicitationMessage.getSubTask(), false); 
			allMessages.add(rejectedMessage);
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void executeTask(){
		remainingTime--;
		if(remainingTime == 0){
			finishSubTask++;
			allMessages.add(finishMessage);
			mySubTask = null;
			finishMessage = null;
		}
	}
	
	//---------------------------------------------------------------------------------------
	public void readMessageAsMember(Message message){
		switch(message.getType()){
		case SOLICITATION:
			solicitationMessages.add(message);
			break;
		case CNP_SOLICITATION:
			solicitationMessages.add(message);
			break;
		case ALLOCATION:
			expectedTasks--;
			if(message.getSubTask() == null){
				updateLeaderEvaluation(false);
				updateDependablity(message, false);
			}else{
				updateLeaderEvaluation(true);
				updateDependablity(message, true);
				preSubTasks.add(message);
				allocatedSubTask[getMyId()]++;
			}
			break;
		case COLLAPSE_TEAM:
			SubTask subTask = message.getSubTask();
			if(mySubTask != null){
				if(subTask.getTaskId() == mySubTask.getTaskId()){
					remainingTime = 0;
					mySubTask = null;
					memberState = MemberState.ACTIVE;
				}
			}else{
				for(Message m : messageQueue){
					if(m.getSubTask().getTaskId() == subTask.getTaskId()){
						messageQueue.remove(m);
						break;
					}
				}
			}
			break;
		case REFUSE:
			updateDependablity(message, false, 0);
			notifyFailure(message);
			break;
		case FINISH:
			finishMemberSubTask(message);
			break;
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	
	private void chooseSubTasks(){
		int tick = Environment.tick;
		sortMessagesByMemberDE(preSubTasks);
		for(int i = 0;i<preSubTasks.size();i++){
			Message message = preSubTasks.get(i);
//				System.out.println("de: " + de[message.from().getMyId()]);
			if(messageQueue.size() >= SUB_TASK_QUEUE_SIZE){
				allMessages.add(new Message(MessageType.REFUSE, this, message.from(), message.getSubTask()));
				
				refusedTask[getMyId()]++;
				rejectedTask[this.getArea().getId()][tick]++;
			}else{
				messageQueue.add(message);
				allocateTimeMap.put(message.getSubTask().getTaskId(), tick);
			}
		}
		preSubTasks.clear();
	}
	
	//---------------------------------------------------------------------------------------
	
	public void updateDependablity(Message message, boolean success){
		double delta = 0.0;
		if(success){
			delta = (double)message.getSubTask().getutility() / (double)(this.getdistance(message.from().getMyId()) * 2 + getExcutingTime(message.getSubTask()));
//				System.out.println("excutiontime " + this.getdistance(message.from().getMyId()));
//				delta = 1.0 / (double)(this.getdistance(message.from().getMyId()) * 2 + getExcutingTime(message.getSubTask()));
//				delta = 1;
		}
		this.memberDe[message.from().getMyId()] = 
					(1.0 - LEARNING_RATE/**/) * this.memberDe[message.from().getMyId()] 
					+ LEARNING_RATE * delta;
		//System.out.println("de[" + message.getfrom().getmyid() + "] = " + this.de[message.getfrom().getmyid()]);
		
	}
	
	
	//---------------------------------------------------------------------------------------
	
	public boolean roleChangable(){
		return roleChangable;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getExcutiontime(){
		return remainingTime;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void selectMemberAttitude(){
		if(MEMBER_DEPENDABLITY_AGENT_THRESHOLD <= deAgents.size()){
			reciprocityAction = true;
		}else{
			reciprocityAction = false;
		}
	}
	
	
	//---------------------------------------------------------------------------------------

	public boolean haveMessages(){
		return !solicitationMessages.isEmpty();
	}
	
	//---------------------------------------------------------------------------------------
	public int getSubTaskQueueSize(){
		return messageQueue.size();
	}
	
	//For leaders

	public void act(List<Agent> agents){
		int tick = Environment.tick;
		leaderCount[getMyId()]++;
		switch(leaderState){
		case SELECT_MEMBER:
			if(!area.taskIsEmpty()){//タスクがあれば
				//タスクを取得
				Task task = area.pushTask();
				//候補メンバーに送るメッセージを決める(e-greedy法)
				int p = eGreedy();
				List<Message> messages = null;
				if(CNP_MODE == true){
					messages = selectCnpMember(agents, task);
				}else{
					if(p == 0){
						messages = selectMember(agents, task);
					}else if(p == 1){
						//System.out.println("Epsilon");
						messages = selectMemberRandomly(agents, task);
					}
				}
				//メッセージを送る
				for(int j=0;j<messages.size();j++){
					allMessages.add(messages.get(j));
				}
				leaderState = LeaderState.WAIT_MEMBER;
			}else{
				updateLeaderEvaluation(false);
			}
			break;
		case WAIT_MEMBER:
			time++;
			if(time == 100){
				System.out.println("leader " + getMyId() + " not active.");
				System.exit(1);
			}
			
			//メッセージの返信を見て
			int judge = waitReply();
			if(judge == 0){
				//全部返信がきててアロケーションできるなら
				taskAllocate();
				leaderState = LeaderState.EXECUTING_TASK;
				clearLeaderInstance();
			}else if(judge == 1){
				//全部返信がきててアロケーションできないなら
				failAllocate();
				wastedTask[this.getArea().getId()][tick]++;
				leaderState = LeaderState.EXECUTING_TASK;
				clearLeaderInstance();
			}else{
				if(mySubTask != null){
					executeSubTask();
				}
			}
			break;
		case EXECUTING_TASK: 
			if(mySubTask == null){
				leaderState = LeaderState.SELECT_MEMBER;
			}else{
				executeSubTask();
			}
			break;
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public void updateLeaderEvaluation(boolean success){
		double delta;
		if(success){
			delta = 1.0;
		}else{
			delta = 0.0;
		}
		leaderEvaluation = (1.0 - LEARNING_RATE) * leaderEvaluation + LEARNING_RATE * delta; 
	}
	
	//---------------------------------------------------------------------------------------
	
	public List<Message> selectCnpMember(List<Agent> agents, Task task){
		List<Message> messages = new ArrayList<Message>();
		List<SubTask> subTasks = task.getSubTasks();
		List<Agent> copyAgents = new ArrayList<Agent>(agents);
		copyAgents.remove(this);
		
		mySubTask = subTasks.get(0);
		subTasks.remove(mySubTask);
		remainingTime = getExcutingTime(mySubTask);
		
		subTasksList = new ArrayList<SubTask>(subTasks);
		
		for(int i=0;i<copyAgents.size();i++){
			Agent agent = copyAgents.get(i);
			if(this.getdistance(agent.getMyId()) <= 2){
				Message message = new Message(MessageType.CNP_SOLICITATION, this, agent, subTasks);
				messages.add(message);
				preMembers.add(agent.getMyId());
			}
		}
		return messages;
	}
	
	//---------------------------------------------------------------------------------------
	
	public List<Message> selectMemberRandomly(List<Agent> agents, Task task){
		List<Message> messages = new ArrayList<Message>();
		List<SubTask> subTasks = task.getSubTasks();
		List<Agent> copyAgents = new ArrayList<Agent>(agents);
		copyAgents.remove(this);
		Collections.shuffle(copyAgents, Environment.r);
		
		mySubTask = subTasks.get(0);
		subTasks.remove(mySubTask);
		remainingTime = getExcutingTime(mySubTask);
		
		for(int i=0;i<SOLICITATION_REDUNDANCY;i++){
			for(int j=0;j<subTasks.size();j++){
				SubTask subtask = subTasks.get(j);
				Agent agent = copyAgents.get(0);
				Message message = new Message(MessageType.SOLICITATION, this, agent, subtask);
				copyAgents.remove(agent);
				messages.add(message);
				preMembers.add(agent.getMyId());
			}
		}
		return messages;
	}
	
	//---------------------------------------------------------------------------------------
	
	public List<Message> selectMember(List<Agent> agents, Task task){
		List<Message> messages = new ArrayList<Message>();
		List<SubTask> subTasks = task.getSubTasks();
		Collections.sort(subTasks, new SubUtilityComparator());
		//信頼エージェントに渡すサブタスク
		List<SubTask> confSubTask = new ArrayList<SubTask>();
		List<Agent> copyAgents = new ArrayList<Agent>(agents);
//		copyAgents.removeAll(executingMembers);
		copyAgents.remove(this);
		Collections.shuffle(copyAgents, Environment.r);
		sortAgentsByLeaderDE(copyAgents);
		
		HashMap<Integer, List<Agent>> specificSortingAgentsMap = new HashMap<Integer, List<Agent>>();
		for(int i=0;i<3;i++){
			List<Agent> copySpecificAgents = new ArrayList<Agent>(copyAgents);
			mergeSortAgentBySpecificLeaderDE(copySpecificAgents, i);
			copySpecificAgents.remove(this);
			specificSortingAgentsMap.put(i, copySpecificAgents);
		}
		
		//リーダーは自分の処理するサブタスクを取っておく。
		mySubTask = subTasks.get(0);
		subTasks.remove(mySubTask);
		remainingTime = getExcutingTime(mySubTask);
		
		if(this.isReciprocity()){
			List<Agent> copyDeAgents = new ArrayList<Agent>(deAgents);
//			copyDeAgents.removeAll(executingMembers);
			sortAgentsByLeaderDE(copyDeAgents);
			HashMap<Integer, List<Agent>> specificSortingDeAgentsMap = new HashMap<Integer, List<Agent>>();
			for(int i=0;i<3;i++){
				List<Agent> copySpecificDeAgents = new ArrayList<Agent>(specificDeAgentsMap.get(i));
				mergeSortAgentBySpecificLeaderDE(copySpecificDeAgents, i);
				specificSortingDeAgentsMap.put(i, copySpecificDeAgents);
			}

			for(int i=0;i<SOLICITATION_REDUNDANCY;i++){
				for(int j=0;j<subTasks.size();j++){
					SubTask subtask = subTasks.get(j);
					if(confSubTask.contains(subtask)){
						continue;
					}
					//ループ1週目のサブタスクの集合＝処理すべきサブタスクの集合
					if(i == 0){
						subTasksList.add(subtask);
					}
					Agent agent = null;
					int type = subtask.getType();

					if(type == 0){
						if(!copyDeAgents.isEmpty()){
							agent = copyDeAgents.get(0);
							copyDeAgents.remove(agent);
							confSubTask.add(subtask);
						}else{
							agent = copyAgents.get(0);
						}
					}else{
						if(!specificSortingDeAgentsMap.get(type - 1).isEmpty()){
							agent = specificSortingDeAgentsMap.get(type - 1).get(0);
							confSubTask.add(subtask);
						}else{
//							if(!copyDeAgents.isEmpty()){
//								agent = copyDeAgents.get(0);
//								confSubTask.add(subtask);
//							}else{
//								agent = specificSortingAgentsMap.get(type - 1).get(0);
//								if(this.getLeaderSpecificDependablity(type - 1, agent.getMyId()) == 0){
//									agent = copyAgents.get(0);
//								}
//							}
							agent = specificSortingAgentsMap.get(type - 1).get(0);
							if(this.getLeaderSpecificDependablity(type - 1, agent.getMyId()) == 0){
								agent = copyAgents.get(0);
							}
						}
					}
					if(copyDeAgents.contains(agent)){
						copyDeAgents.remove(agent);
					}
					for(int k=0;k<3;k++){
						specificSortingAgentsMap.get(k).remove(agent);
						if(specificSortingDeAgentsMap.get(k).contains(agent)){
							specificSortingDeAgentsMap.get(k).remove(agent);
						}
					}
					Message message = new Message(MessageType.SOLICITATION, this, agent, subtask);
					copyAgents.remove(agent);
					messages.add(message);
					preMembers.add(agent.getMyId());
				}
			}
			
		}else{
			for(int i=0;i<SOLICITATION_REDUNDANCY;i++){
				for(int j=0;j<subTasks.size();j++){
					SubTask subtask = subTasks.get(j);
					Agent agent = null;
					int type;
					
					//ループ1週目のサブタスクの集合＝処理すべきサブタスクの集合
					if(i == 0){
						subTasksList.add(subtask);
					}
					
					if((type = subtask.getType()) == 0){
						agent = copyAgents.get(0);
					}else{
						agent = specificSortingAgentsMap.get(type - 1).get(0);
						if(this.getLeaderSpecificDependablity(type - 1, agent.getMyId()) == 0){
							agent = copyAgents.get(0);
						}
					}
					
					copyAgents.remove(agent);
					for(int k=0;k<3;k++){
						specificSortingAgentsMap.get(k).remove(agent);
					}
					Message message = new Message(MessageType.SOLICITATION, this, agent, subtask);
					messages.add(message);
					preMembers.add(agent.getMyId());
				}
			}
		}
		return messages;
	}
	
	//---------------------------------------------------------------------------------------
	
	private SubTask getMySubTask(List<SubTask> subTasks){
		double max = 0;
		SubTask confSubTask = null; 
		for(int i=0;i<subTasks.size();i++){
			SubTask subTask = subTasks.get(i);
			double utility;
			if(max < (utility = (double)subTask.getutility() / this.getExcutingTime(subTask))){
				max = utility;
				confSubTask = subTask;
			}
		}
		return confSubTask;
	}
	
	//---------------------------------------------------------------------------------------
	
	public List<Message> selectMemberPreviously(List<Agent> agents, Task task){
		List<Message> messages = new ArrayList<Message>();
		List<SubTask> subTasks = task.getSubTasks();
		Collections.sort(subTasks, new SubUtilityComparator());
		//信頼エージェントに渡すサブタスク
		List<SubTask> confSubTask = new ArrayList<SubTask>();
		List<Agent> copyAgents = new ArrayList<Agent>(agents);
		copyAgents.remove(this);
		Collections.shuffle(copyAgents, Environment.r);
		sortAgentsByLeaderDE(copyAgents);
		
		//リーダーは自分の処理するサブタスクを取っておく。
		mySubTask = getMySubTask(subTasks);
		subTasks.remove(mySubTask);
		remainingTime = getExcutingTime(mySubTask);
		
		List<Agent> copyDeAgents = new ArrayList<Agent>(deAgents);
		sortAgentsByLeaderDE(copyDeAgents);

		for(int i=0;i<SOLICITATION_REDUNDANCY;i++){
			for(int j=0;j<subTasks.size();j++){
				SubTask subtask = subTasks.get(j);
				if(confSubTask.contains(subtask)){
					continue;
				}
				//ループ1週目のサブタスクの集合＝処理すべきサブタスクの集合
				if(i == 0){
					subTasksList.add(subtask);
				}
				Agent agent = null;
				if(!copyDeAgents.isEmpty()){
					agent = copyDeAgents.get(0);
					copyDeAgents.remove(agent);
					confSubTask.add(subtask);
				}else{
					agent = copyAgents.get(0);
				}
				Message message = new Message(MessageType.SOLICITATION, this, agent, subtask);
				copyAgents.remove(agent);
				messages.add(message);
				preMembers.add(agent.getMyId());
			}
		}
			
		
		return messages;
	}
	//---------------------------------------------------------------------------------------
	
	private void makeTeam(Message message){
		SubTask subtask = message.getSubTask();
		Agent member = message.from();
		//サブタスクとそれを処理するメンバの組み合わせを作っている
		if(CNP_MODE == true){
			if(team.containsKey(subtask)){
				if(team.get(subtask).getExcutingTime(subtask) > member.getExcutingTime(subtask)){
					team.put(subtask, member);
				}
			}else{
				team.put(subtask, member);
				subTasksList.remove(subtask);
			}
		}else{
			if(team.containsKey(subtask)){
				if(deAgents.contains(member) && !deAgents.contains(team.get(subtask))){
					team.put(subtask, member);
				}else if(leaderDe[member.getMyId()] > leaderDe[team.get(subtask).getMyId()] && 
						!(!deAgents.contains(member) && deAgents.contains(team.get(subtask)))){
					team.put(subtask, member);
				}
			}else{
				team.put(subtask, member);
				subTasksList.remove(subtask);
			}
		}
	}
	
	//---------------------------------------------------------------------------------------
	public void readMessageAsLeader(Message message){
		switch(message.getType()){
		case SOLICITATION:
			allMessages.add(new Message(MessageType.ACCEPTANCE, this, message.from(), message.getSubTask(), false));
			break;
		case CNP_SOLICITATION:
			allMessages.add(new Message(MessageType.ACCEPTANCE, this, message.from(), null, false));
			break;
		case ACCEPTANCE:
			if(message.isAccepted()){
				makeTeam(message);
				acceptMembers.add(message.from());
			}else{
				updateDependablity(message, false, 0);
			}
			preMembers.remove(Integer.valueOf(message.from().getMyId()));	
			break;
		case REFUSE:
			updateDependablity(message, false, 0);
			notifyFailure(message);
//			updateRoleEvaluation(false);
			break;
		case FINISH:
			finishMemberSubTask(message);
			break;
		}
	}
	
	
	//---------------------------------------------------------------------------------------


	public int waitReply(){
		if(preMembers.isEmpty()){
			//メッセージを送ったメンバー全員から返信がきていたら
			if(subTasksList.isEmpty()){
				//処理すべきサブタスクが全て処理できるなら
				//0がallocation成功
				return 0;
			}else{
				//1が失敗
				return 1;
			}
		}else{
			//2は継続中
			return 2;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void failAllocate(){
		for(int i=0;i<acceptMembers.size();i++){
			Message message = new Message(MessageType.ALLOCATION, this, acceptMembers.get(i), (SubTask)null);
			allMessages.add(message);
		}
		updateLeaderEvaluation(false);
	}
	
	//---------------------------------------------------------------------------------------
	
	public void taskAllocate(){
		int tick = Environment.tick;
        Iterator<SubTask> subtask_itr = team.keySet().iterator();
        int taskId = -1;
        // hasNextを使用して値がある場合はループを継続する
        // keyの取得
        while(subtask_itr.hasNext()) {
            // nextを使用して値を取得す
            SubTask subtask = (SubTask)subtask_itr.next();
            taskId = subtask.getTaskId();
            Agent member = team.get(subtask);
            Message message = new Message(MessageType.ALLOCATION, this, member, subtask);
            allocationMemberCount[this.getArea().getId()][member.getArea().getId()][tick]++; 
            allMessages.add(message);
            //this.updatede(new MessagetoLeader(message.getto(),message.getfrom(),message.getsubtask(),0,message.getto().setexcutiontime(message.getsubtask())), true);
            membersExcuting.add(member);
            executingMembers.add(member);
            acceptMembers.remove(team.get(subtask));
        }
        for(int i=0;i<acceptMembers.size();i++){
			Message message = new Message(MessageType.ALLOCATION, this, acceptMembers.get(i), (SubTask)null);
			allMessages.add(message);
		}
  
        memberListMap.put(taskId, new ArrayList<Agent>(membersExcuting));
        executionTimeMap.put(taskId, tick);
        updateLeaderEvaluation(true);
	}
	
	//---------------------------------------------------------------------------------------
	
	public void selectLeaderAttitude(boolean reciprocity){
		reciprocityAction = reciprocity;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void selectLeaderAttitude(){
		if(LEADER_DEPENDABLITY_AGENT_THRESHOLD <= deAgents.size()){
			reciprocityAction = true;
		}else{
			reciprocityAction = false;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void executeSubTask(){
		remainingTime--;
		if(remainingTime == 0){
			finishSubTask++;
			mySubTask = null;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void clearLeaderInstance(){
		subTasksList.clear();
		preMembers.clear();
		membersExcuting.clear();
		acceptMembers.clear();
		team.clear();
		time = 0;
	}

}
