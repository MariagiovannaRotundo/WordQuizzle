

//classe che contiene le traduzioni delle parole

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;



public class Translates {
	
	private static Translates instance=null;
	private ConcurrentHashMap <String,Vector<String>> translates;
	
	public Translates(){
		translates=new ConcurrentHashMap <String,Vector<String>>();
	}
	
	//non ho un problema di concorrenza visto che l'istanza non viene mai cancellata
	//la creo all'inizio quando non ho concorrenza e poi la richiamo in modo concorrente
	public static Translates getInstance(){
		if(instance==null){
			instance=new Translates();
		}
		return instance;
	}
	
	public void addElement(String key, Vector<String> value){
		translates.putIfAbsent(key, value);
	}
	
	public boolean isCorrect(String italian, String english){
		return translates.get(italian).contains(english);
	}
	
	public boolean isThere(String key){
		return translates.containsKey(key);
	}
	
	
}
