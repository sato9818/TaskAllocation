
public class SubTask {
	int reqCapa[] = new int[3];
	private int utility = 0;
	
	SubTask(Sfmt rnd){
		for(int i=0;i<3;i++){
			reqCapa[i] = 5 + (int)(rnd.NextUnif()*6);
			utility += reqCapa[i];
		}
	}
	public int getutility(){
		return utility;
	}
	
}
