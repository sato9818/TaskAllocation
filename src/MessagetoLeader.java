
public class MessagetoLeader extends Message{
	private Member from;
	private Leader to;
	
	private boolean accept;
	private int excutingtime;
	
	MessagetoLeader(Member f, Leader t, SubTask s, boolean b, int ty){
		super(s, ty);
		from = f;
		to = t;
		accept = b;
		setdelay(f,t);
	}
	
	MessagetoLeader(Member f, Leader t, SubTask s, int et, int ty){
		super(s, ty);
		from = f;
		to = t;
		excutingtime = et;
		setdelay(f,t);
	}
	
	public Member getfrom(){
		return from;
	}
	
	public Leader getto(){
		return to;
	}
	
	
	public boolean memberaccept(){
		return accept;
	}
}
