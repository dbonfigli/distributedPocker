package game;

import java.io.Serializable;

import protocol.PeerCredential;

public class PlayerState implements Serializable, Cloneable {

	private PeerCredential peerCredential;
	private int totalMoney;
	private int moneyBet;
	private boolean online;
	private BlindEnum blind;
	private PlayerChoiceEnum choice;
	private Card card1;
	private Card card2;
	private int position;
	
	public enum PlayerChoiceEnum { RAISE, CALL, FOLD, EMPTY }
	public enum BlindEnum { LITTLE_BLIND, BLIND, NO }
	
	//TODO da cancellare
	public String toString() {
		return peerCredential.getPeerName() + " " + choice + " " + totalMoney;
	}
	
	public PlayerState clone() {
		
		PlayerState ps = new PlayerState(peerCredential, position);
		ps.peerCredential = new PeerCredential(peerCredential.getPeerURL(), peerCredential.getPeerName());
		ps.totalMoney = totalMoney;
		ps.moneyBet = moneyBet;
		ps.online = online;
		ps.blind = blind;
		ps.choice = choice;
		if(card1!=null)
			ps.card1 = card1.clone();
		if(card2!=null)
			ps.card2 = card2.clone();
		
		return ps;
		
	}
	
	public PlayerState(PeerCredential pc, int position) {
		this.position = position;
		peerCredential = pc;
		totalMoney = 1000;
		moneyBet = 0;
		online = true;
		blind = BlindEnum.NO;
		choice = PlayerChoiceEnum.EMPTY;
		card1 = null;
		card2 = null;
	}
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int pos) {
		position = pos;
	}

	public int getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(int totalMoney) {
		this.totalMoney = totalMoney;
	}

	public int getMoneyBet() {
		return moneyBet;
	}

	public void setMoneyBet(int moneyBet) {
		this.moneyBet = moneyBet;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}
	
	public BlindEnum getBlind() {
		return blind;
	}

	public void setBlind(BlindEnum blind) {
		this.blind = blind;
	}

	public PlayerChoiceEnum getChoice() {
		return choice;
	}

	public void setChoice(PlayerChoiceEnum choice) {
		this.choice = choice;
	}

	public PeerCredential getPeerCredential() {
		return peerCredential;
	}

	public Card getCard1() {
		return card1;
	}

	public void setCard1(Card card1) {
		this.card1 = card1;
	}

	public Card getCard2() {
		return card2;
	}

	public void setCard2(Card card2) {
		this.card2 = card2;
	}
	
	
}
