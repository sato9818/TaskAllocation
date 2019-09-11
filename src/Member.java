import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Member extends Agent{
	
	//このメンバにきたメッセージの集合
	private List<MessagetoMember> messagestom = new ArrayList<MessagetoMember>();
	//処理が終了したメッセージ
	private MessagetoLeader finishexcution = null; 
	
	private MessagetoMember taskmessage = null;
	
	private int excutiontime;
	
	private boolean active = false;
	
	static int countr = 0,counta = 0;
	
	//List<Member> deagent = new ArrayList<Member>();
	
	Member(Sfmt rnd){
		super(rnd);
		numofdeagent = 1;
		threshold = 0.5 * averageOfCapability();
	}
	
	public MessagetoMember decideRandomMessage(List<MessagetoMember> messages, Sfmt rnd){
		MessagetoMember decide = null;
		int messageSize = messages.size();
		int p = (int)(rnd.NextUnif()*messageSize);
		decide = messages.get(p);
		return decide;
	}
	
	
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
	
	public List<MessagetoMember> getmessages(){
		return messagestom; 
	}
	
	public void sendreplymessages(Environment e, MessagetoMember decide, List<MessagetoMember> messages){
		
		if(decide != null){
			excutiontime = setexcutiontime(decide.getsubtask());
			MessagetoLeader mtol = new MessagetoLeader(this, decide.getfrom(), decide.getsubtask(), true, 0/*type*/, excutiontime); 
			System.out.println("send message from Member " + mtol.getfrom().getmyid() + " to Leader " + mtol.getto().getmyid() + " " + mtol.getsubtask() + " " + mtol.memberaccept());
			e.addmessagetoleader(mtol);
		}
		for(int i=0;i<messages.size();i++){
			MessagetoMember mtom = messages.get(i);
			if(!(mtom.equals(decide))){
				MessagetoLeader mtol = new MessagetoLeader(this, mtom.getfrom(), mtom.getsubtask(), false, 0/*type*/,excutiontime); 
				
				System.out.println("send message from Member " + mtol.getfrom().getmyid() + " to Leader " + mtol.getto().getmyid() + " " + mtol.getsubtask() + " " + mtol.memberaccept());
				if(this.getPhase() == 0){
					countr++;
					System.out.println("because of regret " + countr);
				}else if(this.getPhase() == 1){
					counta++;
					System.out.println("because of active " + counta);
				}
				e.addmessagetoleader(mtol);
			}
		}
	}
	public void sendreplymessagesCNP(Environment e, SubTask decide, MessagetoMember mtom){
		int et = 0;
		if(decide != null){
			excutiontime = setexcutiontime(decide);
			et = excutiontime;
		}
		MessagetoLeader mtol = new MessagetoLeader(this, mtom.getfrom(), decide, 2/*type*/, et); 
		e.addmessagetoleader(mtol);
	}
	public int setexcutiontime(SubTask s){
		int et = 0;
		for(int i=0;i<3/*リソースの種類*/;i++){
			if(s.getcapacity(i) != 0){
				et =(int)Math.ceil((double)s.getcapacity(i) / capacity[i]);
			}
		}
		return et;
	}
	public void taskexcution(MessagetoMember message){
		System.out.println("Start Excution from Leader " + message.getfrom().getmyid() + " to Member " + message.getto().getmyid() + " " + message.getsubtask()+ " excutiontime : " + excutiontime);
		finishexcution = new MessagetoLeader(this, message.getfrom(), message.getsubtask(), excutiontime, 1);
	}
	
	public int checkexcution(Environment e){
		if(excutiontime == 0 && finishexcution != null){
			//e.addmessagetoleader(finishexcution);
			//System.out.println("finishexcution id : " + this.getmyid());
			return 0;
		}
		if(finishexcution == null){
			return 1;
		}
		return 2;
	}
	public void reduceexcutiontime(){
		excutiontime--;
	}
	
	public void getmessage(MessagetoMember message, Environment e){
		if(message.gettype() == 0/*message 受理or拒否*/){
			messagestom.add(message);
		}else if(message.gettype() == 1/*task allocate*/){
			taskmessage = message;
			updatedeRational(message, message.taskisallocated());
		}else if(message.gettype() == 2){
			taskmessage = message;
		}
		
	}
	
	public void updatede(MessagetoMember message, boolean success){
		double delta = 0.0;
		if(success){
			delta = (double)message.getsubtask().getutility() / (message.getdistance() * 2 + excutiontime);
			//System.out.println("excutiontime " + excutiontime);
		}
		this.de[message.getfrom().getmyid()] = 
					(1.0 - 0.01/**/) * this.de[message.getfrom().getmyid()] 
					+ 0.01 * delta;
		//System.out.println("de[" + message.getfrom().getmyid() + "] = " + this.de[message.getfrom().getmyid()]);
		
	}
	
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
	public int getExcutiontime(){
		return excutiontime;
	}
	
	public boolean isactive(){
		return active;
	}
	public void setcondition(boolean b){
		active = b;
	}

	public boolean havemessage(){
		return !messagestom.isEmpty();
	}
	
	public void clearall(){
		finishexcution = null;
		taskmessage = null;
	}
	public void clearmessages(){
		messagestom.clear();
	}
	public MessagetoMember gettaskmessage(){
		return taskmessage;
	}
}