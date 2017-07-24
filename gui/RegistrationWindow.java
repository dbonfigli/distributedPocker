package gui;

import game.Controller;
import game.Player;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import protocol.PeerCredential;

import server.CannotCreateGameException;
import server.CannotRegisterException;
import server.CannotUnregisterException;
import server.GameDescription;
import server.GameDescription.gameStateEnum;

public class RegistrationWindow extends JFrame implements ActionListener {

	private JList gameList;
	private DefaultListModel listModel;
	
	private JButton joinB;
	private JButton unjoinB;
	private JButton createGameB;
	private JButton updateB;
	private JTextField nameNewGameTF;
	private JSpinner nPlayerS;
	
	private Controller controller;
	
	public RegistrationWindow(Controller controller) {
		
		this.controller = controller;
		this.setTitle("games");
		this.setSize(800, 500);
		this.setLocation(50, 50);
				
		/////////////////////////////////////////////////////////////
		
		createGameB = new JButton("create");
		createGameB.setMnemonic(KeyEvent.VK_C);
		createGameB.setActionCommand("buttonCreateNew");
		createGameB.addActionListener(this);
		
		nameNewGameTF = new JTextField("game name");
		nPlayerS = new JSpinner(new SpinnerNumberModel(10, 2, 10, 1));
		
		
		JPanel panelNew = new JPanel();
		panelNew.setLayout(new GridBagLayout());
		panelNew.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("create new game"),
				BorderFactory.createEmptyBorder(3,3,3,3)));
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		c.insets = new Insets(3,3,3,3);
		panelNew.add(nameNewGameTF, c);
		
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.weighty = 0;
		panelNew.add(new JLabel("no. of players: "), c);
		
		c.gridx = 2;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.weighty = 0;
		panelNew.add(nPlayerS, c);
		
		c.gridx = 3;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.weighty = 0;
		panelNew.add(createGameB, c);
		
		///////////////////////////////////////////////////
		
		joinB = new JButton("join");
		joinB.setMnemonic(KeyEvent.VK_J);
		joinB.setActionCommand("buttonJoin");
		joinB.addActionListener(this);
		
		unjoinB = new JButton("unjoin");
		unjoinB.setMnemonic(KeyEvent.VK_N);
		unjoinB.setActionCommand("buttonUnjoin");
		unjoinB.addActionListener(this);
		
		updateB = new JButton("update");
		updateB.setMnemonic(KeyEvent.VK_U);
		updateB.setActionCommand("buttonUpdate");
		updateB.addActionListener(this);
		
		listModel = new DefaultListModel();
		gameList = new JList(listModel);
		gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		
		/*
		gameList.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("join a game"),
				BorderFactory.createEmptyBorder(30,30,30,30)));
		*/
		
		setLayout(new GridBagLayout());
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
	//	c.ipadx = 3;
		//c.ipady = 3;
		JScrollPane jsc = new JScrollPane(gameList);
		jsc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(jsc, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		add(panelNew, c);
		
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		add(joinB, c);
		
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		add(unjoinB, c);
		
		c.gridx = 2;
		c.gridy = 2;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		add(updateB, c);

		//unjoinB.setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals("buttonCreateNew"))
			buttonCreateNewPressed();
		else if(e.getActionCommand().equals("buttonJoin"))
			buttonJoinPressed();
		else if(e.getActionCommand().equals("buttonUnjoin"))
			buttonUnjoinPressed();		
		else if(e.getActionCommand().equals("buttonUpdate"))
				updateList();
		
	}
	
	private void buttonUnjoinPressed() {
		
		String desc = (String) gameList.getSelectedValue();
		if(desc == null) {
			JOptionPane.showMessageDialog(new JFrame(), "select a game to unjoin from it", "error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		else {
			String name = desc.substring(0, desc.indexOf(" "));
		
			/*
			class T extends Thread {
				String name;
				public T(String name) {
					this.name = name;
				}
				public void run() { 
					try {
						controller.unregisterToServer(name);
						joinB.setEnabled(true);
						unjoinB.setEnabled(false);
						updateList();
					} catch (RemoteException e) {
						JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
					} catch (CannotUnregisterException e) {
						JOptionPane.showMessageDialog(new JFrame(), "you have not joined this game, the game does not exist or it is already started", "error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			
			(new T(name)).start();
			*/		
			

			try {
				controller.unregisterToServer(name);
				//joinB.setEnabled(true);
				//unjoinB.setEnabled(false);
				updateList();
				JOptionPane.showMessageDialog(new JFrame(), "unjoined from " + name, "unjoined", JOptionPane.INFORMATION_MESSAGE);
			} catch (RemoteException e1) {
				JOptionPane.showMessageDialog(new JFrame(), e1.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
			} catch (CannotUnregisterException e1) {
				JOptionPane.showMessageDialog(new JFrame(), "you have not joined this game, the game does not exist or it is already started", "error", JOptionPane.ERROR_MESSAGE);
			}
			
		}	
	}

	private void buttonCreateNewPressed() {
		
		try {
			controller.createNewGameInServer(nameNewGameTF.getText(), (Integer) nPlayerS.getValue() );
			updateList();
			JOptionPane.showMessageDialog(new JFrame(), nameNewGameTF.getText() + " created", "game created", JOptionPane.INFORMATION_MESSAGE);
		} catch (RemoteException e1) {
			JOptionPane.showMessageDialog(new JFrame(), e1.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		} catch (CannotCreateGameException e1) {
			JOptionPane.showMessageDialog(new JFrame(), "game name already exists!", "error", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
		
	}

	private void buttonJoinPressed() {
		
		String desc = (String) gameList.getSelectedValue();
		if(desc == null) {
			JOptionPane.showMessageDialog(new JFrame(), "select a game to join it", "error", JOptionPane.ERROR_MESSAGE);
		}
		else {
			String name = desc.substring(0, desc.indexOf(" "));		
			try {
				controller.registerToServer(name);
				updateList();
				//joinB.setEnabled(false);
				//unjoinB.setEnabled(true);	
				JOptionPane.showMessageDialog(new JFrame(), name + " joined", "game joined", JOptionPane.INFORMATION_MESSAGE);
			} catch (RemoteException e1) {
				JOptionPane.showMessageDialog(new JFrame(), e1.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
			} catch (CannotRegisterException e1) {
				JOptionPane.showMessageDialog(new JFrame(), "someone with the same name of yours is already" +
						" registered or you are already registered in another game", "error", JOptionPane.ERROR_MESSAGE);
			}
			
		}
		
	}

	public void updateList() {
		
		HashMap<String, GameDescription> listOfGames;
		
		try {
			listOfGames = controller.getAvailableGamesFromServer();
		} catch (RemoteException e1) {
			JOptionPane.showMessageDialog(new JFrame(), e1.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		listModel.clear();
		
		int i = 0;
		for(Map.Entry<String, GameDescription> game : listOfGames.entrySet()) {
			
			GameDescription g = game.getValue();
			
			String rep = game.getKey() + " -> ";
			if(g.getState().equals(gameStateEnum.GAME_STARTED))
				rep += "STARTED; ";
			else
				rep += "WAITING PLAYERS ; ";
			rep += "players required: " + g.getNPlayersRequired();
			
			if(g.getPlayers().size() != 0) {
				rep += " ; players: ";
				for(PeerCredential pc : g.getPlayers()) {
					rep += pc.getPeerName() + " ";
				}
			}
			
			listModel.add(i++, rep);
		}
		
	}
		
}
