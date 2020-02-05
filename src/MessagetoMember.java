
public class MessagetoMember extends Message{
	private Leader from;
	private Member to;
	private Member mfrom;
	private Task task;
	private boolean havetask = false;
	
	//---------------------------------------------------------------------------------------
	
	MessagetoMember(Leader f, Member t, SubTask s){
		//タスクを受理するかしないか
		super(s, 0);
		from = f;
		to = t;
		setdelay(f,t);
	}
	
	//---------------------------------------------------------------------------------------
	
	MessagetoMember(Leader f, Member t, SubTask s, boolean th){
		//受理メッセージに対するタスクを与える
		super(s, 1);
		from = f;
		to = t;
		setdelay(f,t);
		havetask = th;
	}
	
	//---------------------------------------------------------------------------------------
	
	MessagetoMember(Leader from, Member to, Task task){
		//CNP
		super(null, 0);
		this.from = from;
		this.to = to;
		this.task = task;
		setdelay(from, to);
	}
	
	//---------------------------------------------------------------------------------------
	
	MessagetoMember(Member mfrom, Member to,SubTask s){
		super(s, 3);
		this.mfrom = mfrom;
		this.to = to;
		setdelay(mfrom, to);
	}
	
	//---------------------------------------------------------------------------------------
	
	public Task gettask(){
		return task;
	}
	
	//---------------------------------------------------------------------------------------
	
	public Leader getfrom(){
		return from;
	}
	
	//---------------------------------------------------------------------------------------
	
	public Member getmfrom(){
		return mfrom;
	}
	
	//---------------------------------------------------------------------------------------
	
	public Member getto(){
		return to;
	}
	
	//---------------------------------------------------------------------------------------
	
	public boolean taskisallocated(){
		return havetask;
	}
	
	//---------------------------------------------------------------------------------------
}
