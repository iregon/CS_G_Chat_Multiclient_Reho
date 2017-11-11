import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class Client extends JFrame implements ActionListener{
	JTextField NickName,tx;
	JButton Con, Esci, Invia,Sta,St,Ut,Cr_sta,Ca_sta,Pers;
	JTextArea ta,ts;
	Socket so;
	PrintWriter pwr;
	BufferedReader br;
	boolean connessione=false;
	GestFin1 gestfin;
	
	public Client(){
		Container co=getContentPane();
		co.setLayout(new BorderLayout());
		
		JPanel jp=new JPanel();
		NickName=new JTextField(20);
		Con=new JButton("Connetti");
		Con.addActionListener(this);
		Esci=new JButton("Disconnetti");
		Esci.addActionListener(this);
		jp.add(new JLabel("NickName: "));
		jp.add(NickName);
		jp.add(Con);
		jp.add(Esci);
		co.add(jp,BorderLayout.NORTH);
		
		JPanel jp1=new JPanel();
		ta=new JTextArea(12,60);
		ta.setFont(new Font("Courier",1,12));
	   ta.setText("*************             My- chat    ****************\n");
		ta.append("*                                                    *\n");
		ta.append("*                                                    *\n");
		ta.append("*                                                    *\n");
		ta.append("*                                                    *\n");
		ta.append("*                                                    *\n");
		ta.append("*                                                    *\n");
		ta.append("*                                                    *\n");
		ta.append("*                                                    *\n");
		ta.append("*                                                    *\n");
		ta.append("*******************************************************\n");
		ta.setEditable(false);
		JScrollPane js=new JScrollPane(ta);
		jp1.add(js);
		co.add(jp1,BorderLayout.CENTER);
		
		JPanel jp2=new JPanel();
		tx=new JTextField(50);
		Invia=new JButton("Invia a tutti");
		Invia.addActionListener(this);
		Pers=new JButton("Messaggio privato");
		Pers.addActionListener(this);		
		jp2.add(tx);
		jp2.add(Invia);
		jp2.add(Pers);
		co.add(jp2,BorderLayout.SOUTH);
		
		JPanel jp3=new JPanel();
		jp3.setLayout(new GridBagLayout());
		GridBagConstraints cs=new GridBagConstraints();
		cs.gridx=0;
		cs.gridy=0;
		cs.gridwidth=3; 
		cs.gridheight=1;
		cs.fill=GridBagConstraints.BOTH;
	    JPanel jp4=new JPanel();
	    jp4.setLayout(new GridLayout(1,3));
	    St=new JButton("Stanze");
	    St.setBackground(Color.blue);
	    St.setForeground(Color.white);
	    St.setBorder(BorderFactory.createRaisedBevelBorder());
	    Ut=new JButton("Utenti");
	    Ut.setForeground(Color.white);
	    Ut.setBackground(new Color(150,0,0));
	    Ut.setBorder(BorderFactory.createRaisedBevelBorder());
	    Sta=new JButton("Stato");
	    Sta.setForeground(Color.white);
	    Sta.setBackground(Color.gray);
	    Sta.setBorder(BorderFactory.createRaisedBevelBorder());
	    St.addActionListener(this);
	    Ut.addActionListener(this);
	    Sta.addActionListener(this);
	    jp4.add(St);
	    jp4.add(Ut);
	    jp4.add(Sta);
	    jp3.add(jp4,cs);
	    
	    GridBagConstraints cs1=new GridBagConstraints();
		cs1.gridx=0;
		cs1.gridy=1; 
		cs1.gridwidth=3;
		cs1.gridheight=4;
		cs.fill=GridBagConstraints.BOTH;
	    ts=new JTextArea(10,30);
	    ts.setBackground(Color.gray);
	    ts.setForeground(Color.white);
	    Font f=new Font("Arial",1,12);
	    ts.setFont(f);
	    ts.setLineWrap(true);
	    ts.setWrapStyleWord(true);
	    ts.setText("Per usare la chat ricordati che devi prima di tutto connetterti al server");
	    ts.setEditable(false); 
	    JScrollPane jsq=new JScrollPane(ts);
		jp3.add(jsq,cs1);
		
		
	    JPanel jp5=new JPanel();
	    Cr_sta=new JButton("Crea stanza");
	    Cr_sta.addActionListener(this);
	    jp5.add(Cr_sta);
	    Ca_sta=new JButton("Cambia stanza");
	    Ca_sta.addActionListener(this);
	    jp5.add(Ca_sta);
	    GridBagConstraints cs2=new GridBagConstraints();
		cs2.gridx=0;
		cs2.gridy=5; 
		cs2.gridwidth=3;
		cs2.gridheight=1;
		jp3.add(jp5,cs2);
		
		
		co.add(jp3,BorderLayout.EAST);
		gestfin=new GestFin1();
		addWindowListener(gestfin);
		
		
	    
	}
	
	public void actionPerformed(ActionEvent e){
		Object o=e.getSource();
		
		if (o==Esci){
			if (connessione){
				try{
					connessione=false;
                    ts.setText("Avvenuta disconnessione dal server");
					ts.setBackground(Color.gray);
					ta.setText("");
					pwr.println("END");					
					so.close();
					pwr.close();
					br.close();
				}catch(Exception v){
					System.out.println("errore in uscita:"+v);
					ts.setText("errore in uscita:"+v);
					ts.setBackground(Color.gray);
				}
			}
		}	
		else
		if (o==St){
			ts.setBackground(Color.blue);
			ts.setText("");
			if (connessione){
			  pwr.println("QST#");
			  }	
			else
		     ts.append("\nPrima devi connetterti al server!");
			}
		else
	if (o==Ut){
		 ts.setBackground(new Color(150,0,0));
		 ts.setText("");
		 if (connessione)
		   pwr.println("QUT#");
		 else
		   ts.append("\nPrima devi connetterti al server!");
		}
		else
	if (o==Cr_sta){
		 
		 if (connessione){
         String nome=JOptionPane.showInputDialog(null,"Inserisci il nome di una nuova stanza","Nuova Stanza",JOptionPane.PLAIN_MESSAGE);
		 pwr.println("MES#"+"C'è una nuova stanza di nome "+nome+"Chi vuole entrare?");	
		 pwr.println("STA#<<"+nome+">>");
		 ts.setBackground(Color.gray);
		 ts.setText("Ho creato una nuova stanza di nome: "+nome);
		 ts.append("e sono entrato in questa nuova stanza!");
		 }
		   
		 else
		   ts.append("\nPrima devi connetterti al server!");
		}
		else
		if (o==Ca_sta){
		 
		 if (connessione){
         String nome=JOptionPane.showInputDialog(null,"Inserisci il nome della stanza dove entrare","Stanza",JOptionPane.PLAIN_MESSAGE);
		 pwr.println("NST#<<"+nome+">>");
		 }
		   
		 else
		   ts.append("\nPrima devi connetterti al server!");
		}
		else
		if (o==Sta){
			ts.setText("");
			ts.setBackground(Color.gray);
		}
		
		//INVIO messaggio pubblico
		
		else if (o==Invia){
			try{
			 pwr.println("MES#"+tx.getText());
			}catch(Exception x){
				ts.append("\nErrore di invio: "+x);
			}
			
		}
		//INVIO messaggio privato
		
		else if (o==Pers){
			try{
			String nome=JOptionPane.showInputDialog(null,"Inserisci il nome del destinatario","Destinatario",JOptionPane.PLAIN_MESSAGE);
			 pwr.println("PRIV#"+nome+"#"+tx.getText());
			}catch(Exception x){
				ts.append("\nErrore di invio: "+x);
			}
			
		}		
		else
		if (o==Con) {
		  try{
		  	if (!connessione){
		  	if (NickName.getText().trim().equals(""))
		  	{
		  		ts.setText("");
		  		ts.append("Devi inserire un Nome prima di connetterti!");
		  		ts.setBackground(Color.gray);
		  	}
		  	else{
		  	so=new Socket("localhost",4000);
		  	ts.setText("");
		  	ts.append("Connessione avvenuta: "+so);
		  	ts.setBackground(Color.gray);
			pwr=new PrintWriter(so.getOutputStream(),true);
			gestfin.setP(pwr,so);
			InputStreamReader in=new InputStreamReader(so.getInputStream());
			br=new BufferedReader(in);
			String s=br.readLine();
			ts.setText("");
			ts.append(s);
			connessione=true;
			pwr.println("CON#"+NickName.getText());
			ta.setText("");
			ta.append("\n"+br.readLine());
			ta.append("\nSei connesso nella stanza principale della nostra chat");
			new ReceiverCliente(ta,ts,so,br);
			}
			}
			else{
				ts.append("\nSei già connesso!");
				ts.setBackground(Color.gray);
			}
			
		 }catch (Exception ed)	{
			System.out.println("Errore: "+ed);
		  }
		}
		
	}
	
	public static void main(String[] args){
	 Client c=new Client();
	 c.pack();
	 c.setVisible(true);
		
	}
}

