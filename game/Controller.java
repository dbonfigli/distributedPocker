package game;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import protocol.Message;
import protocol.Node;
import protocol.Peer;
import protocol.PeerCredential;
import protocol.Ring;
import protocol.Message.MessageType;
import server.CannotCreateGameException;
import server.CannotRegisterException;
import server.CannotUnregisterException;
import server.GameDescription;
import server.ServerRemoteInterface;
import sun.text.normalizer.UProperty;
import game.GameState.StateOfPlay;
import game.PlayerState.BlindEnum;
import game.PlayerState.PlayerChoiceEnum;
import gui.MainWindow;

public class Controller {

	private MainWindow mainWindow;

	private ServerRemoteInterface remoteServer;
	private Player player;
	
	private int myPosition;
	
	private PeerCredential myPeerCredential;
	
	GameState gameState;
	
	public synchronized void disconnect() {

		mainWindow.getButtonsPanel().setEnabledFoldB(false);
		mainWindow.getButtonsPanel().setEnabledCheckB(false);
		mainWindow.getButtonsPanel().setEnabledBetB(false);
		
		for(int i=0; i<10; i++)
			gameState.playersState[i] = null;
		for(int i=0; i<5; i++)
			gameState.cardsInTable[i] = null;
		
		mainWindow.updateGui(gameState, myPosition, false);
		
		((Peer) player).stop();
		
		try {
			UnicastRemoteObject.unexportObject(player, true);
		} catch (NoSuchObjectException e) {
			e.printStackTrace();
		}
		
		try {
			player = new Player(myPeerCredential, this);
		} catch (RemoteException e) {
			System.out.println("errore creazione player");
			e.printStackTrace();
		}
		
		try{
			Naming.rebind(myPeerCredential.getPeerURL(), player);
		} catch (MalformedURLException e) {
			System.out.println("url del player malformato");
			e.printStackTrace();
		} catch(RemoteException e) {
			System.out.println("errore rebind per il player");
			e.printStackTrace();
		}
		
		
	}
	
	// creo grafica server e mio peer (player)
	public Controller(String[] args) {
				
		//crea player e mettilo online ///////////////
		String name = args[0];
		String peerPort = args[1];
		PeerCredential peerCredential = new PeerCredential("rmi://127.0.0.1:"+ peerPort + "/" + name, name);
		myPeerCredential = peerCredential;
		try {
			player = new Player(peerCredential, this);
		} catch (RemoteException e) {
			System.out.println("errore creazione player");
			e.printStackTrace();
		}
		
		try{
			Naming.rebind(peerCredential.getPeerURL(), player);
		} catch (MalformedURLException e) {
			System.out.println("url del player malformato");
			e.printStackTrace();
		} catch(RemoteException e) {
			System.out.println("errore rebind per il player");
			e.printStackTrace();
		}
		//////////////////////////////////////////////
		
		//crea server ////////////////////////////////
		
		try {
			String address = args[2];
			String serverPort = args[3];
			remoteServer = (ServerRemoteInterface) Naming.lookup("rmi://"+ address + ":" + serverPort + "/ServerXXX");
		} catch (Exception e) {
			System.out.println("errore lookup server");
			e.printStackTrace();
		}
		
		///////////////////////////////////////////////
		
		//crea interfaccia grafica ////////////////////
		mainWindow = new MainWindow(this);
		///////////////////////////////////////////////
		
		
		//TODO da eliminare quando finisco i test
		try {
			remoteServer.register(player.GetPeerCredential(), "gioco1");
		} catch (RemoteException e) {
		
			e.printStackTrace();
		} catch (CannotRegisterException e) {
		
			e.printStackTrace();
		}
		
	}

	///// metodi per la registrazione //////////////////////////////
	
	public  HashMap<String, GameDescription> getAvailableGamesFromServer() throws RemoteException {
		return remoteServer.getAvailableGames();
	}

	public void createNewGameInServer(String gameName, int playerNum) throws RemoteException, CannotCreateGameException {
		remoteServer.createNewGame(gameName.replace(" ", "_"), playerNum);
	}

