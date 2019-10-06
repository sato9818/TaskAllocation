
public class SubTask {
	private int reqCapa[] = new int[3/**/];
	private int utility = 0;
	Leader from;
	Member to;
	
	SubTask(Sfmt rnd){
		int r = rnd.NextInt(3); 
		reqCapa[r] = 5 + rnd.NextInt(6);
		utility += reqCapa[r];
		
	}
	public int getutility(){
		return utility;
	}
	
	public int getcapacity(int i){
		return reqCapa[i];
	}
	
	public void setfrom(Leader l){
		from = l;
	}
	
	public Leader getfrom(){
		return from;
	}
	
	public void setto(Member m){
		to = m;
	}
	
	public Member getto(){
		return to;
	}
}
