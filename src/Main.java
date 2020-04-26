import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import Agent.Agent;

import static Constants.Constants.*;

import Environment.Environment;
import Random.Sfmt;

public class Main {
	
	public static void main(String[] args){
		Environment e = new Environment(7);
		for(int tick=0;tick<EXPERIMENTAL_DURATION;tick++){
			e.run(tick);
		}
		export();
		e.printArea();
	}
	private static void export(){
		for(int i=0;i<NUM_OF_AREA;i++){
			try{
				FileWriter fw = new FileWriter("Area" + i + ".csv", false); 
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
	        		
	        		for(int j=0;j<NUM_OF_AREA;j++){
	        			allocatedMember[j] += Agent.allocationMemberCount[i][j][tick];
		        	}
	        		
					if(tick % 100 == 0){
						pw.print(tick);
			        	pw.print(",");
			        	pw.print(executedTask);
			        	pw.print(",");
			        	pw.print(wastedTask);
			        	pw.print(",");
			        	pw.print(communicationTime / 100);
			        	pw.print(",");
			        	pw.print(executedTime / 100);
			        	pw.print(",");
			        	pw.print(waitingTime / 100);
			        	pw.print(",");
			        	pw.print(allExecutedTime / 100);
			        	pw.print(",");
			        	pw.print(Environment.countLeaders[i][tick]);
			        	pw.print(",");
			        	pw.print(Environment.countMembers[i][tick]);
			        	for(int j=0;j<NUM_OF_AREA;j++){
			        		pw.print(",");
				        	pw.print(allocatedMember[j]);
			        	}
			        	pw.println();
			        	
			        	executedTask = 0;
		        		wastedTask = 0;
		        		communicationTime = 0;
		        		executedTime = 0;
		        		waitingTime = 0;
		        		allExecutedTime = 0;
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
			FileWriter fw = new FileWriter("Environment.csv", false); 
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
        	pw.println();
        	
            int executedTask = 0;
        	int wastedTask = 0;
        	double communicationTime = 0;
        	double executedTime = 0;
        	double waitingTime = 0;
        	double allExecutedTime = 0;
        	int leaderCount = 0;
        	int memberCount = 0;
            for(int tick=0;tick<EXPERIMENTAL_DURATION;tick++){
            	for(int i=0;i<NUM_OF_AREA;i++){
            		executedTask += Agent.executedTask[i][tick];
	        		wastedTask += Agent.wastedTask[i][tick];
	        		communicationTime += divide(Environment.communicationTime[i][tick], Environment.countSentMessages[i][tick]);
	        		executedTime += divide(Agent.executedTime[i][tick] , Agent.executedSubTask[i][tick]);
	        		waitingTime += divide(Agent.waitingTime[i][tick] , Agent.executedSubTask[i][tick]);
	        		allExecutedTime += divide(Agent.allExecutedTime[i][tick] , Agent.executedSubTask[i][tick]);
	        		if(tick % 100 == 0){
	        			leaderCount += Environment.countLeaders[i][tick];
	        			memberCount += Environment.countMembers[i][tick];
	        		}
	        	}
            	if(tick % 100 == 0){
					pw.print(tick);
		        	pw.print(",");
		        	pw.print(executedTask);
		        	pw.print(",");
		        	pw.print(wastedTask);
		        	pw.print(",");
		        	pw.print(communicationTime / 100);
		        	pw.print(",");
		        	pw.print(executedTime / 100);
		        	pw.print(",");
		        	pw.print(waitingTime / 100);
		        	pw.print(",");
		        	pw.print(allExecutedTime / 100);
		        	pw.print(",");
		        	pw.print(leaderCount);
		        	pw.print(",");
		        	pw.print(memberCount);
		        	pw.println();
		        	
		        	executedTask = 0;
	        		wastedTask = 0;
	        		communicationTime = 0;
	        		executedTime = 0;
	        		waitingTime = 0;
	        		allExecutedTime = 0;
	        		leaderCount = 0;
	        		memberCount = 0;
	        		
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
