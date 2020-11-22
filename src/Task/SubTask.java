package Task;
import Agent.Leader;
import Agent.Member;
import Random.Sfmt;
import Environment.Environment;
import static Constants.Constants.*;

public class SubTask {
	private int reqCapa[] = new int[3/**/];
	private int utility = 0;
	private int taskId;
	private int type;
	Leader from;
	Member to;
	
	//---------------------------------------------------------------------------------------
	
	SubTask(int id){
		//int r = randomInt(rnd);
		int r = Environment.rnd.NextInt(TYPES_OF_RESOURCE);
		int c = 5 + Environment.rnd.NextInt(6);
		if(c >= 8){
			type = r + 1;
		}else{
			type = 0;
		}
		for(int i=0;i<TYPES_OF_RESOURCE;i++){
			if(i == r){
				reqCapa[i] = c;
			}else{
				reqCapa[i] = 0;
			}
		}
		utility += reqCapa[r];	
		this.taskId = id;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getTaskId(){
		return taskId;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getTaskType(){
		return type;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getutility(){
		return utility;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getcapacity(int i){
		return reqCapa[i];
	}
	
	//---------------------------------------------------------------------------------------
	
	public void setfrom(Leader l){
		from = l;
	}
	
	//---------------------------------------------------------------------------------------
	
	public Leader getfrom(){
		return from;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void setto(Member m){
		to = m;
	}
	
	//---------------------------------------------------------------------------------------
	
	public Member getto(){
		return to;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int randomInt(Sfmt rnd){
		int p = rnd.NextInt(10);
		if(p < 7){
			return 0;
		}else if(p < 9){
			return 1;
		}else{
			return 2;
		}
	}
	
	//---------------------------------------------------------------------------------------
}
