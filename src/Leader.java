import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.sun.javafx.collections.MappingChange.Map;

public class Leader extends Agent{
	//処理すべきサブタスクのリスト
	List<SubTask> presubtasks = new ArrayList<SubTask>();
	//メッセージを送ったメンバのリスト
	List<Member> premembers = new ArrayList<Member>();
	
	//タスクを処理しているメンバのリスト
	List<Member> membersexcuting = new ArrayList<Member>();
	//サブタスクとそれを処理するメンバのリスト
	HashMap<SubTask, Member> team = new HashMap<SubTask, Member>();
	
	
	Task task = null;
	
	Leader(Sfmt rnd){
		super(rnd);
	}
	
	
	
	public void selectmember(List<Member> members, Environment e){
		
		List<SubTask> subtasks = task.getSubTasks();
		List<SubTask> confsubtask = new ArrayList<SubTask>();
		
		Collections.sort(subtasks, new SubUtilityComparator());
		//sort member
		Collections.sort(members, new Comparator<Member>(){
			public int compare(Member m1, Member m2) {
				 return de[m1.getmyid()] > de[m2.getmyid()] ? -1 : 1;
			}
		});
		
		for(int i=0;i<2/*N_d*/;i++){
			int k = 0;
			int subtasksize = subtasks.size();
			for(int j=0;j<subtasksize;j++){
				SubTask subtask = subtasks.get(j);
				if(i == 0){
					presubtasks.add(subtask);
				}
				if(!confsubtask.contains(subtask)){
					Member member = members.get(k++);
					//System.out.println("put:");
					MessagetoMember m = new MessagetoMember(this, member, subtask);
					//member.setwaittime(getmanhattan(getPositionx(), getPositiony(), member.getPositionx(), member.getPositiony()));
					e.addmessagetomember(m);
					premembers.add(member);
					//presubtasks.add(subtask);
					if(deagent.contains(member)){
						confsubtask.add(subtask);
					}
				}
				//System.out.println("confsubtasksize:" + confsubtask.size());
			}
		}
		//System.out.println("preteamsize:" + presubtasks.size());
	}
	
	public void getmessage(MessagetoLeader message){
		if(message.gettype() == 0){
			SubTask subtask = message.getsubtask();
			Member member = message.getfrom();
			//サブタスクとそれを処理するメンバの組み合わせを作っている
			if(message.taskexcutionpossible){
				if(team.containsKey(subtask)){
					if(deagent.contains(member) && !deagent.contains(team.get(subtask))){
						team.put(subtask, member);
					}else if(de[member.getmyid()] > de[team.get(subtask).getmyid()] && !(!deagent.contains(member) && deagent.contains(team.get(subtask)))){
						team.put(subtask, member);
					}
				}else{
					team.put(subtask, member);
				}
				presubtasks.remove(presubtasks.indexOf(subtask));
			}
			premembers.remove(premembers.indexOf(member));
		}else if(message.gettype() == 1){
			Member member = message.getfrom();
			membersexcuting.remove(membersexcuting.indexOf(member));
		}
		
	}
	public int checkallocation(){
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
		if(membersexcuting.isEmpty()){
			//0がexcution成功
			return 0;
		}else{
			//1は継続中
			return 1;
		}
	}
	public void taskallocate(){
		
	}
	
	public int getmanhattan(int x1, int y1, int x2, int y2){
		return Math.abs(x1-x2) + Math.abs(y1-y2);
	}
	public void setTask(Task t){
		task = t;
	}
}
