import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Agent {
	static int num = 0;
	int capacity[] = new int[3];
	double de[] = new double[500];
	private int myid;
	private int gridx;
	private int gridy;
	private int phase = 0;
	List<Agent> deagent = new ArrayList<Agent>();
	protected int numofdeagent;
	private double capave = 0.0;
	protected double threshold;
	private int distance[] = new int[500];
	
	Agent(Sfmt rnd){
		setcapacity(rnd);
		initialde();
		myid = num;
		num++;
		//initial de
		
	}
	
	public void setdistance(Agent agent){
		int dis = manhattan(this.getPositionx(), agent.getPositionx(), this.getPositiony(), agent.getPositiony()) / 10/**/ + 1;
		distance[agent.getmyid()] = dis;
	}
	
	private int manhattan(int x1, int x2, int y1, int y2){
		return Math.abs(x1-x2) + Math.abs(y1-y2);
	}
	
	public int getdistance(int id){
		return distance[id];
	}
	
	private void initialde(){
		for(int i=0;i<500;i++){
			if(i != this.getmyid()){
				de[i] = 0.5;
			}
		}
	}
	
	private void setcapacity(Sfmt rnd){
		while(capacity[0] == 0 && capacity[1] == 0 && capacity[2] == 0){
			int p = 0;
			for(int i=0;i<3;i++){
				capacity[i] = (int)(rnd.NextUnif()*6);
				capave += (double)capacity[i];
				if(capacity[i] != 0){
					p++;
				}
			}
			capave /= (double)p;
		}
	}
	
	public void setPosition(int x, int y){
		gridx = x;
		gridy = y;
	}
	
	public int getPositionx(){
		return gridx;
	}
	
	public int getPositiony(){
		return gridy;
	}
	
	public void reducede(int id){
		de[id] = Math.max(de[id]-0.000002, 0.0);
	}
	
	public int getmyid(){
		return myid;
	}
	
	public int getPhase(){
		return phase;
	}
	
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
	public double averageOfCapability(){
		return capave;
	}
	
	public void adddeagent(Agent agent){
		deagent.add(agent);
	}
	
	public double getthreshold(){
		return threshold;
	}
	
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
}
