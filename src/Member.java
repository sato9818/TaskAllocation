import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

public class Member extends Agent{
	
	//このメンバにきたメッセージの集合
	private List<MessagetoMember> messagestom = new ArrayList<MessagetoMember>();
	//処理が終了したメッセージ
	private MessagetoLeader finishexcution = null; 
	
	private MessagetoMember taskmessage = null;
	
	HashMap<Integer, List<Member>> memberListMap = new HashMap<Integer, List<Member>>();
	
	HashMap<Integer, Integer> executionTimeMap = new HashMap<Integer, Integer>();
	
	HashMap<Integer, Integer> allocateTimeMap = new HashMap<Integer, Integer>();
	
	private int excutiontime;
	
	final int thresholdV = 100;
	
	private boolean active = false;
	
	static int countr = 0,counta = 0;
	
	Queue<MessagetoMember> taskqueue = new ArrayDeque<MessagetoMember>();
	
	SubTask excutingtask = null;
	
	int lastSelectTick = 0;
	
	int messageAgreeCount = 0;
	
	int receiveTaskMessageCount = 0;
	
	int numOfMessage = 0;
	
	int countExecutedTask = 0;
	
	//List<Member> deagent = new ArrayList<Member>();
	
	//---------------------------------------------------------------------------------------
	
	Member(Sfmt rnd){
		super(rnd);
		numofdeagent = 0;
		threshold = 0.5 * averageOfCapability();
	}
	
	//---------------------------------------------------------------------------------------
	
