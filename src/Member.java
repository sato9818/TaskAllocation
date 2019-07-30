import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Member extends Agent{
	
	//このメンバにきたメッセージの集合
	private List<MessagetoMember> messagestom = new ArrayList<MessagetoMember>();
	//処理が終了したメッセージ
	private MessagetoLeader finishexcution = null; 
	
	int excutiontime;
	
	private boolean active = false;
	
	
	Member(Sfmt rnd){
		super(rnd);
		numofdeagent = 1000;
		threshold = 0.5 * averageOfCapability();
	}
	
	
	
	public MessagetoMember decideMessage(List<MessagetoMember> messages){
		MessagetoMember decide = null;
		for(int i=0;i<messages.size();i++){
			MessagetoMember mtom = messages.get(i);
			Leader lfrom = mtom.getfrom();
			if(deagent.isEmpty()){
				if(decide == null){ 
					decide = mtom;
					active = true;
				}else{
					if(de[lfrom.getmyid()] > de[decide.getfrom().getmyid()]){
						decide = mtom;
						active = true;
					}
				}	
			}else{
				if(deagent.contains(lfrom) && !deagent.contains(decide)){
					decide = mtom;
					active = true;
				}else if(deagent.contains(lfrom) && deagent.contains(decide)){
					if(de[lfrom.getmyid()] > de[decide.getfrom().getmyid()]){
						decide = mtom;
						active = true;
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
			MessagetoLeader mtol = new MessagetoLeader(this, decide.getfrom(), decide.getsubtask(), true, 0); 
			System.out.println("message from Member " + mtol.getfrom().getmyid() + " to Leader " + mtol.getto().getmyid() + " " + mtol.getsubtask() + " " + mtol.memberaccept());
			e.addmessagetoleader(mtol);
		}
		for(int i=0;i<messages.size();i++){
			MessagetoMember mtom = messages.get(i);
			if(!(mtom.equals(decide))){
				MessagetoLeader mtol = new MessagetoLeader(this, mtom.getfrom(), mtom.getsubtask(), false, 0); 
				System.out.println("message from Member " + mtol.getfrom().getmyid() + " to Leader " + mtol.getto().getmyid() + " " + mtol.getsubtask() + " " + mtol.memberaccept());
				e.addmessagetoleader(mtol);
			}
		}
	}
	
	public void taskexcution(MessagetoMember message){
		SubTask s = message.getsubtask();
		
		for(int i=0;i<3/*リソースの種類*/;i++){
			if(s.getcapacity(i) != 0){
				excutiontime =(int)Math.ceil((double)s.getcapacity(i) / capacity[i]);
			}
		}
		System.out.println("Start Excution from Leader " + message.getfrom().getmyid() + " to Member " + message.getto().getmyid() + " " + message.getsubtask()+ " excutiontime : " + excutiontime);
		finishexcution = new MessagetoLeader(this, message.getfrom(), message.getsubtask(), excutiontime, 1);
	}
	
	public int checkexcution(Environment e){
		if(excutiontime <= 0 && finishexcution != null){
			e.addmessagetoleader(finishexcution);
			active = false;
			System.out.println("finishexcution id : " + this.getmyid());
			return 0;
		}
		return 1;
	}
	public void reduceexcutiontime(){
		excutiontime--;
	}
	
	public void getmessage(MessagetoMember message, Environment e){
		if(message.gettype() == 0/*message 受理or拒否*/){
			if(active){
				e.addmessagetoleader(new MessagetoLeader(this, message.getfrom(), message.getsubtask(), false, 0));
			}else{
				messagestom.add(message);
			}
		}else if(message.gettype() == 1/*task allocate*/){
			if(message.taskisallocated()){
				taskexcution(message);
			}
			updatede(message, message.taskisallocated());
		}
		
	}
	
	public void updatede(MessagetoMember message, boolean success){
		double delta = 0.0;
		if(success){
			delta = (double)message.getsubtask().getutility() / (message.getdistance() * 2 + excutiontime);
			//System.out.println("Delta " + delta);
		}
		this.de[message.getfrom().getmyid()] = 
					(1.0 - 0.01/**/) * this.de[message.getfrom().getmyid()] 
					+ 0.01 * delta;
		System.out.println("de[" + message.getfrom().getmyid() + "] = " + this.de[message.getfrom().getmyid()]);
		
	}
	
	public boolean isactive(){
		return active;
	}
	
	public boolean havemessage(){
		return !messagestom.isEmpty();
	}
	
	public void clearall(){
		finishexcution = null;
	}
	public void clearmessages(){
		messagestom.clear();
	}
}