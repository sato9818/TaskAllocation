import java.util.Comparator;

public class LeaderIdComparator implements Comparator<Leader>{
	public int compare(Leader leader1, Leader leader2){
		return leader1.getmyid() - leader2.getmyid();
	}
}
