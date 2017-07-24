package game;

import game.PlayerState.BlindEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import protocol.PeerCredential;

public class GameState implements Serializable, Cloneable {

	public Deck deck;
	public int turn;
	public StateOfPlay stateOfPlay;
	public Card[] cardsInTable;
	public PlayerState[] playersState;
	public int moneyOnTable;
	public int greatestBet;
	public int dealer;
	public PPlaying pWhoMustBet;
	
	//TODO da togliere
	public void printState() {
		for(PlayerState ps : playersState) {
			System.out.print(ps + " ; ");
		}
		String s = "";
		for(Integer i : pWhoMustBet.getActive())
			s += i + " ";
		System.out.println("\ndealer: " + dealer + " turn: " + turn + " [ " + s + "]");
	}
	
	public enum StateOfPlay { PREFLOP, PLAYING }
	
	public GameState clone() {
		GameState gs = new GameState();
		gs.deck = deck.clone();
		gs.turn = turn;
		//gs.lastWhoRised = lastWhoRised;
		gs.moneyOnTable = moneyOnTable;
		gs.greatestBet = greatestBet;
		gs.dealer = dealer;
		gs.cardsInTable = new Card[5];
		for(int i=0; i<5; i++)
			if(cardsInTable[i] != null)
				gs.cardsInTable[i] = cardsInTable[i].clone();
		gs.stateOfPlay = stateOfPlay;
		gs.playersState = new PlayerState[10];
		for(int i=0; i<10; i++)
			if(playersState[i] != null)
				gs.playersState[i] = playersState[i].clone();
		gs.pWhoMustBet = pWhoMustBet.clone();
		return gs;
	}
	
	public boolean uncoverACard() {
		
		boolean uncovered = false;
		int i=0;
		while(i<5 && !uncovered) {
			if(cardsInTable[i] == null) {
				cardsInTable[i] = deck.takeCard();
				uncovered = true;
			}
			else
				i++;
		}
		if(uncovered)
			return true;
		else
			return false;
	}
		
	public static GameState createFirstGameState(List<PeerCredential> pclist, int myPosition, List<PeerCredential> exclude) {
		
		// sono il responsabile dell'inizializzazione, crea il gameState iniziale
		GameState gameState = new GameState();
		gameState.deck = new Deck();
		gameState.stateOfPlay = StateOfPlay.PREFLOP;

		gameState.cardsInTable = new Card[5];
		gameState.playersState = new PlayerState[10];
		gameState.moneyOnTable = 30;
		gameState.greatestBet = 20;
		
		int i=0;
		for(PeerCredential pc : pclist) {
			gameState.playersState[i] = new PlayerState(pc, i);
			gameState.playersState[i].setCard1(gameState.deck.takeCard());
			gameState.playersState[i].setCard2(gameState.deck.takeCard());
			if(exclude.contains(pc))
				gameState.playersState[i].setOnline(false);
			i++;
		}
		
		gameState.pWhoMustBet = new PPlaying(gameState.playersState);
		
		int dealer = myPosition;
		int littleBlind = Turn.calculateNext(gameState, dealer);
		int blind = Turn.calculateNext(gameState, littleBlind);
		int whoStart =  Turn.calculateNext(gameState, blind);
		
		
		gameState.dealer = dealer;
		
		gameState.playersState[littleBlind].setBlind(BlindEnum.LITTLE_BLIND);
		gameState.playersState[littleBlind].setMoneyBet(10);
		
		gameState.playersState[blind].setBlind(BlindEnum.BLIND);
		gameState.playersState[blind].setMoneyBet(20);
		
		//gameState.turn = -1 ; // flag
		//gameState.lastWhoRised = whoStart;
		
		
		gameState.turn = whoStart; // flag
		gameState.pWhoMustBet.makeFirst(whoStart);
		gameState.pWhoMustBet.getActive().add(0,1000);
				
		return gameState;
			
	}	
	
}


/*
 * ammettiamo questo scenario:
 * il gioco e' finito
 * un giocatore si accorge che e' il dealer e spedisce il nuovo gioco
 * ma prima che il messaggio venga ricapitato lui muore
 * cio' non desta problemi perche' i messaggi vengono accodati
 * perche' la procedura e' synchrinized
 * 
 */

/*
 * altro scenario: io posso avere abilitati i bottoni e spedire messaggio
 * ERRONEAMENTE se non era gisuto il dealer? no, perche comunque
 * quando finisce il gioco il turn va al dealer che da il nuovo stato
 * che se ricevuto e' sicuramente giusto
 * 
 */

//public Deck deck;
/*
 * e' possibile che io ho una versione piu' aggironata di carte?
 * se le carte variano in mezzo, no, tutti hanno le stesse carte perche sono due
 * se le carte variano all'inizio, dipende se e' possibile che due giocatori dicano di
 * essere dealer entrambi, ma cioe' non e' possibile perche' l'algoritmo di scelta e'
 * preciso, se un giocatore decide che se stesso e' leader allora e' sicuramente vero
 * => va bene che le carte siano sovrascritte sempre
 * 
 */

//public int turn;
/*
 * turn e' usato solo per sapere chi era il turno precedente
 * turn e' sempre uguale per tutti in gioco, se il turn e' mio allora
 * e' sicuramente mio perche' l'informazione sui crashed e' completa 
 * eventualmente in ritardo. se indico che il turno e' mio allora non
 * puo' essere di un eventuale crashed perche' sarebbe indietro nel turno.
 * se un crash avviene a gioco finito il dealer potrebbe sbagliare a dire
 * di chi e' il turno, ma appena si accorge del crash tutti si riallineano
 * 
 */
//public StateOfPlay stateOfPlay;
/*
 * e' sempre allineato
 */

//public Card[] cardsInTable;
/*
 * cards in table e' modificato da ognuno e quindi sempre allineato
 * a meno dell'inizio, quando un giocatore e' crashed a gioco finito
 * ma il discorso e' lo stesso di cards visto che e' impossibile che riceva
 * due giocatori si proclamino leader a gioco finito
 *  
 */
//public PlayerState[] playersState;
/*
 * devo fare il merge degli offline, non posso sovrascrivere perche'
 * perderei info su chi e' crashed se io ho gia' ricevuto la notifica
 * ma quello che ha spedito il nuovo stato no
 * 
 */
//public int moneyOnTable;
/*
 * il merge e' necessario visto che solo quello che spedisce il nuovo
 * stato sa quanti soldi ci sono a tavola
 */
//public int greatestBet;
/*
 * stesso discorso di moneyontable
 */
//public int dealer;
/*
 * stesso discorso di fatto che non ci sono mai ......
 */
//public PPlaying pWhoMustBet;
/*
 * devo fare il merge perche potrebbe esserci qualche giocatore
 * offline per la quale chi ha inviato non sa che sia cosi'
 */
