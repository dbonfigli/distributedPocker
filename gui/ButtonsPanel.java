package gui;

import game.Controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.Format;
import java.util.Formatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ButtonsPanel extends JPanel implements ActionListener, ChangeListener {
	
	private JSlider betAmountS;
	private JLabel betAmountL;
	private JLabel totalMoneyL;
	private Controller controller;
	
	private JButton checkB;
	private JButton foldB;
	private JButton betB;
	
	public ButtonsPanel(Controller c) {
		
		controller = c;
		
		checkB = new JButton(" CHECK / CALL");
		checkB.setMnemonic(KeyEvent.VK_C);
		checkB.setActionCommand("buttonCheck");
		checkB.addActionListener(this);
		
		foldB = new JButton(" FOLD ");
		foldB.setMnemonic(KeyEvent.VK_F);
		foldB.setActionCommand("buttonFold");
		foldB.addActionListener(this);
		
		betB = new JButton("BET / RISE");
		betB.setMnemonic(KeyEvent.VK_B);
		betB.setActionCommand("buttonBet");
		betB.addActionListener(this);
		
		betAmountS = new JSlider(1, 1000);
		betAmountS.setValue(1);
		betAmountS.addChangeListener(this);
		
		betAmountL = new JLabel("1$");
		//betAmountL.setOpaque(true);
		//betAmountL.setBackground(Color.cyan);
		betAmountL.setHorizontalTextPosition(SwingConstants.CENTER);
		Dimension d = betAmountL.getPreferredSize();
		betAmountL.setPreferredSize(new Dimension(d.width+120,d.height));
		
		totalMoneyL = new JLabel("1$");
		//totalMoneyL.setOpaque(true);
		//totalMoneyL.setBackground(Color.cyan);
		totalMoneyL.setHorizontalTextPosition(SwingConstants.CENTER);
		d = totalMoneyL.getPreferredSize();
		totalMoneyL.setPreferredSize(new Dimension(d.width+120,d.height));
		setTotalMoney(1000);
		
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("make your game"),
				BorderFactory.createEmptyBorder(1,1,1,1)));
		
		setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
		
		
		add(checkB);
		add(foldB);
		add(betB);
		add(betAmountS);
		add(betAmountL);
		add(totalMoneyL);
		
		checkB.setEnabled(false);
		foldB.setEnabled(false);
		betB.setEnabled(false);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
			
		if(e.getActionCommand().equals("buttonCheck")) {
			controller.check();
		}
		else if(e.getActionCommand().equals("buttonFold")) {
			controller.fold();
		}
		else if(e.getActionCommand().equals("buttonBet")) {
			controller.bet(betAmountS.getValue());
		}
		
	}
	
	/*
	private int getMoneyBet() {
		return betAmountS.getValue();
	}*/

	@Override
	public void stateChanged(ChangeEvent e) {
		
		if(e.getSource() == betAmountS) {
			String amount = Integer.toString(betAmountS.getValue()) + "$";
			betAmountL.setText(amount);
		}		
	}
	
	public void setTotalMoney(int money) {
		totalMoneyL.setText("total: " + Integer.toString(money) + "$");
	}
	
	public void setMaxBet(int maxBet) {
		// se chiamo questo evento a catena chiama l'evento state changed per la slider, tutto ok
		betAmountS.setMaximum(maxBet);
	}
	
	public void setMinBet(int minBet) {
		betAmountS.setMinimum(minBet);
	}
	
	public void setEnabledCheckB(boolean yes) {
		checkB.setEnabled(yes);
	}
	
	public void setEnabledFoldB(boolean yes) {
		foldB.setEnabled(yes);
	}
	
	public void setEnabledBetB(boolean yes) {
		betB.setEnabled(yes);
	}

}
