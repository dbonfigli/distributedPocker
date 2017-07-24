package gui;

import game.Card;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.*;

public class TablePanel extends JPanel {

	private JLabel jackpotL;
	private CardCanvas[] cardCanvas;
	
	public void setJackpotL(int jackpot) {
		jackpotL.setText(Integer.toString(jackpot) + "$");
	}
	
	public void setCardCanvas(Card[] cards) {
		for(int i=0; i<5; i++)
				cardCanvas[i].paintCard(cards[i]);
	}
	
	
	public TablePanel() {
		
		JLabel jackpotTitleL = new JLabel("jackpot:");
		Dimension d = jackpotTitleL.getPreferredSize();
		jackpotTitleL.setPreferredSize(new Dimension(90,d.height));
		
		jackpotL = new JLabel("0$");
		d = jackpotTitleL.getPreferredSize();
		jackpotL.setPreferredSize(new Dimension(90,d.height));
		
		cardCanvas = new CardCanvas[5];
		for(int i=0; i<5; i++) {
			cardCanvas[i] = new CardCanvas();
			cardCanvas[i].setPreferredSize(new Dimension(53, 70));
			cardCanvas[i].setBackground(Color.cyan);
		}
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0,5,0,5);
		add(jackpotTitleL, c);
		
		c.gridx = 0;
		c.gridy = 1;
		add(jackpotL, c);
		
		for(int i=0; i<5; i++) {
			c.gridx = i+1;
			c.gridy = 0;
			c.gridheight = 2;
			add(cardCanvas[i], c);
			
		}
		
		
	}
	
}
