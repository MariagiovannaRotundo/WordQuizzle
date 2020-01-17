

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

//viene solo letto quindi non possono esserci problemi dovuti a concorrenza


public class Dictionary {

	
	private String nameFile;
	//mantiene tutte le relazioni di amicizia
	private String[] words;
	private int N;
	private String encoding;
	
	public Dictionary(String nameFile, String encoding) {
		
		this.nameFile=nameFile;
		this.encoding=encoding;
		readDictionary();

	}
	
	
	public void readDictionary(){
				
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
					N=0;
				}
				else{
					words=s.split("\r\n");
					N=words.length;
				}
				inChannel.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		else{//il file non esiste
			N=0;
		}
	}
	
	
	public int getN(){
		return N;
	}
	
	
	//parola se ok, 0 se errore
	public String getWord(int i){
		if(i<0 || i>=N)
			return "0";
		else
			
			return words[i];
	}
	
}
