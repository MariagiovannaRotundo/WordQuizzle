

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class RemoteRegistration implements Registration{

	private ManagerFileRegistrations mf;
	private String encoding;
	
	public RemoteRegistration(ManagerFileRegistrations mf, String encoding) throws RemoteException{
		this.mf=mf;
		this.encoding=encoding;
	}
	
	
	
	//0: ok, 1: già registrato, -1: errore
	@SuppressWarnings("unchecked")
	public int addUser(String username, String password)throws RemoteException{
		
		FileChannel outChannel=null;
		
		mf.BeWriter().lock();
		
		//se è già registrato
		int r=mf.isInside(username);
		if(r==1){
			mf.BeWriter().unlock();
			return 1;
		}
			
		//l'utente non è registrato
		r=mf.RemoveCreateFile();
		if(r==1){//se errore
			mf.BeWriter().unlock();
			return -1;
		}
		
		try {
			outChannel = FileChannel.open(Paths.get(mf.getNameFile()),StandardOpenOption.WRITE);
		} catch (IOException e) {
			mf.BeWriter().unlock();
			e.printStackTrace();
			return -1;
		}
		
		JSONObject obj = new JSONObject ();
		
		obj.put("nick", username);
		obj.put("password", password);
		
	
		//aggiungo l'oggetto alla lista
		JSONArray array=mf.AddToArray(obj);
		
		//memorizzo l'oggetto 
		String s=array.toJSONString();
		int n=0;
		int l=s.length();
		ByteBuffer buff = ByteBuffer.allocateDirect(2000);
		try {
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
			mf.BeWriter().unlock();
			e.printStackTrace();
			return -1;
		}
		mf.BeWriter().unlock();
		return 0;
	}
	
}
