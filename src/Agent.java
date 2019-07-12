import java.util.ArrayList;
import java.util.List;

public class Agent {
	static int num = 0;
	int capacity[] = new int[3];
	double de[] = new double[500];
	private int myid;
	private int gridx;
	private int gridy;
	List<Agent> deagent = new ArrayList<Agent>();
	
	Agent(Sfmt rnd){
		while(capacity[0] == 0 && capacity[1] == 0 && capacity[2] == 0){
			for(int i=0;i<3;i++){
				capacity[i] = (int)(rnd.NextUnif()*6);
			}
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
