package gui;

import game.Card;
import game.PlayerState;
import game.PlayerState.BlindEnum;
import game.PlayerState.PlayerChoiceEnum;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import protocol.PeerCredential;

public class PlayerPanel extends JPanel {

	private TitledBorder titledBorder;
	private JLabel moneyL;
	private JLabel choiceL;
	private JLabel betL;
	private CardCanvas card1;
	private CardCanvas card2;
	
	
	public void updateState(PlayerState ps, boolean turn, boolean dealer) {
		moneyL.setText(Integer.toString(ps.getTotalMoney() - ps.getMoneyBet()) + "$");
		setTitle(ps, turn, dealer);
		setChoose(ps);
	}
	
	public PlayerPanel() {
		
		PlayerState ps = new PlayerState(new PeerCredential("", "empty"), 0);
		ps.setOnline(false);
		ps.setMoneyBet(0);
		
		BorderFactory.createCompoundBorder();
		titledBorder = new TitledBorder("player");
		CompoundBorder cb = new CompoundBorder();
		cb = BorderFactory.createCompoundBorder( titledBorder, BorderFactory.createEmptyBorder(1,1,1,1) );
		setBorder(cb);
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
				
		moneyL = new JLabel("0$");
		Dimension d = moneyL.getPreferredSize();
		moneyL.setPreferredSize(new Dimension(90,d.height));
		
		choiceL = new JLabel("free sit");
		d = choiceL.getPreferredSize();
		choiceL.setPreferredSize(new Dimension(90,d.height));
		
		betL = new JLabel("  ");
		d = betL.getPreferredSize();
		betL.setPreferredSize(new Dimension(90,d.height));
		
		card1 = new CardCanvas();
		card1.setPreferredSize(new Dimension(53,70));
		
		card2 = new CardCanvas();
		card2.setPreferredSize(new Dimension(53,70));
		
		updateState(ps, false, false);
		
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.weighty = 0.5;
		add(moneyL, c);
		
		c.gridx = 0;
		c.gridy = 1;
		add(choiceL, c);
		
		c.gridx = 0;
		c.gridy = 2;
		add(betL, c);
		
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 3;
		c.insets = new Insets(0,5,5,0);
		add(card1, c);
		
		c.gridx = 2;
		c.gridy = 0;
		c.gridheight = 3;
		add(card2, c);
		
	}
	
	public void setCard1(Card c) {
		card1.paintCard(c);
	}
	public void setCard2(Card c) {
		card2.paintCard(c);
	}
	
	
	private void setTitle(PlayerState pc, boolean turn, boolean dealer) {
		
		String title = pc.getPeerCredential().getPeerName();
		
		if(turn) 
			title = title + " (TURN)";

		if(!pc.isOnline())
			title = title + " (OFFLINE)";
		
		if(dealer)
			title = title + " (D) ";
		
		if(pc.getBlind().equals(BlindEnum.BLIND))
			title = title + " (B)";
		else if(pc.getBlind().equals(BlindEnum.LITTLE_BLIND))
			title = title + " (LB)";
		
		titledBorder.setTitle(title);
		
		if(pc.isOnline()) {
			if(turn)
				titledBorder.setTitleColor(Color.red);
			else
				titledBorder.setTitleColor(Color.black);
		}
		else {
			if(turn)
				titledBorder.setTitleColor(new Color(170,50,50));
			else
				titledBorder.setTitleColor(Color.gray);
		}
		
		
		repaint();
	}
	
	private void setChoose(PlayerState p) {
		
		if(p.getChoice().equals(PlayerChoiceEnum.CALL)) {
			choiceL.setText("call/check");
		}
		else if(p.getChoice().equals(PlayerChoiceEnum.RAISE)) {
			choiceL.setText("bet/raise");
		}
		else if(p.getChoice().equals(PlayerChoiceEnum.FOLD)) {
			choiceL.setText("fold");
		}
		else if(p.getChoice().equals(PlayerChoiceEnum.EMPTY)) {
			choiceL.setText("-");
		}
		
		String moneyBet = Integer.toString(p.getMoneyBet()) + "$";
		if(p.getTotalMoney() == 0 && !p.getChoice().equals(PlayerChoiceEnum.EMPTY))
			moneyBet += " ALL-IN";
		betL.setText(moneyBet);
		
		
	}
 
}
