

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ManagerBattles {

	private Dictionary d;
	private int T1, K, N, Y;
	private URL url;
	private Translates t;
	
	//struttura dati per la gestione delle battaglie
	ConcurrentHashMap <String,Battle> battles;
	
	public ManagerBattles(String nameFile, String encoding) {
		battles=new ConcurrentHashMap <String,Battle>();
		this.d=new Dictionary(nameFile, encoding);
		this.t=Translates.getInstance();
		
		N=getN();
		K=ParametersBattle.getK();	
		Y=ParametersBattle.getY();
		T1=ParametersBattle.getT1();	
		
	}
	
	public int getN(){
		return d.getN();
	}
	
	//0:Ok, 1:non possibile
	public int challenge(String to, String from,Thread t){
		//inserito: richiesta mandata
		if(battles.putIfAbsent(to, new Battle(from, t, Y))==null){
			return 0;
		}
		else{//occupata in un'altra sfida/richiesta di sfida
			return 1;
		}
	}
	
	//0:sfida, 1:rifiuto o errore
	public int seeResponse(String to){
		try {
			Thread.sleep(T1);
			
			battles.get(to).getLock().lock();
			if(!battles.get(to).isAccepted()){
				//cancello anche la lock con questo
				battles.remove(to);
				return 1;
			}
			else{
				//mi metto in attesa della generazione delle parole
				while(battles.get(to).getVSize()<K && battles.get(to).isAccepted()){
					battles.get(to).getWaitWords().await();
				}
				if(!battles.get(to).isAccepted()){//errore
					battles.get(to).getLock().unlock();
					 return 1;
				}
				
				battles.get(to).getLock().unlock();
				//tutte le parole sono state tradotte
				return 0;
			}
			
		} catch (InterruptedException e) {
			try{
				//ottengo una risposta
				battles.get(to).getLock().lock();
				if(battles.get(to).isAccepted()){//se la sfida è stata accettata
					//mi metto in attesa della generazione delle parole
					while(battles.get(to).getVSize()<K && battles.get(to).isAccepted()){
						try {
							battles.get(to).getWaitWords().await();
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					if(!battles.get(to).isAccepted()){//errore
						battles.get(to).getLock().unlock();
						return 1;
					}
					
					battles.get(to).getLock().unlock();
					//tutte le parole sono state tradotte
					return 0;
				}
				else{//sfida rifiutata
					battles.remove(to);
					return 1;
				}
			}catch(NullPointerException e1){//errore
				return 1;
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public int generate(String username){
		Random rand = new Random();
		
		battles.get(username).getLock().lock();
		
		while(battles.get(username).getVSize()<K){
			int num=rand.nextInt(N);
			String w=d.getWord(num);
			
			//per avere parole tutte diverse
			if(!battles.get(username).isInside(w)){
			
				//aggiungo la parola e la traduzione alla struttura dati
				try{
					//aggiungo la parola alla sfida
					battles.get(username).addToVector(w);
					
					//la parola non è stata già tradotta da qualcun altro
					//e non è, quindi, già memorizzata, ma va tradotta
					if(!t.isThere(w)){
						
						//url = new URL("https://mymemory.translated.net/doc/spec.php");
						String req=new String(("https://api.mymemory.translated.net/get?q="+w+"&langpair=it|en").getBytes(),"UTF-8");
						url = new URL(req);
						InputStream in;
						
						in=url.openStream();
						
						in = new BufferedInputStream(in);
						// chain the InputStream to a Reader
						Reader r = new InputStreamReader(in);
						int c;
						String response="";
						JSONObject respJson=null;
						Vector<String> v=new Vector<>();
						
						while ((c = r.read()) != -1) {
							response+=(char) c;
						}
	
						//parso la risposta
						//la risposta non è mai vuota in quanto il servizio restituisce
						//sempre qualcosa e nel caso in cui non riesco a connettermi al servizio
						//gestisco la cosa nel catch
						
						JSONParser parser = new JSONParser();
						try {
							respJson = (JSONObject) parser.parse(response);
						} catch (ParseException e) {
							
							battles.get(username).getLock().unlock();
							e.printStackTrace();
						}
							
						//devo prendere il valore del campo "matches"
						//questo valore è un array di oggetti json e contiene tutte 
						//le traduzioni
						JSONArray matches = (JSONArray) respJson.get("matches");
						
						//trovo l'oggetto con quel nick
						Iterator<JSONObject> iterator = matches.iterator();
						while (iterator.hasNext()) {
							//per ogni oggetto prendo il campo "translation" che 
							//contiene la traduzione
							JSONObject obj=iterator.next();
							String translate = (String) obj.get("translation");
							
							if(!v.contains(translate.toLowerCase())){
								v.add(translate.toLowerCase());
							}
						}	
						
						//aggiungo la parola e la traduzione alla struttura 
						//dati che fa da cache e memorizza quindi le traduzioni 
						//per tutta la durata della sfida
						t.addElement(w, v);
					}
					
					
				}catch (NoRouteToHostException e) {
					battles.get(username).deleteResponse();
					battles.get(username).getWaitWords().signal();
					battles.get(username).getLock().unlock();
					System.err.println(e); 
					return 2;
				}catch (MalformedURLException e) {
					battles.get(username).deleteResponse();
					battles.get(username).getWaitWords().signal();
					battles.get(username).getLock().unlock();
					System.err.println(e); 
					return 2;
				} catch (IOException e) {
					battles.get(username).deleteResponse();
					battles.get(username).getWaitWords().signal();
					battles.get(username).getLock().unlock();
					e.printStackTrace();
					return 2;
				}
			}
		}
		
		battles.get(username).getWaitWords().signal();
		
		battles.get(username).getLock().unlock();
		return 0;
	}
	
	
	public void reject(String username){
		try{
			battles.get(username).reject();
		}catch(NullPointerException e){
			//l'oggetto è già stato rimosso
		}
		
	}
	
	public int accept(String username){
		try{
			battles.get(username).accept();
			return 0;
		}catch(NullPointerException e){
			//richiesta scaduta
			return 1;
		}
	}
	
	public Vector<String> getIndexes(String key){
		return battles.get(key).getWords();
	}
	
	//restituisce il punteggio avversario e comunica il proprio
	public int winner(String username, int score){
		
		//è stato sfidato
		if(battles.containsKey(username)){
			battles.get(username).getLock().lock();
			//aggiorno il mio punteggio
			battles.get(username).setScores(true, score);
			//vedo se il mio avversario ha abbandonato
			if(battles.get(username).isSurrendered()){
				//se ha abbandonato
				battles.get(username).getLock().unlock();
				//cancello la sfida
				battles.remove(username);
				return (Y*K-1);
			}
			//vedo se il mio avversario ha già inserito il suo
			else if(battles.get(username).getScores(true)!=(Y-1)){
				
				//il punteggio è stato inserito e il mio avversario è in wait
				//leggo il punteggio avversario
				int scoreF=battles.get(username).getScores(true);
				//sveglio il thread avversario ed esco
				battles.get(username).getCondition().signal();
				battles.get(username).getLock().unlock();
				return scoreF;
				
			}
			else{
				
				//sono il primo ad inserire e aspetto il mio avversario
				while(!battles.get(username).isSurrendered() &&
						battles.get(username).getScores(true)==(Y-1)){
					try {
						battles.get(username).getCondition().await();
					} catch (InterruptedException e) {
						battles.get(username).getLock().unlock();
						e.printStackTrace();
					}
					
				}
				
				battles.get(username).getLock().unlock();
				
				//il mio avversario ha inserito: da ora non ci sarà più concorrenza
				//sull'oggetto
				int scoreF;
				//vedo se la partita è stata abbandonata
				if(battles.get(username).isSurrendered()){
					scoreF=Y*K-1;
				}
				else{
					//leggo il punteggio avversario
					scoreF=battles.get(username).getScores(true);
				}
				
				//cancello la sfida
				battles.remove(username);
				return scoreF;
				
			}
			
		}
		else{//devo cercare la sfida
			Iterator <String> i =battles.keySet().iterator();
			while(i.hasNext()){
				String k= i.next();
				if(username.equals(battles.get(k).getBattleFrom())){
					//sfida trovata!
					
					battles.get(k).getLock().lock();
					
					//aggiorno il mio punteggio
					battles.get(k).setScores(false, score);
					//vedo se il mio avversario ha abbandonato
					if(battles.get(k).isSurrendered()){
						//se ha abbandonato
						battles.get(k).getLock().unlock();
						//cancello la sfida
						battles.remove(k);
						return (Y*K-1);
					}
					//vedo se il mio avversario ha già inserito il suo
					else if(battles.get(k).getScores(false)!=(Y-1)){
						//il punteggio è stato inserito e il mio avversario è in wait
						//leggo il punteggio avversario
						int scoreF=battles.get(k).getScores(false);
						//sveglio il thread avversario ed esco
						battles.get(k).getCondition().signal();
						battles.get(k).getLock().unlock();
						return scoreF;
						
					}
					else{
						//sono il primo ad inserire e aspetto il mio avversario
						while(!battles.get(k).isSurrendered() &&
								battles.get(k).getScores(false)==(Y-1)){
							try {
								battles.get(k).getCondition().await();
							} catch (InterruptedException e) {
								battles.get(k).getLock().unlock();
								e.printStackTrace();
							}
						}
						battles.get(k).getLock().unlock();
						
						//il mio avversario ha inserito: da ora non ci sarà più concorrenza
						//sull'oggetto
						int scoreF;
						//vedo se la partita è stata abbandonata
						if(battles.get(k).isSurrendered()){
							scoreF=Y*K-1;
						}
						else{
							//leggo il punteggio avversario
							scoreF=battles.get(k).getScores(false);
						}
						
						//cancello la sfida
						battles.remove(k);
						return scoreF;
						
					}
					
					
				}
			}
			//non è possibile averlo come valore di ritorno
			return (Y*K-1);
		}


	}
	
	
	
	
	//abbandona la sfida
	public void leave(String username){
			
		//è stato sfidato
		if(battles.containsKey(username)){
			battles.get(username).getLock().lock();
			//vedo se il mio avversario ha abbandonato la partita
			//se si rimuovo la battaglia ed esco
			if(battles.get(username).isSurrendered()){
				battles.get(username).getLock().unlock();
				//cancello la sfida
				battles.remove(username);
				return;
			}
			else{//sono il primo che abbandona
				
				//indico che ho abbandonato la partita
				battles.get(username).surrender();
				//vedo se il mio avversario ha già inserito il suo punteggio
				if(battles.get(username).getScores(true)!=(Y-1)){
					//il punteggio è stato inserito e il mio avversario è in wait
					//sveglio il thread avversario ed esco
					battles.get(username).getCondition().signal();
					battles.get(username).getLock().unlock();
					return;
				}
				else{
					//sono il primo a finire quindi esco
					battles.get(username).getLock().unlock();
					return;
				}
				 
			}
		}
		else{//devo cercare la sfida
			Iterator <String> i =battles.keySet().iterator();
			while(i.hasNext()){
				String k= i.next();
				if(username.equals(battles.get(k).getBattleFrom())){
					//sfida trovata!
								
					battles.get(k).getLock().lock();
					
					//vedo se il mio avversario ha abbandonato la partita
					//se si rimuovo la battaglia ed esco
					if(battles.get(k).isSurrendered()){
						battles.get(k).getLock().unlock();
						//cancello la sfida
						battles.remove(k);
						return;
					}
					else{//sono il primo che abbandona
						
						//indico che ho abbandonato la partita
						battles.get(k).surrender();
						//vedo se il mio avversario ha già inserito il suo punteggio
						if(battles.get(k).getScores(false)!=(Y-1)){
							
							//il punteggio è stato inserito e il mio avversario è in wait
							//sveglio il thread avversario ed esco
							battles.get(k).getCondition().signal();
							battles.get(k).getLock().unlock();
							return;
						}
						else{
							//sono il primo a finire quindi esco
							battles.get(k).getLock().unlock();
							return;
						}
					}				
				}
			}
		}
	}
	
	
	public void remove(String username){
		battles.remove(username);
	}
	
}
