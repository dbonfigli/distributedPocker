package gui;

import game.Controller;
import game.GameState;
import game.PlayerState;

import java.awt.*;
import javax.swing.*;

import protocol.PeerCredential;

public class MainWindow extends JFrame {

	private MessageArea messageArea;
	private PlayerPanel[] playerPanel;
	private TablePanel tablePanel;
	private ChatArea chatArea;
	ButtonsPanel buttonsPanel;
	
	public MainWindow(Controller controller) {
	
		setTitle("DistPoker");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(890, 550);
		setLocation(50,50);
		initComponents(controller);		
		setVisible(true);	
	}
	
	public MessageArea getMessageArea() {
		return messageArea;
	}
	
	public PlayerPanel[] getPlayerPanel() {
		return playerPanel;
	}

	public TablePanel getTablePanel() {
		return tablePanel;
	}

	public ButtonsPanel getButtonsPanel() {
		return buttonsPanel;
	}

	private void initComponents(Controller controller) {
		
		// area di gioco ///////////////////////////////////////////
		tablePanel = new TablePanel();
		////////////////////////////////////////////////////////////
		
		// menu ////////////////////////////////////////////////////
		setJMenuBar(new Menu(controller));
		////////////////////////////////////////////////////////////
		
		// messageArea e chat //////////////////////////////////////
		messageArea = new MessageArea();
		JScrollPane scrollPane = new JScrollPane(messageArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		chatArea = new ChatArea(controller);
		////////////////////////////////////////////////////////////
		
		// area bottoni ////////////////////////////////////////////
		buttonsPanel = new ButtonsPanel(controller);
		////////////////////////////////////////////////////////////
		
		// area gioco //////////////////////////////////////////////
		playerPanel = new PlayerPanel[10];
		JPanel gameArea = new JPanel();
		gameArea.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
 
		for(int i=0; i<4; i++) {
			c1.gridx = i;
			c1.gridy = 0;
			playerPanel[i] = new PlayerPanel();
			gameArea.add(playerPanel[i], c1);
		}
		
		c1.gridx = 3;
		c1.gridy = 1;
		playerPanel[4] = new PlayerPanel();
		gameArea.add(playerPanel[4], c1);
		
		int pos = 5;
		for(int i=3; i>=0; i--) {
			c1.gridx = i;
			c1.gridy = 2;
			playerPanel[pos] = new PlayerPanel();
			gameArea.add(playerPanel[pos], c1);
			pos++;
		}
		
		c1.gridx = 0;
		c1.gridy = 1;
		playerPanel[9] = new PlayerPanel();
		gameArea.add(playerPanel[9], c1);

		c1.gridx = 1; 
		c1.gridy = 1;
		c1.gridwidth = 2;
		gameArea.add(tablePanel, c1);
			
		////////////////////////////////////////////////////////////
				
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		c.insets = new Insets(5,0,5,0);
		//c.ipady????
		add(gameArea, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		add(buttonsPanel, c);
		
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		add(chatArea, c);
		
		
		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		add(scrollPane, c);
		
		setVisible(true);
		gameArea.setPreferredSize(gameArea.getSize());
		gameArea.setMaximumSize(gameArea.getSize());
		gameArea.setMinimumSize(gameArea.getSize());
		//pack();
		
		
	}
	
	public void updateGui(GameState gs, int myPosition, boolean finished) {
		
		if(gs != null) {
		
			// setto tutte le carte da visualizzare nel tavolo, il jackpot e i miei soldi			
			tablePanel.setCardCanvas(gs.cardsInTable);		
			tablePanel.setJackpotL(gs.moneyOnTable);
			if(gs.playersState[myPosition] != null)
				buttonsPanel.setTotalMoney(gs.playersState[myPosition].getTotalMoney());
						
			//setto tutte le players area
			for(int i=0; i<10; i++) {
		
				PlayerState ps = gs.playersState[i]; 
				
				if(ps != null) {
					playerPanel[i].updateState(ps, gs.turn == i, gs.dealer == i);
					if(finished || myPosition == i) {
						playerPanel[i].setCard1(ps.getCard1());
						playerPanel[i].setCard2(ps.getCard2());
					}
					else { // altrimenti disegna la cover
						playerPanel[i].setCard1(null); 
						playerPanel[i].setCard2(null);
					}
				}
				else { // se il giocatore non esiste disegnane uno fake empty
					playerPanel[i].setCard1(null); 
					playerPanel[i].setCard2(null);
					PlayerState pl = new PlayerState(new PeerCredential("", "empty"), 0);
					pl.setOnline(false);
					playerPanel[i].updateState(pl, false, false);
				}
				
			} //fine for

			repaint();
		} // fine if
		
	}



	
}
