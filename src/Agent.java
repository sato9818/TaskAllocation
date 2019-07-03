
public class Agent {
	static int num = 0;
	int capacity[] = new int[3];
	double de[] = new double[500];
	private int myid;
	int gridx;
	int gridy;
	
	Agent(){
		for(int i=0;i<3;i++){
			
		}
		myid = num;
		num++;
		//initial de
		for(int i=0;i<500;i++){
			if(i != myid){
				de[i] = 0.5;
			}
			
		}
	}
	
	public void setPosition(int x, int y){
		gridx = x;
		gridy = y;
	}
	
	public void updatede(){
		
	}
	public int getmyid(){
		return myid;
	}
	
}
