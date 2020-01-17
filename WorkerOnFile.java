

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class WorkerOnFile implements Runnable{
	
	//tempo che indica ogni quanto aggiornare i file in caso non è prevista la 
	//chiusura del server. Altrimenti l'aggiornamento vine eseguito subito
	private int time=1000*60*5;//ogni 5 minuti
	
	private ManagerFileFriends mfFriend;
	private String encoding;
	private boolean closed;
	private ManagerUsers mu;
	
	
	public WorkerOnFile(ManagerFileFriends mfFriend, ManagerUsers mu, String encoding){
		this.mfFriend=mfFriend;
		this.encoding=encoding;
		this.closed=false;
		this.mu=mu;
	}
	
	
	public void run(){
		
		while(!closed){
			System.out.println("Dormo");
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				//se il thread si sveglia prima faccio il backup.
				//può essere richiesto un backup "speciale", cioè prima del tempo
			}
			
			closed=mu.getLastBackup();
				
			System.out.println("Salvataggio in corso");
				
			//aggiorno la struttura dati che rappresenta il file
			//lock all'interno del metodo presa prima di scrivere
			mu.update(mfFriend);
			
				
			//leggo la struttura dati aggiornata
			//lock in lettura superflua visto che è l'unico thread che 
			//potrebbe scrivere
			String s= mfFriend.ReadArray().toJSONString();
				
			//scrivo nel file la struttura dati
			mfFriend.RemoveCreateFile();
					
			//apro il file
			//non ho mai concorrenza perchè nessuno legge il file e solo
			//questo thread lo scrive
			try {
				FileChannel outChannel = FileChannel.open(Paths.get(mfFriend.getNameFile()),StandardOpenOption.WRITE);
						
				int n=0;
				int l=s.length();
				ByteBuffer buff = ByteBuffer.allocateDirect(2000);
				while (n<l){
					buff.put(s.getBytes(encoding));
					n+=buff.position();
					s=s.substring(n);
						
					buff.flip();
					while (buff.hasRemaining()){
						outChannel.write(buff);
					}
					buff.clear();
				}
				outChannel.close();
			} catch (IOException e) {
						
				e.printStackTrace();
				return;
			}
					
				
			System.out.println("Salvataggio completato");
				
		}
		
		System.out.println("Ultimo salvataggio completato");
	}
	
}
