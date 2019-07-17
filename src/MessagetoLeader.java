
public class MessagetoLeader extends Message{
	private Member from;
	private Leader to;
	private int type;
	boolean taskexcutionpossible;
	
	MessagetoLeader(Member f, Leader t, SubTask s, boolean b){
		super(s);
		from = f;
		to = t;
		taskexcutionpossible = b;
	}
	
	public Member getfrom(){
		return from;
	}
	
	public Leader getto(){
		return to;
	}
	
	public int gettype(){
		return type;
	}
}
