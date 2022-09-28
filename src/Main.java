import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Random;

import Agent.Agent;
import Constants.Constants;

import static Constants.Constants.*;

import Environment.Area;
import Environment.Environment;
import Random.Seed;
import Random.Sfmt;

public class Main {
	static String csv_base_path;
	static int tick = 0;
	public static void main(String[] args){
		getConstants();
		String mode = args[0];
		if(mode.equals("RECIPROCITY")){
			RECIPROCITY = true;
			csv_base_path = "csv/Reciprocity";
		}else if(mode.equals("RATIONAL")){
			RECIPROCITY = false;
			csv_base_path = "csv/Rational";
		}else if(mode.equals("CNP")){
			CNP_MODE = true;
			csv_base_path = "csv/CNP";
		}
		System.out.println(CNP_MODE);
		long start = System.currentTimeMillis();
		
		for(int trial = 0;trial<TRIAL_COUNT;trial++){
			Environment e = new Environment(Seed._seeds[trial]);
			for(tick=0;tick<EXPERIMENTAL_DURATION;tick++){
				e.run(tick);
//				if(RECIPROCITY == true && (tick == CHANGE_SUBTASKS_TIME || tick == RESTORE_SUBTASKS_TIME || tick == FIRST_MEASURE_TIME)){
//					e.exportAgentConnection(tick);
//					e.exportForCytoscape(tick);
//				}
			} 
//			e.printArea();
//			e.printDeAgent();
//			e.exportAllocatedSubTask(RECIPROCITY);
//			e.exportOwnedSubTask(RECIPROCITY);
			
			
		}
		long end = System.currentTimeMillis();
		System.out.println((end - start)  + "ms");
		export();
		
	}
	