	Member(Leader ld){
		super(ld);
		numofdeagent = 0;
		threshold = 0.5 * averageOfCapability();
		memberListMap = ld.memberListMap;
		executionTimeMap = ld.executionTimeMap;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getCountExecutedTask(){
		int c = countExecutedTask;
		countExecutedTask = 0;
		return c;
	}
	
	//---------------------------------------------------------------------------------------
	
	public MessagetoMember decideRandomMessage(List<MessagetoMember> messages, Sfmt rnd){
		MessagetoMember decide = null;
		int messageSize = messages.size();
		int p = (int)(rnd.NextUnif()*messageSize);
		decide = messages.get(p);
		return decide;
	}
	
	//---------------------------------------------------------------------------------------
	
	
	public MessagetoMember decideMessage(List<MessagetoMember> messages){
		MessagetoMember decide = null;
		for(int i=0;i<messages.size();i++){
			MessagetoMember mtom = messages.get(i);
			Leader lfrom = mtom.getfrom();
			if(deagent.isEmpty()){
				if(decide == null){ 
					decide = mtom;
				}else{
					if(de[lfrom.getmyid()] > de[decide.getfrom().getmyid()]){
						decide = mtom;
					}
				}	
			}else{
				if(deagent.contains(lfrom) && !deagent.contains(decide)){
					decide = mtom;
				}else if(deagent.contains(lfrom) && deagent.contains(decide)){
					if(de[lfrom.getmyid()] > de[decide.getfrom().getmyid()]){
						decide = mtom;
					}
				}else if(!deagent.contains(lfrom) && !deagent.contains(decide)){
					decide = null;
				}
			}
		}
		return decide;
	}
	
	//---------------------------------------------------------------------------------------
	
	public List<MessagetoMember> getmessages(){
		return messagestom; 
	}
	
	//---------------------------------------------------------------------------------------
	
	public void sendreplymessages(Environment e, MessagetoMember decide, List<MessagetoMember> messages){
		
		if(decide != null){
			excutiontime = setexcutiontime(decide.getsubtask());
			MessagetoLeader mtol = new MessagetoLeader(decide.getto(), decide.getfrom(), decide.getsubtask(), true, 0/*type*/, excutiontime); 
			mtol.setNewMember(this);
//			System.out.println("send message from Member " + mtol.getfrom().getmyid() + " to Leader " + mtol.getto().getmyid() + " " + mtol.getsubtask() + " " + mtol.memberaccept());
			e.addmessagetoleader(mtol);
		}
		for(int i=0;i<messages.size();i++){
			MessagetoMember mtom = messages.get(i);
			if(!(mtom.equals(decide))){
				MessagetoLeader mtol = new MessagetoLeader(mtom.getto(), mtom.getfrom(), mtom.getsubtask(), false, 0/*type*/,excutiontime);
				mtol.setNewMember(this);
//				System.out.println("Member " + getmyid() + " reject message from " + mtol.getto().getmyid() + " because of active");
				e.addmessagetoleader(mtol);
			}
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void sendreplymessages(Environment e, MessagetoMember mtom, boolean decide){
		
		if(decide){
			excutiontime = setexcutiontime(mtom.getsubtask());
			MessagetoLeader mtol = new MessagetoLeader(mtom.getto(), mtom.getfrom(), mtom.getsubtask(), true, 0/*type*/, excutiontime); 
			mtol.setNewMember(this);
//			System.out.println("send message from Member " + mtol.getfrom().getmyid() + " to Leader " + mtol.getto().getmyid() + " " + mtol.getsubtask() + " " + mtol.memberaccept());
			e.addmessagetoleader(mtol);
		}else{
			MessagetoLeader mtol = new MessagetoLeader(mtom.getto(), mtom.getfrom(), mtom.getsubtask(), false, 0/*type*/,excutiontime);
			mtol.setNewMember(this);
//			System.out.println("Member " + getmyid() + " reject message from " + mtol.getto().getmyid() + " because of active");
			e.addmessagetoleader(mtol);
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void sendreplymessagesCNP(Environment e, SubTask decide, MessagetoMember mtom){
		int et = 0;
		if(decide != null){
			excutiontime = setexcutiontime(decide);
			et = excutiontime;
		}
		MessagetoLeader mtol = new MessagetoLeader(this, mtom.getfrom(), decide, 2/*type*/, et); 
		e.addmessagetoleader(mtol);
	}
	
	//---------------------------------------------------------------------------------------
	
	public int setexcutiontime(SubTask s){
		int et = 0;
		for(int i=0;i<3/*リソースの種類*/;i++){
			int a = (int)Math.ceil((double)s.getcapacity(i) / capacity[i]);
			if(et < a ){
				et = a;
			}
		}
		return et;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void taskexcution(MessagetoMember message){
		//System.out.println("Start Excution from Leader " + message.getfrom().getmyid() + " to Member " + message.getto().getmyid() + " " + message.getsubtask()+ " excutiontime : " + excutiontime);
		excutiontime = setexcutiontime(message.getsubtask());
//		System.out.println("taskexcuting" + this + " " + this.getmyid());
		finishexcution = new MessagetoLeader(message.getto(), message.getfrom(), message.getsubtask(), 1, excutiontime);
	}
	
	//---------------------------------------------------------------------------------------
	
	public SubTask taskexcution(Environment e){
		SubTask st = null;
		if(excutingtask == null){
			if(!taskqueue.isEmpty()){
				MessagetoMember mToM = taskqueue.poll();
				excutingtask = mToM.getsubtask(); 
				excutiontime = setexcutiontime(excutingtask);
				finishexcution = new MessagetoLeader(mToM.getto(), mToM.getfrom(), mToM.getsubtask(), 1, excutiontime);	
				return st;
			}
			return st;
		}else{
			excutiontime--;
			if(excutiontime == 0){
//				System.out.println("sent finish excution message");
				e.addmessagetoleader(finishexcution);
				st = excutingtask;
				excutingtask = null;
				finishexcution = null;
			}
			return st;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public int checkexcution(Environment e){
		if(excutiontime == 0 && finishexcution != null){
			e.addmessagetoleader(finishexcution);
//			System.out.println("send finish message from " + this + " " + this.getmyid() + " to " + finishexcution.getto().getmyid() );
			return 0;
		}
		if(finishexcution == null){
			return 1;
		}
		return 2;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void reduceexcutiontime(){
		excutiontime--;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void getmessage(MessagetoMember message){
		if(message.gettype() == 0/*message 受理or拒否*/){
			messagestom.add(message);
		}else if(message.gettype() == 1/*task allocate*/){
			receiveTaskMessageCount++;
			numOfMessage--;
			taskmessage = message;
			//System.out.println(taskmessage.getsubtask());
			//excutiontime = setexcutiontime(message.getsubtask());
			if(message.taskisallocated()){
				taskqueue.add(message);
				allocateTimeMap.put(message.getsubtask().getTaskId(),getTick());
			}
			
			updateE(1,message.taskisallocated());
			updatede(message, message.taskisallocated());
		}else if(message.gettype() == 2){
			taskmessage = message;
		}else if(message.gettype() == 3/*処理終了*/){
			//System.out.println("from member " + message.getfrom().getmyid());
			finishMemberSubTask(message);
		}
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public void finishMemberSubTask(MessagetoMember message){
		Member member = message.getmfrom();
		SubTask subTask = message.getsubtask();
		int taskId = subTask.getTaskId();
		List<Member> executingMember = memberListMap.get(taskId);
		System.out.println("taskid = " + taskId);
		executingMember.remove(member);
		if(executingMember.isEmpty()){
			memberListMap.remove(taskId);
			countExecutedTask++;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void updatede(MessagetoMember message, boolean success){
		double delta = 0.0;
		if(success){
//			delta = (double)message.getsubtask().getutility() / (message.getdistance() * 2 + excutiontime);
			//System.out.println("excutiontime " + excutiontime);
			delta = 1 / (message.getdistance() * 2 + excutiontime);
//			delta = 1;
		}
		this.de[message.getfrom().getmyid()] = 
					(1.0 - 0.01/**/) * this.de[message.getfrom().getmyid()] 
					+ 0.01 * delta;
		//System.out.println("de[" + message.getfrom().getmyid() + "] = " + this.de[message.getfrom().getmyid()]);
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public void updatedeRational(MessagetoMember message, boolean success){
		double delta = 0.0;
		if(success){
			delta = (double)message.getsubtask().getutility() / (excutiontime);
			//System.out.println("excutiontime " + excutiontime);
		}
		this.de[message.getfrom().getmyid()] = 
					(1.0 - 0.01/**/) * this.de[message.getfrom().getmyid()] 
					+ 0.01 * delta;
		//System.out.println("de[" + message.getfrom().getmyid() + "] = " + this.de[message.getfrom().getmyid()]);
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getExcutiontime(){
		return excutiontime;
	}
	
	//---------------------------------------------------------------------------------------
	
	public boolean isactive(){
		return active;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void setcondition(boolean b){
		active = b;
	}
	
	//---------------------------------------------------------------------------------------

	public boolean havemessage(){
		return !messagestom.isEmpty();
	}
	
	//---------------------------------------------------------------------------------------
	
	public void clearall(){
		finishexcution = null;
		taskmessage = null;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void clearmessages(){
		messagestom.clear();
	}
	
	//---------------------------------------------------------------------------------------
	
	public MessagetoMember gettaskmessage(){
		return taskmessage;
	}
	
	//---------------------------------------------------------------------------------------
}