	public void registerToServer(String gameName) throws RemoteException, CannotRegisterException {	
		remoteServer.register(player.GetPeerCredential(), gameName);
	}

	public void unregisterToServer(String gameName) throws RemoteException, CannotUnregisterException {
		remoteServer.unregister(player.GetPeerCredential(), gameName);	
	}
	
	//// inizializzazione ring /////////////////////////////////////////////////
	List<PeerCredential> credentialList;
	public void ringInitiated(List<PeerCredential> pclist) {
		
		credentialList = pclist;
		
		// setto la mia posizione
		int pos=0;
		for(PeerCredential pc : pclist) {
			if( pc.equals(player.GetPeerCredential()) ) {
				myPosition = pos;
				break;
			}
			pos++;
		}
		
		if(myPosition == 0)
			gameState = GameState.createFirstGameState(pclist, myPosition, new ArrayList<PeerCredential>());		
		
		(new Thread(new waitAndLaunchControl())).start();
	}

	public class waitAndLaunchControl implements Runnable {

		public void run() {
			//System.out.println("aspetto 3 secondi per lanciare il controllo");
		/*	try {//TODO aggiungere o no?
				//JOptionPane.showMessageDialog(new JFrame(), "the game will start in 3 seconds!", "ready?", JOptionPane.INFORMATION_MESSAGE);
				Thread.sleep(3000);
			} catch (Exception e) {
				System.out.println("errore Thread.sleep");
				e.printStackTrace();
			} */
			
			// se sono in posizione 0 allora ho gia' creato un nuovo gamestate, adesso devo inviarlo a tutti
			if(myPosition == 0) {
				player.broadcast(MessageType.GAME_MESSAGE, gameState);
				Turn.calculateNextState(gameState, myPosition); //TODO perche' non chiamo newStateReceived?
				mainWindow.updateGui(gameState, myPosition, false);
			}
			
			player.control();
			
		}
	}
	
	//// metodi di risposta a bottoni //////////////////////////////////////////
	
	public synchronized void chat(String text) {
		mainWindow.getMessageArea().println(player.GetPeerCredential().getPeerName() + ": " + text);
		player.broadcast(MessageType.CHAT_MESSAGE, text);
	}
		
	public synchronized void check() {

		// tolgo i soldi per il check, imposto me stesso a check, spedisco a tutti lo stato (anche a me)
		
		//disabilito tutti i bottoni
		mainWindow.getButtonsPanel().setEnabledFoldB(false);
		mainWindow.getButtonsPanel().setEnabledCheckB(false);
		mainWindow.getButtonsPanel().setEnabledBetB(false);
		
		
		PlayerState me = gameState.playersState[myPosition];
		me.setChoice(PlayerChoiceEnum.CALL);
		
		// se ho i soldi
		if(gameState.greatestBet <= me.getTotalMoney()) {
			gameState.moneyOnTable += gameState.greatestBet - me.getMoneyBet();
			me.setMoneyBet(gameState.greatestBet);
		}
		else { // non ho i soldi
			gameState.moneyOnTable += me.getTotalMoney() - me.getMoneyBet();
			me.setMoneyBet(me.getTotalMoney());
		}
		
		player.broadcast(MessageType.GAME_MESSAGE, gameState);
		//newGameStateReceived(gameState);
		new NewStateRecv((GameState) gameState).run();
		
	}
		
	public synchronized void fold() {
		
		mainWindow.getButtonsPanel().setEnabledFoldB(false);
		mainWindow.getButtonsPanel().setEnabledCheckB(false);
		mainWindow.getButtonsPanel().setEnabledBetB(false);
		
		// aggirono il mio stato
		PlayerState me = gameState.playersState[myPosition];
		me.setChoice(PlayerChoiceEnum.FOLD);
		
		player.broadcast(MessageType.GAME_MESSAGE, gameState);
		//newGameStateReceived(gameState);
		new NewStateRecv((GameState) gameState).run();
	}

