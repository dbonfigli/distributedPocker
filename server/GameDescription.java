package server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import protocol.PeerCredential;

public class GameDescription implements Serializable {

	public enum gameStateEnum { WAITING_PALYERS, GAME_STARTED }
	
	private List<PeerCredential> players;
	private gameStateEnum state;
	private int nPlayersRequired;
	
	public GameDescription(int playerRequired) {
		this.nPlayersRequired = playerRequired;
		state = gameStateEnum.WAITING_PALYERS;
		players = new ArrayList<PeerCredential>();
	}
	
	public List<PeerCredential> getPlayers() {
		return players;
	}

	public gameStateEnum getState() {
		return state;
	}

	public void setState(gameStateEnum state) {
		this.state = state;
	}

	public int getNPlayersRequired() {
		return nPlayersRequired;
	}
	
	 
}
