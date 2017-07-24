package gui;

import game.Card;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.*;

public class CardPool {

	static private CardPool instance;
	private Image[][] cardImage;
	private Image coverImage;
	
	public Image getCard(Card card) {
		if(card == null)
			return coverImage;
		else
			return cardImage[card.color][card.value];
	}
	
	public Image getCover() {
		return coverImage;
	}
	
	private CardPool() {
		
		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File("media/cards.png"));
		} catch (IOException e) {
			System.out.println("errore nel caricare l'immagine cards.png");
			e.printStackTrace();
		}
		
		cardImage = new Image[4][13];
		
		for(int i=0; i<13; i++)
			for(int j=0; j<4; j++) {
				BufferedImage bf = img.getSubimage(i*79, j*123, 79, 123);
				cardImage[j][i] = bf.getScaledInstance(53, 70, Image.SCALE_SMOOTH);
			}
		
		//adesso devo scambiare la riga 0 con la 3, la 2 con la 1, la 1 con la 3
		for(int i=0; i<13; i++) {
			Image app = cardImage[0][i];
			cardImage[0][i] = cardImage[3][i];
			cardImage[3][i] = app;
		}
		for(int i=0; i<13; i++) {
			Image app = cardImage[2][i];
			cardImage[2][i] = cardImage[1][i];
			cardImage[1][i] = app;
		}
		for(int i=0; i<13; i++) {
			Image app = cardImage[1][i];
			cardImage[1][i] = cardImage[3][i];
			cardImage[3][i] = app;
		}	
		
		BufferedImage bf = img.getSubimage(2*79, 4*123, 79, 123);
		coverImage = bf.getScaledInstance(53, 70, Image.SCALE_SMOOTH);
		
	}
	
	public static CardPool instance() {
		
		if(instance == null)
			instance = new CardPool();
	
		return instance;
	}
	
}
