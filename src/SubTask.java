
public class SubTask {
	private int reqCapa[] = new int[3/**/];
	private int utility = 0;
	Leader from;
	Member to;
	
	//---------------------------------------------------------------------------------------
	
	SubTask(Sfmt rnd){
		//int r = randomInt(rnd);
		int r = rnd.NextInt(3);
		int c = 5 + rnd.NextInt(6);
		for(int i=0;i<3;i++){
			if(i == r){
				reqCapa[i] = c;
			}else{
				reqCapa[i] = 0;
			}
		}
		utility += reqCapa[r];	
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getutility(){
		return utility;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getcapacity(int i){
		return reqCapa[i];
	}
	
	//---------------------------------------------------------------------------------------
	
	public void setfrom(Leader l){
		from = l;
	}
	
	//---------------------------------------------------------------------------------------
	
	public Leader getfrom(){
		return from;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void setto(Member m){
		to = m;
	}
	
	//---------------------------------------------------------------------------------------
	
	public Member getto(){
		return to;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int randomInt(Sfmt rnd){
		int p = rnd.NextInt(10);
		if(p < 7){
			return 0;
		}else if(p < 9){
			return 1;
		}else{
			return 2;
		}
	}
	
	//---------------------------------------------------------------------------------------
}
