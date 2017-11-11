import java.io.*;
import java.net.*;
import java.util.*;
import java.text.DateFormat;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class ServerChat extends JFrame implements ActionListener{
	JTextArea ta;
	private JPanel p_log, p_manageUsers;
	private JTabbedPane tp_main;
	String a,b;
	
	PrintWriter pw;
	private Vector Stanze, Utenti,Messaggi;
	public ServerChat() throws IOException{
		
		Container co=getContentPane();
		ta=new JTextArea(30,30);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
	    DateFormat fmt=DateFormat.getDateInstance(DateFormat.SHORT,Locale.ITALY);
    	a =fmt.format(new Date());
    	String d=new Date().toString();
    	d=d.substring(10,20);
    	ta.setText("Server aperto il "+a+" --"+d+"\n");
    	a=a.replace('/','_');
    	
    	
		JScrollPane js=new JScrollPane(ta);
		
		tp_main = new JTabbedPane();
		
		p_log = new JPanel();
		p_log.add(js);
		
		p_manageUsers = new JPanel();
		
		tp_main.addTab("Log", p_log);
		tp_main.addTab("Manage Users", p_manageUsers);
		
		co.add(tp_main);
		
		
 		DateFormat fmt2=DateFormat.getDateInstance(DateFormat.FULL,Locale.ITALY);
    	b =fmt2.format(new Date());
 		String s1="log"+a+".dat";
 		FileWriter f=new FileWriter(s1);
		pw=new PrintWriter(f,true);
		Stanze=new Vector(3,2);
		Utenti=new Vector(3,2);         //Utenti di tutto il Sistema
		Messaggi=new Vector(10,5);      //Messaggi inviati da tutti gli utenti
		
		//creo la stanza principale
		Stanze.addElement(new Stanza("<<principale>>",Utenti));
		
        addWindowListener(new GestFin(pw));
	}
	
	public void execute() throws IOException{
		Socket s=null;
    	
 		ServerSocket ss=new ServerSocket(4000);
		
		try{
			
	// attivo il server che provvederà allo smistamento dei messaggi
    // a tutti gli utenti della chat
			new ChatServer(Messaggi,Stanze,Utenti);
			
	// mi preoccupo di rispondere ad ogni nuovo utente che chiede di chattare
			while(true)
			{
				s=ss.accept();
				setArea("Connessione avvenuta il: "+b+ " da "+s+"\n");
				new ServerThread(s,pw,Utenti,Stanze,Messaggi,ta);
				addBanButton(s.getRemoteSocketAddress().toString());
			}
		}catch(Exception e){ setArea("Errore "+e);
			
		}finally{
			ss.close();
            
			pw.close();
		}
	}
	
	public void setArea(String s){
		ta.append(s+"\n");
		
	}
	
	public void addBanButton(String textButton) {
		JButton btn = new JButton("Ban " + textButton);
		p_manageUsers.add(btn);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args)throws IOException{
		ServerChat mm=new ServerChat();
 		mm.setVisible(true);
 		mm.pack();
 	    mm.execute();
	}
}
	
	
class ServerThread extends Thread{
	Socket so;
	PrintWriter log,bout;
	BufferedReader br;
	Vector sta,u,messaggi;
	int id_utente,id_stanza;
	JTextArea ar;
	public ServerThread(Socket s,PrintWriter pw,Vector ut,Vector st,Vector mes,JTextArea ta){
	  so=s;
	  log=pw; 
	  u=ut;
	  sta=st;
	  ar=ta;
	  messaggi=mes;
	  Date d=new Date();
	  DateFormat fmt=DateFormat.getDateInstance(DateFormat.MEDIUM,Locale.ITALY);
      String a =fmt.format(d);
	  String l=d.toString().substring(10,20);
	  try{
		pw.println("\n***Connessione avvenuta il: "+a+ " alle ore "+l+"\nda "+so);
		
		bout=new PrintWriter(so.getOutputStream(),true);
		InputStreamReader in=new InputStreamReader(so.getInputStream());
		br=new BufferedReader(in);
	  }catch(IOException ex){
			System.out.println("Errore "+ex);
	        }
      start();
	}
	
// cerca dato il nickname di un utente il corrispondente indice nel vettore
// degli utenti (-1 se non lo trova)
	public int cerca(String nome){
		
		for(int i=0;i<u.size();i++){
		  Utente ux=(Utente)u.elementAt(i);
		 if (ux.getNik().equals(nome))
		    return i;
		}    
		return -1;    
	}
	
