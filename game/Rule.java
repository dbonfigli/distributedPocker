package game;

import game.PlayerState.PlayerChoiceEnum;

import java.util.*;
import java.util.Map.Entry;

import protocol.PeerCredential;

public class Rule {

	// controlla le regole su 7 carte
	
	public static BestGameToShow winnerInGameState(GameState gs) {
		
		HashMap<Integer, BestGame> result = new HashMap<Integer, BestGame>();
		
		for(int i=0; i<10; i++) {
			PlayerState ps = gs.playersState[i];
			if( ps != null &&
				( ps.getChoice().equals(PlayerChoiceEnum.CALL) ||
				  ps.getChoice().equals(PlayerChoiceEnum.RAISE)	)
			   ) { //giusto, chi non ha puntato (empy) magari perche offline non deve vedersi
				
				//asso vale 1
				List<Card> lc = new ArrayList<Card>();
				for(Card c : gs.cardsInTable)
					lc.add(c);
				lc.add(ps.getCard1());
				lc.add(ps.getCard2());
				BestGame bg = chooseBest5(lc);
				
				//asso vale 14
				List<Card> lc2 = new ArrayList<Card>();
				for(Card c : gs.cardsInTable)
					if(c.value == 0)
						lc2.add(new Card(c.color, 13));
					else
						lc2.add(c);
				
				if(ps.getCard1().value == 0)
					lc2.add(new Card(ps.getCard1().color, 13));
				else
					lc2.add(ps.getCard1());
				
				if(ps.getCard2().value == 0)
					lc2.add(new Card(ps.getCard2().color, 13));
				else
					lc2.add(ps.getCard2());
				
				BestGame bg2 = chooseBest5(lc2);
				
				////// la migliore mano
				if(winner(bg, bg2) == 1)
					result.put(i, bg);
				else
					result.put(i, bg2);
			}
		}
		
		///// lista ordinata di esiti, insertion sort
		
		List<BestGame> bgwl = new ArrayList<BestGame>();
		List< List<Integer> > lw = new ArrayList< List<Integer> >(); // lista ordinata di vincitori (piu' di uno per posizione)
		for(Entry<Integer, BestGame> e : result.entrySet()) {
			
			// se era vuoto winnerH allora setto il vincitore e
			if(lw.size() == 0) {
				lw.add(new ArrayList<Integer>());
				lw.get(0).add(e.getKey());
				bgwl = new ArrayList<BestGame>();
				bgwl.add(e.getValue());
			}
			else {
				
				int i = 0;
				boolean added = false;
				for(BestGame bg : bgwl) {
					
					if( winner(bg, e.getValue()) == 2 ) { // se e e' piu' grande come vincita
						bgwl.add(i, e.getValue());
						lw.add(i, new ArrayList<Integer>());
						lw.get(i).add(e.getKey());
						added = true;
						break;
					} else if( winner(bg, e.getValue()) == 1) { // e non e' il vincitore, contiuno
						i++;
					} else { // sono pari
						lw.get(i).add(e.getKey());
						added = true;
						break;
					}					
				}
				if(!added) {
					List<Integer> l = new ArrayList<Integer>();
					l.add(e.getKey());
					lw.add(l);
					bgwl.add(e.getValue());
				}
			}
		}
				

		BestGameToShow bgts = new BestGameToShow();
		bgts.results = result;
		bgts.orderedWins = lw;
		
		return bgts;

		///////////////////////////////////
		
		/*
		//// soltanto lista di vincitori
		
		///// adesso ho preparato gli esiti, calcolo il vincitore
		BestGame bgw = null;
		List<Integer> lw = new ArrayList<Integer>();
		for(Entry<Integer, BestGame> e : result.entrySet()) {
			
			// se era vuoto winnerH allora setto il vincitore e
			if(lw.size() == 0) {
				lw.add(e.getKey());
				bgw = e.getValue();
			}
			else if( winner(bgw, e.getValue()) == 2 ) { //vince il secondo
				lw = new ArrayList<Integer>();
				lw.add(e.getKey());
				bgw = e.getValue();
			} 
			else if( winner(bgw, e.getValue()) ==  0) 
				lw.add(e.getKey());
					
			}
		
		BestGameToShow bgts = new BestGameToShow();
		bgts.results = result;
		bgts.winners = lw;
		
		return bgts;
		*/
	}
	
	
	// 1 se vince il primo, 2 se vince il secondo, 0 se pari
	public static int winner(BestGame bg1, BestGame bg2) {
		
		if(bg1.value < bg2.value)
			return 2;
		else if(bg1.value > bg2.value)
			return 1;
		
		else { //if(bg1.value == bg2.value)
			
			if(bg1.valComb1 < bg2.valComb1)
				return 2;
			else if(bg1.valComb1 > bg2.valComb1)
				return 1;
			else { // bg1.valComb1 == bg2.valComb1
				
				if(bg1.valComb2 < bg2.valComb2)
					return 2;
				else if(bg1.valComb2 > bg2.valComb2)
					return 1;
				else // bg1.valComb2 == bg2.valComb2
					return highCardRule(bg1.cards, bg2.cards);
			}
		}
	}
	
