

public class Concurrent implements Runnable{

	private Interfaces i;
	private String s;
	
	public Concurrent(Interfaces i, String s){
		this.i=i;
		this.s=s;
	}
	
	
	public void run(){
		//si occupa dell'accettazione della sfida
		i.askConfirm(s);
	}
	
}
