

import javax.swing.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;



public class Interfaces implements ActionListener{

	//porta per l'oggetto remoto
	private int port;
	//porta per la connessione TCP
	private int portsocket;
    private String host;
    //path per le immagini
    private String pathImages;
    private String encoding;
    
    int portUDP;
    DatagramSocket socketUDP;
    
    String UserScore;
    String infoBattle;
    String T2;
    
    Timer timer;
    
    String username, firstword;
    Socket socket;
    BufferedReader reader;
    BufferedWriter writer;
    
	//creo il frame
	JFrame f;
	
	JTextField t1, t2, tword, ttime;
	JTextArea ta,ta1;
	JLabel lword;
	
	public Interfaces(int port, int portsocket, String host, String pathImages, String encoding ){
		
		this.port =port;
		this.portsocket = portsocket;
	    this.host = host;
	    this.pathImages="./src/progetto/immagini/";
	    this.encoding=encoding;
		
		this.UserScore="0";
		this.infoBattle="0";
		this.socket=null;
		this.reader=null;
		this.writer=null;
		
	}
	
	
	
	//interfaccia iniziale
	public void Start(){
		f=new JFrame("Word Quizzle");
		Container c=f.getContentPane();
		
		JPanel p0=new JPanel();
		p0.setBackground(Color.WHITE);
				
		JLabel logo=new JLabel(new ImageIcon(pathImages+"lettere.jpg"));
		JLabel accedi=new JLabel(new ImageIcon(pathImages+"accedi.jpg"));
		
		
		//pannello per le credenziali
		JPanel p1=new JPanel();
		p1.setBackground(Color.WHITE);
		p1.setLayout(new GridLayout(4,1,5,20));
		
		JLabel username=new JLabel("Inserisci il nickname");
		username.setFont(new Font("sansserif",Font.PLAIN,40));
		JLabel password=new JLabel("Inserisci la password");
		password.setFont(new Font("sansserif",Font.PLAIN,40));
		t1=new JTextField("",20);	
		t2=new JTextField("",20);

		p1.add(username);
		p1.add(t1).setFont(new Font("sansserif",Font.PLAIN,40));
		p1.add(password);
		p1.add(t2).setFont(new Font("sansserif",Font.PLAIN,40));
		
		JLabel empty=new JLabel("                              ");
		empty.setFont(new Font("sansserif",Font.PLAIN,70));
		
		JPanel p2=new JPanel();
		p2.setBackground(Color.WHITE);
		p2.setLayout(new GridLayout(2,1,5,10));
		
		
		JButton b1=new JButton(new ImageIcon(pathImages+"accedibottone.png"));
		JButton b2=new JButton(new ImageIcon(pathImages+"Registrati.png"));
		
		p2.add(b1);
		p2.add(b2);
		
		p0.add(logo);
		p0.add(accedi);
		p0.add(p1);
		p0.add(empty);
		p0.add(p2);
		
		//aggiungo i pannelli al frame
		c.add(p0);
				
		//stabilisco la dimensione del frame, la sua posizione sullo schermo 
		//alla creazione e lo rendo visibile. Quando chiudo la finestra 
		//il client si disconnette
		f.setSize(905,1000);
		f.setLocation(550, 5);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		f.setResizable(false);
		f.setIconImage(new ImageIcon(pathImages+"icona.png").getImage());
		
		//registro gli ascoltatori
		b1.setActionCommand("accedi");
		b2.setActionCommand("registrati");
		b1.addActionListener(this);
		b2.addActionListener(this);
		
	}
	
	
	
	
	//interfaccia dopo aver fatto l'accesso
	public void home(){
	
		f.dispose();
		f=new JFrame("Word Quizzle");
		Container c=f.getContentPane();
		
		//creo dei tab per le operazioni. uno per ogni operazione dell'utente
		JTabbedPane tab=new JTabbedPane();
		tab.setTabPlacement(JTabbedPane.NORTH);
		
		//pannello del tab
		JPanel p0=new JPanel();
		JPanel p1=new JPanel();
		JPanel p2=new JPanel();
		JPanel p3=new JPanel();
		JPanel p4=new JPanel();
		
		//aggiunta delle icone e del testo ai tab
		tab.addTab("", new ImageIcon(pathImages+"sfida.png"), p0, "sfida un amico!");
		tab.addTab("", new ImageIcon(pathImages+"64508.png"), p1, "clicca per aggiungere un amico");
		tab.addTab("", new ImageIcon(pathImages+"icona-amici.png"), p2, "clicca per vedere gli amici");
		tab.addTab("", new ImageIcon(pathImages+"medaglia.png"), p3, "clicca per vedere la classifica");
		tab.addTab("", new ImageIcon(pathImages+"logout.png"), p4, "Logout");

		//creo la home per la sfida
		p0.setLayout(new BorderLayout());
		p0.setBackground(Color.WHITE);
		
		//pannello per i punti
		JPanel pscore=new JPanel();
		pscore.setBackground(Color.WHITE);
		JLabel lpunti=new JLabel(new ImageIcon(pathImages+"punteggio.png"));
		
		
		//punteggio dell'utente
		JLabel lscore=new JLabel("Il tuo punteggio : "+UserScore);
		lscore.setFont(new Font("sansserif",Font.PLAIN,40));
		pscore.add(lpunti);
		pscore.add(lscore);
		
		//pannello per la sfida
		JPanel pplay=new JPanel();
		pplay.setBackground(Color.WHITE);
		
		JLabel play=new JLabel("Sfida un amico!");
		play.setFont(new Font("sansserif",Font.PLAIN,70));
		JLabel againstSomeone=new JLabel("Inserisci il nickname di chi vuoi sfidare");
		againstSomeone.setFont(new Font("sansserif",Font.PLAIN,35));
		againstSomeone.setPreferredSize(new Dimension(600,140));
		againstSomeone.setVerticalAlignment(SwingConstants.BOTTOM);
		JLabel empty2=new JLabel("                             ");
		empty2.setFont(new Font("sansserif",Font.PLAIN,70));
		
		t1=new JTextField("",20);

		JButton b1=new JButton(new ImageIcon(pathImages+"bottonesfida2.png"));
		pplay.add(play);
	
		pplay.add(againstSomeone);
		pplay.add(t1).setFont(new Font("sansserif",Font.PLAIN,40));
		pplay.add(empty2);
		pplay.add(b1);
		
		p0.add(pscore,BorderLayout.NORTH);
		p0.add(pplay,BorderLayout.CENTER);

		//pannello per l'aggiunta degli amici :p1
		p1.setLayout(new BorderLayout());
		p1.setBackground(Color.WHITE);
		
		//pannello per i punti
		JPanel pimage=new JPanel();
		pimage.setBackground(Color.WHITE);
		JLabel lp1=new JLabel(new ImageIcon(pathImages+"AggiungiAmico.jpg"));
		pimage.add(lp1);
		
		//pannello per il corpo
		JPanel paddFriend=new JPanel();
		paddFriend.setBackground(Color.WHITE);
		
		JLabel addSomeone=new JLabel("Inserisci il nickname di chi vuoi aggiungere");
		addSomeone.setFont(new Font("sansserif",Font.PLAIN,35));
		addSomeone.setPreferredSize(new Dimension(700,140));
		addSomeone.setVerticalAlignment(SwingConstants.BOTTOM);
		addSomeone.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel empty4=new JLabel("                                               ");
		empty4.setFont(new Font("sansserif",Font.PLAIN, 70));
		
		t2=new JTextField("",20);

		JButton b2=new JButton(new ImageIcon(pathImages+"bottoneAggiungi.png"));

		paddFriend.add(addSomeone);
		paddFriend.add(t2).setFont(new Font("sansserif",Font.PLAIN,40));
		paddFriend.add(empty4);
		paddFriend.add(b2);
		
		p1.add(pimage,BorderLayout.NORTH);
		p1.add(paddFriend,BorderLayout.CENTER);
		
		
		//pannello per gli amici :p2
		p2.setLayout(new BorderLayout());
		p2.setBackground(Color.WHITE);
		
		JLabel lp2=new JLabel(new ImageIcon(pathImages+"P2.jpg"));
		
		JLabel lleft=new JLabel(new ImageIcon(pathImages+"left.jpg"));
		JLabel lright=new JLabel(new ImageIcon(pathImages+"right.jpg"));
		
		JPanel left=new JPanel();
		left.setBackground(Color.WHITE);
		
		left.add(lleft);
		
		JPanel right=new JPanel();
		right.setBackground(Color.WHITE);
		
		right.add(lright);
	
		ta=new JTextArea();
		ta.setEditable(false);
		ta.setAutoscrolls(false);
		
		JScrollPane scroll=new JScrollPane(ta);
		
		ta.setText("\n\n\n      Nessun amico da\n             mostrare.\n    Aggiungi Qualcuno!");
		
		
		scroll.setBorder(BorderFactory.createEmptyBorder());
		ta.setFont(new Font("serif",Font.PLAIN,53));
		
		scroll.getVerticalScrollBar().setPreferredSize( new Dimension(0,0) );
		scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0,15));
		
		p2.add(lp2,BorderLayout.NORTH);
		p2.add(scroll,BorderLayout.CENTER);
		p2.add(left,BorderLayout.WEST);
		p2.add(right,BorderLayout.EAST);
		
		//pannello per la classifica :p3
		p3.setLayout(new BorderLayout());
		p3.setBackground(Color.WHITE);
				
		//pannello per l'intestazione
		JPanel pclass=new JPanel();
		pclass.setBackground(Color.WHITE);
		JLabel classifica=new JLabel(new ImageIcon(pathImages+"podioOro.png"));
				
		pclass.add(classifica);
				
		JLabel lleft2=new JLabel(new ImageIcon(pathImages+"left2.jpg"));
		JLabel lright2=new JLabel(new ImageIcon(pathImages+"right2.jpg"));
				
		JPanel left2=new JPanel();
		left2.setBackground(Color.WHITE);
				
		left2.add(lleft2);
				
		JPanel right2=new JPanel();
		right2.setBackground(Color.WHITE);
				
		right2.add(lright2);
			
		ta1=new JTextArea();
		ta1.setEditable(false);
		ta1.setAutoscrolls(false);
				
		JScrollPane scroll1=new JScrollPane(ta1);
			
		ta1.setText("");
				
		scroll1.setBorder(BorderFactory.createEmptyBorder());
		ta1.setFont(new Font("serif",Font.PLAIN,45));
		scroll1.getVerticalScrollBar().setPreferredSize( new Dimension(0,0) );
		scroll1.getHorizontalScrollBar().setPreferredSize(new Dimension(0,15));
				
		p3.add(pclass,BorderLayout.NORTH);
		p3.add(scroll1,BorderLayout.CENTER);
		p3.add(left2,BorderLayout.WEST);
		p3.add(right2,BorderLayout.EAST);
		
		
		//pannello di logout
		p4.setBackground(Color.WHITE);
		
		JLabel leave=new JLabel("Sicuro di voler uscire?");
		leave.setFont(new Font("sansserif",Font.PLAIN,70));
		JLabel limm=new JLabel(new ImageIcon(pathImages+"imm.jpg"));
		JButton b3=new JButton(new ImageIcon(pathImages+"esci.png"));
		p4.add(leave);
		p4.add(b3);
		p4.add(limm);
		
		
		//aggiungo i pannelli al frame
		c.add(tab);
		
		//stabilisco la dimensione del frame, la sua posizione sullo schermo 
		//alla creazione e lo rendo visibile. Quando chiudo la finestra 
		//il client si disconnette
		f.setSize(905,1000);
		f.setLocation(550, 5);
		f.setVisible(true);
		f.setResizable(false);
		f.setIconImage(new ImageIcon(pathImages+"icona.png").getImage());
		
		//registro gli ascoltatori
		b1.setActionCommand("sfida");
		b3.setActionCommand("esci");
		b2.setActionCommand("aggiungi");
		b1.addActionListener(this);
		b3.addActionListener(this);
		b2.addActionListener(this);
		
		p2.addComponentListener(new PanelAmici());
		p3.addComponentListener(new PanelClassifica());
		f.addWindowListener(new ListenFrame());
		
		
		
	}
	
	
	//interfaccia all'interno della sfida
	public void battle(){
		f.dispose();
		f=new JFrame("Word Quizzle");
		Container c=f.getContentPane();
		
		JPanel pbattle=new JPanel();
		pbattle.setLayout(new BorderLayout());
		pbattle.setBackground(Color.WHITE);
		
		JLabel ltime=new JLabel(infoBattle);
		ltime.setFont(new Font("sansserif",Font.PLAIN,30));
		
		
		JPanel pnord=new JPanel();
		pnord.setBackground(Color.WHITE);
		
		String time=Integer.toString((Integer.parseInt(T2)/1000));
		
		//textfield per il conto alla rovescia
		ttime=new JTextField(time,3);
		ttime.setFont(new Font("sansserif",Font.PLAIN,30));
		ttime.setEditable(false);
		ttime.setBackground(Color.WHITE);
		ttime.setBorder(BorderFactory.createEmptyBorder());
		
		
		JLabel empty=new JLabel(" ");
		empty.setFont(new Font("sansserif",Font.PLAIN,60));
		empty.setPreferredSize(new Dimension(700,100));
		
		JPanel ptranslate=new JPanel();
		ptranslate.setBackground(Color.WHITE);
		
		JLabel ltranslate=new JLabel(new ImageIcon(pathImages+"Traduci.png"));
		
		
		//parola da tradurre
		lword=new JLabel(firstword);
		lword.setFont(new Font("sansserif",Font.PLAIN,60));
		lword.setPreferredSize(new Dimension(500,250));
		lword.setHorizontalAlignment(SwingConstants.CENTER);
		
		tword=new JTextField("",20);

		
		JButton b1=new JButton(new ImageIcon(pathImages+"salta.png"));
		JButton b2=new JButton(new ImageIcon(pathImages+"Invia.png"));
		
		ptranslate.add(ltranslate);
		ptranslate.add(lword);
		ptranslate.add(tword).setFont(new Font("sansserif",Font.PLAIN,40));
		ptranslate.add(empty);
		ptranslate.add(b1);
		ptranslate.add(b2);
		
		//pbattle.add(ltime, BorderLayout.NORTH);
		pnord.add(ltime);
		pnord.add(ttime);
		
		pbattle.add(pnord, BorderLayout.NORTH);
		pbattle.add(ptranslate, BorderLayout.CENTER);
		
		
		//aggiungo i pannelli al frame
		c.add(pbattle);
		
		f.setSize(905,1000);
		f.setLocation(550, 5);
		f.setVisible(true);
		f.setResizable(false);
		f.setIconImage(new ImageIcon(pathImages+"icona.png").getImage());

		
		//registro gli ascoltatori
		b1.setActionCommand("salta");
		b2.setActionCommand("invia");
		b1.addActionListener(this);
		b2.addActionListener(this);
		
		f.addWindowListener(new ExitBattle());
		
		//creo un conto alla rovescia
		//il thread mi gestisce anche il timer
		timer = new Timer(1000 ,null);
        timer.setRepeats(true);;
        ActionListener taskPerformer=new timeout(timer);
        
        timer.addActionListener(taskPerformer);
		//ogni secondo esegue taskPerformer
        timer.start();
		
	}
	
	
	//creo gli ascoltatori
	public void actionPerformed(ActionEvent e){
		if("accedi".equals(e.getActionCommand())){
			//leggo i parametri inseriti dall'utente
			String u = t1.getText().trim();
			String p = t2.getText().trim();
			this.username=u;
			JLabel error = new JLabel("");
			error.setFont(new Font("Arial", Font.PLAIN, 30));
			//se non sono validi errore
			if(u.equals("") || p.equals("")|| u.length()>20 || p.length()>20){
				error.setText("username e/o password errati");
				JOptionPane.showMessageDialog(null, error, "Parametri non corretti", JOptionPane.ERROR_MESSAGE);
			}
			else{
				//mi connetto al server
				try{
					//creo la socket per connettermi al server
					socket= new Socket();
					socket.connect(new InetSocketAddress(host,portsocket));
					reader= new BufferedReader(new InputStreamReader(
						socket.getInputStream(), encoding));
					writer= new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream(), encoding));
					
					
					//faccio il login
					writer.write("login\n"+u+"\n"+p+"\n"+portUDP+"\n");
					writer.flush();
					//leggo la risposta
					String response= reader.readLine();
					
					//se va bene -> home (OK) (altrimenti errore)
					if("ok".equalsIgnoreCase(response)){
						writer.write("getScore\n");
						writer.flush();
						//leggo la risposta
						UserScore= reader.readLine();
						this.home();
					}
					else if("already".equalsIgnoreCase(response)){
						error.setText("Utente già loggato");
						JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
						reader.close();
						writer.close();
						socket.close();
					}
					else if("notexist".equalsIgnoreCase(response)){
						error.setText("Utente inesistente: registrarsi");
						JOptionPane.showMessageDialog(null, error, "Parametri non corretti", JOptionPane.ERROR_MESSAGE);
						reader.close();
						writer.close();
						socket.close();
					}
					else if("errpw".equalsIgnoreCase(response)){
						error.setText("Password errata");
						JOptionPane.showMessageDialog(null, error, "Parametri non corretti", JOptionPane.ERROR_MESSAGE);
						reader.close();
						writer.close();
						socket.close();
					}
				}catch (SocketException err) {
					error.setText("Errore");
					JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
					System.out.println("Server closed .");
				} catch (UnknownHostException err) {
					error.setText("Errore");
					JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
					//chiudo la connessione UDP
					closeUdp();
					f.dispose();
					err.printStackTrace();
				} catch (IOException err) {
					error.setText("Errore");
					JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
					//chiudo la connessione UDP
					closeUdp();
					f.dispose();
					System.out.println("Server closed connection or an error appeared.");
				}	
			}
		}
		
		
		if("registrati".equals(e.getActionCommand())){
			//leggo i parametri inseriti dall'utente
			String u = t1.getText().trim();
			String p = t2.getText().trim();
			this.username=u;
			JLabel error = new JLabel("");
			error.setFont(new Font("Arial", Font.PLAIN, 30));
			//errori
			if(u.equals("") || p.equals("")){
				error.setText("username e password non possono essere vuoti");
				JOptionPane.showMessageDialog(null, error, "Parametri non corretti", JOptionPane.ERROR_MESSAGE);
			}
			else if(u.length()>20 || p.length()>20){
				error.setText("I campi possono avere massimo 20 caratteri");
				JOptionPane.showMessageDialog(null, error, "Parametri non corretti", JOptionPane.ERROR_MESSAGE);
			}
			else{
				int choice=-1;
				if(p.length()>0 && p.length()<6){
					error.setText("La password è poco sicura. Continuare?");
					choice=JOptionPane.showConfirmDialog(null, error, "Attenzione", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				}
				//se si oppure se non mi serviva la scelta
				if(choice==0 || choice==-1){
					try {
			            Registry registry = LocateRegistry.getRegistry(host, port);
			            Registration registration = (Registration) registry.lookup(Registration.SERVICE_NAME);
			            int r=registration.addUser(u, p);
			            if(r==0){
			            	error.setText("Registrazione avvenuta con successo!");
							JOptionPane.showMessageDialog(null, error, "Inizia a giocare!", JOptionPane.INFORMATION_MESSAGE);
							
							//dopo la registrazione mi connetto al server
				            socket= new Socket();
							socket.connect(new InetSocketAddress(host,portsocket));
							reader= new BufferedReader(new InputStreamReader(
									socket.getInputStream()));
							writer= new BufferedWriter(new OutputStreamWriter(
									socket.getOutputStream()));
								
							//faccio il login
							writer.write("login\n"+u+"\n"+p+"\n"+portUDP+"\n");
							writer.flush();
							//leggo la risposta
							String response= reader.readLine();
								
							//se va bene -> home (OK)
							if("ok".equalsIgnoreCase(response)){
								writer.write("getScore\n");
								writer.flush();
								//leggo la risposta
								UserScore= reader.readLine();
								
								this.home();
							}
							else if("already".equalsIgnoreCase(response)){
								error.setText("Utente già loggato");
								JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
								reader.close();
								writer.close();
								socket.close();
							}
							else if("notexist".equalsIgnoreCase(response)){
								error.setText("Utente inesistente: registrarsi");
								JOptionPane.showMessageDialog(null, error, "Parametri non corretti", JOptionPane.ERROR_MESSAGE);
								reader.close();
								writer.close();
								socket.close();
							}
							else if("errpw".equalsIgnoreCase(response)){
								error.setText("Password errata");
								JOptionPane.showMessageDialog(null, error, "Parametri non corretti", JOptionPane.ERROR_MESSAGE);
								reader.close();
								writer.close();
								socket.close();
							}
			            }
			            if(r==1){
			            	error.setText("Username già preso");
							JOptionPane.showMessageDialog(null, error, "Attenzione", JOptionPane.WARNING_MESSAGE);
			            }
			            if(r==-1){
			            	error.setText("Errore durante la registrazione");
							JOptionPane.showMessageDialog(null, error, "Attenzione", JOptionPane.WARNING_MESSAGE);
			            }
					}catch (RemoteException err) {
						error.setText("Connessione rifiutata");
						JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
			        } catch (NotBoundException err) {
			        	error.setText("Errore");
						JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
			        }catch (SocketException err) {
			        	error.setText("Errore");
						JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
						System.out.println("Server closed.");
					} catch (UnknownHostException err) {
						error.setText("Errore");
						JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
						err.printStackTrace();
					} catch (IOException err) {
						error.setText("Registrazione effettuata. Errore durante il login");
						JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
						//chiudo la connessione UDP
						closeUdp();
						f.dispose();
						System.out.println("Server closed connection or an error appeared.");
					}
				}
			}
		}
		
		if("sfida".equals(e.getActionCommand())){
			String nickAmico =t1.getText().trim();
			JLabel error = new JLabel("");
			error.setFont(new Font("Arial", Font.PLAIN, 30));
			//errori
			if(nickAmico.equals("")){
				error.setText("Nickname amico non inserito");
				JOptionPane.showMessageDialog(null, error, "Parametri non corretti", JOptionPane.ERROR_MESSAGE);
			}
			else if(nickAmico.length()>20){
				error.setText("Nickname amico non valido");
				JOptionPane.showMessageDialog(null, error, "Parametri non corretti", JOptionPane.ERROR_MESSAGE);
				t2.setText("");
			}
			else if(nickAmico.equals(username)){
				error.setText("Non puoi sfidare te stesso");
				JOptionPane.showMessageDialog(null, error, "Parametri non corretti", JOptionPane.ERROR_MESSAGE);
				t2.setText("");
			}
			else{//mando al server
				try {
					writer.write("sfida\n"+nickAmico+"\n");
					writer.flush();
					
					//leggo la risposta
					//0:ok, 1:non siete amici, -1:utente inesistente
					//2:richiesta rifiutata
					String response= reader.readLine();

					if(response.equals("-1")){
		            	error.setText("Utente inesistente");
						JOptionPane.showMessageDialog(null, error, "Attenzione", JOptionPane.ERROR_MESSAGE);
		            }
					if(response.equals("1")){
		            	error.setText("Tu e "+nickAmico+" non siete amici");
						JOptionPane.showMessageDialog(null, error, "Attenzione", JOptionPane.WARNING_MESSAGE);
		            }
					if(response.equals("2")){
		            	error.setText(nickAmico+" ha rifiutato la tua richiesta");
						JOptionPane.showMessageDialog(null, error, "Attenzione", JOptionPane.WARNING_MESSAGE);
		            }
					if(response.equals("0")){
						//Partita!!
						//leggere T2 e K
						T2= reader.readLine();
						infoBattle= reader.readLine();
						//prima parola
						firstword= reader.readLine();
						battle();
					}
					
				} catch (IOException e1) {
					error.setText("Errore del server");
					JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
					//chiudo la connessione UDP
					closeUdp();
					f.dispose();
				}
				
			}
			
		}
		
		
		if("aggiungi".equals(e.getActionCommand())){
			String nickAmico =t2.getText().trim();
			JLabel error = new JLabel("");
			error.setFont(new Font("Arial", Font.PLAIN, 30));
			//errori
			if(nickAmico.equals("")){
				error.setText("Nickname amico non inserito");
				JOptionPane.showMessageDialog(null, error, "Parametri non corretti", JOptionPane.ERROR_MESSAGE);
			}
			else if(nickAmico.length()>20){
				error.setText("Nickname amico non valido");
				JOptionPane.showMessageDialog(null, error, "Parametri non corretti", JOptionPane.ERROR_MESSAGE);
				t2.setText("");
			}
			else if(nickAmico.equals(username)){
				error.setText("Non puoi aggiungerti come amico");
				JOptionPane.showMessageDialog(null, error, "Parametri non corretti", JOptionPane.ERROR_MESSAGE);
				t2.setText("");
			}
			else{//mando al server (sono già connessa)
				try {
					
					writer.write("aggiungiamico\n"+nickAmico+"\n");
					writer.flush();
					
					//leggo la risposta
					//0:ok, 1:già amici, -1:utente inesistente
					String response= reader.readLine();

					if(response.equals("-1")){
		            	error.setText("Utente inesistente");
						JOptionPane.showMessageDialog(null, error, "Attenzione", JOptionPane.ERROR_MESSAGE);
		            }
					if(response.equals("1")){
		            	error.setText("Siete già amici");
						JOptionPane.showMessageDialog(null, error, "Attenzione", JOptionPane.WARNING_MESSAGE);
		            }
					if(response.equals("0")){
		            	error.setText("Ora tu e "+nickAmico+" siete amici!");
						JOptionPane.showMessageDialog(null, error, "Nuovo Amico!", JOptionPane.INFORMATION_MESSAGE);
		            }
					
				} catch (IOException e1) {
					error.setText("Errore del server");
					JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
					//chiudo la connessione UDP
					closeUdp();
					f.dispose();
				}
			}
		}
		
		if("esci".equals(e.getActionCommand())){
			
			try {
				
				//faccio il logout
				writer.write("logout\n");
				writer.flush();
				
				if(reader!=null)reader.close();
				if(writer!=null)writer.close();
				if(socket!=null)socket.close();
				
				//chiudo la connessione UDP
				closeUdp();
				
				f.dispose();
				
			} catch (IOException e1) {
				JLabel error=new JLabel("Errore del server");
				error.setFont(new Font("Arial", Font.PLAIN, 30));
				JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
				//chiudo la connessione UDP
				closeUdp();
				f.dispose();
			}
			
		}
		
		
		if("salta".equals(e.getActionCommand())){
			try {
				writer.write("saltaparola\n");
				writer.flush();
			
				//leggo la risposta
				String response= reader.readLine();
				
				
				//sono state tradotte tutte le parole
				if(response.equals("end")){
					response= reader.readLine();
					
					//fermo il timer
					timer.setRepeats(false);
					
					//richiedo il nuovo punteggio e torno alla home
					writer.write("getScore\n");
					writer.flush();
					//leggo il punteggio
					UserScore= reader.readLine();
					
					JTextArea ta2=new JTextArea();
					ta2.setEditable(true);
					ta2.setAutoscrolls(false);
					ta2.setFont(new Font("serif",Font.PLAIN,30));
					ta2.setEditable(false);
					ta2.setBackground(new Color(238, 238, 238));
					//torno alla schermata principale
					this.home();
					
					response=response.replaceAll("/","\n");
					ta2.setText(response);
					//mostro all'utente i risultati
					JOptionPane.showMessageDialog(null, ta2, "Risultati!", JOptionPane.INFORMATION_MESSAGE);
					
				}
				else{//parola nuova
					lword.setText(response);
					tword.setText("");
				}
			} catch (IOException e1) {
				JLabel error=new JLabel("Errore del server");
				error.setFont(new Font("Arial", Font.PLAIN, 30));
				JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
				//chiudo la connessione UDP
				closeUdp();
				f.dispose();
			}
		}
		
		
		if("invia".equals(e.getActionCommand())){
			try {
				String parola =tword.getText().trim();
				writer.write("inviaparola\n"+parola+"\n");
				writer.flush();
			
				//leggo la risposta
				String response= reader.readLine();
				
				//sono state tradotte tutte le parole
				if(response.equals("end")){
					response= reader.readLine();
					
					//fermo il timer
					timer.setRepeats(false);
					
					//richiedo il nuovo punteggio e torno alla home
					writer.write("getScore\n");
					writer.flush();
					//leggo il punteggio
					UserScore= reader.readLine();
					
					JTextArea ta2=new JTextArea();
					ta2.setEditable(true);
					ta2.setAutoscrolls(false);
					ta2.setFont(new Font("serif",Font.PLAIN,30));
					ta2.setEditable(false);
					ta2.setBackground(new Color(238, 238, 238));
					//torno alla schermata principale
					this.home();
					
					response=response.replaceAll("/","\n");
					ta2.setText(response);
					
					JOptionPane.showMessageDialog(null, ta2, "Risultati!", JOptionPane.INFORMATION_MESSAGE);
					
				}
				else{//parola nuova
					lword.setText(response);
					tword.setText("");
				}
				
			} catch (IOException e1) {
				JLabel error=new JLabel("Errore del server");
				error.setFont(new Font("Arial", Font.PLAIN, 30));
				JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
				//chiudo la connessione UDP
				closeUdp();
				f.dispose();
			}
			
		}
		
	}
	
		
	
	//gestisco la chiusura del frame 
	class ListenFrame implements WindowListener{
		
		@Override
		public void windowClosed(WindowEvent arg0) {}

		@Override
		public void windowClosing(WindowEvent arg0) {
			try {
				//faccio il logout
				writer.write("logout\n");
				writer.flush();
				
				if(reader!=null)reader.close();
				if(writer!=null)writer.close();
				if(socket!=null)socket.close();
				
				//chiudo la connessione UDP
				closeUdp();
				
				f.dispose();
				
			} catch (IOException e1) {
				JLabel error=new JLabel("Errore del server");
				error.setFont(new Font("Arial", Font.PLAIN, 30));
				JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
				//chiudo la connessione UDP
				closeUdp();
				f.dispose();
			}
			
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {}
		@Override
		public void windowDeiconified(WindowEvent arg0) {}
		@Override
		public void windowIconified(WindowEvent arg0) {}
		@Override
		public void windowOpened(WindowEvent arg0) {}
		@Override
		public void windowActivated(WindowEvent arg0) {}
	}
	

	
	class PanelAmici implements ComponentListener{

		@Override
		public void componentHidden(ComponentEvent arg0) {}
		@Override
		public void componentMoved(ComponentEvent arg0) {}
		@Override
		public void componentResized(ComponentEvent arg0) {}
		
		@Override
		public void componentShown(ComponentEvent arg0) {
			
			//chiedo l'oggetto json che rappresenta la lista di amici
			try {
				
				writer.write("mostraAmici\n");
				writer.flush();
				
				//leggo la risposta
				String response= reader.readLine();
				
				//parso la risposta
				JSONParser parser = new JSONParser();
				JSONObject f = (JSONObject) parser.parse(response);
				
				String list = (String) f.get("Friends");
				
				//se ci sono amici
				if(!"[]".equalsIgnoreCase(list)){
					list=list.substring(1, list.length()-1);
					String[] friend=list.split(",");
					
					
					ta.setText("\n");
					
					for(int i=0;i<friend.length;i++){
						
						ta.append("      "+friend[i].trim()+"\n");
					}
					
					ta.setCaretPosition(0);
				}
				
				
			} catch (IOException e1) {
				JLabel error=new JLabel("Errore del server");
				error.setFont(new Font("Arial", Font.PLAIN, 30));
				JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
				//chiudo la connessione UDP
				closeUdp();
				f.dispose();
			} catch (ParseException e) {
				JLabel error=new JLabel("Errore");
				error.setFont(new Font("Arial", Font.PLAIN, 30));
				JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			
		}

	}
	
	
	class PanelClassifica implements ComponentListener{

		@Override
		public void componentHidden(ComponentEvent arg0) {}
		@Override
		public void componentMoved(ComponentEvent arg0) {}
		@Override
		public void componentResized(ComponentEvent arg0) {}
		
		@Override
		public void componentShown(ComponentEvent arg0) {
			
			//chiedo l'oggetto json che rappresenta la lista di amici
			try {
				
				writer.write("mostraClassifica\n");
				writer.flush();
				
				//leggo la risposta
				String response= reader.readLine();
				
				//parso la risposta
				JSONParser parser = new JSONParser();
				JSONArray allU = (JSONArray) parser.parse(response);
				
				int n=allU.size();
				
				JSONObject obj;
				JSONObject[] u= new JSONObject[n];
				
				for(int i=0;i<n;i++){
					obj= (JSONObject)allU.get(i);
					
					//leggo la posizione e metto l'oggetto nella posizione corrispondente
					Long pos = (Long) obj.get("position");
					u[pos.intValue()]=obj;

				}
				
				ta1.setText("\n");
				
				for(int i=0;i<n;i++){
					
					String name=(String)u[i].get("nick");
					Long score=(Long)u[i].get("score");
					//posizione, nome, punti
					ta1.append(" "+(i+1)+". "+name+"\t\t"+score.toString()+"\n");
					
				}
				
				//posiziono il cursore all'inizio
				ta1.setCaretPosition(0);
					
				
			} catch (IOException e1) {
				JLabel error=new JLabel("Errore del server");
				error.setFont(new Font("Arial", Font.PLAIN, 30));
				JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
				//chiudo la connessione UDP
				closeUdp();
				f.dispose();
			} catch (ParseException e) {
				JLabel error=new JLabel("Errore");
				error.setFont(new Font("Arial", Font.PLAIN, 30));
				JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}

	}
	
	//gestisco la chiusura del frame 
	class timeout implements ActionListener{
		
		Timer timer;
		
		public timeout(Timer timer){
			this.timer=timer;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
            String t=ttime.getText();
            if(Integer.parseInt(t)>0){
            		
            	t=Integer.toString(Integer.parseInt(t)-1);
            	ttime.setText(t);
            	
            	//tempo scaduto
            	if(Integer.parseInt(t)-1==0){
            		
            		timer.setRepeats(false);
            		
            		try {
						
						writer.write("scaduto\n");
						writer.flush();
						//leggo la risposta
						String response= reader.readLine();
							
						timer.setRepeats(false);
						//leggo il punteggio
						writer.write("getScore\n");
						writer.flush();
						UserScore= reader.readLine();
						
						JTextArea ta2=new JTextArea();
						ta2.setEditable(true);
						ta2.setAutoscrolls(false);
						ta2.setFont(new Font("serif",Font.PLAIN,30));
						ta2.setEditable(false);
						ta2.setBackground(new Color(238, 238, 238));
						
						home();
						
						response=response.replaceAll("/","\n");
						ta2.setText(response);
						
						JOptionPane.showMessageDialog(null, ta2, "Risultati!", JOptionPane.INFORMATION_MESSAGE);
						
					} catch (IOException e) {
						JLabel error=new JLabel("Errore del server");
						error.setFont(new Font("Arial", Font.PLAIN, 30));
						JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
						//chiudo la connessione UDP
						closeUdp();
						f.dispose();
					}
            		
            	}
            	
            	
            }
                
           }
 
		
	}
	
	//gestisco la chiusura del frame 
	class ExitBattle implements WindowListener{
			
		@Override
		public void windowClosed(WindowEvent arg0) {}

		@Override
		public void windowClosing(WindowEvent arg0) {

			
			try {
				//chiedo di uscire dalla battaglia
				writer.write("uscitabattaglia\n");
				writer.flush();
				//fermo il timer
				timer.setRepeats(false);
				
				home();
				
			} catch (IOException e) {
				JLabel error=new JLabel("Errore del server");
				error.setFont(new Font("Arial", Font.PLAIN, 30));
				JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
				//chiudo la connessione UDP
				closeUdp();
				f.dispose();
			}
			
			
				
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {}
		@Override
		public void windowDeiconified(WindowEvent arg0) {}
		@Override
		public void windowIconified(WindowEvent arg0) {}
		@Override
		public void windowOpened(WindowEvent arg0) {}
		@Override
		public void windowActivated(WindowEvent arg0) {}
	}
	
	
	public void setSocketUDP(DatagramSocket ds){
		this.socketUDP=ds;
	}
	
	public void setPortUdp(int p){
		portUDP=p;
	}
	
	public void closeUdp(){
		socketUDP.close();
	}
	
	
	
	//gestire la richiesta di sfida
	public void askConfirm(String nick){
		JLabel m = new JLabel(nick+" ti ha sfidato!\n Accetti la sfida?");
		m.setHorizontalAlignment(SwingConstants.CENTER);
		m.setFont(new Font("Arial", Font.PLAIN, 40));
		int choice=JOptionPane.showConfirmDialog(null, m, "Sfida", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		
		//sfida accettata
		try {
			if(choice==0){
				
				writer.write("confirm\n");
				writer.flush();
				//leggo la risposta
				String response= reader.readLine();
				if(response.equals("1")){
					//la richiesta era scaduta
	            	m.setText("Richiesta scaduta");
					JOptionPane.showMessageDialog(null, m, "Attenzione", JOptionPane.WARNING_MESSAGE);
	            }
				if(response.equals("2")){
					//errore
	            	m.setText("Errore");
					JOptionPane.showMessageDialog(null, m, "Attenzione", JOptionPane.WARNING_MESSAGE);
	            }
				if(response.equals("0")){
					//Partita!!
					//leggere T2 e K
					T2= reader.readLine();
					infoBattle= reader.readLine();
					//prima parola
					firstword= reader.readLine();
					battle();
				}
			}
			else{//richiesta rifiutata
				writer.write("notconfirm\n");
				writer.flush();
			}
		} catch (IOException e) {
			JLabel error=new JLabel("Errore del server");
			error.setFont(new Font("Arial", Font.PLAIN, 30));
			JOptionPane.showMessageDialog(null, error, "Errore", JOptionPane.ERROR_MESSAGE);
			//chiudo la connessione UDP
			closeUdp();
			f.dispose();
		}
	}
	
}
