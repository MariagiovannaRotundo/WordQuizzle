

import java.io.File;
import java.io.IOException;
import java.nio.*;
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

public class ManagerFileRegistrations {

	private String nameFile;
	private ReentrantReadWriteLock readWriteLock;
	private Lock reader;
	private Lock writer;
	private JSONArray array;
	private String encoding;
	
	public ManagerFileRegistrations(String nameFile, String encoding) {
		this.nameFile=nameFile;
		this.encoding=encoding;
		
		this.array=ReadFileRegistration();
		
		readWriteLock = new ReentrantReadWriteLock();
		reader = readWriteLock.readLock();
		writer = readWriteLock.writeLock();
	}
	
	//la chiamo solo al momento della creazione dell'oggetto
	//Non ci sarà mai concorrenza su questo metodo
	public JSONArray ReadFileRegistration(){
		
		File fileRegistry = new File(this.nameFile);
		if(fileRegistry.exists()) {
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
					    array=new JSONArray();
					}
					else{
						 JSONParser parser = new JSONParser();
						 array = (JSONArray) parser.parse(s);
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
				fileRegistry.createNewFile();
				array=new JSONArray();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		return array;
	}
	
	
	//richiamata solo da scrittori RemoteRegistration
	public String getNameFile(){
		return this.nameFile;
	}
	
	//richiamata solo da scrittori RemoteRegistration
	//0:ok, 1:errore
	public int RemoveCreateFile(){
		File elimina = new File(this.nameFile);
		if(elimina.exists())
			elimina.delete();
		try {
			elimina.createNewFile();
			return 0;
		} catch (IOException e1) {
			e1.printStackTrace();
			return 1;
		}
	}
	
	//richiamata solo da scrittori RemoteRegistration
	@SuppressWarnings("unchecked")
	public JSONArray AddToArray(JSONObject obj){
		writer.lock();
		this.array.add(obj);
		writer.unlock();
		return array;
	}
	
	//usato solo da lettori Worker
	//richiamato sempre all'interno di una lock in lettura
	public JSONArray ReadArray(){
		return array;
	}
	
	public Lock BeReader(){
		return reader;
	}
	
	public Lock BeWriter(){
		return writer;
	}
	
	//richiamata solo da scrittori RemoteRegistration
	//controlla se è già presente un utente con quel nick
	//0:no, 1:si
	@SuppressWarnings("unchecked")
	public int isInside(String username){
		//vedo se già esiste un oggetto con quel nick
		Iterator<JSONObject> iterator = array.iterator();
		while (iterator.hasNext()) {
			String nameOfUser = (String) iterator.next().get("nick");
			//se l'utente è già registrato
			if(nameOfUser.equals(username)){
				return 1;
			}
		}
		//non è già registrato
		return 0;
	}
	
}
