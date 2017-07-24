package gui;

import game.Card;

import java.awt.Graphics;

import javax.swing.JPanel;

import com.sun.media.sound.Toolkit;

// 79 x 123

public class CardCanvas extends JPanel implements java.awt.image.ImageObserver {
		
	Card card; 
	
	public void paintCard(Card card) {
		this.card = card;
		repaint();
	}
	
    public void paintComponent(Graphics g) {
    	g.drawImage(CardPool.instance().getCard(card), 0, 0, this);
    }
        
}