	private static int highCardRule(List<Card> l1, List<Card> l2) {
		
		//devono essere 5 carte
		for(int i=0; i<l1.size(); i++) {
			if(l1.get(i).value > l2.get(i).value )
				return 1;
			else if(l1.get(i).value > l2.get(i).value )
				return 2;
		}
		
		return 0;
		
	}
	
	// ricorda, il colore non ha valore!!!!
	public static BestGame chooseBest5(final List<Card> cards) {
		
		if(cards.size() != 7)
			return null;
		
		BestGame bg;
		
		bg = straightFlushBestGame(cards);
		
		if(bg == null)
			bg = fourOfAKindBestGame(cards);
		
		if(bg == null)
			bg = fullHouseBestGame(cards);
		
		if(bg == null)
			bg = flushBestGame(cards);
		
		if(bg == null)
			bg = straightBestGame(cards);
		
		if(bg == null)
			bg = threeOfAKindBestGame(cards);
		
		if(bg == null)
			bg = pairOrDoublePairBestGame(cards);
		
		if(bg == null) {		
			// carte piu alte
			List<Card> resp = sort(cards);
			resp.remove(0);
			resp.remove(0);
			bg = new BestGame(resp, 0);
		}
		
		return bg;
	}
	
	private static BestGame straightFlushBestGame(final List<Card> card) {
		
		List<Card>resp = straightFlush(card);
		if(resp != null) {
			BestGame bg = new BestGame(resp, 8);
			bg.valComb1 = resp.get(resp.size()-1).value;
			return bg;
		}
		else
			return null;
	}

	private static BestGame fourOfAKindBestGame(final List<Card> card) {
		
		List<Card> resp = fourOfAKind(card);
		if(resp != null) {
			card.removeAll(resp);
			resp.add(highestCard(card));
			BestGame bg = new BestGame(resp, 7);
			bg.valComb1 = resp.get(0).value;
			bg.cards = sort(bg.cards);
			return bg;
		}
		else
			return null;
	}

	private static BestGame fullHouseBestGame(final List<Card> card) {
		
		List<List<Card>> respl = threeOfAKind(card);
		if(respl.size() !=0) {
			List<Card> bestTris = respl.get(0);
			List<Card> bestPair = null;
			
			for(int i=0; i<respl.size(); i++) {
				
				//prendi il tris a cui sono interessato
				List<Card> tris = respl.get(i);
				
				//copia la lista delle 7 carte e togli il tris
				List<Card> c = new ArrayList<Card>();
				for(Card x : card)
					c.add(x);
				c.removeAll(tris);
				
				//adesso vedi se ci sono delle coppie
				List<List<Card>> pairs = pair(c);
				if(pairs.size() != 0) {
					if(
						(bestPair == null) ||
						(	
							bestTris.get(0).value == tris.get(0).value &&
							bestPair.get(0).value < pairs.get(pairs.size()-1).get(0).value
						) ||
						(bestTris.get(0).value < tris.get(0).value)
							
					// se bestPair era null
					// se bestTris = tris && bestPair < pair
					// se bestTris < tris
					
					) {
						bestPair = pairs.get(pairs.size()-1);
						bestTris = tris;
					}
				}				
			}
			
			if(bestPair != null) {
				List<Card> ret = new ArrayList<Card>();
				for(Card x : bestTris)
					ret.add(x);
				ret.addAll(bestPair);
				BestGame bg = new BestGame(ret, 6);
				bg.valComb1 = bestTris.get(0).value;
				bg.valComb2 = bestPair.get(0).value;
				bg.cards = sort(bg.cards);
				return bg;
			}
			else
				return null;
			
		}
		else
			return null;
		
	}
	