	public void run(){
		String cmd="";
		try{
			bout.println("Ciao sono il tuo chat-server!");
			while (true){
		   	  cmd=br.readLine();
			  interpreta(cmd);
			  if (cmd.equals("END"))
			   break;
			}
			bout.close();
			so.close();
			Utente ui=(Utente)u.elementAt(id_utente);
			ar.append("\n***Connessione chiusa da utente: "+ui.getNik());
			Stanza z=(Stanza)sta.elementAt(id_stanza);
			z.togli_ut(id_utente);
		}catch(Exception es){
			if  (!cmd.equals("END"))
			System.out.println("Er: "+es);
			else
			System.out.println("Er-comd: "+es);
		}
	}
		
		
 // interpreta i comandi inviati dagli utenti della chat		
	public void interpreta(String c){
	
	//comando di richiesta di connessione ed identificazione tramite nickname
	
		if (c.substring(0,3).equals("CON"))
		{
			StringTokenizer ss=new StringTokenizer(c,"#");
			String nick=ss.nextToken();
			if (ss.hasMoreTokens())
			 nick=ss.nextToken();
			u.addElement(new Utente(so,nick,0));
            Stanza stan=(Stanza) sta.elementAt(0);
            id_stanza=0;
            id_utente=u.size()-1;
            stan.addUtente(id_utente);
			bout.println("Ciao "+nick);
		}
		else
	// comando che invia un messaggio pubblico all'interno della stanza	
		if (c.substring(0,3).equals("MES")){
			try{
				StringTokenizer ss=new StringTokenizer(c,"#");
			    String mess=ss.nextToken();
			if (ss.hasMoreTokens())
			 mess=ss.nextToken();
			Mess messaggio=new Mess(id_stanza,mess,id_utente,sta,u); 
			messaggi.addElement(messaggio);
			
			//FOR DEBUGGING
			System.out.println("Ho inserito un messaggio");
			
			}catch(Exception xx){
				System.out.println("Errore: "+xx);
			}
		}
       else
       
  // invia un messaggio privato ad un altro utente     
       
		if (c.substring(0,3).equals("PRI")){
			try{
				StringTokenizer ss=new StringTokenizer(c,"#");
			    String nick=ss.nextToken();
			if (ss.hasMoreTokens())
			 nick=ss.nextToken();
			int destinatario=cerca(nick);
			String mess_p=ss.nextToken();
			invia_pri(destinatario,mess_p,id_utente); 
			
			//FOR DEBUGGING
			System.out.println("Ho inserito un messaggio privato");
			
			}catch(Exception xx){
				System.out.println("Errore: "+xx);
			}
		}		
	 else
       
  // Crea una nuova stanza     
       
		if (c.substring(0,3).equals("STA")){
			try{
				StringTokenizer ss=new StringTokenizer(c,"#");
			    String nome=ss.nextToken();
			if (ss.hasMoreTokens())
			 nome=ss.nextToken();
			
			Stanza stan_n=new Stanza(nome,u);
			Stanza z=(Stanza)sta.elementAt(id_stanza);
			z.togli_ut(id_utente);
			stan_n.addUtente(id_utente);
			id_stanza=sta.size();
			sta.addElement(stan_n);
			
			}catch(Exception xx){
				System.out.println("Errore: "+xx);
			}
		}
		 // ENTRA in una nuova stanza
       
		if (c.substring(0,3).equals("NST")){
			try{
                StringTokenizer ss=new StringTokenizer(c,"#");
			    String nome=ss.nextToken();
			if (ss.hasMoreTokens())
			 nome=ss.nextToken();
            Stanza sa=null; 
            int i;
			for(i=0;i<sta.size();i++){
				sa=(Stanza)sta.elementAt(i);
				if (sa.getN().equals(nome)) break;
				}	
			
			if (!sa.getN().equals(nome)) 
			 bout.println("Il nome della stanza non è corretto!");
			else{
				Stanza z=(Stanza)sta.elementAt(id_stanza);
			    z.togli_ut(id_utente);
			    id_stanza=i;
			    sa.addUtente(id_utente);  
			    bout.println("Sei entrato nella nuova stanza!");
			}
			
			
			  
			}catch(Exception xx){
				System.out.println("Errore: "+xx);
			}
		}				
		 // CHIEDE quali sono le stanze
        else
		if (c.substring(0,3).equals("QST")){
			try{
				
			for(int i=0;i<sta.size();i++){
				Stanza sa=(Stanza)sta.elementAt(i);
				bout.println("<<STA>>"+sa.getN());
			}	
			bout.println("END");
			  
			}catch(Exception xx){
				System.out.println("Errore: "+xx);
			}
		}
		
		else
		//CHIEDE in quale stanza mi trovo
		if (c.substring(0,3).equals("DOV")){
			try{
			    Stanza sa=(Stanza)sta.elementAt(id_stanza);
		  	    bout.println("<<STA>>"+sa.getN());
			}catch(Exception xx){
				System.out.println("Errore: "+xx);
			}
		}
		
			 // CHIEDE quali sono GLI UTENTI DELLA STANZA CORRENTE
       
		if (c.substring(0,3).equals("QUT")){
			try{
				Stanza sa=(Stanza)sta.elementAt(id_stanza);
				bout.println("<<UT>>Gli utenti della stanza corrente ("+sa.getN()+") sono:");
				int i=0;
                while(sa.getUtente(i)!=null){
                Utente uut=sa.getUtente(i);
				bout.println("<<UT>>"+uut.getNik());
				i++;
			}	
			bout.println("END");
			  
			}catch(Exception zx){
				System.out.println("Errore: "+zx);
			}
		}
	}	
		
