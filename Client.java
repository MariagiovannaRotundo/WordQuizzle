
public class Client {

	//porta per l'oggetto remoto
	private static int port = 4500;
	//porta per la connessione TCP
	private static int portsocket = 6600;
	private static String host = "localhost";
	//path per le immagini
	private static String pathImages="./src/progetto/immagini/";
	private static String encoding="Windows-1252";
	
	
	public static void main(String[] args) {
	
		if(args.length!=0 && args.length!=2){
			System.out.println("Numero argomenti non valido\n"
					+ "<pathImages><codifica>");
			System.exit(0);
		}
		
		if(args.length==2){
			pathImages=args[0];
			encoding=args[1];
		}
		
		Interfaces i=new Interfaces(port, portsocket, host, pathImages, encoding);
		
		//thread che sta in ascolto sulla socket UDP
		AskBattle b= new AskBattle(i,encoding);
		Thread threadForBattle=new Thread(b);
		threadForBattle.start();
		
		
		//passo la socket così da poterla utilizzare in un evento
		i.setSocketUDP(b.getSocket());
		//passo la porta della socket così da poterla associarla al client nel server
		i.setPortUdp(b.getPortSocket());
		
		//richiamo il metodo che crea l'interfaccia grafica
		i.Start();
		
	}
	
}
