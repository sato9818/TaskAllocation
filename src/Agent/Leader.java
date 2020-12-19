package Agent;
import static Constants.Constants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import Comparator.SubUtilityComparator;
import Environment.Area;
import Environment.Environment;
import Message.Message;
import Random.Sfmt;
import Task.SubTask;
import Task.Task;


public class Leader extends Agent{
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
	
	
	//---------------------------------------------------------------------------------------
	public Leader(Area area, int x, int y){
		super(area, x, y);
	}
	//---------------------------------------------------------------------------------------
	public Leader(Member mem){
		super(mem);
	}
	
	//---------------------------------------------------------------------------------------
	
	public void act(List<Agent> agents, int tick){
//		ld.setTick(tick);
//		roleReject += ld.getRejectMessages().size();
//		ld.sendRejectMessage(e);
		leaderCount[getMyId()]++;
		switch(phase){
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
				phase = EXECUTING_TASK;
			}else{
				updateRoleEvaluation(false);
			}
			break;
		case EXECUTING_TASK:
			if(executeSubTask()){
				phase = WAIT_MEMBER;
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
				taskAllocate(tick);
				phase = SELECT_MEMBER;
				clearall();
				time = 0;
			}else if(judge == 1){
				//全部返信がきててアロケーションできないなら
				failAllocate();
				wastedTask[this.getArea().getId()][tick]++;
				phase = SELECT_MEMBER;
				clearall();
				time = 0;
			}
			
			break;
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public void updateRoleEvaluation(boolean success){
		double delta;
		if(success){
			delta = 1.0;
		}else{
			delta = 0.0;
		}
		leaderEvaluation = (1.0 - LEARNING_RATE) * leaderEvaluation + LEARNING_RATE * delta; 
	}

	//---------------------------------------------------------------------------------------
	
	public List<Member> sortmemberDistance(List<Member> members){
		for (int i = 0; i < members.size() - 1; i++) {
            for (int j = members.size() - 1; j > i; j--) {
                if (getdistance(members.get(j - 1).getMyId()) > getdistance(members.get(j).getMyId())) {
                    Collections.swap(members,j-1,j);
                }
            }
        }
		return members;
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
				Message message = new Message(CNP_SOLICITATION, this, agent, subTasks);
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
				Message message = new Message(SOLICITATION, this, agent, subtask);
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
		copyAgents.remove(this);
		Collections.shuffle(copyAgents, Environment.r);
		copyAgents = sortAgentByLeaderDe(copyAgents);
		
		HashMap<Integer, List<Agent>> specificSortingAgentsMap = new HashMap<Integer, List<Agent>>();
		for(int i=0;i<3;i++){
			List<Agent> copySpecificAgents = sortAgentByLeaderDe(new ArrayList<Agent>(agents));
			copySpecificAgents.remove(this);
			specificSortingAgentsMap.put(i, copySpecificAgents);
		}
		
		//リーダーは自分の処理するサブタスクを取っておく。
		mySubTask = subTasks.get(0);
		subTasks.remove(mySubTask);
		remainingTime = getExcutingTime(mySubTask);
		
		if(this.isReciprocity()){
			List<Agent> copyDeAgents = new ArrayList<Agent>(deAgents);
			copyDeAgents = sortAgentByLeaderDe(copyDeAgents);
			
			HashMap<Integer, List<Agent>> specificSortingDeAgentsMap = new HashMap<Integer, List<Agent>>();
			for(int i=0;i<3;i++){
				List<Agent> copySpecificDeAgents = sortAgentByLeaderDe(new ArrayList<Agent>(specificDeAgentsMap.get(i)));
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
					int type;
					if((type = subtask.getType()) == 0){
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
							if(!copyDeAgents.isEmpty()){
								agent = copyDeAgents.get(0);
								confSubTask.add(subtask);
							}else{
								agent = specificSortingAgentsMap.get(type - 1).get(0);
								if(this.getLeaderSpecificDependablity(type - 1, agent.getMyId()) == 0){
									agent = copyAgents.get(0);
								}
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
					Message message = new Message(SOLICITATION, this, agent, subtask);
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
					Message message = new Message(SOLICITATION, this, agent, subtask);
					messages.add(message);
					preMembers.add(agent.getMyId());
				}
			}
		}
		return messages;
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
		copyAgents = sortAgentByLeaderDe(copyAgents);
		
		//リーダーは自分の処理するサブタスクを取っておく。
		mySubTask = subTasks.get(0);
		subTasks.remove(mySubTask);
		remainingTime = getExcutingTime(mySubTask);
		
		List<Agent> copyDeAgents = new ArrayList<Agent>(deAgents);
		copyDeAgents = sortAgentByLeaderDe(copyDeAgents);

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
				Message message = new Message(SOLICITATION, this, agent, subtask);
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
	@Override
	public void readMessage(Message message, int tick){
		switch(message.getType()){
		case SOLICITATION:
			allMessages.add(new Message(ACCEPTANCE, this, message.from(), message.getSubTask(), false));
			break;
		case CNP_SOLICITATION:
			allMessages.add(new Message(ACCEPTANCE, this, message.from(), null, false));
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
			notifyFailure(message,tick);
//			updateRoleEvaluation(false);
			break;
		case FINISH:
			finishMemberSubTask(message, tick);
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
			Message message = new Message(ALLOCATION, this, acceptMembers.get(i), (SubTask)null);
			allMessages.add(message);
		}
		updateRoleEvaluation(false);
	}
	
	//---------------------------------------------------------------------------------------
	
	public void taskAllocate(int tick){
        Iterator<SubTask> subtask_itr = team.keySet().iterator();
        int taskId = -1;
        // hasNextを使用して値がある場合はループを継続する
        // keyの取得
        while(subtask_itr.hasNext()) {
            // nextを使用して値を取得す
            SubTask subtask = (SubTask)subtask_itr.next();
            taskId = subtask.getTaskId();
            Agent member = team.get(subtask);
            Message message = new Message(ALLOCATION, this, member, subtask);
            allocationMemberCount[this.getArea().getId()][member.getArea().getId()][tick]++; 
            allMessages.add(message);
            //this.updatede(new MessagetoLeader(message.getto(),message.getfrom(),message.getsubtask(),0,message.getto().setexcutiontime(message.getsubtask())), true);
            membersExcuting.add(member);
            acceptMembers.remove(team.get(subtask));
        }
        for(int i=0;i<acceptMembers.size();i++){
			Message message = new Message(ALLOCATION, this, acceptMembers.get(i), (SubTask)null);
			allMessages.add(message);
		}
  
        memberListMap.put(taskId, new ArrayList<Agent>(membersExcuting));
        executionTimeMap.put(taskId, tick);
        updateRoleEvaluation(true);
	}
	
	
	
	
	
	//---------------------------------------------------------------------------------------
	
	public void selectAction(){
		if(LEADER_DEPENDABLITY_AGENT_THRESHOLD <= deAgents.size()){
			reciprocityAction = true;
		}else{
			reciprocityAction = false;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public boolean executeSubTask(){
		remainingTime--;
		if(remainingTime == 0){
			finishSubTask++;
			mySubTask = null;
			return true;
		}else{
			return false;
		}
	}
	
	
	//---------------------------------------------------------------------------------------
	
	public void clearall(){
		subTasksList.clear();
		preMembers.clear();
		membersExcuting.clear();
		acceptMembers.clear();
		team.clear();
	}
	
}
