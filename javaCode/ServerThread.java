import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.SwingUtilities;
/**
 * This class is dedicated to 2 players who want to play the cards memory game. Players retrieve data from server.
 * This class extends threads class and it implements interface Protocols( Contains all the codes and protocols).
 * @author harman Randhawa
 */
public class ServerThread extends Thread implements Protocols{
	private Card[] Cards = new Card[10];

	private Socket connectionClient1;
	private ObjectOutputStream outputClient1;
	private ObjectInputStream inputClient1;

	private Socket connectionClient2;
	private ObjectOutputStream outputClient2;
	private ObjectInputStream inputClient2;

	private Server server;
	private int currentPlayer;
	private int numCardsMatched = 0;
	/**
	 * Constructor for this class which initializes the sockets between server and players
	 * @param socket1 Socket which connects server to player 1
	 * @param socket2 Socket which connects server to player 2
	 * @param server Server to which the clients are connected 
	 */
	ServerThread( Socket socket1 , Socket socket2, Server server ){
		this.connectionClient1 = socket1;
		this.connectionClient2 = socket2;
		this.server = server;
		createCards();
	}

	/**
	 * Override method of threads class. It sets up the streams between clients and server. 
	 * Once the connections are all set, it starts the game with exchange of data.
	 */
	public void run(){
		try {
			setupStreams();
			String message = STARTGAME+"/" ;
			sendMessage(message);
			sendClientID();
			setPlayer(1);
			int buttonNum = STARTGAME;
			int numOfMoves = 0;
			int[] buttonsClicked = new int[2];
			showMessage("\n firstMessageSent");
			while( numCardsMatched < 5){
				if ( ( buttonNum = getInputFromCurrentPlayer() ) !=  QUIT )	{
					if ( buttonNum != -1 ){//int is read properly from client 
						sendMessage(FILENAME + "/" + buttonNum + "/" + Cards[buttonNum].getFileName());//send image filename to both the clients
						buttonsClicked[numOfMoves] = buttonNum;
						numOfMoves++;
						if ( numOfMoves == 2) {
							constructInstructions(buttonsClicked);
							numOfMoves = 0;
							switchPlayer();
						}
					}
				}
				else 
					numCardsMatched = 5;
			}
			//either the user pressed quit button or all the cards are matched 
			if ( buttonNum == QUIT )//user pressed the quit button
				sendMessage( FORFEITGAME + "/" + currentPlayer );  
			else //all the cards are matched
				sendMessage(GAMEOVER + "");

			closeAllConnections();//closing all connections since game is finished

		} catch (IOException e) {

		}
	}
	/**
	 * This method sets up the input and output streams between server and both the clients 
	 * @throws IOException Throws this exception when it can not set up the input/output streams.
	 */
	private void setupStreams() throws IOException{
		outputClient1 = new ObjectOutputStream(connectionClient1.getOutputStream());
		outputClient1.flush();
		inputClient1 = new ObjectInputStream(connectionClient1.getInputStream());
		showMessage("\n Streams are now setup for Client1 \n");

		outputClient2 = new ObjectOutputStream(connectionClient2.getOutputStream());
		outputClient2.flush();
		inputClient2 = new ObjectInputStream(connectionClient2.getInputStream());
		showMessage("\n Streams are now setup for Client2 \n");
	}
	/**
	 * Once the game is finished, all the sockets between server and clients are closed
	 */
	private void closeAllConnections(){
		try{
			outputClient1.close();
			inputClient1.close();
			connectionClient1.close();
			outputClient2.close();
			inputClient2.close();
			connectionClient2.close();
		}
		catch( IOException e ){
			//do nothing
		}
	}
	/**
	 * This method sends message to both the clients
	 * @param message Message to be sent
	 */
	private void sendMessage(String message) {
		try{
			outputClient1.writeObject(message);
			outputClient1.flush();
			outputClient2.writeObject(message);
			outputClient2.flush();
		}
		catch( IOException e ){
			server.displayArea.append(" \n ERROR: I CANT SEND THAT MESSAGE\n" + message);
		}
	}
	/**
	 * This method makes the instructions that are then sent to both the clients. Instructions have a format
	 * that includes / after each integer. It is then broken down into an array by Client class for 
	 * decrypting purposes  
	 * @param arr This array consists of 2 buttons that are clicked by current player
	 */
	private void constructInstructions( int[] arr ){
		String instructions = INSTRUCTIONS + "/" +currentPlayer + "/" + arr[0] + "/" + arr[1]+"/";
		boolean cardsMatch = evaluateCards(arr[0], arr[1]);
		if ( cardsMatch ){
			instructions += MATCH;
			numCardsMatched++;
		}
		else {
			instructions+= NON_MATCH;
		}
		sendMessage(instructions);
	}
	/**
	 * This method will check whether the cards at these 2 indexes are equal or not
	 * @param card1 Index of card1 in cards database/array
	 * @param card2 Index of card2 in cards database/array
	 * @return True if the cards match else returns false
	 */
	private boolean evaluateCards(int card1, int card2) {
		return Cards[card1].equals(Cards[card2]);
	}
	/**
	 * This method will get an int which would be the button pressed by current player. If the streams are
	 * unable to read the incoming int then it returns -1 as a false flag
	 * @return index of pressed button
	 */
	private int getInputFromCurrentPlayer() {
		int index = -1;
		try{
			if ( currentPlayer == 1 ){
				index = inputClient1.readInt();
			}
			else {
				index = inputClient2.readInt();
			}
		}
		catch ( IOException e ){
			//do nothing
		}
		return index;
	}

	/**
	 * This message gives control to client i. The other client cannot send any data.
	 */
	private void setPlayer( int i ) {
		sendMessage ( REMOVELISTENERS + "/Player/" + (3-i) );
      sendMessage ( ADDLISTENERS + "/Player/" + i );
		currentPlayer = i;
	}
	/**
	 * Switch the current player so that the controls can be switched to other player. This is done 
	 * after a player presses 2 buttons
	 */
	private void switchPlayer() {
		currentPlayer = 1 + currentPlayer%2;
		setPlayer(currentPlayer);
		sendMessage( ADDLISTENERS + "/Player/" + currentPlayer);
	}
	/**
	 * This message displays a message in the chat window and updates the chat window
	 * @param str String to be displayed
	 */
	private void showMessage ( final String str ){
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						server.displayArea.append(str);
					}
				}	
				);
	}
	/**
	 * This method sends respective client id to the clients 
	 */
	private void sendClientID(){
		try{
			outputClient1.writeObject(CLIENTID + "/1");
			outputClient2.writeObject(CLIENTID + "/2");
		}
		catch( IOException e) {
			System.out.println( "Couldnt send ID's");
		}
	}
	/**
	 * This method initializes the cards in an array 
	 */
	private void createCards() {
		for ( int i =0; i< 5; i++){
			Cards[i] = new Card(i); 
		}
		for ( int i =5; i< 10; i++){
			Cards[i] = new Card(9-i); 
		}
		shuffleArray();
	}
	/**
	 * This method will shuffle some cards in the Cards array.
	 */
	private void shuffleArray() {
		Random rand = new Random();
		int y;
		Card temp;
		for ( int i = 0; i<5; i++ ){
			y = rand.nextInt(5);
			temp = Cards[y];
			Cards[y] = Cards[9-y];
			Cards[9-y] = temp;
		}
	}
}
