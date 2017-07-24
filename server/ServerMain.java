package server;

import java.rmi.Naming;


public class ServerMain {

	public static void main(String[] args) {
					
		try {
			Server s = new Server();
			Naming.rebind("rmi://127.0.0.1:"+ args[0] +"/ServerXXX", s);
			s.createNewGame("gioco1", 4);
		} catch(Exception e) {
			System.out.println("errore nella creazione del server");
			e.printStackTrace();
		}
		
		
		
	}
	
	
}
