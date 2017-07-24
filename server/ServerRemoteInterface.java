package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import protocol.PeerCredential;

public interface ServerRemoteInterface extends Remote {
 
	public void createNewGame(String gameName, int palyerNum) throws RemoteException, CannotCreateGameException;
	public HashMap<String, GameDescription> getAvailableGames() throws RemoteException;
	public void register(PeerCredential peerCredential, String gameName) throws RemoteException, CannotRegisterException;
	public void unregister(PeerCredential peerCredential, String gameName) throws RemoteException, CannotUnregisterException;
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////
	
	//public List<PeerCredential> register(String playerURL, String playerName) throws RemoteException;
	
	
}
