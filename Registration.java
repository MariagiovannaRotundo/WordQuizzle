

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Registration extends Remote{

	public final static String SERVICE_NAME = "Registration";
	
	public int addUser(String username, String password)throws RemoteException;
	
}
