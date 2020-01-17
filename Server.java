

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//codifica di default Windows no UTF-8


public class Server {
	
	//porta per l'oggetto remoto
	private static int port = 4500 ;
	//porta per la connessione TCP
	private static int portsocket = 6600 ;
	//path del file con gli utenti
	private static String pathRegistry="./src/progetto/Utenti.json";
	//path del file con le relazioni di amicizia
	private static String pathFriends="./src/progetto/Amicizie.json";
	//path del file con le parole da tradurre
	private static String pathWords="./src/progetto/Dizionario.txt";
	//per l'encoding
	private static String encoding="Windows-1252";
	
	@SuppressWarnings("unused")
	public static void main(String[] args)  {
		
		Registry r=null;
		RemoteRegistration registration=null;
		
		if(args.length!=0 && args.length!=4){
			System.out.println("Numero di argomenti non valido\n<path file utenti>"
					+ "<path punteggi><path dizionario><encoding>");
			System.exit(0);
		}
		if(args.length==4){
			pathRegistry=args[0];
			pathFriends=args[1];
			pathWords=args[2];
			encoding=args[3];
		}
		

		//gestore degli utenti attivi
		ManagerUsers mu= new ManagerUsers();
		//gestore del file di registrazione
		ManagerFileRegistrations mf= new ManagerFileRegistrations(pathRegistry, encoding);
		//gestore del file di amicizia
		ManagerFileFriends mfFriend=new ManagerFileFriends(pathFriends, encoding);
		//parametri per la battaglia
		ParametersBattle pb= new ParametersBattle();
		//gestore delle battaglie
		ManagerBattles mb=new ManagerBattles(pathWords, encoding);
		
		//controllo se i parametri sono validi
		//N,K,X,Y,Z,T1,T2;
		if(mb.getN()==0 || ParametersBattle.getK()<=0 || ParametersBattle.getX()<=0 || 
				ParametersBattle.getY()>=0 || ParametersBattle.getZ()<0 ||
					ParametersBattle.getT1()<=0 || ParametersBattle.getT2()<=0
					||mb.getN()<ParametersBattle.getK()){
			System.out.println("Valori per la sfida non validi");
			System.exit(0);
		}
		
		
		
		try{
			//creo l'oggetto remoto e gli passo il file degli utenti
			registration= new RemoteRegistration(mf, encoding);
			//esporto l'oggetto remoto creato
			Registration stub = (Registration)UnicastRemoteObject.exportObject(registration, 0);
			r =LocateRegistry.createRegistry(port);
			
			//pubblico lo stub nel registry 
			r.rebind(Registration.SERVICE_NAME, stub);

		}catch (RemoteException e) {
            System.out.println("Errore durante l'impostazione del server RMI: " + e.getMessage());
        }
		
		//thread che salva su file
		WorkerOnFile wf= new WorkerOnFile(mfFriend, mu, encoding);
		Thread threadForFile=new Thread(wf);
		threadForFile.start();
		
		//thread per gestire il server
		ManagerServer ms= new ManagerServer(threadForFile, mu, portsocket);
		Thread manager=new Thread(ms);
		manager.start();
		
		//pool di thread
		ThreadPoolExecutor executor= (ThreadPoolExecutor)Executors.newCachedThreadPool();
		
		
		//socket per connettersi al server e login
		try (ServerSocket server = new ServerSocket(portsocket);){
			
			//acetto le connessioni con i client e li passo ai thread
			while(!mu.getShotdown()){
			
				Socket client=server.accept();	
				executor.execute(new Worker(client, mf, mfFriend, mu, mb, encoding));
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
			
			
		//non permetto altre registrazioni
		try {
				r.unbind(Registration.SERVICE_NAME);
				UnicastRemoteObject.unexportObject(registration,false);
			} catch (NotBoundException e) {
			} catch (AccessException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			} 
			
		//devo chiudere il server
		//aspetto che i client finiscano ma segnalo al pool la chiusura
			
		executor.shutdown();
		//aspetto che il pool sia chiuso
			
		boolean flag=false;
		while(!flag){
			try{
				flag=executor.awaitTermination(60, TimeUnit.MILLISECONDS);
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			
		System.out.println("Pool terminato correttamente");
			
		//faccio il backup finale
		mu.setLastBackup();
		threadForFile.interrupt();
		try {
			threadForFile.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			
		System.out.println("Server chiuso");
			
	}

}
