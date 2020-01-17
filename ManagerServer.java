

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;


//si occupa di leggere da riga di comando e permette di fare un backup prima 
//del tempo o di chiudere il server

public class ManagerServer implements Runnable{

	private Thread backup;
	private int port;
	private ManagerUsers mu;
	
	private String host="localhost";
	
	public ManagerServer(Thread backup, ManagerUsers mu, int port){
		this.backup=backup;
		this.port=port;
		this.mu = mu;
	}
	
	public void run(){

		String command;
		 
		System.out.println("help: ottenere la lista di possibili comandi\n"
				+ "backup: fare un backup dei dati\nshutdown: "
				+ "chiudere il server");
		
		//lo chiudo quando chiudo il server
		try(BufferedReader reader= new BufferedReader(new InputStreamReader(System.in));){
			while((command= reader.readLine())!=null){
				
				if("help".equalsIgnoreCase(command)){
					System.out.println("help: ottenere la lista di possibili comandi\n"
							+ "backup: fare un backup dei dati\nshutdown: "
							+ "chiudere il server");
				}
				
				//richiesta di backup anticipato
				if("backup".equalsIgnoreCase(command)){
					backup.interrupt();
				}
				
				
				//richiesta di chiusura del server
				if("shutdown".equalsIgnoreCase(command)){
					System.out.println("In chiusura");
					mu.setShotdown();
					
					//connessione che fa da "veleno" per il server
					Socket socket= new Socket();
					socket.connect(new InetSocketAddress(host,port));
					socket.close();
					
					break;
				}
				
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
