package gui;

import game.Controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ChatArea extends JPanel implements KeyListener {

	private JTextField chatTF;
	private Controller controller;
	
	public ChatArea(Controller controller) {
		
		this.controller = controller;
		
		chatTF = new JTextField();
		//chatTF.setText("chat with the other players");
		chatTF.addKeyListener(this);
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		
		add(chatTF, c);
		
	}

	@Override
	public void keyPressed(KeyEvent e) { }

	@Override
	public void keyReleased(KeyEvent e) { }

	@Override
	public void keyTyped(KeyEvent e) {
		if( e.getKeyChar() == '\n') {
			controller.chat(chatTF.getText());
			chatTF.setText("");
		}
	}
}
