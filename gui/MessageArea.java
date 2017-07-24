package gui;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MessageArea extends JTextArea {

	public MessageArea() {
		setEditable(false);
	}
	
	public void println(String s) {
		   append(s + "\n");
		   setCaretPosition(getDocument().getLength());
	}
}