	public synchronized void bet(int moneyBet) {
		
		mainWindow.getButtonsPanel().setEnabledFoldB(false);
		mainWindow.getButtonsPanel().setEnabledCheckB(false);
		mainWindow.getButtonsPanel().setEnabledBetB(false);
		
		PlayerState me = gameState.playersState[myPosition];
		
		//int moneyBet = //mainWindow.getButtonsPanel().getMoneyBet();
		gameState.greatestBet = moneyBet;
		
		//gameState.lastWhoRised = myPosition;
		gameState.pWhoMustBet = new PPlaying(gameState.playersState);
		gameState.pWhoMustBet.makeFirst(myPosition);
		
		
		gameState.moneyOnTable +=  moneyBet - me.getMoneyBet(); 
		
		// aggirono il mio stato
		me.setChoice(PlayerChoiceEnum.RAISE);
		me.setMoneyBet(moneyBet);
		
		player.broadcast(MessageType.GAME_MESSAGE, gameState);
		//newGameStateReceived(gameState);
		new NewStateRecv((GameState) gameState).run();
		
	}

	/////// metodi chiamati se arriva un messaggio ///////////////////////////////////////////
	
	public void receivedMessage(Message m) {
		
		if(m.messageType.equals(MessageType.CHAT_MESSAGE)) {
			mainWindow.getMessageArea().println(m.senderCredential.getPeerName() + ": " + (String) m.message);
		}
		else if(m.messageType.equals(MessageType.GAME_MESSAGE)) {
			//newGameStateReceived((GameState) m.message);
			new NewStateRecv((GameState) m.message).start();
		}
		
	}

	public class NewStateRecv extends Thread {
		GameState gs;
		public NewStateRecv(GameState gs) {
			this.gs = gs;
		}
		public void run() {
			newGameStateReceived(gs);	
		}
	}
		
	public void mergeState(GameState gs) {		
		
		// clono perche il gs deve essere mantenuto e non modificato per tutta la
		// durata del broadcast che presumibilmente ancora deve concludersi
		GameState gsc = gs.clone();
		
		if(gameState != null) {
			// fai il merge dei giocatori offline
			for(int i=0; i<10; i++)
				if( gameState.playersState[i] !=null &&
					!gameState.playersState[i].isOnline() )
					gsc.playersState[i].setOnline(false);
			
			// controlla che non ci siano giocatori nella coda di scommessa che sono andati offline
			List<Integer> toRemove = new ArrayList<Integer>();
			for(Integer i : gsc.pWhoMustBet.getActive())
				if( i != 1000 && gameState.playersState[i] != null && !gameState.playersState[i].isOnline())
					toRemove.add(i);
			for(Integer i : toRemove) {
				int indexToRemove = gsc.pWhoMustBet.getActive().indexOf(i);
				if(indexToRemove != -1) 
					gsc.pWhoMustBet.getActive().remove(indexToRemove);
			}
		}
		
		gameState = gsc;		
	}
	
