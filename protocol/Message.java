package protocol;

import java.io.Serializable;

public class Message implements Serializable {

	public MessageType messageType;
	public Object message;
	public int messageId;
	public PeerCredential senderCredential;
	
	public enum MessageType { CHAT_MESSAGE, GAME_MESSAGE }
}
