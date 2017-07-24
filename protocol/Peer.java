package protocol;

import game.GameState;

import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

import protocol.Message.MessageType;




public abstract class Peer extends UnicastRemoteObject implements PeerRemoteInterface {
	
	private volatile int lastMessageId;
	private List<PeerCredential> playersList;
	
	// ROBA UTILE ALLE SOTTOCLASSI /////////////////
	private Ring ring;
	private PeerCredential peerCredential;
	
	protected abstract void receivedMessage(Message m);
	protected abstract void ringInitiated(List<PeerCredential> playersList);
	protected abstract void crashDetected(Node n); //attenzione e' preso il lock di n.getActive() quando chiamata
	//public void broadcast(String messageText)
	//public void control()
	/////////////////////////////////////////////////
	
	public Peer(PeerCredential pc) throws RemoteException {
		this.peerCredential = pc;
	}
	
	public PeerCredential GetPeerCredential() {
		return peerCredential;
	}
	
	public void startNewGame() {
		//callback a game
		ringInitiated(playersList);
	}
	
	//crea lista di nodi, imposta la struttura ring e callback a game
	public void initRing(List<PeerCredential> playersList) throws RemoteException {
		
		lastMessageId = 0;
		
		this.playersList = playersList;
		
		// trovo me stesso all'interno della lista
		int index = -1;
		//System.out.println("io sono: " + peerCredential.getPeerURL() + " " + peerCredential.getPeerName());
		for(int i = 0; i < playersList.size(); i++) {
			if(playersList.get(i).equals(peerCredential)) {
				index = i;
				break;
			}
		}
		
		if(index == -1) { // TODO lanciare eccezzione? questo in teoria non dovrebbe mai accadere
			System.out.println("NON HO TROVATO ME STESSO NELLA LISTA DEI GIOCATORI MANDATA DAL SERVER, ERRORE!");
			return;
		}
		
		// creo la lista di nodi ordinata
		List<Node> nodeList = new ArrayList<Node>();
		for(int i = index + 1; i < index + playersList.size(); i++) {
			Node n = new Node( playersList.get(i % playersList.size()) );
			nodeList.add(n);
		}
		
		ring = new Ring(nodeList);
		
	}	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	// spedisco un messaggio di broadcast nella rete
	public void broadcast(MessageType type, Object payload) {
		
		Message m = new Message();
		
		m.message = payload;
		m.messageType = type;
		
		lastMessageId++;
		m.messageId = lastMessageId;
		m.senderCredential = peerCredential;
	
		sendBroadcastMessage(m);

	}
	
	// un client lo chiama per spedirgli il messaggio che poi forwardera'
	@Override
	public void forwardBroadcast(Message m) throws RemoteException {
		
		//System.out.println("ricevo il messaggio " + m.messageId + " del nodo " + m.senderCredential.getPeerName());
		
		boolean canForward = false;
		
		Node sender = ring.getNodeByName(m.senderCredential.getPeerName());
		
		/*
		for(Node n : ring.getNodeList()) {
			System.out.println("ho: " + n.getPeerCredential().getPeerName());
		}*/
		
		if( !m.senderCredential.equals(peerCredential) && sender != null) {
		
			synchronized(sender.getLastMessageId()) {
				if(sender.getLastMessageId() < m.messageId) {
					sender.setLastMessageId(m.messageId);
					canForward = true;
				}
			}
		}
		
		if(canForward) {
			receivedMessage(m);
			//ystem.out.println("(lo inoltro)");
			sendBroadcastMessage(m);
		}
		else {
			//System.out.println("(NON lo inoltro)");
		}
	}
	
