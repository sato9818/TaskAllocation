
public class MessagetoLeader extends Message{
	private Member from;
	private Leader to;
	private Leader lFrom;
	private Member newMember;
	private boolean accept;
	private int excutingtime;
	
	//---------------------------------------------------------------------------------------
	
	MessagetoLeader(Member f, Leader t, SubTask s, boolean b, int ty, int et){
		super(s, ty);
		from = f;
		to = t;
		accept = b;
		setdelay(f,t);
		excutingtime = et;
	}
	
	//---------------------------------------------------------------------------------------
	
	MessagetoLeader(Member f, Leader t, SubTask s, int ty, int et){
		super(s, ty);
		from = f;
		to = t;
		excutingtime = et;
		setdelay(f,t);
	}
	
	//---------------------------------------------------------------------------------------
	
	MessagetoLeader(Leader f, Member from, int ty){
		super(null, ty);
		this.from = from;
		lFrom = f;
	}
	
	//---------------------------------------------------------------------------------------
	
	public Member getfrom(){
		return from;
	}
	
	//---------------------------------------------------------------------------------------
	
	public Leader getto(){
		return to;
	}
	
	//---------------------------------------------------------------------------------------
	
	public Leader getLFrom(){
		return lFrom;
	}
	
	//---------------------------------------------------------------------------------------
	
	public boolean memberaccept(){
		return accept;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getExcutionTime(){
		return excutingtime;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void setNewMember(Member m){
		newMember = m;
	}
	
	//---------------------------------------------------------------------------------------
	
	public Member getNewMember(){
		return newMember;
	}
	
	//---------------------------------------------------------------------------------------
}
