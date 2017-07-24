package gui;

import game.Controller;

import java.awt.event.*;

import javax.swing.*;

public class Menu extends JMenuBar implements ActionListener {

	private RegistrationWindow registrationWindow;
	private Controller controller;
	
	public Menu(Controller controller) {
		
		this.controller = controller;
		
		JMenu menuGame = new JMenu("Game");
		menuGame.setMnemonic(KeyEvent.VK_G);
		add(menuGame);
		
		JMenuItem menuItemNewGame = new JMenuItem("New game");
		menuItemNewGame.setMnemonic(KeyEvent.VK_N);
		menuItemNewGame.setActionCommand("menuItemNewGame");
		menuItemNewGame.addActionListener(this);
		menuGame.add(menuItemNewGame);
		
		JMenuItem menuItemDisconnect = new JMenuItem("Disconnect");
		menuItemDisconnect.setMnemonic(KeyEvent.VK_D);
		menuItemDisconnect.setActionCommand("menuItemDisconnect");
		menuItemDisconnect.addActionListener(this);
		menuGame.add(menuItemDisconnect);
		
		menuGame.addSeparator();
		
		JMenuItem menuItemExit = new JMenuItem("Exit");
		menuItemExit.setMnemonic(KeyEvent.VK_E);
		menuItemExit.setActionCommand("menuItemExit");
		menuItemExit.addActionListener(this);
		menuGame.add(menuItemExit);
		
		registrationWindow = new RegistrationWindow(controller);
		registrationWindow.setVisible(false);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("menuItemNewGame")) {
			registrationWindow.setVisible(true);
			registrationWindow.updateList();
		}
		else if(e.getActionCommand().equals("menuItemExit")) {
			System.exit(0);
		}
		else if(e.getActionCommand().equals("menuItemDisconnect")) {
			controller.disconnect();
		}
		
	}
}
