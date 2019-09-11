import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Leader extends Agent{
	//処理すべきサブタスクのリスト
	private List<SubTask> presubtasks = new ArrayList<SubTask>();
	//メッセージを送ったメンバのリスト
	private List<Member> premembers = new ArrayList<Member>();
	//受理してくれたメンバーのリスト
	private List<Member> acceptmembers = new ArrayList<Member>();
	//タスクを処理しているメンバのリスト
	private List<Member> membersexcuting = new ArrayList<Member>();
	//サブタスクとそれを処理するメンバのリスト
	private HashMap<SubTask, Member> team = new HashMap<SubTask, Member>();
	//CNPで送った１００メンバーのリスト
	private List<Member> CNPMembers = new ArrayList<Member>();
	//サブタスクとそれを処理するメンバのリストCNP用
	private HashMap<SubTask, Member> CNPteam = new HashMap<SubTask, Member>();
	//処理すべきサブタスクの集合CNP
	private List<SubTask> subtasksCNP = new ArrayList<SubTask>();
	//null以外のサブタスクに入札したメンバー
	private List<Member> activeMembersCNP = new ArrayList<Member>();
	
	Leader(Sfmt rnd){
		super(rnd);
		numofdeagent = 1000;
		threshold = 1.5;
	}
	
	public List<Member> sortmember(List<Member> members){
		return sortmemberID(members);
	}

	public List<Member> sortmemberID(List<Member> members){
		for (int i = 0; i < members.size() - 1; i++) {
            for (int j = members.size() - 1; j > i; j--) {
                if (de[members.get(j - 1).getmyid()] < de[members.get(j).getmyid()]) {
                    Collections.swap(members,j-1,j);
                }
            }
        }
		return members;
	}
	
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
	
	public List<Member> selectMemberCNP(List<Member> members, Task task){
		List<Member> confmembers = new ArrayList<Member>();
		
		members = sortmemberDistance(members);
		for(int i=0;i<100;i++){
			confmembers.add(members.get(i));
			CNPMembers.add(members.get(i));
		}
		List<SubTask> subtasks = task.getSubTasks();
		for(int i=0;i<subtasks.size();i++){
			System.out.println(subtasks.get(i));
			subtasksCNP.add(subtasks.get(i));
		}
		return confmembers;
	}
	public List<MessagetoMember> selectrandommember(List<Member> realmembers, Task task){
		List<SubTask> subtasks = task.getSubTasks();
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
						if(!members.get(k).isactive()){
							//サブタスクの処理に必要な能力を持っているか
							if(members.get(k).capacity[cap] != 0){
								member = members.get(k);
								members.remove(member);
								break;
							}
						}
						//activeでないメンバーがいなかったら
						if(k == members.size() - 1){
							System.out.println("There is no active member.");
							return null;
						}
					}
					
					MessagetoMember m = new MessagetoMember(this, member, subtask);
					messages.add(m);		
					premembers.add(member);
				}
				//System.out.println("confsubtasksize:" + confsubtask.size());
			}
		}
		return messages;
	}
	
	public List<MessagetoMember> selectmember(List<Member> realmembers, Task task){
		
		List<SubTask> subtasks = task.getSubTasks();
		//処理する相手が決まっているサブタスク
		List<SubTask> confsubtask = new ArrayList<SubTask>();
		
		List<MessagetoMember> messages = new ArrayList<MessagetoMember>();
		
		List<Agent> copydeagent = new ArrayList<Agent>(deagent);
		
		List<Member> members = new ArrayList<Member>(realmembers);
		
		Collections.sort(subtasks, new SubUtilityComparator());
		//sort member
		members = sortmemberID(members);
		
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
							if(!members.get(k).isactive()){
								//サブタスクの処理に必要な能力を持っているか
								if(members.get(k).capacity[cap] != 0){
									member = members.get(k);
									members.remove(member);
									break;
								}
							}
							//activeでないメンバーがいなかったら
							if(k == members.size() - 1){
								System.out.println("There is no active member.");
								return null;
							}
						}
					}
					
					MessagetoMember m = new MessagetoMember(this, member, subtask);
					messages.add(m);		
					premembers.add(member);
				}
				//System.out.println("confsubtasksize:" + confsubtask.size());
			}
		}
		return messages;
		//System.out.println("preteamsize:" + presubtasks.size());
	}
	
	/*
	public List<MessagetoMember> selectmemberCNP(List<Member> realmembers){
		Map<Agent, Integer> distancetoagent
		//sort distancetoagent
		
		return messages;
		
	}
	*/

	public void sendmessagetomember(MessagetoMember message, Environment e){
		e.addmessagetomember(message);
	}
	
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
	
	public void getmessage(MessagetoLeader message){
		if(message.gettype() == 0/*受理or拒否*/){
			if(message.memberaccept()){
				maketeam(message);
				acceptmembers.add(message.getfrom());
			}
			premembers.remove(message.getfrom());
			updatedeRational(message, message.memberaccept());
		}else if(message.gettype() == 1/*処理終了*/){
			Member member = message.getfrom();
			
			System.out.println(
					"Finish Excution from Member " + 
					message.getfrom().getmyid() + 
					" to leader " + message.getto().getmyid() + " " + message.getsubtask());
			
			membersexcuting.remove(member);
		}else if(message.gettype() == 2/*CNP*/){
			bidSubtask(message);
			CNPMembers.remove(message.getfrom());
		}
		
	}
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
		
	}
	public void updatede(MessagetoLeader message, boolean success){
		double delta = 0.0;
		if(success){
			delta = (double)message.getsubtask().getutility() / (message.getdistance() * 2 + message.getExcutionTime());
			//System.out.println("excutiontime " + message.getExcutionTime());
		}
		this.de[message.getfrom().getmyid()] = 
					(1.0 - 0.01/**/) * this.de[message.getfrom().getmyid()] 
					+ 0.01 * delta;
		
	}
	public void updatedeRational(MessagetoLeader message, boolean success){
		double delta = 0.0;
		if(success){
			delta = 1.0;
		}
		this.de[message.getfrom().getmyid()] = 
					(1.0 - 0.01/**/) * this.de[message.getfrom().getmyid()] 
					+ 0.01 * delta;
		
	}


	public int waitreply(){
		//System.out.println("checkallocation");
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
	
	public void failallocate(Environment e){
		for(int i=0;i<acceptmembers.size();i++){
			MessagetoMember message = new MessagetoMember(this, acceptmembers.get(i), null, false);
			e.addmessagetomember(message);
		}
	}
	public void failallocateCNP(Environment e){
		for(int i=0;i<activeMembersCNP.size();i++){
			MessagetoMember message = new MessagetoMember(this, activeMembersCNP.get(i), null, false);
			e.addmessagetomember(message);
		}
	}
	
	public void taskallocate(Environment e){
        Iterator<SubTask> subtask_itr = team.keySet().iterator();
        // hasNextを使用して値がある場合はループを継続する
        // keyの取得
        while(subtask_itr.hasNext()) {
            // nextを使用して値を取得する
            SubTask subtask = (SubTask)subtask_itr.next();
            MessagetoMember message = new MessagetoMember(this, team.get(subtask), subtask, true);
            
            System.out.println(
            		"Send task from Leader " + 
            		message.getfrom().getmyid() + 
            		" to Member " + message.getto().getmyid() + " " + message.getsubtask() );

            e.addmessagetomember(message);
            membersexcuting.add(message.getto());
            acceptmembers.remove(team.get(subtask));
        }
        for(int i=0;i<acceptmembers.size();i++){
			MessagetoMember message = new MessagetoMember(this, acceptmembers.get(i), null, false);
			
            System.out.println(
            		"Don't send task from Leader " + 
            		message.getfrom().getmyid() + 
            		" to Member " +message.getto().getmyid() + " " + message.getsubtask() );
            
			e.addmessagetomember(message);
		}
        /*
        for(int i=0;i<membersexcuting.size();i++){
			MessagetoMember message = new MessagetoMember(this, acceptmembers.get(i), false);
			e.addmessagetomember(message);
		}
        */
	}
	public void taskallocateCNP(Environment e){
        Iterator<SubTask> subtask_itr = CNPteam.keySet().iterator();
        // hasNextを使用して値がある場合はループを継続する
        // keyの取得
        while(subtask_itr.hasNext()) {
            // nextを使用して値を取得する
            SubTask subtask = (SubTask)subtask_itr.next();
            MessagetoMember message = new MessagetoMember(this, CNPteam.get(subtask), subtask, true);
            
            System.out.println(
            		"Send task from Leader " + 
            		message.getfrom().getmyid() + 
            		" to Member " + message.getto().getmyid() + " " + message.getsubtask() );

            e.addmessagetomember(message);
            //membersexcuting.add(message.getto());
            activeMembersCNP.remove(CNPteam.get(subtask));
        }
        for(int i=0;i<activeMembersCNP.size();i++){
			MessagetoMember message = new MessagetoMember(this, activeMembersCNP.get(i), null, false);
			
            System.out.println(
            		"Don't send task from Leader " + 
            		message.getfrom().getmyid() + 
            		" to Member " +message.getto().getmyid() + " " + message.getsubtask() );
            
			e.addmessagetomember(message);
		}
        
	}
	
	public void clearall(){
		presubtasks.clear();
		premembers.clear();
		membersexcuting.clear();
		acceptmembers.clear();
		team.clear();
	}
	public void clearallCNP(){
		CNPMembers.clear();
		CNPteam.clear();
		subtasksCNP.clear();
		activeMembersCNP.clear();
	}
}
