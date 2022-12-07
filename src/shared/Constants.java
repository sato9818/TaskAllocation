package shared;

public class Constants {
	private Constants(){};
	//設定系
	//Option
	public static final boolean ROLE_CHNAGEABLE = true;
	public static boolean CNP_MODE = false;
	public static boolean RECIPROCITY = false;
	public static final boolean FULL_RESOURCE = false;
	public static final boolean THRESHOLD_FIXED = true;
	public static final boolean COOPERATIVE = false;
	public static final boolean	DECAYED_EPSILON = false;
	
	//負荷
	public static final double LOW_WORKLOAD = 3;
	public static final double MODERATE_WORKLOAD = 22;
	public static final double HIGH_WORKLOAD = 16;
	
	//エリア分割
	public static final int NUM_OF_VERTICAL_DIVISION = 1;
	public static final int NUM_OF_HORIZONTAL_DIVISION = 1;
	public static final int NUM_OF_AREA = NUM_OF_VERTICAL_DIVISION * NUM_OF_HORIZONTAL_DIVISION;
	public static final int TASK_QUEUE_SIZE = 1000;
	// public static final double[] WORKLOADS = { LOW_WORKLOAD, MODERATE_WORKLOAD, HIGH_WORKLOAD, LOW_WORKLOAD };
	public static final double[] WORKLOADS = { MODERATE_WORKLOAD };
	
	//グリッドの範囲
	public static final int GRID_X = 50;
	public static final int GRID_Y = 50;
	
	//エージェントの数
	public static final int NUM_OF_AGENT = 500;
	
	//学習系
	public static final double LEARNING_RATE = 0.05;
	public static final double INITIAL_EPSILON = 0.01;
	public static final double EPSILON_DECAY_RATE = 0.9999;
	
	//エージェント/メッセージ
	public static final int SOLICITATION_REDUNDANCY = 2;
	public static final int SUB_TASK_QUEUE_SIZE = 5;
	
	//リーダー
	public static final int LEADER_DEPENDABLITY_AGENT_THRESHOLD = 1;
//	public static final double LEADER_DEPENDABLITY_DEGREE_THRESHOLD = 1.0;
	public static final double INITAIL_LEADER_DEPENDABLITY_DEGREE_THRESHOLD = 1.0;
	public static final double LEADER_THRESHOLD_INCREASING_RATE = 0.01;
	public static final double LEADER_THRESHOLD_DECREASING_RATE = 0.00001;
	
	//メンバ
	public static final int INACTIVE_THRESHOLD = 100;
	public static final int MEMBER_DEPENDABLITY_AGENT_THRESHOLD = 5;
	public static final double INITAIL_MEMBER_DEPENDABLITY_DEGREE_THRESHOLD = 1.0;
//	public static final double MEMBER_DEPENDABLITY_DEGREE_THRESHOLD = 1.0;
	public static final double MEMBER_THRESHOLD_INCREASING_RATE = 0.01;
	public static final double MEMBER_THRESHOLD_DECREASING_RATE = 0.00001;
	
	//エージェント
	public static final int TYPES_OF_RESOURCE = 3;
	
	//タスク数
	public static final int BASIC_SUBTASKS = 3;
	public static final int SUBTASK_FLUCTUATION = 3;
	
	//サブタスクリソース
	public static final int BASIC_RESOURCE = 5;
	public static final int RESOURCE_FLUCTUATION = 0;
	public static final int ADDITIONAL_RESOURCE = 5;
	
	//tick
	public static final int EXPERIMENTAL_DURATION = 150001;
	
	public static final int CHANGE_WORKLOAD_TIME = EXPERIMENTAL_DURATION;
	public static final int RESTORE_WORKLOAD_TIME = EXPERIMENTAL_DURATION;
	public static final int CHANGE_WORKLOAD_TIME2 = EXPERIMENTAL_DURATION;
	public static final int RESTORE_WORKLOAD_TIME2 = EXPERIMENTAL_DURATION;
	public static final int CHANGE_SUBTASKS_TIME = EXPERIMENTAL_DURATION;
	public static final int RESTORE_SUBTASKS_TIME = EXPERIMENTAL_DURATION;
	public static final int TIME_TO_RESET_DE = EXPERIMENTAL_DURATION;
	
	public static final int FIRST_MEASURE_TIME = EXPERIMENTAL_DURATION;
	
	public static final int TRIAL_COUNT = 1;
	
//	-------------------------------------------------------------------------------------------------

	
	//リーダーの状態
	public static final int SELECT_MEMBER = 0;
	public static final int WAIT_MEMBER = 1;
	
	//メンバの状態
	public static final int INACTIVE = 0;
	public static final int ACTIVE = 1;
	public static final int EXECUTING_TASK = 2;
}
