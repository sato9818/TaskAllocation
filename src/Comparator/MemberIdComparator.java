package Comparator;
import java.util.Comparator;

import Agent.Member;

public class MemberIdComparator implements Comparator<Member>{
	public int compare(Member member1, Member member2){
		return member1.getMyId() - member2.getMyId();
	}
}
