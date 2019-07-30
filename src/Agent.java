import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
	
	Agent(Sfmt rnd){
		while(capacity[0] == 0 && capacity[1] == 0 && capacity[2] == 0){
			for(int i=0;i<3;i++){
				capacity[i] = (int)(rnd.NextUnif()*6);
				capave += (double)capacity[i];
			}
			capave /= 3;
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
			Collections.sort(deagent, new Comparator<Agent>(){
				public int compare(Agent m1, Agent m2) {
					 return de[m1.getmyid()] > de[m2.getmyid()] ? -1 : 1;
				}
			});
			List<Agent> buf = new ArrayList<Agent>();
			for(int i=0;i<numofdeagent;i++){
				buf.add(deagent.get(i));
			}
			deagent = buf;
		}
	}
}
