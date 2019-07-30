import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


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
	
	
	
	Leader(Sfmt rnd){
		super(rnd);
		numofdeagent = 1;
		threshold = 1.5;
	}
	
	
	
	public List<MessagetoMember> selectmember(List<Member> members, Task task){
		
		List<SubTask> subtasks = task.getSubTasks();
		//処理する相手が決まっているサブタスク
		List<SubTask> confsubtask = new ArrayList<SubTask>();
		
		List<MessagetoMember> messages = new ArrayList<MessagetoMember>();
		
		Collections.sort(subtasks, new SubUtilityComparator());
		//sort member
		Collections.sort(members, new Comparator<Member>(){
			public int compare(Member m1, Member m2) {
				int i =  de[m1.getmyid()] > de[m2.getmyid()] ? -1 : 1;
				System.out.println(i + " " + de[m1.getmyid()] + " " + de[m2.getmyid()]);
				 return i;
			}
		});
		int k = 0;
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
					while(true){
						//activeでないメンバーを選択
						if(!members.get(k).isactive()){
							//サブタスクの処理に必要な能力を持っているか
							if(members.get(k).capacity[cap] != 0){
								member = members.get(k);
								k++;
								break;
							}
						}
						//activeでないメンバーがいなかったら
						if(k == members.size() - 1){
							System.out.println("There is no active member.");
							return null;
						}
						k++;
					}
					
					MessagetoMember m = new MessagetoMember(this, member, subtask);
					messages.add(m);		
					premembers.add(member);
					if(deagent.contains(member)){
						confsubtask.add(subtask);
					}
				}
				//System.out.println("confsubtasksize:" + confsubtask.size());
			}
		}
		return messages;
		//System.out.println("preteamsize:" + presubtasks.size());
	}
	
	public void sendmessagetomember(MessagetoMember message, Environment e){
		e.addmessagetomember(message);
	}
	
	public void maketeam(MessagetoLeader message){
		SubTask subtask = message.getsubtask();
		Member member = message.getfrom();
		//サブタスクとそれを処理するメンバの組み合わせを作っている
		
			if(team.containsKey(subtask)){
				if(deagent.contains(member) && !deagent.contains(team.get(subtask))){
					team.put(subtask, member);
				}else if(de[member.getmyid()] > de[team.get(subtask).getmyid()] && !(!deagent.contains(member) && deagent.contains(team.get(subtask)))){
					team.put(subtask, member);
				}
			}else{
				team.put(subtask, member);
				presubtasks.remove(presubtasks.indexOf(subtask));
			}
			
	}
	
	public void getmessage(MessagetoLeader message){
		if(message.gettype() == 0/*受理or拒否*/){
			if(message.memberaccept()){
				maketeam(message);
				acceptmembers.add(message.getfrom());
			}
			premembers.remove(premembers.indexOf(message.getfrom()));
			updatede(message, message.memberaccept());
		}else if(message.gettype() == 1/*処理終了*/){
			Member member = message.getfrom();
			System.out.println("Finish Excution from Member " + message.getfrom().getmyid() + " to leader " + message.getto().getmyid() + " " + message.getsubtask());
			membersexcuting.remove(membersexcuting.indexOf(member));
		}
		
	}
	
	public void updatede(MessagetoLeader message, boolean success){
		double delta = 0.0;
		if(success){
			delta = (double)message.getsubtask().getutility() / (message.getdistance() * 2);
		}
		this.de[message.getfrom().getmyid()] = 
					(1.0 - 0.01/**/) * this.de[message.getfrom().getmyid()] 
					+ 0.01 * delta;
		
	}
	
	public int checkallocation(){
		return waitreply();
	}



	public int waitreply(){
		//System.out.println("checkallocation");
		if(premembers.isEmpty()){
			if(presubtasks.isEmpty()){
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
			MessagetoMember message = new MessagetoMember(this, acceptmembers.get(i), false);
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
            System.out.println("Allocate from Leader " + message.getfrom().getmyid() + " to Member " +message.getto().getmyid() + " " + message.getsubtask() );
            e.addmessagetomember(message);
            membersexcuting.add(message.getto());
            acceptmembers.remove(acceptmembers.indexOf(team.get(subtask)));
        }
        for(int i=0;i<acceptmembers.size();i++){
			MessagetoMember message = new MessagetoMember(this, acceptmembers.get(i), false);
			e.addmessagetomember(message);
		}
        /*
        for(int i=0;i<membersexcuting.size();i++){
			MessagetoMember message = new MessagetoMember(this, acceptmembers.get(i), false);
			e.addmessagetomember(message);
		}
        */
	}
	
	public void clearall(){
		presubtasks.clear();
		premembers.clear();
		membersexcuting.clear();
		acceptmembers.clear();
		team.clear();
	}
}