	// la storia e' questa: se c'e' un crash COMUNQUE E SEMPRE a un certo punto me lo dice crashdetected
	// il punto e' che se me lo dice prima crashdetected e poi newgameStateReceived e' un casino
	// quindi devo sempre fare un controllo se il turno e' mio anche in newGameStateReceived 
	private synchronized void newGameStateReceived(GameState gs) {
		
		System.out.println("prima: ");
		if(gameState!=null) gameState.printState();
		System.out.println("ricevuto: ");
		gs.printState();
		
		mergeState(gs);
	
		System.out.println("merge: ");
		gameState.printState();
		
		Turn.calculateNextState(gameState, myPosition);
		
		System.out.println("calcolato: ");
		gameState.printState();
		
		////////////////////////////////////////////////////////////////////////////////////////
		// SE GIOCO FINITO
		if(Turn.isGameFinished(gameState)) {

			// se il gioco e' finito e non ho gia' calcolato l'outcome lo faccio adesso
			if(!doNotCalculateOutcome)
				calculateOutcome();
			
			// reiposto la scelta dei players perche' se tutti erano fold e crash l'unico
			// non fold allora nel calculateNext c'e' loop infinito
			for(PlayerState ps : gameState.playersState)
				if(ps != null) ps.setChoice(PlayerChoiceEnum.EMPTY);
			
			// calcolo il nuovo dealer e gli metto il turno
			gameState.dealer = Turn.calculateNext(gameState, gameState.dealer);
			gameState.turn = gameState.dealer;
			
			// se sono io il dealer allora calcolo il prossimo stato iniziale 
			// e lo broadcasto, anche a me stesso
			if(myPosition == gameState.turn) {

				/*
				//controllo failover da eliminare quando capisco che e' tutto ok!
				System.out.println("dsdsasadads   " + gameState.turn + " " + myPosition);
				if(myPosition == 1)
					System.exit(0);
				*/
				
				// se sono tutti offline allora basta
				if(checkAllOffine()) {
					notifyYouAreAlone();
					return;
				}
				
				Turn.calculateNextFirstState(gameState);
				player.broadcast(MessageType.GAME_MESSAGE, gameState);
				newGameStateReceived(gameState);	
			}
			
			mainWindow.updateGui(gameState, myPosition, false);
		}
		
		////////////////////////////////////////////////////////////////////////////////////////
		// GIOCO NON FINITO E TURNO E' MIO
		else if(myPosition == gameState.turn) {
			
			// abilito sempre i tasti fold e check
			mainWindow.getButtonsPanel().setEnabledFoldB(true);
			mainWindow.getButtonsPanel().setEnabledCheckB(true);
			
			// abilito bet solo se ho i soldi
			PlayerState me = gameState.playersState[myPosition];
			if( me.getTotalMoney() <= gameState.greatestBet )
				mainWindow.getButtonsPanel().setEnabledBetB(false);
			else {
				mainWindow.getButtonsPanel().setEnabledBetB(true);
				mainWindow.getButtonsPanel().setMaxBet(me.getTotalMoney());
				mainWindow.getButtonsPanel().setMinBet(gameState.greatestBet + 1);
			}
			mainWindow.updateGui(gameState, myPosition, false);
		} 
		
		////////////////////////////////////////////////////////////////////////////////////////
		// GIOCO FINITO E TURNO NON MIO
		else 
			mainWindow.updateGui(gameState, myPosition, false);
	}
	
	private void calculateOutcome() {
		
		String winnerS = null;
		
		// controllo quanti giocatori rimasti
		int active = 0;
		PlayerState winP = null;
		for(PlayerState ps : gameState.playersState)
			if( ps != null &&
				(ps.getChoice().equals(PlayerChoiceEnum.CALL) || 
				 ps.getChoice().equals(PlayerChoiceEnum.RAISE) ||
				 ( ps.getChoice().equals(PlayerChoiceEnum.EMPTY) && ps.isOnline() )
				)
			) {
				winP = ps;
				active++;
			}
		
		if(active == 1) {
			winP.setTotalMoney(gameState.moneyOnTable + winP.getTotalMoney());
			winnerS = winP.getPeerCredential().getPeerName() + " ";
		} else if(active > 1) {
			
			// calcolo i vincitori e printo i risultato
			BestGameToShow bg = Rule.winnerInGameState(gameState);
			mainWindow.getMessageArea().println(bg.printResult(gameState));
			
			//calcola i soldi da dare, per adesso tutti in parti uguali
			int moneyToGiveEach = gameState.moneyOnTable / bg.orderedWins.get(0).size(); 
			
			// dalli a chi se lo merita
			for(Integer i : bg.orderedWins.get(0)) {
				PlayerState ps = gameState.playersState[i];
				ps.setTotalMoney(moneyToGiveEach + ps.getTotalMoney());
			}
			
			// se la divisione da 1 di resto lo do al primo
			if(gameState.moneyOnTable % bg.orderedWins.get(0).size() == 1) {
				PlayerState ps = gameState.playersState[bg.orderedWins.get(0).get(0)];
				ps.setTotalMoney(ps.getTotalMoney() + 1);
			}
			
			// stringa dei nomi dei vincitori
			winnerS = "";
			for(Integer i : bg.orderedWins.get(0))
				winnerS += gameState.playersState[i].getPeerCredential().getPeerName() + " ";
		}
		
		//togli i soldi scommessi a tutti
		for(int i=0; i<10; i++) {
			PlayerState ps = gameState.playersState[i];
			if(ps != null && ps.isOnline())
				ps.setTotalMoney( ps.getTotalMoney() - ps.getMoneyBet() );
		}
		
		// metto la scommessa di ognuno a 0 cosi' si vedono bene i soldi totali che hanno
		for(PlayerState ps : gameState.playersState)
			if(ps!=null) ps.setMoneyBet(0);		
		mainWindow.updateGui(gameState, myPosition, true);
		
		// faccio vedere un dialog e aspetto che accetti per giocare di nuovo
		// calculate outcome infatti e' chiamata da newGameStateReceived che e'
		// synchronized e quindi se arriva un nuovo stato si mette in coda

		String me = gameState.playersState[myPosition].getPeerCredential().getPeerName();
		if(winnerS != null)
			JOptionPane.showMessageDialog(new JFrame(), me + ": the hand is ended, " +
				winnerS + "won! ready for the next hand?", "results", JOptionPane.INFORMATION_MESSAGE);
		else
			JOptionPane.showMessageDialog(new JFrame(), me + ": the hand is ended, nobody won, ready for the next hand?",
				"results", JOptionPane.INFORMATION_MESSAGE);
		
	}	

