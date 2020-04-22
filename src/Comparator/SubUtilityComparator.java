package Comparator;
import java.util.Comparator;

import Task.SubTask;

public class SubUtilityComparator implements Comparator<SubTask>{
	@Override
	public int compare(SubTask p1, SubTask p2) {
		return p1.getutility() > p2.getutility() ? -1 : 1;
	}
}
