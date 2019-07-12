
public class SubTask {
	int reqCapa[] = new int[3];
	private int utility = 0;
	
	SubTask(Sfmt rnd){
		int r = (int)(rnd.NextUnif()*3); 
		reqCapa[r] = 5 + (int)(rnd.NextUnif()*6);
		utility += reqCapa[r];
		
	}
	public int getutility(){
		return utility;
	}
	
}