	private static BestGame flushBestGame(final List<Card> card) {
		
		List<Card>resp = flush(card);
		if(resp != null) {
			BestGame bg = new BestGame(resp, 5);
			bg.valComb1 = resp.get(0).value;
			return bg;
		}
		else
			return null;
	}
	
	private static BestGame straightBestGame(final List<Card> card) {
		
		List<Card>resp = straight(card);
		if(resp != null) {
			BestGame bg = new BestGame(resp, 4);
			bg.valComb1 = resp.get(resp.size()-1).value;
			return bg;
		}
		else
			return null;
	}
	
	private static BestGame threeOfAKindBestGame(final List<Card> card) {
		
		List<List<Card>> respList = threeOfAKind(card);
		if(respList.size() !=0 ) {
			
			// prendi il tris migliore, cioe' l'ultimo
			List<Card> resp = respList.get(respList.size()-1);
			BestGame bg = new BestGame(resp, 3);
			bg.valComb1 = resp.get(0).value;
			
			//rimovi dalle carte il tris
			card.removeAll(resp);
			
			//aggiungi al tris le due carte migliori rimanenti
			Card highest = highestCard(card);
			resp.add(highest);
			card.remove(highest);
			highest = highestCard(card);
			resp.add(highest);
			
			bg.cards = sort(bg.cards);
			return bg;
		}
		else
			return null;
	}

	// precondizione: non ci devono essere tris o altro piu grande
	// qui sono sicuro che non ho tris perche altrimenti me ne accorgevo prima
	// quindi le coppie sono disgiunte, vado tranquillo
	private static BestGame pairOrDoublePairBestGame(final List<Card> cards) {
	
		List<Card> card = new ArrayList<Card>();
		for(Card c : cards)
			card.add(c);
		
		List<List<Card>> respList = pair(card);
		if(respList.size() !=0 ) {
			if(respList.size() > 1 ) { // se ho almeno due coppie ...
				
				//prendo le ultime due
				List<Card> resp1 = respList.get(respList.size()-1);
				List<Card> resp2 = respList.get(respList.size()-2);
				
				//li unisco
				List<Card> resp = new ArrayList<Card>();
				resp.addAll(resp1);
				resp.addAll(resp2);
				
				//rimuovo da card le due coppie
				card.removeAll(resp);
				
				//aggiungo la carta piu alta come rimanente
				Card highest = highestCard(card);
				resp.add(highest);
				BestGame bg = new BestGame(resp, 2);
				bg.valComb1 = resp1.get(0).value;
				bg.valComb2 = resp2.get(0).value;
				bg.cards = sort(bg.cards);
				return bg;	
			}
			else { // solo una coppia ...
				List<Card> resp = respList.get(0);
				
				//rimuovo la coppia, ordino e tolgo i due piu bassi per far posto alla coppia, poi la aggiungo
				card.removeAll(resp);
								
				card = sort(card);
								
				card.remove(0);
				card.remove(0);
				
				resp.addAll(card);
				
				BestGame bg = new BestGame(resp, 1);
				bg.valComb1 = respList.get(0).get(0).value;
				bg.cards = sort(bg.cards);
				return bg;
			}			
		}
		else
			return null;
	
	}
	
	private static Card highestCard(final List<Card> cards) {
		
		if(cards.size() == 0)
			return null;
		
		Card best = cards.get(0);
		for(int i=0;i<cards.size();i++)
			if(cards.get(i).compare(best) == 1)
				best = cards.get(i);
		
		return best;
	}
	
	// torna tutte le coppie possibili indipendentemente dalla lunghezza
	// lunghezza minima: non importante
	private static List<List<Card>> pair(final List<Card> card) {
		
		List<Card> sortedCard = sort(card);
		
		List<List<Card>> ret = new ArrayList<List<Card>>();
		
		for(int i=0; i<sortedCard.size()-1; i++)
			for(int j=i+1; j<sortedCard.size(); j++)
				if(sortedCard.get(i).value == sortedCard.get(j).value) {
					List<Card> lc = new ArrayList<Card>();
					lc.add(sortedCard.get(i));
					lc.add(sortedCard.get(j));
					ret.add(lc);
				}
		
		return ret;
		
	}
	
