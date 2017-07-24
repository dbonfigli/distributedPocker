package game;

import java.util.List;

public class BestGame {
	public List<Card> cards; // lista di _5_ carte solamente messe in ordine
	public int value; //niente 0, coppia 1, doppia coppia 2, tris 3, scala 4, colore 5, full 6, poker 7, scala di colore 8
	public int valComb1; // solo se e' coppia tris poker
	public int valComb2; // solo doppia coppia e full
	
	public BestGame(List<Card> cards, int value) {
		this.cards = cards;
		this.value = value;
		valComb1 = 0;
		valComb2 = 0;
	}
	
}
