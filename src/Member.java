import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Member extends Agent{
	//このメンバが受理したメッセージ
	private MessagetoMember decide = null;
	//このメンバにきたメッセージの集合
	private List<MessagetoMember> messagestom = new ArrayList<MessagetoMember>();
	//処理が終了したメッセージ
	private MessagetoLeader finishexcution = null; 
	
	int excutiontime;
	
	boolean active = false;
	
	Member(Sfmt rnd){
		super(rnd);
	}
	
	
	
	public void decideSubtask(){
		for(int i=0;i<messagestom.size();i++){
			MessagetoMember mtom = messagestom.get(i);
			Leader lfrom = mtom.getfrom();
			if(deagent.isEmpty()){
				if(decide == null){ 
					active = true;
					decide = mtom;
				}else{
					if(de[lfrom.getmyid()] > de[decide.getfrom().getmyid()]){
						decide = mtom;
						active = true;
					}
				}	
			}else{
				if(deagent.contains(lfrom)){
					decide = mtom;
					active = true;
				}
			}
		}
		
	}
	
	public void sendreplymessages(Environment e){
		if(!(decide == null)){
			MessagetoLeader mtol = new MessagetoLeader(this, decide.getfrom(), decide.getsubtask(), true, 0); 
			e.addmessagetoleader(mtol);
		}
		for(int i=0;i<messagestom.size();i++){
			MessagetoMember mtom = messagestom.get(i);
			if(!(mtom.equals(decide))){
				MessagetoLeader mtol = new MessagetoLeader(this, mtom.getfrom(), mtom.getsubtask(), false, 0); 
				e.addmessagetoleader(mtol);
			}
		}
	}
	
	public void taskexcution(MessagetoMember message){
		SubTask s = message.getsubtask();
		int max = 0;
		for(int i=0;i<3/*リソースの種類*/;i++){
			if(s.getcapacity(i) != 0){
				excutiontime = s.getcapacity(i) / capacity[i] + 1;
			}
		}
		finishexcution = new MessagetoLeader(this, message.getfrom(), message.getsubtask(), excutiontime, 1);
	}
	
	public int checkexcution(Environment e){
		if(excutiontime <= 0 && finishexcution != null){
			e.addmessagetoleader(finishexcution);
			active = false; 
			return 0;
		}
		return 1;
	}
	public void reduceexcutiontime(){
		excutiontime--;
	}
	
	public void getmessage(MessagetoMember message){
		if(message.gettype() == 0/*message 受理or拒否*/){
			messagestom.add(message);
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
		}
		this.de[message.getfrom().getmyid()] = 
					(1.0 - 0.01/**/) * this.de[message.getfrom().getmyid()] 
					+ 0.01 * delta;
		
	}
	
	public boolean isactive(){
		return active;
	}
	
	public boolean havemessage(){
		return !messagestom.isEmpty();
	}
	public boolean decidemessage(){
		if(decide == null){
			return false;
		}else{
			return true;
		}
	}
	public void clearall(){
		messagestom.clear();
		decide = null;
		finishexcution = null;
	}
}