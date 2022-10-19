import static shared.Constants.*;

import environment.Environment;
import random.Seed;

public class TaskAllocationThread extends Thread {
	private final Environment e;
	//スレッド実処理はThread派生クラス
	TaskAllocationThread(Environment e) {
		this.e = e;
	}
	@Override
	public void run() {
		for(int tick=0;tick<EXPERIMENTAL_DURATION;tick++){
			e.run(tick);
//			if(RECIPROCITY == true && (tick == CHANGE_SUBTASKS_TIME || tick == RESTORE_SUBTASKS_TIME || tick == FIRST_MEASURE_TIME)){
//				e.exportAgentConnection(tick);
//				e.exportForCytoscape(tick);
//			}
		} 
	}
}
