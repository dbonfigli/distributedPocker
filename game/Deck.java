package game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Deck implements Serializable {

	private List<Card> cardList;
	
	public Deck() {
		
		cardList = new ArrayList<Card>();
		for(int i=0; i<4; i++)
			for(int j=0; j<13; j++)
				cardList.add(new Card(i,j));
		
		shuffle();
		
	}
	
	private Deck(boolean dontBuild) { }
	
	public Deck clone() {
		Deck d = new Deck(true);
		d.cardList = new ArrayList<Card>();
		for(Card c : cardList)
			d.cardList.add(c.clone());
		return d;
	}
	
	private void shuffle() {
		
		Random r = new Random();
		int totRandom = r.nextInt(100) + 5;
		for(int i=0; i<totRandom; i++) {
			for(int j=0; j<52; j++) {
				int dest = r.nextInt(52);
				Card a = cardList.get(dest);
				Card b = cardList.get(j);
				cardList.set(dest, b);
				cardList.set(j, a);
			}
		}
		
	}
	
	public Card takeCard() {
		try {
			return cardList.remove(0);
		} catch (IndexOutOfBoundsException e) {
			System.err.println("deck out of cards!");
			return null;
		}
	}
	
}
