package comparator;
import java.util.Comparator;

import agent.Agent;

public class AgentIdComparator implements Comparator<Agent>{
	public int compare(Agent leader1, Agent leader2){
		return leader1.getMyId() - leader2.getMyId();
	}
}
