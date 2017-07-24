package protocol;

import java.io.Serializable;

public class PeerCredential implements Serializable {
	
	private final String peerURL;
	private final String peerName;
	
	public PeerCredential(String peerURL, String peerName) {
		this.peerName = peerName;
		this.peerURL = peerURL;
	}

	public String getPeerURL() {
		return peerURL;
	}

	public String getPeerName() {
		return peerName;
	}
	
	@Override
	public boolean equals(Object pc) {
		if(peerURL.equals( ((PeerCredential) pc).peerURL) && peerName.equals( ((PeerCredential) pc).peerName))
			return true;
		else
			return false;
	}
	
	
}