	private int ncrashed = 0;
	private List<PeerCredential> exclude = new ArrayList<PeerCredential>();
	// doNotCalculateOutcome e' a true solo per il tempo della chiamata a newGameStateReceived
	private boolean doNotCalculateOutcome = false;
	public synchronized void crashDetected(PeerCredential peerCredential) {
		
		mainWindow.getMessageArea().println("player "+ peerCredential.getPeerName() + " crashed!");
		
		if(gameState!=null)
			for(int i=0; i<10; i++) {
				PlayerState ps = gameState.playersState[i];
				if(ps != null && ps.getPeerCredential().equals(peerCredential) ) {
					ps.setOnline(false);
									
					// controlla che ci siano altri giocatori online
					if(checkAllOffine()) {
						notifyYouAreAlone();
					} else if(gameState.turn == i) { 
						// se era morto proprio chi aveva il turno ....						 
						
						// ma se' e' morto il dealer che doveva dare le carte a gioco finito...
						if(Turn.isGameFinished(gameState) && gameState.turn == gameState.dealer)
							doNotCalculateOutcome = true;							
							
						newGameStateReceived(gameState);
						doNotCalculateOutcome = false;
					}
					else { // elimino da quelli che ancora devono scommettere il crashed
						int indexToRemove = gameState.pWhoMustBet.getActive().indexOf(i);
						if(indexToRemove != -1) 
							gameState.pWhoMustBet.getActive().remove(indexToRemove);
						mainWindow.updateGui(gameState, myPosition, false);
					}
					
					break;
				} // fine if
			} // fine for
		else { // lo stato non e' ancora stato creato
			ncrashed++;
			exclude.add(peerCredential);
			// se il gamestate non era ancora inizializzato
			if(myPosition == ncrashed) {
				gameState = GameState.createFirstGameState(credentialList, myPosition, exclude);
				//elimina il crashed
				player.broadcast(MessageType.GAME_MESSAGE, gameState);
				Turn.calculateNextState(gameState, myPosition);
				mainWindow.updateGui(gameState, myPosition, false);
			}
		}
		
	}
	
	private void notifyYouAreAlone() {
		JOptionPane.showMessageDialog(new JFrame(), "all players lost all the money or are gone offline, game finisched!",
				"results", JOptionPane.INFORMATION_MESSAGE);
		mainWindow.getButtonsPanel().setEnabledFoldB(false);
		mainWindow.getButtonsPanel().setEnabledCheckB(false);
		mainWindow.getButtonsPanel().setEnabledBetB(false);
		mainWindow.updateGui(gameState, myPosition, true);
	}

	public boolean checkAllOffine() {
		// controlla che te non sia il solo giocatore rimasto
		int alive=0;
		for(PlayerState ps : gameState.playersState)
			if(ps != null && ps.isOnline() && ps.getTotalMoney()>0)
				alive++;
		if(alive <= 1)  // sono rimasto solo
			return true;
		else
			return false;
	}
		
}

