package game;

import java.io.Serializable;

public class Card implements Serializable, Cloneable {

	public int color;
	public int value;
	
	public Card(int color, int value) {
		this.color = color;
		this.value = value;
	}
	
	public Card clone() {
		return new Card(color, value);
	}
	
	public int compare(Card c) {
		
		if(color == c.color && value == c.value)
			return 0;
		
		if(color == c.color && value < c.value)
			return -1;

		if(color == c.color && value > c.value)
			return 1;
		
		if(color < c.color && value == c.value)
			return -1;
		
		if(color > c.color && value == c.value)
			return 1;
		
		if(color < c.color && value == c.value)
			return -1;
			
		if(value < c.value)
			return -1;
		else
			return 1;
	}
}