	public void invia_pri(int des,String m,int id_utente){
		Utente ut=(Utente) u.elementAt(des);
		try{
			Socket ss=ut.getSok();
			PrintWriter pwri=new PrintWriter(ss.getOutputStream(),true);
			ut=(Utente)u.elementAt(id_utente);
			String nick_mit=ut.getNik();
			pwri.println(nick_mit+"> "+m);
    	} catch(IOException j){
			System.out.println("Errore: (mess_priv) "+j);
		}
		
	} 
		
}

class ChatServer extends Thread{
	Vector messaggi,sstanze,uutenti;
	public ChatServer(Vector m,Vector s,Vector u){
		messaggi=m;
		sstanze=s;
		uutenti=u;
		start();
	}
	public void addMessaggio(String m,int utente,int stanza){
	  Mess ms=new Mess(stanza,m,utente,sstanze,uutenti);	
	  messaggi.addElement(ms);
	}
	
	public void run(){
	   while(true)
	   {
	   	
		for(int i=0;i<messaggi.size();i++){
	
	   //FOR DEBUGGING
			System.out.println("Sto esaminando un messaggio"+ i+"\n");
					
			Mess m=(Mess) messaggi.elementAt(i);
			messaggi.removeElementAt(i);
			Stanza st=m.getStanza();
			int x=0;
			Utente ut,mitt;
			mitt=m.getUtente();
			while((ut=(Utente) st.getUtente(x))!=null){
				try{
					Socket so=ut.getSok();
					PrintWriter pw=new PrintWriter(so.getOutputStream(),true);
					pw.println(mitt.getNik()+"> "+m.getMess());
				}catch(Exception e){
					System.out.println("Errore: "+e);
				}
				x++;
			}
		}
		try{
			sleep(500);
		}catch(InterruptedException i){
		}
		
		}
	}
}

class Mess{
	int id_stanza;
	String m;
    int id_Utente;
    Vector stanze,utenti;
    
    
    public Mess(int s,String mm,int ut,Vector sta,Vector Ut){
    	id_stanza=s;
    	m=mm;
    	id_Utente=ut;
    	stanze=sta;
    	utenti=Ut;
    }
    
    public Stanza getStanza(){
    	if (id_stanza < stanze.size()){
		 return (Stanza) stanze.elementAt(id_stanza);
		}
		else
		 return null; 
    }
    
    public String getMess(){
    	return m;
    }
    
    public Utente getUtente(){
		if(id_Utente<utenti.size()){
		 return (Utente) utenti.elementAt(id_Utente);
		}
		else
		 return null; 
	}
	
	
}
class Stanza {
	String nome;
	Vector ut_stanza,ut;
	
	public Stanza(String n,Vector utenti){
		nome=n;
		ut=utenti;
		ut_stanza=new Vector(3,1);
	}
	
	public String getN(){
		return nome;
	}
	
    public void togli_ut(int id){
    	for (int i=0;i<ut_stanza.size();i++){
    		Integer sa=(Integer)ut_stanza.elementAt(i);
    		if (sa.intValue()==id)
    		  ut_stanza.removeElementAt(i);
    		}
    }

	public void addUtente(int id){
		Integer i=new Integer(id);
		ut_stanza.addElement(i);
	}
	
	public Utente getUtente(int i){
		if(i<ut_stanza.size()){
	 	 Integer x=(Integer) ut_stanza.elementAt(i);
		 return (Utente) ut.elementAt(x.intValue());
		}
		else
		 return null; 
	}
	
}

class Utente {
	Socket s;
	String name;
	int n_stanza;
	public Utente(Socket so,String nik,int n){
		s=so;
		name=nik;
		n_stanza=n;
	}
	
	public Socket getSok(){
	  return s;
	}
	
	public String getNik(){
		return name;
	}
	
	public int getStan(){
		return n_stanza;
	}
	
	
}


class GestFin extends WindowAdapter{
	PrintWriter po;
	DateFormat fmt1;
	String c;
	public GestFin(PrintWriter p){
        fmt1=DateFormat.getDateInstance(DateFormat.FULL,Locale.ITALY);
    	c =fmt1.format(new Date());			
		p.println("Il Sever è aperto il :"+c+ "alle ore:"+ new Date().toString().substring(10,20));		
		po=p;
	}
	
	public void windowClosing(WindowEvent e){
        fmt1=DateFormat.getDateInstance(DateFormat.FULL,Locale.ITALY);
    	c =fmt1.format(new Date());			
		po.println("\nIl Sever si è chiuso il :"+c+ "alle ore:"+ new Date().toString().substring(10,20));		
		
		System.exit(0);
	}
}
	
	
