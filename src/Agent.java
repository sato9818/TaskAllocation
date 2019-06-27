
public class Agent {
	static int num = 0;
	int capacity[] = new int[3];
	double de[] = new double[500];
	int id;
	int gridx;
	int gridy;
	
	Agent(){
		for(int i=0;i<3;i++){
			
		}
		id = num;
		num++;
	}
	
	public void setPosition(int x, int y){
		gridx = x;
		gridy = y;
	}
	
	public void updatede(){
		
	}
	
}
