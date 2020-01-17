

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ManagerUsers {
	
	//struttura dati per la gestione dei punteggi e dei login
	private ConcurrentHashMap <String,User> concurrentMap;
	private boolean shutdown;
	private boolean lastBackup;
	
	public ManagerUsers() {
		shutdown=false;
		lastBackup=false;
		concurrentMap=new ConcurrentHashMap <String,User>();
	}
	
	public boolean getShotdown(){
		return shutdown;
	}
	public void setShotdown(){
		shutdown=true;
	}
	public void setLastBackup(){
		lastBackup=true;
	}
	public boolean getLastBackup(){
		return lastBackup;
	}
	
	public boolean SeeUser(String username){
		return concurrentMap.containsKey(username);
	}
	
	public User getUser(String username){
		return concurrentMap.get(username);
	}
	
	public boolean SeeLogUser(String username){
		User u= concurrentMap.get(username);
		if(u!=null)return u.getLog();
		else return false;
	}
	
	public void setLogUser(String username){
		concurrentMap.get(username).setLog();
	}
	
	public void setPortUser(String username, int port){
		concurrentMap.get(username).setPort(port);
	}
	
	public int getPortUser(String username){
		return concurrentMap.get(username).getPort();
	}
	
	public void AddUser(String username, User u){
		concurrentMap.putIfAbsent(username, u);
	}
	
	public long getUserScore(String username){
		return concurrentMap.get(username).getScore();
	}
	
	public void addUserScore(String username, int score){
		concurrentMap.get(username).addScore(score);
	}
	
	public void RemoveUser(String username){
		concurrentMap.remove(username);
	}
	
	public void update(ManagerFileFriends mff){
			
		mff.BeWriter().lock();
		
		//dati da salvare
		Iterator <String> i =concurrentMap.keySet().iterator();
		while(i.hasNext()){
			//chiave (=username)
			String k= i.next();
			
			//aggiorno gli score e gli amici
			mff.setScoreFriends(k, concurrentMap.get(k).getScore(), concurrentMap.get(k).getFriends() );
			
			//se l'utente non è attivo lo rimuovo dalla HashMap
			if(!SeeLogUser(k)){
				RemoveUser(k);
			}
		}
		
		mff.BeWriter().unlock();
		
	}
	
	//aggiungo a entrambi
	public int addFriend(String username, String Friend){
		//aggiunto il secondo (se uno c'è c'è per forza anche l'opposto)
		if(concurrentMap.get(username).addFriend(Friend)==0){
			//aggiungo anche all'amico
			concurrentMap.get(Friend).addFriend(username);
			return 0;
		}
		else{
			//già erano amici
			return 1;
		}
	}
	
	//vedo se sono amici
	public boolean areFriends(String username, String Friend){
		return concurrentMap.get(username).isFriend(Friend);
	}
		
		
}
