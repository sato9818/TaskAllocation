package task;
import environment.Environment;
import random.Sfmt;

import static shared.Constants.*;

import java.util.ArrayList;
import java.util.List;

import agent.Agent;

public class SubTask {
	private int requiredResources[] = new int[TYPES_OF_RESOURCE];
	private int utility;
	private int taskId;
	private int type = -1;
	Agent from;
	Agent to;
	
	//---------------------------------------------------------------------------------------
	
	SubTask(int id, Sfmt rnd){
		
		List<Integer> maxIndexs = new ArrayList<Integer>();
		int maxRequiredResource = 0;
		
//		if(r == 0){
//			basicResource += RESOURCE_FLUCTUATION;
//		}
//		if(r == 1){
//			basicResource -= RESOURCE_FLUCTUATION;
//		}
		for(int i=0;i<TYPES_OF_RESOURCE;i++){
			int requiredResource = BASIC_RESOURCE + rnd.NextInt(ADDITIONAL_RESOURCE + 1);
			
			if(requiredResource > maxRequiredResource){
				maxIndexs.clear();
				maxIndexs.add(i);
				maxRequiredResource = requiredResource;
			}else if(requiredResource == maxRequiredResource){
				maxIndexs.add(i);
			}
			requiredResources[i] = requiredResource;
		}
		
		type = maxIndexs.get(rnd.NextInt(maxIndexs.size()));
		
		if(!FULL_RESOURCE) {
			int resourceIndex = leaveOneResouce(rnd);
			type = resourceIndex;
		}
		
		this.taskId = id;
		setUtility();
		// type = -1;
	}
	
	//---------------------------------------------------------------------------------------
	
	private int leaveOneResouce(Sfmt rnd) {
		int resourceIndex = rnd.NextInt(TYPES_OF_RESOURCE);
		for(int i=0;i<TYPES_OF_RESOURCE;i++){
			if(resourceIndex != i) requiredResources[i] = 0;
		}
		return resourceIndex;
	}
	
	//---------------------------------------------------------------------------------------
	
	private void setUtility() {
		utility = 0;
		for(int requiredResource :requiredResources) {
			utility += requiredResource;
		}
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
		return requiredResources[i];
	}
	
	//---------------------------------------------------------------------------------------
	
	public void setfrom(Agent l){
		from = l;
	}
	
	//---------------------------------------------------------------------------------------
	
	public Agent getfrom(){
		return from;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void setto(Agent m){
		to = m;
	}
	
	//---------------------------------------------------------------------------------------
	
	public Agent getto(){
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