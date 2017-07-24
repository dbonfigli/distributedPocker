package server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.*;

import protocol.Message;
import protocol.PeerCredential;
import protocol.PeerRemoteInterface;
import server.GameDescription.gameStateEnum;


public class Server extends UnicastRemoteObject implements ServerRemoteInterface {

	HashMap<String, GameDescription> availableGames;
	
	public Server() throws RemoteException {
		availableGames = new HashMap<String, GameDescription>();
	}
	
	@Override
	public synchronized HashMap<String, GameDescription> getAvailableGames() throws RemoteException {
		return availableGames;		
	}

	@Override
	public synchronized void createNewGame(String gameName, int playerNum)
		throws RemoteException, CannotCreateGameException {
		
		if(availableGames.get(gameName) != null || playerNum > 10 || playerNum <= 2)
			throw new CannotCreateGameException();
		else {
			GameDescription gm = new GameDescription(playerNum);
			availableGames.put(gameName, gm);
		}
		
	}

	@Override
	public synchronized void register(PeerCredential peerCredential, String gameName)
		throws RemoteException, CannotRegisterException {
		
		//se e' gia registrato a una partita non puo' registrarsi due volte
		for(Map.Entry<String, GameDescription> e : availableGames.entrySet())
			for(PeerCredential pc : e.getValue().getPlayers())
				if(pc.equals(peerCredential))
					throw new CannotRegisterException(); 
		
		GameDescription game = availableGames.get(gameName);
		
		//se il gioco non esisteva do errore
		if(game == null)
			throw new CannotRegisterException();
		else {
			
			// se esisteva gia uno con lo stesso nome do errore
			for(PeerCredential pc : game.getPlayers()) {
				if(pc.getPeerName().equals(peerCredential.getPeerName()))
						throw new CannotRegisterException();
			}
			
			game.getPlayers().add(peerCredential);
			if(game.getPlayers().size() == game.getNPlayersRequired()) {
				
				// sveglia tutti, il gioco puo' iniziare setta lo stato a game started
				game.setState(gameStateEnum.GAME_STARTED);
				for(PeerCredential pc : game.getPlayers()) {
					
					try {
						PeerRemoteInterface remoteRef = (PeerRemoteInterface) Naming.lookup(pc.getPeerURL());
						System.out.println("do la risposta a " + pc.getPeerURL());					
						remoteRef.initRing(game.getPlayers());
					}
					catch(Exception e) {
						System.out.println("errore nel dare la risposta di initRing a un player");
						e.printStackTrace();
					}
				}
				
				for(PeerCredential pc : game.getPlayers()) {
					
					try {
						PeerRemoteInterface remoteRef = (PeerRemoteInterface) Naming.lookup(pc.getPeerURL());
						System.out.println("consento l'avvio a " + pc.getPeerURL());					
						remoteRef.startNewGame();
					}
					catch(Exception e) {
						System.out.println("errore nel dare la risposta di startNewGame a un player");
						e.printStackTrace();
					}
				}
				
				availableGames.remove(gameName);
				
			}
		}
		
	}

	@Override
	public synchronized void unregister(PeerCredential peerCredential, String gameName)
			throws RemoteException, CannotUnregisterException {
		
		GameDescription game = availableGames.get(gameName);
		if(game == null || game.getState() == gameStateEnum.GAME_STARTED || !game.getPlayers().contains(peerCredential))
			throw new CannotUnregisterException();
		else
			game.getPlayers().remove(peerCredential);
		
	} 

	
	
	
	
	
	
	
	
	
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////
	
	/*
	private List<PeerCredential> registered;
	
	private Object monitor;
	
	final static int PLAYER_REQUIRED = 4;
	
	
	public Server() throws RemoteException {
		monitor = new Object();
		registered = new ArrayList<PeerCredential>();
	}
	
	public List<PeerCredential> register(String playerURL, String playerName) throws RemoteException {
		
		PeerCredential registeredPeer;
		
		synchronized(monitor) {
						
			registeredPeer = new PeerCredential(playerURL, playerName);
			registered.add(registeredPeer);
			if(registered.size() == PLAYER_REQUIRED)
				monitor.notifyAll();
			else try {
					monitor.wait();					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
				
		//adesso qui ci sono tutti i giocatori
		//creo una lista ordinata con tutti i successori nella ring
		List<PeerCredential> info = new ArrayList<PeerCredential>();
		int playerIndex = registered.indexOf(registeredPeer);
		for(int i = playerIndex + 1; i < playerIndex + registered.size(); i++) {
			info.add(registered.get(i%registered.size()));
			System.out.println("consegno a " + playerURL + " " 
					+ registered.get(i%registered.size()).peerName + " size " + registered.size());
		}
		return info;
			
	}
	*/
	
}
