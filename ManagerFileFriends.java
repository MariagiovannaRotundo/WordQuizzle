

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ManagerFileFriends {
	
	//lock per gestire la concorrenza
	private ReentrantReadWriteLock readWriteLock;
	private Lock reader;
	private Lock writer;
	private String encoding;
	
	private String nameFile;
	//mantiene tutte le relazioni di amicizia
	private JSONArray allFriends;
	
	
	public ManagerFileFriends(String nameFile, String encoding) {
		
		this.nameFile=nameFile;
		this.encoding=encoding;
		
		this.allFriends=ReadFileFriends();
		
		readWriteLock = new ReentrantReadWriteLock();
		reader = readWriteLock.readLock();
		writer = readWriteLock.writeLock();
	}
	
	
		//Creo il file se nn esiste
		//chiamata solo alla creazione dell'oggetto. Non ci sarà mai concorrenza 
		public JSONArray ReadFileFriends(){
			
			File file = new File(this.nameFile);
			if(file.exists()) {
				//apro il file degli utenti
					try{
						FileChannel inChannel = FileChannel.open(Paths.get(this.nameFile), StandardOpenOption.READ);
						ByteBuffer buff = ByteBuffer.allocateDirect(3000);
						

						String s=new String();
						byte[] b = new byte[1];
						//leggo un numero di byte pari alla lunghezza del buffer
						//corrisponde a read(buff,0(offset), buff.lengt)
						while ((inChannel.read(buff))!=-1){
							//passo in modalità lettura
							buff.flip();
							while (buff.hasRemaining()){
								b[0]=buff.get();
								s+=new String(b,encoding);	
							}
								            
							buff.clear();
						}
						
						//se il file è vuoto
						if(s.length()==0){
							allFriends=new JSONArray();
						}
						else{
							 JSONParser parser = new JSONParser();
							 allFriends = (JSONArray) parser.parse(s);
						}
						inChannel.close();
					}catch(IOException e){
					    e.printStackTrace();
					}catch(ParseException e){
						e.printStackTrace();
					}
			
			}
			else{
				try {
					file.createNewFile();
					allFriends=new JSONArray();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
			return allFriends;
		}
	
		
		//richiamata solo dal thread WorkerOnFile
		//è l'unico thread che opera sul file, non c'è concorrenza
		public void RemoveCreateFile(){
			File elimina = new File(this.nameFile);
			if(elimina.exists())
				elimina.delete();
			
			try {
				elimina.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		//richiamata solo dagli scrittori, solo in questa classe
		@SuppressWarnings("unchecked")
		public JSONArray AddToArray(JSONObject obj){
			this.allFriends.add(obj);
			return allFriends;
		}
		
	
		public Lock BeReader(){
			return reader;
		}
		
		public Lock BeWriter(){
			return writer;
		}
		
		//usato solo da worker on file
		public JSONArray ReadArray(){
			return allFriends;
		}
		
		//usato solo da worker on file
		public String getNameFile(){
			return this.nameFile;
		}
	
		
		@SuppressWarnings("unchecked")
		public void setScoreFriends(String name, Long score, String f){
			
			int i=0;
			
			JSONObject obj= new JSONObject ();
			obj.put("nick", name);
			obj.put("score", score);
			obj.put("friends", f);
			
			//trovo l'oggetto con quel nick
			Iterator<JSONObject> iterator = allFriends.iterator();
			while (iterator.hasNext()) {
				JSONObject u=iterator.next();
				String nameOfUser = (String) u.get("nick");
				//quando trovo l'utente aggiorno
				if(nameOfUser.equals(name)){
					
					allFriends.remove(i);
				
					//aggiungo l'oggetto alla lista
					allFriends=AddToArray(obj);
					return;
				}
				else{
					i++;
				}
			}
			
			//nn ho trovato l'utente quindi lo aggiungo
			this.AddToArray(obj);
		}
		
		@SuppressWarnings("unchecked")
		public int getScore(String name){
			
			//trovo l'oggetto con quel nick
			Iterator<JSONObject> iterator = allFriends.iterator();
			while (iterator.hasNext()) {
				JSONObject u=iterator.next();
				String nameOfUser = (String) u.get("nick");
				//quando trovo l'utente stampo
				if(nameOfUser.equals(name)){
					Long score = (Long) u.get("score");
					
					return score.intValue();
				}
			}
			return 0;
		}
		
		@SuppressWarnings("unchecked")
		public String getFriends(String name){
			
			//trovo l'oggetto con quel nick
			Iterator<JSONObject> iterator = allFriends.iterator();
			while (iterator.hasNext()) {
				JSONObject u=iterator.next();
				String nameOfUser = (String) u.get("nick");
				//quando trovo l'utente stampo
				if(nameOfUser.equals(name)){
					String f = (String) u.get("friends");

					return f;
				}
			}
			
			return "";
		}
		
		
}
