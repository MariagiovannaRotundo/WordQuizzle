
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class AskBattle implements Runnable{

	private Interfaces i;
	private String encoding;
	
	DatagramSocket socketUDP;
	
	//creo la socket su una porta disponibile
	public AskBattle(Interfaces i, String encoding){
		
		this.i=i;
		this.encoding=encoding;
		
		try {
			this.socketUDP = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
				
		//creo un pacchetto per ricevere richieste di sfida
		byte[] buffer=new byte[100];
		DatagramPacket receivedPacketBattle = new DatagramPacket(buffer,buffer.length);

					
		//controllo se ci sono richieste di sfida
		while(true){
								
			try{
				// vedo se ci sono richieste
				socketUDP.receive(receivedPacketBattle);
				//ci sono richieste
				//creo un thread che si occupa di accettare la richiesta
				Concurrent c= new Concurrent(i,new String(receivedPacketBattle.getData(),0, receivedPacketBattle.getLength(), encoding));
				Thread threadForBattle=new Thread(c);
				//faccio partire il nuovo thread
				threadForBattle.start();
				
			}catch (IOException e) {
				//chiudo il client o errore
				break;
			}
								
		}
				
	}
	
	
	public DatagramSocket getSocket(){
		return socketUDP;
	}
	
	public int getPortSocket(){
		return socketUDP.getLocalPort();
	}
	
	
}
