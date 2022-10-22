package comparator;
import java.util.Comparator;

import agent.Leader;

public class LeaderIdComparator implements Comparator<Leader>{
	public int compare(Leader leader1, Leader leader2){
		return leader1.getMyId() - leader2.getMyId();
	}
}
