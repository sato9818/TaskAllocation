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
	
	
	//---------------------------------------------------------------------------------------
	public Leader(Sfmt rnd, Area area, int x, int y){
		super(rnd, area, x, y);
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
		
		switch(phase){
		case SELECT_MEMBER:
			if(!area.taskIsEmpty()){//タスクがあれば
				//タスクを取得
				Task task = area.pushTask();
				tookTask ++;
				//候補メンバーに送るメッセージを決める(e-greedy法)
				int p = eGreedy(rnd);
				List<Message> messages = null;
				if(p == 0){
					messages = selectMember(agents, task, false);
				}else if(p == 1){
					//System.out.println("Epsilon");
					messages = selectMember(agents, task, true);
				}
				
				//メッセージを送る
				for(int j=0;j<messages.size();j++){
					allMessages.add(messages.get(j));
				}
				phase = WAIT_MEMBER;
			}else{
				updateRoleEvaluation(false);
			}
			break;
		case WAIT_MEMBER:
			//メッセージの返信を見て
			if(waitReply() == 0){
				//全部返信がきててアロケーションできるなら
				taskAllocate(tick);
				phase = SELECT_MEMBER;
				clearall();
			}else if(waitReply() == 1){
				//全部返信がきててアロケーションできないなら
				failAllocate();
				wastedTask[this.getArea().getId()][tick]++;
				phase = SELECT_MEMBER;
				clearall();
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
	
	public List<Agent> sortmemberDE(List<Agent> agents){
		for (int i = 0; i < agents.size() - 1; i++) {
            for (int j = agents.size() - 1; j > i; j--) {
                if (de[agents.get(j - 1).getMyId()] <= de[agents.get(j).getMyId()]) {
                    Collections.swap(agents,j-1,j);
                }
            }
        }
		return agents;
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
	
	public List<Message> selectMember(List<Agent> agents, Task task, boolean random){
		
		List<SubTask> subtasks = task.getSubTasks();
		List<SubTask> confsubtask = new ArrayList<SubTask>();
		List<Message> messages = new ArrayList<Message>();
		
		List<Agent> copyDeAgents = new ArrayList<Agent>(deagent);
		List<Agent> copyAgents = new ArrayList<Agent>(agents);
		copyAgents.remove(this);
		if(random){
			copyDeAgents.clear();
		}else{
			Collections.sort(subtasks, new SubUtilityComparator());
			copyAgents = sortmemberDE(copyAgents);
			copyDeAgents = sortmemberDE(copyDeAgents);
		}
		
		for(int i=0;i<SOLICITATION_REDUNDANCY;i++){
			for(int j=0;j<subtasks.size();j++){
				SubTask subtask = subtasks.get(j);
				if(confsubtask.contains(subtask)){
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
					confsubtask.add(subtask);
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
		//System.out.println("preteamsize:" + presubtasks.size());
	}
	
	
	//---------------------------------------------------------------------------------------
	
	public int setexcutiontime(SubTask s){
		int et = 0;
		for(int i=0;i<3/*リソースの種類*/;i++){
			if(s.getcapacity(i) != 0){
				et =(int)Math.ceil((double)s.getcapacity(i) / capacity[i]);
			}
		}
		return et;
	}
	
	
	//---------------------------------------------------------------------------------------
	
	private void makeTeam(Message message){
		SubTask subtask = message.getSubTask();
		Agent member = message.from();
		//サブタスクとそれを処理するメンバの組み合わせを作っている
		
			if(team.containsKey(subtask)){
				if(deagent.contains(member) && !deagent.contains(team.get(subtask))){
					team.put(subtask, member);
				}else if(de[member.getMyId()] > de[team.get(subtask).getMyId()] && 
						!(!deagent.contains(member) && 
						deagent.contains(team.get(subtask)))){
					team.put(subtask, member);
				}
			}else{
				team.put(subtask, member);
				subTasksList.remove(subtask);
			}
			
	}
	
	//---------------------------------------------------------------------------------------
	@Override
	public void readMessage(Message message, int tick){
//		if(getMyId() == 280) System.out.println(message);
		switch(message.getType()){
		case SOLICITATION:
			Message m = new Message(ACCEPTANCE, this, message.from(), message.getSubTask(), false);
			allMessages.add(m);
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
			Message message = new Message(ALLOCATION, this, acceptMembers.get(i), null);
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
			Message message = new Message(ALLOCATION, this, acceptMembers.get(i), null);
			allMessages.add(message);
		}
  
        memberListMap.put(taskId, new ArrayList<Agent>(membersExcuting));
        executionTimeMap.put(taskId, tick);
        updateRoleEvaluation(true);
	}
	
	//---------------------------------------------------------------------------------------
	
	public void updatedeagent(){
		if(LEADER_DEPENDABLITY_AGENT_THRESHOLD < deagent.size()){
			deagent = sortagent(deagent);
			List<Agent> buf = new ArrayList<Agent>();
			for(int i=0;i<LEADER_DEPENDABLITY_AGENT_THRESHOLD;i++){
				buf.add(deagent.get(i));
			}
			deagent = buf;
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
