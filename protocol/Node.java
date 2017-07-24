package protocol;

import java.rmi.Naming;
import java.util.Date;
import java.util.List;


public class Node {

	private PeerRemoteInterface remoteRef;
	private PeerCredential peerCredential;
	
	// pericolo di concorrenza
	private volatile Integer lastMessageId;
	private volatile Boolean active; 
	//
	
	public Node(PeerCredential peerCredential) {
		
		this.peerCredential = peerCredential;
		lastMessageId = 0;
		active = true;
		
		try {
			remoteRef = (PeerRemoteInterface) Naming.lookup(peerCredential.getPeerURL());
			System.out.println("cerco di collegarmi a " + peerCredential.getPeerURL());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public Boolean getActive() {
		return active;
	}

	public Boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public PeerRemoteInterface getRemoteRef() {
		return remoteRef;
	}

	public PeerCredential getPeerCredential() {
		return peerCredential;
	}

	public Integer getLastMessageId() {
		return lastMessageId;
	}

	public void setLastMessageId(Integer lastMessageId) {
		this.lastMessageId = lastMessageId;
	}


	
	
	
}