	// tris 
	// torna tutti i tris possibili indipendentemente dalla lunghezza
	// lunghezza minima: non importante
	private static List<List<Card>> threeOfAKind(final List<Card> card) {

		List<Card> sortedCard = sort(card);
		
		List<List<Card>> ret = new ArrayList<List<Card>>();
		
		for(int i=0; i<sortedCard.size()-2; i++)
			for(int j=i+1; j<sortedCard.size()-1; j++)
				for(int k=j+1; k<sortedCard.size(); k++)
					if(sortedCard.get(i).value == sortedCard.get(j).value &&
						sortedCard.get(i).value	== sortedCard.get(k).value) {
						List<Card> lc = new ArrayList<Card>();
						lc.add(sortedCard.get(i));
						lc.add(sortedCard.get(j));
						lc.add(sortedCard.get(k));
						ret.add(lc);
					}
		
		return ret;
			
	}
	
	// colore su 7 carte
	private static List<Card> flush(final List<Card> card) {
		
		List<Card> sortedCard = sort(card);
		
		List<Card> ret = null;
		
		for(int base=0; base<3; base++) {
			
			int i = base;
			int next = i+1;
			boolean error = false;
			
			List<Card> cl = new ArrayList<Card>();
			cl.add(sortedCard.get(base));
						
			while(cl.size()<5 && !error) {
				
				if(next >= sortedCard.size())
					error=true;
				else {					
					if( sortedCard.get(i).color == sortedCard.get(next).color ) {
						cl.add(sortedCard.get(next));
						i=next;
					}
					next++;
				}
				
			}
			if(!error && cl.size() == 5)
				ret = cl;	
		}
		
		return ret;
		
	}
	
	//poker su 7 carte
	private static List<Card> fourOfAKind(final List<Card> card) {
		
		List<Card> sortedCard = sort(card);
		
		List<Card> ret = null;
		
		for(int base=0; base<4; base++) {
			
			int i = base;
			int next = i+1;
			boolean error = false;
			
			List<Card> cl = new ArrayList<Card>();
			cl.add(sortedCard.get(base));
						
			while(cl.size()<4 && !error) {
				
				if(next >= sortedCard.size())
					error=true;
				else {					
					if( sortedCard.get(i).value == sortedCard.get(next).value ) {
						cl.add(sortedCard.get(next));
						i=next;
					}
					next++;
				}
				
			}
			if(!error && cl.size() == 4)
				ret = cl;	
		}
		
		return ret;
		
	}
	
	//scala, torna solo la piu alta, su 7 carte
	private static List<Card> straight(final List<Card> card) {
		
		List<Card> sortedCard = sort(card);
		
		List<Card> ret = null;
				
		for(int base=0; base<3; base++) {
			
			int i = base;
			int next = i+1;
			boolean error = false;
			
			List<Card> cl = new ArrayList<Card>();
			cl.add(sortedCard.get(base));
						
			while(cl.size()<5 && !error) {
				
				if(next >= sortedCard.size())
					error=true;
				else {					
					if( sortedCard.get(i).value == (sortedCard.get(next).value-1) ) {
						cl.add(sortedCard.get(next));
						i=next;
					}
					next++;
				}
				
			}
			if(!error && cl.size() == 5)
				ret = cl;	
		}
		
		return ret;
		
	}
	
	//scala a colore, torna la piu' alta, su 7 carte
	private static List<Card> straightFlush(final List<Card> card) {
		
		List<Card> sortedCard = sort(card);
		
		List<Card> ret = null;
		
		for(int base=0; base<3; base++) {
			
			int i = base;
			int next = i+1;
			boolean error = false;
			
			List<Card> cl = new ArrayList<Card>();
			cl.add(sortedCard.get(base));
						
			while(cl.size()<5 && !error) {
				
				if(next >= sortedCard.size())
					error=true;
				else {					
					if( (sortedCard.get(i).color == sortedCard.get(next).color) &&
						(sortedCard.get(i).value == (sortedCard.get(next).value-1)) ) {
						cl.add(sortedCard.get(next));
						i=next;
					}
					next++;
				}
				
			}
			if(!error && cl.size() == 5)
				ret = cl;	
		}
		
		return ret;
				
	}
	
