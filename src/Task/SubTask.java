package task;
import agent.Agent;
import agent.Leader;
import agent.Member;
import random.Sfmt;
import environment.Environment;
import static shared.Constants.*;

import java.util.ArrayList;
import java.util.List;

public class SubTask {
	private int reqCapa[] = new int[TYPES_OF_RESOURCE];
	private int utility = 0;
	private int taskId;
	private int type = 0;
	Leader from;
	Member to;
	
	//---------------------------------------------------------------------------------------
	
	SubTask(int id){
		List<Integer> maxIndexs = new ArrayList<Integer>();
		int maxResource = 0;
		int basicResource = BASIC_RESOURCE;
		int r = Environment.rnd.NextInt(3);
		if(r == 0){
			basicResource += RESOURCE_FLUCTUATION;
		}
		if(r == 1){
			basicResource -= RESOURCE_FLUCTUATION;
		}
		for(int i=0;i<TYPES_OF_RESOURCE;i++){
			int c;
			if(r == i){
				c = basicResource + Environment.rnd.NextInt(ADDITIONAL_RESOURCE + 1);
			}else{
				c = 0;
			}
			
			if(c > maxResource){
				maxIndexs.clear();
				maxIndexs.add(i);
				maxResource = c;
			}else if(c == maxResource){
				maxIndexs.add(i);
			}
			reqCapa[i] = c;
			utility += reqCapa[i];
		}
		type = maxIndexs.get(Environment.rnd.NextInt(maxIndexs.size()));
		
		this.taskId = id;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getTaskId(){
		return taskId;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getType(){
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
