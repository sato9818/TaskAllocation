import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Leader extends Agent{
	//処理すべきサブタスクのリスト
	private List<SubTask> presubtasks = new ArrayList<SubTask>();
	//メッセージを送ったメンバのリスト
	private List<Integer> premembers = new ArrayList<Integer>();
	//受理してくれたメンバーのリスト
	private List<Member> acceptmembers = new ArrayList<Member>();
	//タスクを処理しているメンバのリスト
	private List<Member> membersexcuting = new ArrayList<Member>();
	//サブタスクとそれを処理するメンバのリスト
	private HashMap<SubTask, Member> team = new HashMap<SubTask, Member>();
	//タスクidとタスクを処理しているメンバのリストのmap
	HashMap<Integer, List<Member>> memberListMap = new HashMap<Integer, List<Member>>();
	//タスクidとそれを割り当てた時間のMap
	HashMap<Integer, Integer> executionTimeMap = new HashMap<Integer, Integer>();
	//CNPで送った１００メンバーのリスト
	private List<Member> CNPMembers = new ArrayList<Member>();
	//サブタスクとそれを処理するメンバのリストCNP用
	private HashMap<SubTask, Member> CNPteam = new HashMap<SubTask, Member>();
	//処理すべきサブタスクの集合CNP
	private List<SubTask> subtasksCNP = new ArrayList<SubTask>();
	//null以外のサブタスクに入札したメンバー
	private List<Member> activeMembersCNP = new ArrayList<Member>();
	//リーダーが処理するサブタスク
	private SubTask subtask; 
	int excutiontime;
	private List<MessagetoLeader> rejectMessages = new ArrayList<MessagetoLeader>();
	int countExecutedTask = 0;
	int countExecutedSubTask = 0;
	int sumOfExecutedTime = 0;
	
	
	
	
	//---------------------------------------------------------------------------------------
	Leader(Sfmt rnd){
		super(rnd);
		numofdeagent = 0;
		threshold = 1.5;
	}
	//---------------------------------------------------------------------------------------
	Leader(Member mem){
		super(mem);
		numofdeagent = 0;
		threshold = 1.5;
		toRejectMessages(mem.getmessages());
		memberListMap = mem.memberListMap;
		executionTimeMap = mem.executionTimeMap;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getCountExecutedTask(){
		int c = countExecutedTask;
		countExecutedTask = 0;
		return c;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void toRejectMessages(List<MessagetoMember> messages){
		for(int i=0;i<messages.size();i++){
			MessagetoLeader message = new MessagetoLeader(messages.get(i).getfrom(), messages.get(i).getto(), 3);
			MessagetoLeader mtol = new MessagetoLeader(message.getfrom(), message.getLFrom(), message.getsubtask(), false, 0/*type*/,excutiontime);
			rejectMessages.add(mtol);
		}
	}
	//---------------------------------------------------------------------------------------
	
	public void reduceexcutiontime(){
		excutiontime--;
	}
	//---------------------------------------------------------------------------------------
	public int checkmyexcution(){
		if(excutiontime <= 0){
			return 0;
		}else{
			return 1;
		}
	}
	//---------------------------------------------------------------------------------------
	
	public List<MessagetoLeader> getRejectMessages(){
		return rejectMessages;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void sendRejectMessage(Environment e){
		for(int i=0;i<rejectMessages.size();i++){
			e.addmessagetoleader(rejectMessages.get(i));
		}
		rejectMessages.clear();
	}
	//---------------------------------------------------------------------------------------
	
	public List<Member> sortmemberDE(List<Member> members){
		for (int i = 0; i < members.size() - 1; i++) {
            for (int j = members.size() - 1; j > i; j--) {
                if (de[members.get(j - 1).getmyid()] <= de[members.get(j).getmyid()]) {
                    Collections.swap(members,j-1,j);
                }
            }
        }
		return members;
	}
	//---------------------------------------------------------------------------------------
	public List<Member> sortmemberDistance(List<Member> members){
		for (int i = 0; i < members.size() - 1; i++) {
            for (int j = members.size() - 1; j > i; j--) {
                if (getdistance(members.get(j - 1).getmyid()) > getdistance(members.get(j).getmyid())) {
                    Collections.swap(members,j-1,j);
                }
            }
        }
		return members;
	}
	//---------------------------------------------------------------------------------------
	public List<Member> selectMemberCNP(List<Member> members, Task task){
		List<Member> confmembers = new ArrayList<Member>();
		
		members = sortmemberDistance(members);
		for(int i=0;i<100;i++){
			confmembers.add(members.get(i));
			CNPMembers.add(members.get(i));
		}
		List<SubTask> subtasks = task.getSubTasks();
		this.subtask = selectmysubtask(subtasks);
		subtasks.remove(this.subtask);
		//System.out.println("open subtasks");
		for(int i=0;i<subtasks.size();i++){
			//System.out.println(subtasks.get(i) + " " + subtasks.get(i).getcapacity(0) + " " + subtasks.get(i).getcapacity(1) + " " + subtasks.get(i).getcapacity(2));
			subtasksCNP.add(subtasks.get(i));
		}
		return confmembers;
	}
	
	//---------------------------------------------------------------------------------------
	
	public List<MessagetoMember> selectrandommember(List<Member> realmembers, Task task){
		List<SubTask> subtasks = task.getSubTasks();
//		this.subtask = selectmysubtask(subtasks);
//		subtasks.remove(this.subtask);
		//処理する相手が決まっているサブタスク
		List<SubTask> confsubtask = new ArrayList<SubTask>();
		
		List<MessagetoMember> messages = new ArrayList<MessagetoMember>();
		
		List<Member> members = new ArrayList<Member>(realmembers);
		
		for(int i=0;i<2/*N_d*/;i++){
			
			int cap = 0;
			for(int j=0;j<subtasks.size();j++){
				SubTask subtask = subtasks.get(j);
				//ループ1週目のサブタスクの集合＝処理すべきサブタスクの集合
				if(i == 0){
					presubtasks.add(subtask);
				}
				//サブタスクの要求リソースの位置を同定
				for(int l=0;l<3;l++){
					if(subtask.getcapacity(l) != 0){
						cap = l;
					}
				}
				if(!confsubtask.contains(subtask)){
					Member member = null;
					for(int k=0;k<members.size();k++){
						//activeでないメンバーを選択
						//if(!members.get(k).isactive())
							//サブタスクの処理に必要な能力を持っているか
							if(members.get(k).capacity[cap] != 0){
								member = members.get(k);
								members.remove(member);
								break;
							}
						
						//activeでないメンバーがいなかったら
						if(k == members.size() - 1){
							//System.out.println("There is no active member.");
							return null;
						}
					}
					
					MessagetoMember m = new MessagetoMember(this, member, subtask);
					messages.add(m);		
					premembers.add(member.getmyid());
				}
				//System.out.println("confsubtasksize:" + confsubtask.size());
			}
		}
		return messages;
	}
	
	//---------------------------------------------------------------------------------------
	
	public List<MessagetoMember> selectmember(List<Member> realmembers, Task task){
		
		List<SubTask> subtasks = task.getSubTasks();
//		this.subtask = selectmysubtask(subtasks);
//		subtasks.remove(this.subtask);

		List<SubTask> confsubtask = new ArrayList<SubTask>();
		
		List<MessagetoMember> messages = new ArrayList<MessagetoMember>();
		
		List<Agent> copydeagent = new ArrayList<Agent>(deagent);
		
		List<Member> members = new ArrayList<Member>(realmembers);
		
		Collections.sort(subtasks, new SubUtilityComparator());
		//sort member
		members = sortmemberDE(members);
		
		for(int i=0;i<2/*N_d*/;i++){
			
			int cap = 0;
			for(int j=0;j<subtasks.size();j++){
				SubTask subtask = subtasks.get(j);
				//ループ1週目のサブタスクの集合＝処理すべきサブタスクの集合
				if(i == 0){
					presubtasks.add(subtask);
				}
				//サブタスクの要求リソースの位置を同定
				for(int l=0;l<3;l++){
					if(subtask.getcapacity(l) != 0){
						cap = l;
					}
				}
				if(!confsubtask.contains(subtask)){
					Member member = null;
					//信頼エージェントから選ぶ
					for(int m=0;m<copydeagent.size();m++){
						Agent agent = copydeagent.get(m);
							
						if(agent.capacity[cap] != 0){
							member = members.get(members.indexOf(agent));
							copydeagent.remove(agent);
							members.remove(member);
							confsubtask.add(subtask);
							break;
						}
					}
					if(member == null){
						for(int k=0;k<members.size();k++){
							//activeでないメンバーを選択
							//if(!members.get(k).isactive())
								//サブタスクの処理に必要な能力を持っているか
								if(members.get(k).capacity[cap] != 0){
									member = members.get(k);
									members.remove(member);
									break;
								}
							
							//activeでないメンバーがいなかったら
							if(k == members.size() - 1){
								//System.out.println("There is no active member.");
								return null;
							}
						}
					}
					
					MessagetoMember m = new MessagetoMember(this, member, subtask);
					messages.add(m);		
					premembers.add(member.getmyid());
				}
				//System.out.println("confsubtasksize:" + confsubtask.size());
			}
		}
		return messages;
		//System.out.println("preteamsize:" + presubtasks.size());
	}
	
	//---------------------------------------------------------------------------------------
	
	public SubTask selectmysubtask(List<SubTask> subtasks){
		SubTask decide = null;
		int et = 100;
		for(int i=0;i<subtasks.size();i++){
			SubTask subtask = subtasks.get(i);
			int cap = 0;
			for(int l=0;l<3;l++){
				if(subtask.getcapacity(l) != 0){
					cap = l;
				}
			}
			if(this.capacity[cap] != 0){
				if(setexcutiontime(subtask) < et){
					decide = subtask;
					et = setexcutiontime(subtask);
				}
			}
		}
		if(decide != null)
			excutiontime = et;
		return decide;
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
	
	public void sendmessagetomember(MessagetoMember message, Environment e){
		e.addmessagetomember(message);
	}
	
	//---------------------------------------------------------------------------------------
	
	private void maketeam(MessagetoLeader message){
		SubTask subtask = message.getsubtask();
		Member member = message.getfrom();
		//サブタスクとそれを処理するメンバの組み合わせを作っている
		
			if(team.containsKey(subtask)){
				if(deagent.contains(member) && !deagent.contains(team.get(subtask))){
					team.put(subtask, member);
				}else if(de[member.getmyid()] > de[team.get(subtask).getmyid()] && 
						!(!deagent.contains(member) && 
						deagent.contains(team.get(subtask)))){
					team.put(subtask, member);
				}
			}else{
				team.put(subtask, member);
				presubtasks.remove(subtask);
			}
			
	}
	
	//---------------------------------------------------------------------------------------
	
	public void getmessage(MessagetoLeader message){
		if(message.gettype() == 0/*受理or拒否*/){
			if(message.memberaccept()){
				maketeam(message);
				acceptmembers.add(message.getfrom());
			}
			premembers.remove(Integer.valueOf(message.getfrom().getmyid()));
			if(!message.memberaccept())
				updatede(message, false, 0);
		}else if(message.gettype() == 1/*処理終了*/){
			//System.out.println("from member " + message.getfrom().getmyid());
			finishMemberSubTask(message);
		}else if(message.gettype() == 2/*CNP*/){
			bidSubtask(message);
			CNPMembers.remove(message.getfrom());
		}else if(message.gettype() == 3){
			MessagetoLeader mtol = new MessagetoLeader(message.getfrom(), message.getLFrom(), message.getsubtask(), false, 0/*type*/,excutiontime);
			rejectMessages.add(mtol);
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public void finishMemberSubTask(MessagetoLeader message){
		Member member = message.getfrom();
		SubTask subTask = message.getsubtask();
		int taskId = subTask.getTaskId();
		List<Member> executingMember = memberListMap.get(taskId);
		executingMember.remove(member);
		int startTick = executionTimeMap.get(taskId);
		int endTick = getTick();
		updatede(message,true,endTick - startTick);
		countExecutedSubTask++;
		sumOfExecutedTime += endTick - startTick;
		if(executingMember.isEmpty()){
			memberListMap.remove(taskId);
			countExecutedTask++;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void bidSubtask(MessagetoLeader mtol){
		SubTask subtask = mtol.getsubtask();
		Member member = mtol.getfrom();
		if(subtask != null){
			activeMembersCNP.add(member);
		}else{
			return;
		}
		if(!CNPteam.containsKey(mtol.getsubtask())){
			CNPteam.put(subtask, member);
			subtasksCNP.remove(subtask);
		}else{
			double upsnew = (double)subtask.getutility() / member.getExcutiontime();
			double upsold = (double)subtask.getutility() / CNPteam.get(subtask).getExcutiontime();
			if(upsnew > upsold){
				CNPteam.put(subtask, member);
			}
		}
		/*
		for (SubTask key : CNPteam.keySet()) {
	        System.out.println(key + ":" + "Member " + CNPteam.get(key).getmyid());
	    }
		System.out.println();
		*/
	}
	
	//---------------------------------------------------------------------------------------
	
	public void updatede(MessagetoLeader message, boolean success, int executedTime){
		double delta = 0.0;
		if(success){
//			delta = (double)message.getsubtask().getutility() 
//			/  //---------------------------------------------------------------
//					(executedTime) ;
//			System.out.println(executedTime);
//			delta = (double)message.getdistance() / 10 * 0.3  + message.getExcutionTime() / 10 * 0.7;
			
//			delta = (double)message.getsubtask().getutility() / (message.getdistance() * 2 + message.getExcutionTime()) ;
			//System.out.println("excutiontime " + message.getExcutionTime());
			delta = 1;
			
		}
		this.de[message.getfrom().getmyid()] = 
					(1.0 - 0.01/**/) * this.de[message.getfrom().getmyid()] 
					+ 0.01 * delta;
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public void updatedeRational(MessagetoLeader message, boolean success){
		double delta = 0.0;
		if(success){
			delta = 1.0;
		}
		this.de[message.getfrom().getmyid()] = 
					(1.0 - 0.01/**/) * this.de[message.getfrom().getmyid()] 
					+ 0.01 * delta;
		
	}
	
	//---------------------------------------------------------------------------------------


	public int waitreply(){
		
		if(premembers.isEmpty()){
			//メッセージを送ったメンバー全員から返信がきていたら
			if(presubtasks.isEmpty()){
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
	
	public int waitReplyCNP(){
		if(CNPMembers.isEmpty()){
			if(subtasksCNP.isEmpty()){
				return 0;
			}else{
				return 1;
			}
		}else{
			return 2;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public int checkexcution(){
		//System.out.println("checkexcution");
		if(membersexcuting.isEmpty()){
			//0がexcution成功
			return 0;
		}else{
			//1は継続中
			return 1;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	
	public void failallocate(Environment e){
		for(int i=0;i<acceptmembers.size();i++){
			MessagetoMember message = new MessagetoMember(this, acceptmembers.get(i), null, false);
			e.addmessagetomember(message);
		}
		updateE(0,false);
	}
	
	
	//--------------------------------------------------------------------------------------
	
	public void failallocateCNP(Environment e){
		for(int i=0;i<activeMembersCNP.size();i++){
			MessagetoMember message = new MessagetoMember(this, activeMembersCNP.get(i), null, false);
			e.addmessagetomember(message);
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public int[] taskallocate(Environment e,int tick){
        Iterator<SubTask> subtask_itr = team.keySet().iterator();
        int areaMemberCount[] = new int[9];
        int max = 0;
        int taskId = -1;
        // hasNextを使用して値がある場合はループを継続する
        // keyの取得
        while(subtask_itr.hasNext()) {
            // nextを使用して値を取得す
            SubTask subtask = (SubTask)subtask_itr.next();
            taskId = subtask.getTaskId();
            Member member = team.get(subtask);
            MessagetoMember message = new MessagetoMember(this, member, subtask, true);
            
//            System.out.println(
//            		"Send task from Leader " + 
//            		message.getfrom().getmyid() + 
//            		" to " +message.getto() + " " + message.getto().getmyid() + " " + message.getsubtask() );
			
            e.addmessagetomember(message);
            //this.updatede(new MessagetoLeader(message.getto(),message.getfrom(),message.getsubtask(),0,message.getto().setexcutiontime(message.getsubtask())), true);
            membersexcuting.add(message.getto());
            areaMemberCount[message.getto().getArea()]++;
            acceptmembers.remove(team.get(subtask));
            int time = this.getdistance(member.getmyid()) * 2 + member.setexcutiontime(subtask);
            if(max < time){
            	max = time;
            }
        }
        for(int i=0;i<acceptmembers.size();i++){
			MessagetoMember message = new MessagetoMember(this, acceptmembers.get(i), null, false);
			
//            System.out.println(
//            		"Don't send task from Leader " + 
//            		message.getfrom().getmyid() + 
//            		" to Member " +message.getto().getmyid() + " " + message.getsubtask() );
            
			e.addmessagetomember(message);
		}
        /*
        for(int i=0;i<membersexcuting.size();i++){
			MessagetoMember message = new MessagetoMember(this, acceptmembers.get(i), false);
			e.addmessagetomember(message);
		}
        */
        memberListMap.put(taskId, new ArrayList<Member>(membersexcuting));
        executionTimeMap.put(taskId, tick);
        updateE(0,true);
        return areaMemberCount;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void taskallocateCNP(Environment e){
        Iterator<SubTask> subtask_itr = CNPteam.keySet().iterator();
        // hasNextを使用して値がある場合はループを継続する
        // keyの取得
        while(subtask_itr.hasNext()) {
            // nextを使用して値を取得する
            SubTask subtask = (SubTask)subtask_itr.next();
            MessagetoMember message = new MessagetoMember(this, CNPteam.get(subtask), subtask, true);
            /*
            System.out.println(
            		"Send task from Leader " + 
            		message.getfrom().getmyid() + 
            		" to Member " + message.getto().getmyid() + " " + message.getsubtask() );
			*/
            e.addmessagetomember(message);
            //membersexcuting.add(message.getto());
            activeMembersCNP.remove(CNPteam.get(subtask));
        }
        for(int i=0;i<activeMembersCNP.size();i++){
			MessagetoMember message = new MessagetoMember(this, activeMembersCNP.get(i), null, false);
			/*
            System.out.println(
            		"Don't send task from Leader " + 
            		message.getfrom().getmyid() + 
            		" to Member " +message.getto().getmyid() + " " + message.getsubtask() );
            */
			e.addmessagetomember(message);
		}
        
	}
	
	
	//---------------------------------------------------------------------------------------
	
	public void clearall(){
		presubtasks.clear();
		premembers.clear();
		membersexcuting.clear();
		acceptmembers.clear();
		team.clear();
	}
	
	//---------------------------------------------------------------------------------------
	
	public void clearallCNP(){
		CNPMembers.clear();
		CNPteam.clear();
		subtasksCNP.clear();
		activeMembersCNP.clear();
	}
}
