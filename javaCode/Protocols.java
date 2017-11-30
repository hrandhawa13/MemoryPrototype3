/**
 * This interface will be used for the protocols to be used by clients and server.
 * It will have codes for different moves.
 * @author harman Randhawa
 *
 */
public interface Protocols {
   /**
    * This is the port number through which server and client interacts to each other
    */
	final int PORT = 6789;
	/**
	 * When 2 moves of a player match, the server send this as a part of instructions
	 */
	final int MATCH = 1000;
	/**
	 * When 2 moves of a player match, the server sends this as part of instructions
	 */
	final int NON_MATCH = 1001;
	/**
	 * When the current player quits the game, the server sends this to both the clients to end the game
	 */
	final int QUIT = 1002;
	/**
	 * When 2 clients connect to server, the server sends this so that game can be started
	 */
	final int STARTGAME = 2003;
	/**
	 * When server gives control to other player, it sends this to disable all the buttons 
	 */
	final int REMOVELISTENERS = 1004;
	/**
	 * When player is given the control back, server sends this to enable all the buttons 
	 */
	final int ADDLISTENERS = 1005;
	/**
	 * When a player forfeits the game, server sends this to notify both the players
	 */
	final int FORFEITGAME = 1006;
	/**
	 * When a player clicks the button, server sends the filename of the card that contains the image
	 */
	final int FILENAME = 1007;
	/**
	 * When server wants to sends some instructions to both the clients
	 */
	final int INSTRUCTIONS = 1008;
	/**
	 * When server tells the player that game is over now
	 */
	final int GAMEOVER = 1009;
	/**
	 * When server sends the client ID to each player
	 */
	final int CLIENTID = 1010;
	/**
	 * This is the file name of the default picture on cards 
	 */
	final static String DEFAULT_FILE_NAME = "Default.jpg";
}
