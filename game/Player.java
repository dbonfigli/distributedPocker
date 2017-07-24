package game;

import java.rmi.RemoteException;
import java.util.List;

import protocol.Message;
import protocol.Node;
import protocol.Peer;
import protocol.PeerCredential;
import protocol.Ring;

public class Player extends Peer {

	private Controller controller;
	
	public Player(PeerCredential pc, Controller c) throws RemoteException {
		super(pc);		
		this.controller = c;
	}
	
	@Override
	protected void crashDetected(Node n) {
		controller.crashDetected(n.getPeerCredential());
		
	}

	@Override
	protected void receivedMessage(Message m) {
		controller.receivedMessage(m);
	}

	@Override
	protected void ringInitiated(List<PeerCredential> pclist) {
		controller.ringInitiated(pclist);		
	}

}
