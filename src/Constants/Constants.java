package Constants;

public class Constants {
	private Constants(){};
	//設定系
	//Option
	public static final boolean ROLE_CHNAGEABLE = true;
	
	//エリア分割
	public static final int NUM_OF_VERTICAL_DIVISION = 2;
	public static final int NUM_OF_HORIZONTAL_DIVISION = 2;
	public static final int NUM_OF_AREA = NUM_OF_VERTICAL_DIVISION * NUM_OF_HORIZONTAL_DIVISION;
	
	//グリッドの範囲
	public static final int GRID_X = 50;
	public static final int GRID_Y = 50;
	
	//エージェントの数
	public static final int NUM_OF_AGENT = 500;
	
	//負荷
	public static final int LOW_WORKLOAD = 2;
	public static final int MODERATE_WORKLOAD = 4;
	public static final int HIGH_WORKLOAD = 8;
	
	//学習系
	public static final double LEARNING_RATE = 0.05;
	public static final double EPSILON = 0.05;
	
	//エージェント/メッセージ
	public static final int SOLICITATION_REDUNDANCY = 2;
	
	//リーダー
	public static final int LEADER_DEPENDABLITY_AGENT_THRESHOLD = 0;
	public static final int LEADER_DEPENDABLITY_DEGREE_THRESHOLD = 1000;
	
	//メンバ
	public static final int INACTIVE_THRESHOLD = 100;
	public static final int MEMBER_DEPENDABLITY_AGENT_THRESHOLD = 0;
	public static final int MEMBER_DEPENDABLITY_DEGREE_THRESHOLD = 1000;
	
	//エージェント
	public static final int TYPES_OF_RESOURCE = 3;
	
	
	//tick
	public static final int EXPERIMENTAL_DURATION = 10001;
	
//	-------------------------------------------------------------------------------------------------
	
	//メッセージの種類
	public static final int SOLICITATION = 0;
	public static final int ACCEPTANCE = 1;
	public static final int ALLOCATION = 2;
	public static final int FINISH = 3;

	
	//リーダーの状態
	public static final int SELECT_MEMBER = 0;
	public static final int WAIT_MEMBER = 1;
	
	//メンバの状態
	public static final int INACTIVE = 0;
	public static final int ACTIVE = 1;
	public static final int EXECUTING_TASK = 2;
	
	
}
