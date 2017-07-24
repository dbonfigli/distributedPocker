package protocol;

import java.rmi.*;
import java.util.List;
import java.util.Set;

import server.GameDescription;

public interface PeerRemoteInterface extends Remote {
	
	public void forwardBroadcast(Message m) throws RemoteException;
	public Set<String> forwardControl(PeerCredential senderCredential, int ttl) throws RemoteException;
	public void startNewGame() throws RemoteException;
	public void initRing(List<PeerCredential> playersList) throws RemoteException;
	
}