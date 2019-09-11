import java.util.Comparator;

public class MemberIdComparator implements Comparator<Member>{
	public int compare(Member member1, Member member2){
		return member1.getmyid() - member2.getmyid();
	}
}
