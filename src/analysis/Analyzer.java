package analysis;

import static shared.Constants.EXPERIMENTAL_DURATION;
import static shared.Constants.NUM_OF_AGENT;
import static shared.Constants.NUM_OF_AREA;
import static shared.Constants.SUB_TASK_QUEUE_SIZE;
import static shared.Constants.TRIAL_COUNT;
import static shared.Constants.TYPES_OF_RESOURCE;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import agent.Agent;
import environment.Area;

public class Analyzer {
	private final String csvBasePath;
	
	public Analyzer(String csvBasePath) {
		this.csvBasePath = csvBasePath;
	}
	//100tick毎の合計を出力する
	//処理したタスク数
	public static int executedTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	//処理したサブタスク数
	public static int executedSubTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	//チームが組めなかったことによるサブタスク破棄
	public static int wastedTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	//途中で断られてチームが解散になったことによるタスク失敗
	public static int rejectedTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	//メッセージの送信数
	public static double countSentMessages[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	//どのエリアのエージェントがどのエリアのエージェントにタスクを割り当てたか
	public static int allocationMemberCount[][][] = new int[NUM_OF_AREA][NUM_OF_AREA][EXPERIMENTAL_DURATION];
	//環境のタスクキューから溢れたタスク
	public static int overflowedTask[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	
	//100tick毎の平均を出すもの（エリア数でも割る必要あり）
	//サブタスクの処理数で割る値
	//サブタスクキューに入ってから、そのサブタスクの処理を始める前までの時間
	public static double waitingTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	//サブタスクの処理時間
	public static double executedTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	//リーダーから見たサブタスクの処理時間
	public static double allExecutedTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	
	//タスク数で割る
	//タスクの処理時間
	static public double taskCompletionTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	
	//送ったメッセージ数で割る
	//通信時間
	public static double communicationTime[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	
	//メンバ数で割る
	//サブタスクキューの中身の数
	public static double subTaskQueueSum[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	
	//リーダー数で割る
	//リーダーの信頼エージェントの数
	public static double leaderDependableAgents[][][] = new double[TYPES_OF_RESOURCE][NUM_OF_AREA][EXPERIMENTAL_DURATION];
	// リーダーの信頼エージェントを決めるための閾値
	public static double leaderAverageThreshold[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	
	//メンバ数で割る
	//メンバの信頼エージェントの数
	public static double memberDependableAgents[][] = new double[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	
	//100tick毎の平均を出すもの
	public static int countMembers[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int countLeaders[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int reciprocalMembers[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int reciprocalLeaders[][] = new int[NUM_OF_AREA][EXPERIMENTAL_DURATION];
	public static int memberSubtaskQueueHist[][][] = new int[SUB_TASK_QUEUE_SIZE + 1][NUM_OF_AREA][EXPERIMENTAL_DURATION];
	
	public void exportAreaData(){
		for(int i=0;i<NUM_OF_AREA;i++){
			try{
				FileWriter fw = new FileWriter(csvBasePath + "/Area" + i + ".csv", false);
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
	        		executedTask += Analyzer.executedTask[i][tick];
	        		wastedTask += Analyzer.wastedTask[i][tick];
	        		communicationTime += divide(Analyzer.communicationTime[i][tick], Analyzer.countSentMessages[i][tick]);
	        		executedTime += divide(Analyzer.executedTime[i][tick] , Analyzer.executedSubTask[i][tick]);
	        		waitingTime += divide(Analyzer.waitingTime[i][tick] , Analyzer.executedSubTask[i][tick]);
	        		allExecutedTime += divide(Analyzer.allExecutedTime[i][tick] , Analyzer.executedSubTask[i][tick]);
	        		messageCount += Analyzer.countSentMessages[i][tick];
	        		overflowedTask += Analyzer.overflowedTask[i][tick];
	        		rejectedTask += Analyzer.rejectedTask[i][tick];
	        		for(int j=0;j<NUM_OF_AREA;j++){
	        			allocatedMember[j] += Analyzer.allocationMemberCount[i][j][tick];
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
			        	pw.print(Analyzer.countLeaders[i][tick] / TRIAL_COUNT);
			        	pw.print(",");
			        	pw.print(Analyzer.countMembers[i][tick] / TRIAL_COUNT);
			        	pw.print(",");
			        	pw.print(Analyzer.reciprocalLeaders[i][tick] / TRIAL_COUNT);
			        	pw.print(",");
			        	pw.print(Analyzer.reciprocalMembers[i][tick] / TRIAL_COUNT);
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
	}
	public void exportEnvironmentData() {
		try{
			FileWriter fw = new FileWriter(csvBasePath + "/Environment.csv", false);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
            pw.print("Tick");
        	pw.print(",");
        	pw.print("Num of completed task");
        	pw.print(",");
        	pw.print("Num of team formation failure");
        	pw.print(",");
        	pw.print("Average communication time");
        	pw.print(",");
        	pw.print("Average subtask completion time from member perspective");
        	pw.print(",");
        	pw.print("Average time of subtask being in subtask queue");
        	pw.print(",");
        	pw.print("Average subtask completion time from leader perspective");
        	pw.print(",");
        	pw.print("Average task completion time");
        	pw.print(",");
        	pw.print("Num of leaders");
        	pw.print(",");
        	pw.print("Num of members");
        	pw.print(",");
        	pw.print("Num of reciprocal leaders");
        	pw.print(",");
        	pw.print("Num of reciprocal members");
        	pw.print(",");
        	pw.print("Num of sent messages");
        	pw.print(",");
        	pw.print("Num of overflowed task from task queue");
        	pw.print(",");
        	pw.print("Average subtask queue size");
        	pw.print(",");
        	for(int type=0;type<TYPES_OF_RESOURCE;type++) {
        		pw.print("Average leader dependable agents type " + type);
            	pw.print(",");
			}
        	pw.print("Average member dependable agents");
			pw.print(",");
			for(int size=0;size<=SUB_TASK_QUEUE_SIZE;size++) {
        		pw.print("Num of members whose subtask queue size is " + size);
            	pw.print(",");
			}
        	pw.print("Num of rejected task");
        	pw.print(",");
        	pw.print("Task completion success rate");
			pw.print(",");
			pw.print("Average of leader threshold");
        	pw.println();
        	
            long executedTask = 0;
        	long wastedTask = 0;
        	double communicationTime = 0;
        	double executedTime = 0;
        	double waitingTime = 0;
        	double allExecutedTime = 0;
        	long leaderCount = 0;
        	long memberCount = 0;
        	long reciprocityMembers = 0;
        	long reciprocityLeaders = 0;
        	long messageCount = 0;
        	long overflowedTask = 0;
        	double avgSubTaskQueue = 0;
        	long rejectedTask = 0;
        	double[] leaderDependableAgents = new double[TYPES_OF_RESOURCE];
        	double memberDependableAgents = 0;
        	long memberSubtaskQueueHist[] = new long[SUB_TASK_QUEUE_SIZE+1];
        	double avgTaskCompletionTime = 0;
			double leaderAverageThreshold = 0;
            for(int tick=0;tick<EXPERIMENTAL_DURATION;tick++){
            	for(int i=0;i<NUM_OF_AREA;i++){
            		executedTask += Analyzer.executedTask[i][tick];
	        		wastedTask += Analyzer.wastedTask[i][tick];
	        		communicationTime += divide(Analyzer.communicationTime[i][tick], Analyzer.countSentMessages[i][tick]);
	        		executedTime += divide(Analyzer.executedTime[i][tick] , Analyzer.executedSubTask[i][tick]);
	        		waitingTime += divide(Analyzer.waitingTime[i][tick] , Analyzer.executedSubTask[i][tick]);
	        		allExecutedTime += divide(Analyzer.allExecutedTime[i][tick], Analyzer.executedSubTask[i][tick]);
	        		messageCount += Analyzer.countSentMessages[i][tick];
	        		overflowedTask += Analyzer.overflowedTask[i][tick];
	        		rejectedTask += Analyzer.rejectedTask[i][tick];
	        		avgSubTaskQueue += divide(Analyzer.subTaskQueueSum[i][tick], Analyzer.countMembers[i][tick]);
	        		avgTaskCompletionTime += divide(Analyzer.taskCompletionTime[i][tick], Analyzer.executedTask[i][tick]);
	        		
	        		for(int type=0;type<TYPES_OF_RESOURCE;type++) {
	        			leaderDependableAgents[type] += divide(Analyzer.leaderDependableAgents[type][i][tick], Analyzer.countLeaders[i][tick]);
	    			}
	        		for(int size=0;size<=SUB_TASK_QUEUE_SIZE;size++) {
	            		memberSubtaskQueueHist[size] += Analyzer.memberSubtaskQueueHist[size][i][tick];
					}
		        	memberDependableAgents += divide(Analyzer.memberDependableAgents[i][tick], Analyzer.countMembers[i][tick]);
					leaderAverageThreshold += divide(Analyzer.leaderAverageThreshold[i][tick], Analyzer.countLeaders[i][tick]);
	        		
	        		
	        		if(tick % 100 == 0){
	        			leaderCount += Analyzer.countLeaders[i][tick];
	        			memberCount += Analyzer.countMembers[i][tick];
	        			reciprocityLeaders += Analyzer.reciprocalLeaders[i][tick];
	        			reciprocityMembers += Analyzer.reciprocalMembers[i][tick];
	        		}
	        	}
            	
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
		        	pw.print(avgTaskCompletionTime / 100 / NUM_OF_AREA);
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
		        	pw.print(avgSubTaskQueue / 100 / NUM_OF_AREA);
		        	pw.print(",");
		        	for(int type=0;type<TYPES_OF_RESOURCE;type++) {
		        		pw.print(leaderDependableAgents[type] / 100 / NUM_OF_AREA);
			        	pw.print(",");
	    			}
		        	pw.print(memberDependableAgents / 100 / NUM_OF_AREA);
		        	pw.print(",");
		        	for(int size=0;size<=SUB_TASK_QUEUE_SIZE;size++) {
		        		pw.print(memberSubtaskQueueHist[size] / 100 / TRIAL_COUNT);
		            	pw.print(",");
					}
		        	pw.print(rejectedTask / TRIAL_COUNT);
		        	pw.print(",");
		        	pw.print((double)executedTask / (executedTask + wastedTask + overflowedTask + rejectedTask));
					pw.print(",");
					pw.print(leaderAverageThreshold / 100 / NUM_OF_AREA);
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
	            	memberDependableAgents = 0;
	            	avgTaskCompletionTime = 0;
	            	for(int type=0;type<TYPES_OF_RESOURCE;type++) {
	            		leaderDependableAgents[type] = 0;
	    			}
	            	for(int size=0;size<=SUB_TASK_QUEUE_SIZE;size++) {
	            		memberSubtaskQueueHist[size] = 0;
					}
					leaderAverageThreshold = 0;
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
