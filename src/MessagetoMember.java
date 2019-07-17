
public class MessagetoMember extends Message{
	private Leader from;
	private Member to;
	private int type;
	
	MessagetoMember(Leader f, Member t, SubTask s){
		super(s);
		from = f;
		to = t;
	}
	
	public Leader getfrom(){
		return from;
	}
	
	public Member getto(){
		return to;
	}
}
