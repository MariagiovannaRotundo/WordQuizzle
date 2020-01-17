
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Battle {

	private String from;
	private boolean response;
	private Vector<String> v;
	private boolean surrendered;
	//thread di chi sfida
	private Thread t;
	
	private ReentrantLock lock;
	Condition first;
	Condition waitWords;
	
	private int scorekey;
	private int score;
	
	public Battle(String from, Thread t, int Y){
		this.from=from;
		this.response=false;
		this.t=t;
		this.v=new Vector<>();
		
		lock=new ReentrantLock();
		first=lock.newCondition();
		waitWords=lock.newCondition();
		
		scorekey=Y-1;
		score=Y-1;
		surrendered=false;
	}
	
	
	public String getBattleFrom(){
		return from;
	}
	
	public boolean isAccepted(){
		return response;
	}
	
	public void deleteResponse(){
		response=false;
	}
	
	public void reject(){
		t.interrupt();
	}
	
	public void accept(){
		lock.lock();
		//setto che la sfida è accettata
		response=true;
		t.interrupt();
		lock.unlock();
	}
	
	public void addToVector(String word){
		this.v.add(word);
	}
	
	public ReentrantLock getLock(){
		return lock;
	}
	
	public Condition getCondition(){
		return first;
	}
	
	public Condition getWaitWords(){
		return waitWords;
	}
	
	public Vector<String> getWords(){
		return v;
	}
	
	public int getScores(boolean key){
		if(key){
			return score;
		}
		else{
			return scorekey;
		}
	}
	
	public void setScores(boolean key, int s){
		if(key){
			scorekey=s;
		}
		else{
			score=s;
		}
	}
	
	public void surrender(){
		surrendered=true;
	}
	
	public boolean isSurrendered(){
		return surrendered;
	}
	
	public int getVSize(){
		return v.size();
	}
	
	public boolean isInside(String w){
		return this.v.contains(w);
	}
}