class GestFin1 extends WindowAdapter{
    Socket so;
    PrintWriter out;
    boolean b;
	public GestFin1(){
       b=false;
        
	}
	public void setP(PrintWriter pp,Socket s){
		so=s;
		out=pp;
		b=true;
	}
public void windowClosing(WindowEvent e){
		if (b)
        {
        	try{
        		out.println("END");		
        	}catch(Exception x){
        		System.out.println("Errore in getfin: "+x);
        	}
        	
        }
		System.exit(0);
	}
}
	
class ReceiverCliente extends Thread{
	JTextArea chat,stat;
	Socket s;
	BufferedReader in;
	boolean NotEsci=true;
	public ReceiverCliente(JTextArea ta,JTextArea ts,Socket so, BufferedReader br){
	  
	  s=so;
	  in=br;
	  chat=ta;
	  stat=ts;
	  start();
	}  
	
	public void run(){
		while (NotEsci){
			try{
				String ric=in.readLine();
				if (ric.length()>3 && ric.substring(0,4).equals("<<ST")){
					stat.append("\n"+ric.substring(7,ric.length()));
				    stat.setBackground(Color.blue);
				}
				else
				if (ric.length()>3 && ric.substring(0,4).equals("<<UT")){
					int n=ric.length();
					stat.append("\n"+ric.substring(6,n));
					stat.setBackground(new Color(150,0,0));
				}
				else
				if (ric.length()>3 && !(ric.substring(0,3).equals("END")))
				 chat.append("\n"+ric);
  			 
			}catch(Exception e){
				System.out.println("errore in ricezione:" +e);
				NotEsci=false;
  		     }
		}
	}
}
      