	// prendo un messaggio e lo invio al prossimo nella rete
	private void sendBroadcastMessage(Message m) {
		
		boolean sent = false;
		
		//try{Thread.sleep(1000);} catch(Exception e) {}
	
		Node next = ring.nearestActive();
		while(!sent && next != null ) {
				
			try {	
				//System.out.println("inoltro a " + next.getPeerCredential().getPeerName());
				next.getRemoteRef().forwardBroadcast(m);
				sent = true;
			} catch(RemoteException e) {
				notifyCrashed(next);
				next = ring.nearestActive();
				e.printStackTrace();
			}
		}
		
		if(next == null) {
			System.out.println("(sono rimasto da solo nella ring!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
	}	
	
	///////////////////////////
	
	// segnala l'errore SOLO se non era stato segnlato prima
	private void notifyCrashed(Node n) {
		
		synchronized(n.getActive()) {
			
			if(n.isActive()) {
				System.out.println("nodo " + n.getPeerCredential().getPeerName() + " down!");
				n.setActive(false);
				crashDetected(n);
			}
			
		}
		
	}
		
	private void reportListCrashed(Set<String> crashed) {
		//System.out.println("/////");
		for(String i : crashed) {
			Node crashedNode = ring.getNodeByName(i);
			//System.out.println(i);
			if(crashedNode != null) {
				notifyCrashed(crashedNode);
			}
		}
		
	}
		
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void control() {
		
		while( !stop && ring.nearestActive() != null ) {
			sendControl(peerCredential, 12);
		}
		
		System.out.println("control ritornato");
		
	}
	
	private Set<String> sendControl(PeerCredential sendeCredential, int ttl) {
						
		Node nextNode = ring.nearestActive();
		if(nextNode != null) {
			try {
				 reportListCrashed( nextNode.getRemoteRef().forwardControl(sendeCredential, --ttl) );
			} catch(RemoteException e) {
				notifyCrashed(nextNode);
			}
		}
		
		return ring.crashedNodesNames();
			
	}

	// spedisco un messaggio di controllo che si ferma quando raggiunge me stesso nella ring
	@Override
	public Set<String> forwardControl(PeerCredential senderCredential, int ttl) {
						
		// se e' tornato a me aspetto per 10 sec e poi torno il fatto tutto ok
		if(senderCredential.equals(peerCredential)) {
		
			try {
				Thread.sleep(3000);
			} catch(Exception e) {
				e.printStackTrace();
			}
			return new HashSet<String>();
			
		} else if(ttl < 0) {
			return new HashSet<String>();
		} else {
			return sendControl(senderCredential, ttl);
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////
	
	boolean stop = false;
	public void stop() {
		stop = true;
	}
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	
	/*
	private void sendBroadcastMessage(Message m) {
			
		boolean sent = false;
		List<Node> activeNodes = ring.activeNodes();
		
		//try{Thread.sleep(1000);}catch(Exception e) {}
		
		while(!sent && (activeNodes.size() != 0) ) {
			
			Node nextNode = activeNodes.get(0);
			
			try {	
				System.out.println("inoltro a " + nextNode.getId());
				nextNode.getRemoteRef().forwardBroadcast(m);
				sent = true;
			} catch(RemoteException e) {

				notifyCrashed(nextNode);
				activeNodes.remove(0);
				
			}
		} // fine while
		
		if(activeNodes.size() == 0) {
			System.out.println("sono rimasto da solo nella ring!!!!");
		}
	}	
		*/
	
	// succede che se muore un player esso viene comunicato al sender originario
	// ma la catena di chiamate fatte dal player morto rimane up
	// cosi si dovrebbe mettere un timer che scatta ogni tanto nel nodo finale
	// in modo che esso si liberi e liberi a catena le precedenti.
	
	/*
	 * ho bisogno di sapere la versione del controllo per evitare che i messaggi viaggino all'infinito
	 * se ad esempio spedisco il controllo e poi crasho prima di ritornare a me il controllo,
	 * il messaggio di controllo continuera' a viaggiare SE la rete si e' adattata prima che il messaggio
	 * sia stato forwardato! potrei mettere un time to live della lunghezza della ring? no, perche i nodi
	 * possono scendere (o si'?) comunque e' meglio non fare mai affidamento su isActive perche 
	 * esso potrebbe essere aggiornato anche dopo molto tempo. Un TTL adesso che ci penso cade a pennello
	 * se si vuole evitare di inserire altre strutture dati.
	 */
	
	
/////
	
	/* e' possibile che due nodi distanti vadano in crash contemporanemanete e quindi control
	 * potrebbe non accrogersi immediatamente del guasto (ma in un secondo momento alla prossima
	 * girata di control si')
	 * 
	 * questo puo' dare problemi? no, se ho la mossa mando in broadcast la risposta e non mi interessa
	 * se un nodo distante era in crash, se invece sto attendendo una mossa proprio dal player che e' andato
	 * in crash comunque gli altri sono fermi perche' sanno che il non sono in crash, quando il control
	 * si accorge che la mossa e' per me (facciamo circa 3 secondi?) allora potro' giocare (o scegliere il
	 * vincitore, o accettare nouvi giocatori dal server).
	 * 
	 * problemi di concorrenza conl capo active? active e' MODIFICATO sempre verso il basso (non active)
	 * soltanto da notifyCrashed che ha la variabile protetta e termina sempre (mai hold and wait per qualcos
	 * altro o per chiamate remote), il problema e' pero' se ho gia' caricato dei nodi da contattare per i quali
	 * suppongo siano active, ma il problema non si pone perche' anche se fossero in crash me ne accorogo subito
	 * quando li contatto.
	 * 
	 * notifyCrashed avra' un hook verso l'interfaccia che si modifichera' facendo vedere chi e' crashato e 
	 * eventualmente abilitando i tasti per fare la giocata, se questo e' pesante computazionalmente allora
	 * lo si fara' in un thread parallelo (ricordare che notifycrashed e' chiamato da metodi remoti)
	 * 
	 * problema: potrebbe esserci problemi DOPO? cioe' immagina che molti nodi siano andati in crash in un tempo
	 * molto vicino tra loro (raro ma possibile), potrebbe fare casino negli aggiornamenti grafici successivi?
	 * scenario possibile: dopo questi crash eventualemente in mezzo alle loro notifiche ho la mossa, ok
	 * no problem, anche se non ho la mossa non vedo problemi
	 * 
	 * IN GENERALE non mi interessa nel fare il broadcast chi sia down, idealmente potrei ogni volta controllare
	 * i vicini e rispedire finalmente a chi non e' down, HO BISOGNO di sapere i crash invece quando sono passivo
	 * e invece potrei ricevere il turno dopo che il precedente e' andato in crash, ma anche se me ne accorgo
	 * dopo 3 secondi questo e' accettabile
	 * 
	 * se c'e' un crash quando ho la mossa io oppure sto spedendo il risultato della partita o altro possono
	 * esserci problemi?? no, la mossa e' sempre valida anche se uno e' andato in crash, per il risultato ok,
	 * lui avra' vinto o perso indipendentemente dal crash, se invece la mossa andava a lui, il successivo
	 * che deve prendere la mossa lo considerera' fold.
	 * 
	 * QUINDI QUESTO METODO CONTROL SEMBRA OK, e al massimo aspetta 3 secondi per accorgeresene, ma rarissimamente,
	 * in generale un crash e' notificato immediatamente
	 * 
	 * 
	 * ATTENZIONE!!!! nel broadcast forwardo se 
	 * * non sono stato io a lanciare (OK)
	 * * il messaggio ricevuto e' nuovo e non e' mai stato visto da me
	 * * se il nodo e' attivo!!!!!!!!!!!!!!
	 * 
	 *  problema! se e' vero che il nodo deve essere attivo, ma chi se ne frega?
	 *  se il messaggio e' nuovo lo ricevo e lo forwardo, esso poi si fermera' subito appena uno
	 *  si accorge che non e' piu' recente ed e' stato visto!! RICODA, no problemi di reti lente
	 *  quindi... non e' possibile che il nodo dichiarato down spedisca qualcosa
	 *  (tra l'atro un nodo dichiarato down per come e' fatto il modello e' veramente down,
	 *  nel senso che e' caduta la connessione e non perche non parla da 10 sec per esempio,
	 *  quindi e' impossibile che un nodo dichiarato down mandi un messaggio postumo)
	 * 
	 */
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
/*
 
	public void control() {
		
		while(ring.activeNodes().size() != 0 ) {
			int crashed = sendControl(thisPeerId);
			if(crashed != -1) { 
				System.out.println("crashed: " + crashed);
			}
			
		}
		
	}
	
	private int sendControl(int idSender) {
		
		if(ring.activeNodes().size() != 0) {
			Node next = ring.activeNodes().get(0);
			int crashed;
			try {
				crashed = next.getRemoteRef().forwardControl(idSender);
			} catch(RemoteException e) {
				next.setActive(false);
				//reportCrashed(next);
				crashed = next.getId();
			}
			return crashed;
		} else 
			return -1;
			
	}

	// spedisco un messaggio di controllo che si ferma quando raggiunge me stesso nella ring
	public int forwardControl(int idSender) {
				
		// succede che se muore un player esso viene comunicato al sender originario
		// ma la catena di chiamate fatte dal player morto rimane up
		// cosi si dovrebbe mettere un timer che scatta ogni tanto nel nodo finale
		// in modo che esso si liberi e liberi a catena le precedenti.
		
		if(idSender == thisPeerId) {
			//wait forever
			Object monitor = new Object();
		
			try { Thread.sleep(10000); } catch(Exception e) {
			    System.out.println("errore sleep in forwadControl");
				e.printStackTrace();
			}
		
			return -1;
		} else
			// non mi segno gli errori rilevati qui ?? essi sono stati rileavti nell'ambito di controlli di altri
			// se li segna in send control
			return sendControl(idSender);
		
	}
	
	
	*/	
	
}




