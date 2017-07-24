package game;

import java.util.ArrayList;
import java.util.List;

import game.GameState.StateOfPlay;
import game.PlayerState.BlindEnum;
import game.PlayerState.PlayerChoiceEnum;
import protocol.Message.MessageType;

public class Turn {

	//dato uno stato ATTUALE, indica se il gioco e' finito 
	public static boolean isGameFinished(GameState gs) {
		
		// calcola i giocatori non null non fold non offline
		List<PlayerState> active = new ArrayList<PlayerState>();
		for(PlayerState ps : gs.playersState) 
			if( ps != null &&
				ps.isOnline() &&
				!ps.getChoice().equals(PlayerChoiceEnum.FOLD))
				active.add(ps);
		
		// se sono 0 o 1 allora il gioco e' finito
		if(active.size() <= 1)
			return true;
				
		// se nessuno deve piu scommettere e gia' sono state scaricarte tutte le carte
		// allora il gioco e' finito
		if(gs.pWhoMustBet.getActive().isEmpty() && gs.cardsInTable[4] != null)
			return true;
		
		return false;
	}
	
	// non faccio controlli se e' finito il gioco
	// faccio controlli se e' finito il preflop
	// calcolo il prossimo a giocare
	
	// dato uno stato ricevuto da un altro, lo aggiorna:
	// questo metodo calcola chi DOVREBBE giocare adesso e modifica StateOfPlay e butta giu' nuove carte se necessario
	
	// 1) rimuovi un giocatore da chi deve scommettere e setta il turno
	// 2) cambia lo stato da preflop a playing se e' il caso
	// 3) se il giro di scommesse era finito lo reimposto e scarico una carta a tavola
	// 4) se il gioco e' finito non faccio niente
	// 5) se il giro di scommesse non e' finito in pratica fa solo il punto 1
	public static void calculateNextState(GameState gs, int myPosition) {
				
		// rimuovo quello che aveva scommesso precendetemente se c'era
		//if(gs.pWhoMustBet.getActive().size() > 0)
		//	gs.pWhoMustBet.getActive().remove(0);
		
		if( gs.pWhoMustBet.getActive().size() > 0 &&
			( gs.pWhoMustBet.getActive().get(0) == 1000 ||
			  gs.pWhoMustBet.getActive().get(0) == gs.turn )
		  ) gs.pWhoMustBet.getActive().remove(0);
			
		
		if(isGameFinished(gs))
			return;
		
		////////////////////////////////////////////////////////////////////////////////////////
		// se nessuno deve piu scommettere e sono in preflop allora...
		if( gs.stateOfPlay.equals(StateOfPlay.PREFLOP) &&
			gs.pWhoMustBet.getActive().isEmpty() ) {
			
			// setto tutti i giocatori rimasti in gioco in modo che possano scommettano
			// con il primo al primo giocatore non fold a sinistra del dealer
			
			// qua e' impossibile che tutti hanno fatto fold perche' me ne sarei accorto prima
			// quando faccio isgamefinisced //TODO sicuro???
			gs.turn = calculateNext(gs, gs.dealer); 
			gs.pWhoMustBet = new PPlaying(gs.playersState);
			gs.pWhoMustBet.makeFirst( gs.turn );
			// scopro le carte e metto in stato playing
			gs.stateOfPlay = StateOfPlay.PLAYING;
			gs.uncoverACard();
			gs.uncoverACard();
			gs.uncoverACard();
			// SOLO CHI NON HA FATTO FOLD, lo metto a empty
			for(PlayerState ps : gs.playersState)
				if(ps != null && !ps.getChoice().equals(PlayerChoiceEnum.FOLD))
					ps.setChoice(PlayerChoiceEnum.EMPTY);
		}
		
		////////////////////////////////////////////////////////////////////////////////////////
		// se il giro di scommesse e' finito allora...
		else if(gs.pWhoMustBet.getActive().isEmpty()) {
			
			//se gioco finito basta non faccio niente
			//if(gs.cardsInTable[4] != null)
				//return;
			
			// scopro una carta e reimposto gli scommettitori
			// setto turn al primo giocatore non fold a sinistra dell'ultimo che ha giocato
			gs.uncoverACard();
			int next = calculateNext(gs, gs.turn);
			gs.turn = next;
			gs.pWhoMustBet = new PPlaying(gs.playersState);
			gs.pWhoMustBet.makeFirst( next );

		} 
		
		////////////////////////////////////////////////////////////////////////////////////////
		// normale gioco, ci sono persone che ancora devono scommettere
		else	
			// il turno e' get(0) perche' ho tolto precedentemente un giocatore che aveva scommesso
			gs.turn = gs.pWhoMustBet.getActive().get(0);
		
		
	}
	
	// calcola il prossimo non null non fold a partire da from, NON PUO' ESSERE offline
	// precondizione: almeno uno e' non null, online e non fold
	public static int calculateNext(GameState gs, int from) {
		
		int next = from + 1;
		boolean found = false;
		
		while(!found) {
			if(gs.playersState[next] != null &&
				gs.playersState[next].isOnline() &&
				gs.playersState[next].getTotalMoney() > 0 &&
				!gs.playersState[next].getChoice().equals(PlayerChoiceEnum.FOLD) )
				found = true;
			else
				next = (next + 1) % 10;
		}
		
		return next;
	}

	public static void calculateNextFirstState(GameState gameState) {
		
		// se sono io calcolo la nuova gamestate e broadcasto
		gameState.deck = new Deck();
		gameState.stateOfPlay = StateOfPlay.PREFLOP;

		gameState.cardsInTable = new Card[5];
		
		for(PlayerState pc : gameState.playersState)
			if(pc != null){
				if( pc.isOnline() && pc.getTotalMoney() != 0) {
					pc.setCard1(gameState.deck.takeCard());
					pc.setCard2(gameState.deck.takeCard());
				}
				else {
					pc.setCard1(null);
					pc.setCard2(null);	
				}
				pc.setChoice(PlayerChoiceEnum.EMPTY);
				pc.setBlind(BlindEnum.NO);
			}

		gameState.pWhoMustBet = new PPlaying(gameState.playersState);
		
		int littleBlind = Turn.calculateNext(gameState, gameState.dealer);
		int blind = Turn.calculateNext(gameState, littleBlind);
		int whoStart = Turn.calculateNext(gameState, blind);
						
		gameState.playersState[littleBlind].setBlind(BlindEnum.LITTLE_BLIND);
		if(gameState.playersState[littleBlind].getTotalMoney() >= 10)
			gameState.playersState[littleBlind].setMoneyBet(10);
		else
			gameState.playersState[littleBlind].setMoneyBet(gameState.playersState[littleBlind].getTotalMoney());
		
		gameState.playersState[blind].setBlind(BlindEnum.BLIND);
		if(gameState.playersState[blind].getTotalMoney() >= 20)
			gameState.playersState[blind].setMoneyBet(20);
		else
			gameState.playersState[blind].setMoneyBet(gameState.playersState[littleBlind].getTotalMoney());
		
		gameState.moneyOnTable = gameState.playersState[blind].getMoneyBet() + gameState.playersState[littleBlind].getMoneyBet();
		
		if(gameState.playersState[blind].getMoneyBet() > gameState.playersState[littleBlind].getMoneyBet())
			gameState.greatestBet = gameState.playersState[blind].getMoneyBet();
		else
			gameState.greatestBet = gameState.playersState[littleBlind].getMoneyBet();
		
		gameState.turn = whoStart;
		gameState.pWhoMustBet.makeFirst(whoStart);
		gameState.pWhoMustBet.getActive().add(0, 1000);
		
	}
}