	private static List<Card> sort(final List<Card> cards) {
				
		List<Card> ret = new ArrayList<Card>();
		for(Card c : cards)
			ret.add(c);
				
		//bubble sort
		for(int i=0; i<ret.size(); i++)
			for(int j=0; j<ret.size()-1; j++)
				if( ret.get(j).compare(ret.get(j+1)) == 1 ) {
					Card app = ret.get(j);
					ret.set(j, ret.get(j+1));
					ret.set(j+1, app);
				}
		
		return ret;
		
	}
	
	/*
	private static void provesingole() {
		
		List<Card> c = new ArrayList<Card>();
		c.add(null);
		c.add(null);
		c.add(null);
		c.add(null);
		c.add(null);
		c.add(null);
		c.add(null);
		
		///////
		c.set(0, new Card(1,7));
		c.set(1, new Card(0,6));
		c.set(2, new Card(0,3));
		c.set(3, new Card(0,5));
		c.set(4, new Card(0,2));
		c.set(5, new Card(0,4));
		c.set(6, new Card(1,1));
				
		List<Card> cl = straightFlush(c);
		for(Card card : cl) {
			System.out.println(card.color + " " + card.value);
		}
		
		c.set(0, new Card(0,7));
		c.set(1, new Card(0,6));
		c.set(2, new Card(0,3));
		c.set(3, new Card(0,5));
		c.set(4, new Card(0,2));
		c.set(5, new Card(0,4));
		c.set(6, new Card(1,1));
		
		
		System.out.println("BWGIN");
		List<Card> xcl = straightFlush(c);
		for(Card card : xcl) {
			System.out.println(card.color + " " + card.value);
		}
		
		///////
		
		System.out.println("//////");
		
		c.set(0, new Card(1,1));
		c.set(1, new Card(2,1));
		c.set(2, new Card(0,3));
		c.set(3, new Card(0,5));
		c.set(4, new Card(3,1));
		c.set(5, new Card(0,4));
		c.set(6, new Card(4,1));
		
		
		List<Card> cl2 = fourOfAKind(c);
		for(Card card : cl2) {
			System.out.println(card.color + " " + card.value);
		}
		
		///////
		
		System.out.println("//////");
		
		c.set(0, new Card(4,1));
		c.set(1, new Card(2,1));
		c.set(2, new Card(4,3));
		c.set(3, new Card(0,5));
		c.set(4, new Card(4,1));
		c.set(5, new Card(4,4));
		c.set(6, new Card(4,1));
		
		
		List<Card> cl3 = flush(c);
		for(Card card : cl3) {
			System.out.println(card.color + " " + card.value);
		}
		
		///////
		/*
		System.out.println("//////");
		
		c[0] = new Card(4,1);
		c[1] = new Card(2,1);
		c[2] = new Card(4,3);
		c[3] = new Card(0,5);
		c[4] = new Card(4,1);
		c[5] = new Card(4,4);
		c[6] = new Card(4,1);
		
		
		List<List<Card>> cll4 = threeOfAKid(c);
		for(Card card : cll4.get(0)) {
			System.out.println(card.color + " " + card.value);
		}
		System.out.println("AA");
		for(Card card : cll4.get(1)) {
			System.out.println(card.color + " " + card.value);
		}
		*/
		///////
		
		/*
		System.out.println("//////");
		
		c[0] = new Card(4,1);
		c[1] = new Card(2,1);
		c[4] = new Card(1,1);
		c[6] = new Card(3,1);
	
		c[2] = new Card(4,3);
		c[3] = new Card(0,5);
		c[5] = new Card(4,4);
		
		List<List<Card>> cll5 = pair(c);
		for(Card card : cll5.get(0)) {
			System.out.println(card.color + " " + card.value);
		}
		System.out.println("AA");
		for(Card card : cll5.get(1)) {
			System.out.println(card.color + " " + card.value);
		}
		System.out.println("AA");
		for(Card card : cll5.get(2)) {
			System.out.println(card.color + " " + card.value);
		}
		System.out.println("AA");
		for(Card card : cll5.get(3)) {
			System.out.println(card.color + " " + card.value);
		}
		System.out.println("AA");
		for(Card card : cll5.get(4)) {
			System.out.println(card.color + " " + card.value);
		}
		System.out.println("AA");
		for(Card card : cll5.get(5)) {
			System.out.println(card.color + " " + card.value);
		}
		*/
		
