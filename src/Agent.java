import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Agent {
	static int num = 0;
	int capacity[] = new int[3];
	double de[] = new double[1000];
	private int myid;
	private int gridx;
	private int gridy;
	private int phase = 0;
	List<Agent> deagent = new ArrayList<Agent>();
	protected int numofdeagent;
	private double capave = 0.0;
	protected double threshold;
	private int distance[] = new int[1000];
	protected double lE = 0.5;
	protected double mE = 0.5;
	private int presentTick = 0;
	
	//---------------------------------------------------------------------------------------
	
	Agent(Sfmt rnd){
		setcapacity(rnd);
		initialde();
		if(num == 500){
			num = 0;
		}
		
		myid = num;
		num++;
		//initial de
	}
	
	//---------------------------------------------------------------------------------------
	
	Agent(Agent agent){
		myid = agent.getmyid();
		capacity = agent.capacity;
		de = agent.de;
		gridx = agent.getPositionx();
		gridy = agent.getPositiony();
		deagent = agent.deagent;
		capave = agent.capave;
		distance = agent.distance;
		lE = agent.lE;
		mE = agent.mE;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void setTick(int tick){
		presentTick = tick;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getTick(){
		return presentTick;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getArea(){
		if(this.getPositionx() < 25 && this.getPositiony() < 25){
			return 0;
		}else if(this.getPositionx() >= 25 && this.getPositiony() < 25){
			return 1;
		}else if(this.getPositionx() < 25 && this.getPositiony() >= 25){
			return 2;
		}else{
			return 3;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getAreaExpand(){
		int x = this.getPositionx();
		int y = this.getPositiony();
		if(x < 33 && y < 33){
			return 0;
		}else if(x >= 33 && x < 66 && y < 33){
			return 1;
		}else if(x >= 66 && x < 99 && y < 33){
			return 2;
		}else if(x < 33 && y >= 33 && y < 66){
			return 3;
		}else if(x >= 33 && x < 66 && y >= 33 && y < 66){
			return 4;
		}else if(x >= 66 && x < 99 && y >= 33 && y < 66){
			return 5;
		}else if(x < 33 && y >= 66 && y < 99){
			return 6;
		}else if(x >= 33 && x < 66 && y >= 66 && y < 99){
			return 7;
		}else if(x >= 66 && x < 99 && y >= 66 && y < 99){
			return 8;
		}else{
			return 9;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void updateE(int roleType, boolean success){
		double delta;
		if(success){
			delta = 1.0;
		}else{
			delta = 0.0;
		}
		if(roleType == 0){
			lE = (1.0 - 0.05) * lE + 0.05 * delta; 
		}else if(roleType == 1){
			mE = (1.0 - 0.05) * mE + 0.05 * delta; 
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void setdistance(Agent agent){
		int dis = (int)Math.ceil((double)manhattan(this.getPositionx(), agent.getPositionx(), this.getPositiony(), agent.getPositiony()) / 200 * 10/**/ );
		distance[agent.getmyid()] = dis;
	}
	
	//---------------------------------------------------------------------------------------
	
	double euclid(double x1, double x2, double y1, double y2) {
		double d = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
		return d;
	}
	
	//---------------------------------------------------------------------------------------
	
	private int manhattantorus(int x1, int x2, int y1, int y2){
		int x = Math.abs(x1-x2);
		int y = Math.abs(y1-y2);
		if(x > 25) x = Math.abs(x-50);
		if(y > 25) y = Math.abs(y-50);
		return x + y;
	}
	
	//---------------------------------------------------------------------------------------
	
	private int manhattan(int x1, int x2, int y1, int y2){
		int x = Math.abs(x1-x2);
		int y = Math.abs(y1-y2);
		return x + y;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getdistance(int id){
		return distance[id];
	}
	
	//---------------------------------------------------------------------------------------
	
	private void initialde(){
		for(int i=0;i<1000;i++){
			if(i != this.getmyid()){
				de[i] = 0.5;
			}
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	private void setcapacity(Sfmt rnd){
		while(capacity[0] == 0 && capacity[1] == 0 && capacity[2] == 0){
			int p = 0;
			for(int i=0;i<3;i++){
				capacity[i] = 1 + rnd.NextInt(5);
				capave += (double)capacity[i];
				if(capacity[i] != 0){
					p++;
				}
			}
			capave /= (double)p;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public void setPosition(int x, int y){
		gridx = x;
		gridy = y;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getPositionx(){
		return gridx;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getPositiony(){
		return gridy;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void reducede(int id){
		de[id] = Math.max(de[id]-0.000002, 0.0);
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getmyid(){
		return myid;
	}
	
	//---------------------------------------------------------------------------------------
	
	public int getPhase(){
		return phase;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void setphase(int phase){
		/*Leader
		 * 0:メンバの選択
		 * 1:メンバからの返信待ち
		 * 2:メンバへ割り当て
		 *Member
		 * 0:メッセージ待ち、メッセージがあればメッセージを選んでリーダーに返信
		 * 1:割り当て待ち
		 * 3:処理
		 */
		this.phase = phase;
		
	}
	
	//---------------------------------------------------------------------------------------
	
	public double averageOfCapability(){
		return capave;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void adddeagent(Agent agent){
		deagent.add(agent);
	}
	
	//---------------------------------------------------------------------------------------
	
	public double getthreshold(){
		return threshold;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void updatedeagent(){
		if(numofdeagent < deagent.size()){
			deagent = sortagent(deagent);
			List<Agent> buf = new ArrayList<Agent>();
			for(int i=0;i<numofdeagent;i++){
				buf.add(deagent.get(i));
			}
			deagent = buf;
		}
	}
	
	//---------------------------------------------------------------------------------------
	
	public List<Agent> sortagent(List<Agent> agents){
		for (int i = 0; i < agents.size() - 1; i++) {
            for (int j = agents.size() - 1; j > i; j--) {
                if (de[agents.get(j - 1).getmyid()] < de[agents.get(j).getmyid()]) {
                    Collections.swap(agents,j-1,j);
                }
            }
        }
		return agents;
	}
	
	//---------------------------------------------------------------------------------------
}
