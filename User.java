

import java.util.Vector;


public class User {

	private Long score;
	private boolean log;
	//contiene gli amici
	private Vector<String> v;
	private int port;
	
	
	public User(int score,boolean log, String friends, int port) {
		this.score=new Long(score);
		this.log=log;
		this.v=new Vector<>();
		this.port=port;
		
		if(!"".equals(friends)){
			friends=friends.substring(1, friends.length()-1);
			String[] friend=friends.split(",");
			
			for(int i=0;i<friend.length;i++){
				v.add(friend[i].trim());
			}
		}
	}
	
	
	public boolean getLog(){
		return log;
	}
	
	public void setLog(){
		log=!log;
	}
	
	public void setPort(int port){
		this.port=port;
	}
	
	public int getPort(){
		return port;
	}
	
	public Long getScore(){
		return score;
	}
	
	public void addScore(int s){
		score+=s;
	}

	public String getFriends(){
		return v.toString();
	}
	
	public boolean isFriend(String friend){
		return v.contains(friend);
	}
	
	//0: aggiunto, 1:già esistente
	public synchronized int addFriend(String f){
		if(!v.contains(f)){
			v.add(f);
			return 0;
		}
		else{
			return 1;
		}
	}
	
}
