package Constants;

public class Constants {
	private Constants(){};
	//設定系
	//Option
	public static final boolean ROLE_CHNAGEABLE = true;
	public static boolean CNP_MODE = true;
	public static boolean RECIPROCITY = true;
	
	//エリア分割
	public static final int NUM_OF_VERTICAL_DIVISION = 1;
	public static final int NUM_OF_HORIZONTAL_DIVISION = 1;
	public static final int NUM_OF_AREA = NUM_OF_VERTICAL_DIVISION * NUM_OF_HORIZONTAL_DIVISION;
	public static final int TASK_QUEUE_SIZE = 1000;
	
	//グリッドの範囲
	public static final int GRID_X = 50;
	public static final int GRID_Y = 50;
	
	//エージェントの数
	public static final int NUM_OF_AGENT = 500;
	
	//負荷
	public static final double LOW_WORKLOAD = 2;
	public static final double MODERATE_WORKLOAD = 16;
	public static final double HIGH_WORKLOAD = 6;
	
	//学習系
	public static final double LEARNING_RATE = 0.05;
	public static final double EPSILON = 0.05;
	
	//エージェント/メッセージ
	public static final int SOLICITATION_REDUNDANCY = 2;
	public static final int SUB_TASK_QUEUE_SIZE = 5;
	
	//リーダー
	public static final int LEADER_DEPENDABLITY_AGENT_THRESHOLD = 1;
	public static final double LEADER_DEPENDABLITY_DEGREE_THRESHOLD = 0.7;
	
	//メンバ
	public static final int INACTIVE_THRESHOLD = 100;
	public static final int MEMBER_DEPENDABLITY_AGENT_THRESHOLD = 3;
	public static final double MEMBER_DEPENDABLITY_DEGREE_THRESHOLD = 0.7;
	
	//エージェント
	public static final int TYPES_OF_RESOURCE = 3;
	
	//タスク数
	public static int BASIC_SUBTASKS = 3;
	public static final int SUBTASK_FLUCTUATION = 3;
	
	//tick
	public static final int EXPERIMENTAL_DURATION = 1001;
	
	public static final int CHANGE_WORKLOAD_TIME = 150001;
	public static final int RESTORE_WORKLOAD_TIME = 150001;
	public static final int CHANGE_SUBTASKS_TIME = 150001;
	public static final int RESTORE_SUBTASKS_TIME = 150001;
	
	public static final int FIRST_MEASURE_TIME = 10000;
	
	public static final int TRIAL_COUNT = 1;
	
//	-------------------------------------------------------------------------------------------------
	
	//サブタスクの種類
	public static final int NORMAL_TASK= 0;
	public static final int REQUIRE_FIRST = 1;
	public static final int REQUIRE_SECOUND = 2;
	public static final int REQUIRE_THIRD = 3;
	
	//メッセージの種類
	public static final int SOLICITATION = 0;
	public static final int ACCEPTANCE = 1;
	public static final int ALLOCATION = 2;
	public static final int FINISH = 3;
	public static final int REFUSE = 4;
	public static final int COLLAPSE_TEAM = 5;
	public static final int CNP_SOLICITATION = 6;

	
	//リーダーの状態
	public static final int SELECT_MEMBER = 0;
	public static final int WAIT_MEMBER = 1;
	
	//メンバの状態
	public static final int INACTIVE = 0;
	public static final int ACTIVE = 1;
	public static final int EXECUTING_TASK = 2;
	
	
}
