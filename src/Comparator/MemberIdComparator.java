package comparator;
import java.util.Comparator;

import agent.Member;

public class MemberIdComparator implements Comparator<Member>{
	public int compare(Member member1, Member member2){
		return member1.getMyId() - member2.getMyId();
	}
}
