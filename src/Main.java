import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Random;

import shared.Constants;
import agent.Agent;
import environment.Area;
import environment.Environment;

import static shared.Constants.*;

import random.Seed;
import random.Sfmt;

public class Main {
	static String csv_base_path;
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
		
		Environment[] envs = new Environment[TRIAL_COUNT];
		Thread[] taskAllcationThreads = new Thread[TRIAL_COUNT];
		
		for(int trial = 0;trial<TRIAL_COUNT;trial++){
			envs[trial] = new Environment(Seed._seeds[trial]);
			taskAllcationThreads[trial] = new TaskAllocationThread(envs[trial]);
			taskAllcationThreads[trial].start();
		}
		try {
			for(int trial = 0;trial<TRIAL_COUNT;trial++){
				taskAllcationThreads[trial].join();
			}
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

		long end = System.currentTimeMillis();
		System.out.println((end - start)  + "ms");
		export(envs);		
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
	
	private static PrintWriter initializeCsvForArea(int areaIndex) {
		FileWriter fw = null;
		PrintWriter pw = null;
		try {
			fw = new FileWriter(csv_base_path + "/Area" + areaIndex + ".csv", false);
	        pw = new PrintWriter(new BufferedWriter(fw));
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
		}catch(IOException ex){
			System.out.println(ex);
		}
		return pw;
	}
	
	private static PrintWriter initializeCsvForEnvironment() {
		FileWriter fw = null;
		PrintWriter pw = null;
		try{
			fw = new FileWriter(csv_base_path + "/Environment.csv", false);
            pw = new PrintWriter(new BufferedWriter(fw));
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
        	pw.print("average leader threshold");
        	pw.print(",");
        	pw.print("average member threshold");
        	pw.print(",");
        	for(int type=0;type<TYPES_OF_RESOURCE;type++) {
        		pw.print("average leader dependable agents type " + type);
            	pw.print(",");
			}
        	pw.print("average member dependable agents");
        	pw.print(",");
        	pw.print("rejected task");
        	pw.print(",");
        	pw.print("success rate");
        	pw.println();
		} catch(IOException ex) {
			System.out.println(ex);
		}
		return pw;
	}
	
	
	private static void export(Environment[] envs){
		for(int areaIndex=0;areaIndex<NUM_OF_AREA;areaIndex++){
			PrintWriter pw = initializeCsvForArea(areaIndex);
			int executedTask = 0, wastedTask = 0, messageCount = 0, overflowedTask = 0, rejectedTask = 0, leadersNum = 0, membersNum = 0, reciprocalLeaders = 0, reciprocalMembers = 0;
        	double communicationTime = 0, executedTime = 0, waitingTime = 0, allExecutedTime = 0;
        	int[] allocatedMember = new int[NUM_OF_AREA];
        	

        	

        	for(int tick=0;tick<EXPERIMENTAL_DURATION;tick++){
        		for(int trial = 0;trial<TRIAL_COUNT;trial++) {
            		Environment env = envs[trial];
	        		executedTask += env.executedTask[areaIndex][tick];
	        		wastedTask += env.wastedTask[areaIndex][tick];
	        		communicationTime += divide(env.communicationTime[areaIndex][tick], env.countSentMessages[areaIndex][tick]);
	        		executedTime += divide(env.executedTime[areaIndex][tick] , env.executedSubTask[areaIndex][tick]);
	        		waitingTime += divide(env.waitingTime[areaIndex][tick] , env.executedSubTask[areaIndex][tick]);
	        		allExecutedTime += divide(env.allExecutedTime[areaIndex][tick] , env.executedSubTask[areaIndex][tick]);
	        		messageCount += env.countSentMessages[areaIndex][tick];
	        		overflowedTask += env.overflowedTask[areaIndex][tick];
	        		rejectedTask += env.rejectedTask[areaIndex][tick];
	        		leadersNum += env.countLeaders[areaIndex][tick];
	        		membersNum += env.countMembers[areaIndex][tick];
	        		reciprocalLeaders = env.reciprocalLeaders[areaIndex][tick];
	        		reciprocalMembers = env.reciprocalMembers[areaIndex][tick];
	        		
	        		for(int j=0;j<NUM_OF_AREA;j++){
	        			allocatedMember[j] += env.allocationMemberCount[areaIndex][j][tick];
		        	}
        		}
	        		
				if(tick % 100 == 0){
					pw.print(tick);
		        	pw.print(",");
		        	pw.print(executedTask / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print(wastedTask / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print(communicationTime / 100 / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print(executedTime / 100 / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print(waitingTime / 100 / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print(allExecutedTime / 100 / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print(leadersNum / TRIAL_COUNT / 100);
		        	pw.print(",");
		        	pw.print(membersNum / TRIAL_COUNT / 100);
		        	pw.print(",");
		        	pw.print(reciprocalLeaders / TRIAL_COUNT / 100);
		        	pw.print(",");
		        	pw.print(reciprocalMembers / TRIAL_COUNT / 100);
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
		}
		
		PrintWriter pw = initializeCsvForEnvironment();
        	
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
    	int memberDependableAgents = 0;
    	double avgLeaderThreshold = 0, avgMemberThreshold = 0;
    	int[] leaderDependableAgents = new int[TYPES_OF_RESOURCE];
    	
        for(int tick=0;tick<EXPERIMENTAL_DURATION;tick++){
        	for(int trial = 0;trial<TRIAL_COUNT;trial++) {
        		Environment env = envs[trial];
	        	for(int i=0;i<NUM_OF_AREA;i++){
	        		executedTask += env.executedTask[i][tick];
	        		wastedTask += env.wastedTask[i][tick];
	        		communicationTime += divide(env.communicationTime[i][tick], env.countSentMessages[i][tick]);
	        		executedTime += divide(env.executedTime[i][tick] , env.executedSubTask[i][tick]);
	        		waitingTime += divide(env.waitingTime[i][tick] , env.executedSubTask[i][tick]);
	        		allExecutedTime += divide(env.allExecutedTime[i][tick] , env.executedSubTask[i][tick]);
	        		messageCount += env.countSentMessages[i][tick];
	        		overflowedTask += env.overflowedTask[i][tick];
	        		rejectedTask += env.rejectedTask[i][tick];
	        		avgSubTaskQueue += env.avgSubTaskQueue[i][tick];
		        	avgLeaderThreshold += env.avgLeaderThreshold[i][tick];
		        	avgMemberThreshold += env.avgMemberThreshold[i][tick];
		        	for(int type=0;type<TYPES_OF_RESOURCE;type++) {
	        			leaderDependableAgents[type] = env.leaderDependableAgents[type][i][tick];
	    			}
		        	memberDependableAgents += env.memberDependableAgents[i][tick];
	        		
	        		if(tick % 100 == 0){
	        			leaderCount += env.countLeaders[i][tick];
	        			memberCount += env.countMembers[i][tick];
	        			reciprocityLeaders += env.reciprocalLeaders[i][tick];
	        			reciprocityMembers += env.reciprocalMembers[i][tick];
	        		}
	        	}
        	}
	        	
	        	
	        	
        	if(tick % 100 == 0){
				pw.print(tick);
	        	pw.print(",");
	        	pw.print(executedTask / TRIAL_COUNT);
	        	pw.print(",");
	        	pw.print(wastedTask / TRIAL_COUNT);
	        	pw.print(",");
	        	pw.print(communicationTime / 100 / NUM_OF_AREA / TRIAL_COUNT);
	        	pw.print(",");
	        	pw.print(executedTime / 100 / NUM_OF_AREA / TRIAL_COUNT);
	        	pw.print(",");
	        	pw.print(waitingTime / 100 / NUM_OF_AREA / TRIAL_COUNT);
	        	pw.print(",");
	        	pw.print(allExecutedTime / 100 / NUM_OF_AREA / TRIAL_COUNT);
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
	        	pw.print(avgLeaderThreshold / TRIAL_COUNT / 100);
	        	pw.print(",");
	        	pw.print(avgMemberThreshold / TRIAL_COUNT / 100);
	        	pw.print(",");
	        	for(int type=0;type<TYPES_OF_RESOURCE;type++) {
	        		pw.print(leaderDependableAgents[type] / TRIAL_COUNT / 100);
		        	pw.print(",");
    			}
	        	pw.print(memberDependableAgents / TRIAL_COUNT / 100);
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
            	avgLeaderThreshold = 0;
            	avgMemberThreshold = 0;
            	memberDependableAgents = 0;
            	for(int type=0;type<TYPES_OF_RESOURCE;type++) {
            		leaderDependableAgents[type] = 0;
    			}
			}
        }
        pw.close();
	}
	public static double divide(double x, double y){
		if(y == 0){
			return 0.0;
		}else{
			return x/y;
		}
		 
	}
}
