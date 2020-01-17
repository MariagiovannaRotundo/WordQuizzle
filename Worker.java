

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Worker implements Runnable{
	
	private String nameServer="localhost";
	private Socket client;
	private ManagerFileRegistrations mf;
	private ManagerFileFriends mfFriend;
	private ManagerBattles mb;
	private String encoding;
	private ManagerUsers mu;
	private Translates t;
	
	public  Worker(Socket client, ManagerFileRegistrations mf, ManagerFileFriends mfFriend, ManagerUsers mu, ManagerBattles mb, String encoding ) throws IOException{
		this.client=client;
		this.mf=mf;
		this.mfFriend=mfFriend;
		this.mb=mb;
		this.encoding=encoding;
		this.mu=mu;
		this.t=Translates.getInstance();
	}
	
	
	@SuppressWarnings("unchecked")
	public void run(){
		
		
		//N,K,X,Y,Z,T1,T2;
		int K=ParametersBattle.getK();
		int X=ParametersBattle.getX();
		int Y=ParametersBattle.getY();
		int Z=ParametersBattle.getZ();
		//tempo di attesa della risposta	
		int T2=ParametersBattle.getT2();
		
		int nWord=0;
		int nSkip=0, nWrong=0, nCorrect=0;
		Vector<String> v=null;
		
		String username=null;
		
		String command;
		try(BufferedReader reader= new BufferedReader(new InputStreamReader(client.getInputStream(), encoding));
			BufferedWriter writer= new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), encoding));
			) {
			
			while((command= reader.readLine())!=null){
				
					//richiesta di login
					if("login".equalsIgnoreCase(command)){
						username= reader.readLine();
						String password= reader.readLine();
						String portUDP= reader.readLine();
						
						String response="notexist\n";
						
						//vedo se esiste un utente con quell'username
						mf.BeReader().lock();
						JSONArray array = mf.ReadArray();
						
						Iterator<JSONObject> iterator = array.iterator();
						while (iterator.hasNext()) {
							JSONObject user=iterator.next();
							String nameOfUser = (String) user.get("nick");
							//esiste
							if(nameOfUser.equals(username)){
								//vedo se la password è corretta
								String pwOfUser = (String) user.get("password");
								//se la password è corretta
								if(pwOfUser.equals(password)){
									//controllo su l'utente è già loggato
									mfFriend.BeReader().lock();
									if(mu.SeeUser(username)){//già presente nella struttura dati
										if(mu.SeeLogUser(username)){//se già loggato
											mfFriend.BeReader().unlock();
											response="already\n";
											mu.setPortUser(username, Integer.parseInt(portUDP));
											break;
										}
										else{//fa il login
											mu.setLogUser(username);
											mfFriend.BeReader().unlock();
											mu.setPortUser(username,Integer.parseInt(portUDP));
											response="ok\n";
											break;
										}
										
									}
									else{//fa il login
										User u=new User(mfFriend.getScore(username),true,mfFriend.getFriends(username),Integer.parseInt(portUDP));
										mu.AddUser(username, u);
										mfFriend.BeReader().unlock();
										response="ok\n";
										break;
									}
								}
								else{//password non corretta
									response="errpw\n";
									break;
								}
								
							}
							
						}
						mf.BeReader().unlock();
						
						//mando la risposta
						writer.write(response);
						writer.flush();
						continue;
						
					}
					
					//richiesta di lettura del punteggio
					if("getScore".equalsIgnoreCase(command)){
						
						Long score=mu.getUserScore(username);
						writer.write(score+"\n");
						writer.flush();
						
						continue;
					}
					
					
					
					
					//richiesta di aggiunta di un amico
					if("aggiungiamico".equalsIgnoreCase(command)){
						// leggo i parametri
						
						String nickAmico= reader.readLine();
						
						int code=-1;
						
						mf.BeReader().lock();
						JSONArray array=mf.ReadArray();
						
						//vedo se esiste un utente con quell'username
						Iterator<JSONObject> iterator = array.iterator();
						while (iterator.hasNext()) {
							String nameOfUser = (String) iterator.next().get("nick");
							//se l'utente esiste
							if(nameOfUser.equals(nickAmico)){
								mfFriend.BeReader().lock();
								//se non è nella tabella hash
								if(!mu.SeeUser(nickAmico)){
									//lo aggiungo alla tabella hash come disconnesso
									User u=new User(mfFriend.getScore(nickAmico),false,mfFriend.getFriends(nickAmico),-1);
									mu.AddUser(nickAmico, u);
								}
								
								// inserisco
								code = mu.addFriend(username, nickAmico);
								mfFriend.BeReader().unlock();
							}
						}
						mf.BeReader().unlock();
						//-1: non esiste, 0: aggiunto, 1:già amici
						writer.write(code+"\n");
						writer.flush();
						
						continue;
					}
					
					
					//richiesta di lettura del punteggio
					if("mostraAmici".equalsIgnoreCase(command)){
						
						JSONObject f=new JSONObject();
						f.put("Friends", mu.getUser(username).getFriends());
						
						writer.write(f+"\n");
						writer.flush();
						
						continue;
					}
					
					
					
					//richiesta di lettura della classifica
					if("mostraClassifica".equalsIgnoreCase(command)){
						
						//leggo gli amici del client
						String list= mu.getUser(username).getFriends();
						//array con la classifica
						JSONArray ranking=new JSONArray();
						
						//se nn ha amici compare in classifica solo lui
						if(list.length()==2){
							JSONObject obj=new JSONObject();
							obj.put("nick", username);
							obj.put("score", mu.getUserScore(username));
							obj.put("position", new Long(0));
							ranking.add(obj);
						}
						else{//ha amici
							list = list.substring(1, list.length()-1);
							String[] friend=list.split(",");
							
							Vector<Long> scores=new Vector<>();
							
							//aggiungo il client
							scores.add(mu.getUserScore(username));
							
							for(int i=0;i<friend.length;i++){
								
								//vedo se è nella hash map
								//se c'è
								mfFriend.BeReader().lock();
								if(mu.SeeUser(friend[i].trim())){
									scores.add(mu.getUserScore(friend[i].trim()));
									
								}
								else{//se non c'è
									Long l=new Long(mfFriend.getScore(friend[i].trim()));
									scores.add(l);
								}
								mfFriend.BeReader().unlock();
							}
							
							//ordino il vettore
							Collections.sort(scores); 
							Collections.reverse(scores);
							
							Long s=mu.getUserScore(username);
							
							//aggiungo il client
							JSONObject obj=new JSONObject();
							obj.put("nick", username);
							obj.put("score",s );
							obj.put("position", scores.indexOf(s));
							scores.set(scores.indexOf(s), new Long(-1));
							ranking.add(obj);
							
							
							//creo un oggetto json per utente
							for(int i=0;i<friend.length;i++){
								
								obj=new JSONObject();
								
								obj.put("nick", friend[i].trim());
								
								mfFriend.BeReader().lock();
								if(mu.SeeUser(friend[i].trim())){
									s=mu.getUserScore(friend[i].trim());
									
								}
								else{//se non c'è
									s=new Long(mfFriend.getScore(friend[i].trim()));
								}
								mfFriend.BeReader().unlock();
								obj.put("score",s);
								obj.put("position", scores.indexOf(s));
								
								scores.set(scores.indexOf(s),new Long(-1));
								
								ranking.add(obj);
							}
							
						}
						
						writer.write(ranking+"\n");
						writer.flush();
						
						continue;
					}
					
					
					//richiesta di disconnessione
					if("logout".equalsIgnoreCase(command)){
						// rimuoverlo dai client loggati
						
						mu.setPortUser(username, -1);
						mu.setLogUser(username);
						client.close();
						
						break;
					}
					
					
					//richiesta di sfida
					if("sfida".equalsIgnoreCase(command)){
	
						String nickAmico= reader.readLine();
						String code="-1";
						
						//faccio i controlli sull'utente amico
						mf.BeReader().lock();
						JSONArray array=mf.ReadArray();
						
						//vedo se esiste un utente con quell'username
						Iterator<JSONObject> iterator = array.iterator();
						while (iterator.hasNext()) {
							String nameOfUser = (String) iterator.next().get("nick");
							//se l'utente esiste
							if(nameOfUser.equals(nickAmico)){
								//controllo se è nella lista amici
								if(mu.areFriends(username, nickAmico)){
									//controllo se è connesso
									if(mu.SeeUser(nickAmico) && mu.SeeLogUser(nickAmico)){
										try {
											
											//inserisco la richiesta di sfida
											if(mb.challenge(nickAmico, username, Thread.currentThread())==0){
												//mando la richiesta di sfida al client
												DatagramSocket socketUDP;
												
												socketUDP = new DatagramSocket();
												
												//mando la richiesta di sfida (UDP)
												//prende il nome (per l'indirizzo) del server
												InetAddress address = InetAddress.getByName(nameServer);
												
												byte[] buffer=username.getBytes();
													
												//prende la porta del server associata al thread amico
												DatagramPacket mypacket = new DatagramPacket(buffer,username.length(),address,mu.getPortUser(nickAmico));
												//mando
												socketUDP.send(mypacket);
												
												//chiudo la socket per i pacchetti UDP
												socketUDP.close();
												
												//attendo una risposta
												int r=mb.seeResponse(nickAmico);
												//le parole sono già state generate e tradotte
												
												
												//sfida accettata
												if(r==0){
													v=mb.getIndexes(nickAmico);
													//sfida
													code="0\n"+T2+"\nAvete "+T2/1000+" secondi per tradurre correttamente "
															+ K+" parole: \n"+v.get(nWord);
													nWord++;
													break;
												}
												else{//sfida rifiutata
													code="2";
													break;
												}
											}
											else{
												//considero la richiesta rifiutata 
												code="2";
												break;
											}
										} catch (SocketException e) {
											System.out.println("Errore creazione socket");
											e.printStackTrace();
											code="2";
											break;
										}
									}
									else{
										//considero la richiesta rifiutata 
										code="2";
										break;
									}
								}
								else{//non sono amici
									code="1";
									break;
								}
							}
						}
						mf.BeReader().unlock();
						//utente inesistente
						writer.write(code+"\n");
						writer.flush();
						
						continue;
					}
					
					
					//una sfida è stata rifiutata
					if("notconfirm".equalsIgnoreCase(command)){
						
						mb.reject(username);
						continue;
					}
					
					
					
					//una sfida è stata accettata
					//1:sfida scaduta, 0:vai alla sfida, 2:errore
					if("confirm".equalsIgnoreCase(command)){
						int r;
						r=mb.accept(username);
						
						//genero i numeri casuali e sveglio l'altro thread
						if(r==0){//la richiesta è stata accettata, genero le parole
							r=mb.generate(username);
							if(r==2){//errore: devo rimuovere la sfida
								mb.remove(username);
							}
						}
						
						String code=Integer.toString(r);
						if(code.equals("0")){
							v=mb.getIndexes(username);
							code+="\n"+T2+"\nAvete "+T2/1000+" secondi per tradurre correttamente "
									+ K+" parole: \n"+v.get(nWord);
							nWord++;
						}
						
						writer.write(code+"\n");
						writer.flush();
						
						continue;
					}
					
						
					//richiesta di sfida
					if("saltaparola".equalsIgnoreCase(command)){
						
						String s=null;
						
						//se ha finito le parole, le ha mandate tutte
						if(nWord==K){
							
							nSkip=K-nCorrect-nWrong;
							
							//calcolo i punti
							int scoreBattle=nCorrect*X+nWrong*Y;
							
							int scoreFriend=mb.winner(username, scoreBattle);
							
							//se ha vinto sull'avversario
							if(scoreFriend==(Y*K-1)){
								
								s="end\nCONGRATULAZIONI. HAI VINTO!/"
										+ "Hai tradotto correttamente "+nCorrect+
										" parole, ne hai sbagliate "+nWrong+
										" e non hai risposto a "+nSkip+"./"+ "Hai totalizzato "
										+scoreBattle+" punti mentre il tuo avversario ha "
										+ "abbandonato la partita./Hai guadagnato "
										+Z+" punti extra";
								scoreBattle+=Z;
							}
							else if(scoreBattle>scoreFriend){
								
								s="end\nCONGRATULAZIONI. HAI VINTO!/"
									+ "Hai tradotto correttamente "+nCorrect+
									" parole, ne hai sbagliate "+nWrong+
									" e non hai risposto a "+nSkip+"./"+ "Hai totalizzato "
									+scoreBattle+" punti mentre il tuo avversario ha "
									+ "totalizzato "+scoreFriend+" punti./Hai guadagnato "
									+Z+" punti extra";
								scoreBattle+=Z;
							}
							else if(scoreBattle<scoreFriend){
								s="end\nHAI PERSO./"
									+ "Hai tradotto correttamente "+nCorrect+
									" parole, ne hai sbagliate "+nWrong+
									" e non hai risposto a "+nSkip+"./"+ "Hai totalizzato "
									+scoreBattle+" punti mentre il tuo avversario ha "
									+ "totalizzato "+scoreFriend+" punti.";
							}
							else{
								s="end\nPAREGGIO!/"
									+ "Hai tradotto correttamente "+nCorrect+
									" parole, ne hai sbagliate "+nWrong+
									" e non hai risposto a "+nSkip+"./"+ "Hai totalizzato "
									+scoreBattle+" punti.";
							}
							
							nCorrect=0;
							nWrong=0;
							nSkip=0;
							
							mu.addUserScore(username, scoreBattle);
							
							nWord=0;
							v=null;
							
						}
						else{//leggo la parola successiva
							s=v.get(nWord);
							nWord++;
						}
						
						writer.write(s+"\n");
						writer.flush();
						
					}
					
					//richiesta di parola successiva
					if("inviaparola".equalsIgnoreCase(command)){
						
						//leggo la parola mandata dal client
						String wordToTranslate= reader.readLine();
						
						//vedo se la traduzione è corretta
						if(t.isCorrect(v.get(nWord-1),wordToTranslate)){
							nCorrect++;
						}
						else{
							nWrong++;
						}
						
						String s=null;
						
						//se ha finito le parole, le ha mandate tutte
						if(nWord==K){
							
							nSkip=K-nCorrect-nWrong;
							
							//calcolo i punti
							int scoreBattle=nCorrect*X+nWrong*Y;
							
							int scoreFriend=mb.winner(username, scoreBattle);
							
							//se ha vinto sull'avversario
							if(scoreFriend==(Y*K-1)){
								s="end\nCONGRATULAZIONI. HAI VINTO!/"
										+ "Hai tradotto correttamente "+nCorrect+
										" parole, ne hai sbagliate "+nWrong+
										" e non hai risposto a "+nSkip+"./"+ "Hai totalizzato "
										+scoreBattle+" punti mentre il tuo avversario ha "
										+ "abbandonato la partita./Hai guadagnato "
										+Z+" punti extra";
								scoreBattle+=Z;
							}
							else if(scoreBattle>scoreFriend){
								s="end\nCONGRATULAZIONI. HAI VINTO!/"
									+ "Hai tradotto correttamente "+nCorrect+
									" parole, ne hai sbagliate "+nWrong+
									" e non hai risposto a "+nSkip+"./"+ "Hai totalizzato "
									+scoreBattle+" punti mentre il tuo avversario ha "
									+ "totalizzato "+scoreFriend+" punti./Hai guadagnato "
									+Z+" punti extra";
								scoreBattle+=Z;
							}
							else if(scoreBattle<scoreFriend){
								s="end\nHAI PERSO./"
									+ "Hai tradotto correttamente "+nCorrect+
									" parole, ne hai sbagliate "+nWrong+
									" e non hai risposto a "+nSkip+"./"+ "Hai totalizzato "
									+scoreBattle+" punti mentre il tuo avversario ha "
									+ "totalizzato "+scoreFriend+" punti.";
							}
							else{
								s="end\nPAREGGIO!/"
									+ "Hai tradotto correttamente "+nCorrect+
									" parole, ne hai sbagliate "+nWrong+
									" e non hai risposto a "+nSkip+"./"+ "Hai totalizzato "
									+scoreBattle+" punti.";
							}
							
							nCorrect=0;
							nWrong=0;
							nSkip=0;
							
							mu.addUserScore(username, scoreBattle);
							
							nWord=0;
							v=null;
							
						}
						else{//leggo la parola successiva
							s=v.get(nWord);
							nWord++;
						}
						
						writer.write(s+"\n");
						writer.flush();
						
					}
					
					
					
					//richiesta di sfida
					if("scaduto".equalsIgnoreCase(command)){
						nSkip=K-nCorrect-nWrong;
						
						String s;
						
						//calcolo i punti
						int scoreBattle=nCorrect*X+nWrong*Y;
							
						int scoreFriend=mb.winner(username, scoreBattle);
							
						//se ha vinto sull'avversario
						if(scoreFriend==(Y*K-1)){
							
							s="CONGRATULAZIONI. HAI VINTO!/"
									+ "Hai tradotto correttamente "+nCorrect+
									" parole, ne hai sbagliate "+nWrong+
									" e non hai risposto a "+nSkip+"./"+ "Hai totalizzato "
									+scoreBattle+" punti mentre il tuo avversario ha "
									+ "abbandonato la partita./Hai guadagnato "
									+Z+" punti extra";
							scoreBattle+=Z;
						}
						else if(scoreBattle>scoreFriend){
							
							s="CONGRATULAZIONI. HAI VINTO!/"
									+ "Hai tradotto correttamente "+nCorrect+
									" parole, ne hai sbagliate "+nWrong+
									" e non hai risposto a "+nSkip+"./"+ "Hai totalizzato "
									+scoreBattle+" punti mentre il tuo avversario ha "
									+ "totalizzato "+scoreFriend+" punti./Hai guadagnato "
									+Z+" punti extra";
							scoreBattle+=Z;
						}
						else if(scoreBattle<scoreFriend){
							s="HAI PERSO./"
									+ "Hai tradotto correttamente "+nCorrect+
									" parole, ne hai sbagliate "+nWrong+
									" e non hai risposto a "+nSkip+"./"+ "Hai totalizzato "
									+scoreBattle+" punti mentre il tuo avversario ha "
									+ "totalizzato "+scoreFriend+" punti.";
						}
						else{
							s="PAREGGIO!/"
									+ "Hai tradotto correttamente "+nCorrect+
									" parole, ne hai sbagliate "+nWrong+
									" e non hai risposto a "+nSkip+"./"+ "Hai totalizzato "
									+scoreBattle+" punti.";
						}
							
						nCorrect=0;
						nWrong=0;
						nSkip=0;
							
						mu.addUserScore(username, scoreBattle);
							
						nWord=0;
						v=null;
							
						writer.write(s+"\n");
						writer.flush();
						
					}
					
					
					//richiesta di uscita dalla battaglia
					//il punteggio della battaglia è 0
					if("uscitabattaglia".equalsIgnoreCase(command)){
					
						//si è arreso
						mb.leave(username);
							
						nCorrect=0;
						nWrong=0;
						nSkip=0;
							
						nWord=0;
						v=null;
						
					}
					
			}
			
			
		}catch (IOException e) {
			//client disconnesso in modo anomalo
			//devo toglierlo dai login così che possa accedere la volta successiva
			mu.setLogUser(username);
			try {
				client.close();
				System.out.println("Client disconnesso in modo anomalo");
			} catch (IOException e1) {
				System.out.println("Errore disconnessione anomala del client");
			}
			
		}
	}
	
}