	private static void getConstants() {
		try{
			FileWriter fw = new FileWriter("config/config.txt", false);
	        PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
	        
	        Field[] fields = Constants.class.getDeclaredFields();
	        
	        for(Field field : fields){
	            field.setAccessible(true);
	            try {
					pw.println(field.getName() + " = " + field.get(field));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
	        }
	        pw.close();
		}catch(IOException ex){
			System.out.println(ex);
		}
	}
	
	private static void export(){
		for(int i=0;i<NUM_OF_AREA;i++){
			try{
				FileWriter fw = new FileWriter(csv_base_path + "/Area" + i + ".csv", false);
	            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
	            pw.print("tick");
	        	pw.print(",");
	        	pw.print("executed task");
	        	pw.print(",");
	        	pw.print("wasted task");
	        	pw.print(",");
	        	pw.print("communication time");
	        	pw.print(",");
	        	pw.print("executed time");
	        	pw.print(",");
	        	pw.print("waiting time");
	        	pw.print(",");
	        	pw.print("all executed time");
	        	pw.print(",");
	        	pw.print("leader count");
	        	pw.print(",");
	        	pw.print("member count");
	        	pw.print(",");
	        	pw.print("reciprocity leader count");
	        	pw.print(",");
	        	pw.print("reciprocity member count");
	        	pw.print(",");
	        	pw.print("message count");
	        	pw.print(",");
	        	pw.print("overflowed task");
	        	pw.print(",");
	        	pw.print("rejected task");
	        	for(int j=0;j<NUM_OF_AREA;j++){
	        		pw.print(",");
		        	pw.print("allocate area "+ j +" member");
	        	}
	        	pw.println();
	        	
	        	int executedTask = 0;
	        	int wastedTask = 0;
	        	double communicationTime = 0;
	        	double executedTime = 0;
	        	double waitingTime = 0;
	        	double allExecutedTime = 0;
	        	int messageCount = 0;
	        	int overflowedTask = 0;
	        	int rejectedTask = 0;
//	        	int leaderCount = 0;
//	        	int memberCount = 0;
	        	
	        	int[] allocatedMember = new int[NUM_OF_AREA];
	        	
	        	for(int tick=0;tick<EXPERIMENTAL_DURATION;tick++){
	        		executedTask += Agent.executedTask[i][tick];
	        		wastedTask += Agent.wastedTask[i][tick];
	        		communicationTime += divide(Environment.communicationTime[i][tick], Environment.countSentMessages[i][tick]);
	        		executedTime += divide(Agent.executedTime[i][tick] , Agent.executedSubTask[i][tick]);
	        		waitingTime += divide(Agent.waitingTime[i][tick] , Agent.executedSubTask[i][tick]);
	        		allExecutedTime += divide(Agent.allExecutedTime[i][tick] , Agent.executedSubTask[i][tick]);
	        		messageCount += Environment.countSentMessages[i][tick];
	        		overflowedTask += Area.overflowedTask[i][tick];
	        		rejectedTask += Agent.rejectedTask[i][tick];
	        		for(int j=0;j<NUM_OF_AREA;j++){
	        			allocatedMember[j] += Agent.allocationMemberCount[i][j][tick];
		        	}
	        		
					if(tick % 100 == 0){
						pw.print(tick);
			        	pw.print(",");
			        	pw.print(executedTask / TRIAL_COUNT);
			        	pw.print(",");
			        	pw.print(wastedTask / TRIAL_COUNT);
			        	pw.print(",");
			        	pw.print(communicationTime / 100 );
			        	pw.print(",");
			        	pw.print(executedTime / 100);
			        	pw.print(",");
			        	pw.print(waitingTime / 100);
			        	pw.print(",");
			        	pw.print(allExecutedTime / 100);
			        	pw.print(",");
			        	pw.print(Environment.countLeaders[i][tick] / TRIAL_COUNT);
			        	pw.print(",");
			        	pw.print(Environment.countMembers[i][tick] / TRIAL_COUNT);
			        	pw.print(",");
			        	pw.print(Environment.reciprocityLeaders[i][tick] / TRIAL_COUNT);
			        	pw.print(",");
			        	pw.print(Environment.reciprocityMembers[i][tick] / TRIAL_COUNT);
			        	pw.print(",");
			        	pw.print(messageCount / TRIAL_COUNT);
			        	pw.print(",");
			        	pw.print(overflowedTask / TRIAL_COUNT);
			        	pw.print(",");
			        	pw.print(rejectedTask / TRIAL_COUNT);
			        	for(int j=0;j<NUM_OF_AREA;j++){
			        		pw.print(",");
				        	pw.print(allocatedMember[j] / TRIAL_COUNT);
			        	}
			        	pw.println();
			        	
			        	executedTask = 0;
		        		wastedTask = 0;
		        		communicationTime = 0;
		        		executedTime = 0;
		        		waitingTime = 0;
		        		allExecutedTime = 0;
		        		messageCount = 0;
		        		overflowedTask = 0;
		        		rejectedTask = 0;
		        		for(int j=0;j<NUM_OF_AREA;j++){
		        			allocatedMember[j] = 0;
			        	}
					}
				}
	        	
	        	pw.close();
			}catch(IOException ex){
				System.out.println(ex);
			}
			
		}
		try{
			FileWriter fw = new FileWriter(csv_base_path + "/Environment.csv", false);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
            pw.print("tick");
        	pw.print(",");
        	pw.print("executed task");
        	pw.print(",");
        	pw.print("wasted task");
        	pw.print(",");
        	pw.print("communication time");
        	pw.print(",");
        	pw.print("executed time");
        	pw.print(",");
        	pw.print("waiting time");
        	pw.print(",");
        	pw.print("all executed time");
        	pw.print(",");
        	pw.print("leader count");
        	pw.print(",");
        	pw.print("member count");
        	pw.print(",");
        	pw.print("reciprocity leader count");
        	pw.print(",");
        	pw.print("reciprocity member count");
        	pw.print(",");
        	pw.print("message count");
        	pw.print(",");
        	pw.print("overflowed task");
        	pw.print(",");
        	pw.print("average subtask queue size");
        	pw.print(",");
        	pw.print("rejected task");
        	pw.print(",");
        	pw.print("success rate");
        	pw.println();
        	
            int executedTask = 0;
        	int wastedTask = 0;
        	double communicationTime = 0;
        	double executedTime = 0;
        	double waitingTime = 0;
        	double allExecutedTime = 0;
        	int leaderCount = 0;
        	int memberCount = 0;
        	int reciprocityMembers = 0;
        	int reciprocityLeaders = 0;
        	int messageCount = 0;
        	int overflowedTask = 0;
        	double avgSubTaskQueue = 0;
        	int rejectedTask = 0;
            for(int tick=0;tick<EXPERIMENTAL_DURATION;tick++){
            	for(int i=0;i<NUM_OF_AREA;i++){
            		executedTask += Agent.executedTask[i][tick];
	        		wastedTask += Agent.wastedTask[i][tick];
	        		communicationTime += divide(Environment.communicationTime[i][tick], Environment.countSentMessages[i][tick]);
	        		executedTime += divide(Agent.executedTime[i][tick] , Agent.executedSubTask[i][tick]);
	        		waitingTime += divide(Agent.waitingTime[i][tick] , Agent.executedSubTask[i][tick]);
	        		allExecutedTime += divide(Agent.allExecutedTime[i][tick] , Agent.executedSubTask[i][tick]);
	        		messageCount += Environment.countSentMessages[i][tick];
	        		overflowedTask += Area.overflowedTask[i][tick];
	        		rejectedTask += Agent.rejectedTask[i][tick];
	        		
	        		if(tick % 100 == 0){
	        			leaderCount += Environment.countLeaders[i][tick];
	        			memberCount += Environment.countMembers[i][tick];
	        			reciprocityLeaders += Environment.reciprocityLeaders[i][tick];
	        			reciprocityMembers += Environment.reciprocityMembers[i][tick];
	        		}
	        	}
            	avgSubTaskQueue += Environment.avgSubTaskQueue[tick];
            	if(tick % 100 == 0){
					pw.print(tick);
		        	pw.print(",");
		        	pw.print(executedTask / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print(wastedTask / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print(communicationTime / 100 / NUM_OF_AREA );
		        	pw.print(",");
		        	pw.print(executedTime / 100 / NUM_OF_AREA);
		        	pw.print(",");
		        	pw.print(waitingTime / 100 / NUM_OF_AREA);
		        	pw.print(",");
		        	pw.print(allExecutedTime / 100 / NUM_OF_AREA);
		        	pw.print(",");
		        	pw.print(leaderCount / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print(memberCount / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print(reciprocityLeaders / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print(reciprocityMembers / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print(messageCount / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print(overflowedTask / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print(avgSubTaskQueue / TRIAL_COUNT / 100);
		        	pw.print(",");
		        	pw.print(rejectedTask / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print((double)executedTask / (executedTask + wastedTask + overflowedTask + rejectedTask));
		        	pw.println();
		        	
		        	executedTask = 0;
	        		wastedTask = 0;
	        		communicationTime = 0;
	        		executedTime = 0;
	        		waitingTime = 0;
	        		allExecutedTime = 0;
	        		leaderCount = 0;
	        		memberCount = 0;
	        		reciprocityMembers = 0;
	            	reciprocityLeaders = 0;
	            	messageCount = 0;
	            	overflowedTask = 0;
	            	avgSubTaskQueue = 0;
	            	rejectedTask = 0;
				}
            }
            pw.close();
		}catch(IOException ex){
			System.out.println(ex);
		}
		
	}
	public static double divide(double x, double y){
		if(y == 0){
			return 0.0;
		}else{
			return x/y;
		}
		 
	}
}
