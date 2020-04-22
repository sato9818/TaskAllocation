package Comparator;
import java.util.Comparator;

import Agent.Leader;

public class LeaderIdComparator implements Comparator<Leader>{
	public int compare(Leader leader1, Leader leader2){
		return leader1.getMyId() - leader2.getMyId();
	}
}