		///////
		
		/*
		System.out.println("//////");
		
		c.set(0, new Card(4,1));
		c.set(1, new Card(2,1));
		c.set(4, new Card(1,1));
		c.set(6, new Card(3,1));
		
		c.set(2, new Card(4,3));
		c.set(3, new Card(0,5));
		c.set(5, new Card(4,4));
		
			
		List<List<Card>> cll5 = threeOfAKind(c);
		for(Card card : cll5.get(0)) {
			System.out.println(card.color + " " + card.value);
		}
		System.out.println("AA");
		for(Card card : cll5.get(1)) {
			System.out.println(card.color + " " + card.value);
		}
		System.out.println("AA");
		for(Card card : cll5.get(2)) {
			System.out.println(card.color + " " + card.value);
		}
		System.out.println("AA");
		for(Card card : cll5.get(3)) {
			System.out.println(card.color + " " + card.value);
		}
	}
	
	*/
	/*
	
	public static void main(String[] args) {
		
		GameState gs = new GameState();
		gs.cardsInTable = new Card[5];
		gs.cardsInTable[0] = new Card(1, 1);
		gs.cardsInTable[1] = new Card(1, 12);
		gs.cardsInTable[2] = new Card(2, 2);
		gs.cardsInTable[3] = new Card(2, 9);
		gs.cardsInTable[4] = new Card(3, 12);
		
		gs.playersState = new PlayerState[10];
		
		gs.playersState[0] = new PlayerState(new PeerCredential(null, "uno"), 0);
		gs.playersState[0].setChoice(PlayerChoiceEnum.CALL);
		gs.playersState[0].setCard1(new Card(3 , 3));
		gs.playersState[0].setCard2(new Card(2 , 4));
		gs.playersState[1] = new PlayerState(new PeerCredential(null, "due"), 0);
		gs.playersState[1].setChoice(PlayerChoiceEnum.CALL);
		gs.playersState[1].setCard1(new Card(3 , 8));
		gs.playersState[1].setCard2(new Card(1 , 10));
		gs.playersState[2] = new PlayerState(new PeerCredential(null, "tre"), 0);
		gs.playersState[2].setChoice(PlayerChoiceEnum.CALL);
		gs.playersState[2].setCard1(new Card(1 , 11));
		gs.playersState[2].setCard2(new Card(2 , 1));
		gs.playersState[3] = new PlayerState(new PeerCredential(null, "quattro"), 0);
		gs.playersState[3].setChoice(PlayerChoiceEnum.CALL);
		gs.playersState[3].setCard1(new Card(0 , 10));
		gs.playersState[3].setCard2(new Card(2 , 8));
		
		BestGameToShow x = winnerInGameState(gs);
		System.out.println(x.printResult(gs));
		
		List<Card> cl = new ArrayList<Card>();
		for(int i=0;i<5;i++)cl.add(gs.cardsInTable[i]);
		cl.add(new Card(0 , 10));
		cl.add(new Card(2 , 8));
		BestGame bg2 = chooseBest5(cl);
		System.out.println(bg2.value);
		for(Card c : bg2.cards)
			System.out.print(" (" +c.color + " " + c.value +") ");
		
	}
	
	/*
	private static void provedoppie() {
		
		List<Card> lc = new ArrayList<Card>();
		lc.add(new Card(2,8));
		lc.add(new Card(3,12));
		lc.add(new Card(2,12));
		lc.add(new Card(4,12));
		lc.add(new Card(4,11));
		lc.add(new Card(2,5));
		lc.add(new Card(2,4));
		
		BestGame bg = chooseBest5(lc);
		System.out.println(bg.value);
		for(Card c : bg.cards)
			System.out.println(c.color + " - " + c.value);
		
	}*/
	
}
