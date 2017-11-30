import java.io.*;
import javax.sound.sampled.*;
import java.net.*;
import java.awt.*;
import javax.swing.border.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.event.*;
/**
 * This class is dedicated to each client. It creates GUI for each player, sets up the connection with server,
 * sets up the streams and transfer data to and from server. It extends JFrame for GUI and it implements
 *  Protocols interface which shares all the codes and protocols with server and other clients
 * @author harman Randhawa
 */
public class Client extends JFrame implements Protocols {

	private static final long serialVersionUID = 1L;
	private JButton[] buttons = new JButton[10];
	private JButton quitButton;
	private JPanel mainPanel;
	private JPanel upperPanel;
	private JPanel lowerPanel;
	private JPanel buttonPanel;
	private JTextArea msg;
	private JTextArea connectionMsg;
	private JTextArea chatWindow;
	private JTextField userText;
	private JLabel player1Score;
	private JLabel player2Score;
	private Color RED = Color.RED;
	private Color GREEN = Color.GREEN;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String serverIP;
	private Socket connection;

	private int clientID;
	private int[] cardsMatched = { NON_MATCH,NON_MATCH,NON_MATCH,NON_MATCH,NON_MATCH,NON_MATCH,NON_MATCH,NON_MATCH,NON_MATCH,NON_MATCH} ;
	/**
	 * Main method that creates an instance of Client object and starts the processing
	 * @param args Default arguments from console. User can enter the first arguments as the name of server
	 */
	public static void main (String[] args){
		String host = "localhost";
		if ( args.length != 0 ){
			host = args[0];
		}
		new Client(host).startRunning();
	}
	/**
	 * This is the constructor for Client class that builds the GUI using JFrame
	 * @param host This is the ID of the server
	 */
	public Client(String host ){
		super("Game");
		serverIP = host;
		add( createGUI());//create all the components like frame, panel, buttons etc
		setVisible(true);
		setSize(700,300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	/**
	 * This method will connect this client with a server and then start the game 
	 */
	public void startRunning(){
		try{
			connectToServer();
			setupStreams();
			userText.setEditable(true);
			chatWindow.setText("");
         disableAllButtons();
			playGame();
		}
		catch( EOFException e ){
			showConnectionMessage("\n Client terminated the connection ");
		}
		catch (IOException e ){
			;
		}
		finally {//closing all the connections after the game is finished
			closeConnections();
		}
	}
	/**
	 * This method connects the client with the server
	 * @throws IOException Throws exception if it is unable to connect to the server
	 */
	private void connectToServer() throws IOException {
		showConnectionMessage(" Attempting connection.... \n");
		connection = new Socket(InetAddress.getByName(serverIP), PORT);
		showConnectionMessage("Connected to: " + connection.getInetAddress().getHostName() + " server");
	}
	/**
	 * Sets up the input/output streams with the server
	 * @throws IOException If the streams are not properly setup 
	 */
	private void setupStreams()throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showConnectionMessage( " Streams are good to go" );
	}
	/**
	 * This method will start the game and will only finish if the server sends a QUIT message 
	 */
	private void playGame(){
		String message = "";
		try{
			while( ! ( message = (String) input.readObject() ).equals(QUIT + "")) {
				message = readInstructions(message);
			}
		}catch( ClassNotFoundException e ){
			showConnectionMessage("\n Idk that object type");
		}catch ( IOException e) {
			//do nothing
		}
	}

	/**
	 * This method gets an instruction from server and proceeds to decode that instruction
	 * @param message Message from server
	 * @return Returns Quit if server asked to end the game either by player forfeiting or if the game is finished 
	 */
	private String readInstructions(String message ){
		String[] arr = message.split("/");
		switch ( Integer.parseInt(arr[0]) ){
		case FORFEITGAME : {
			forfeitGame( Integer.parseInt(arr[1]));
			message = QUIT + "";
			break;
		}
		case GAMEOVER : {
			message = QUIT + "";
			decideWinner();
			break;
		}
		case INSTRUCTIONS :{
			processInstructions(arr);
			break;
		}
		case STARTGAME : {
			showMessage ("Lets start Playing", GREEN);
			break;
		}
		case FILENAME :{
			setImage(Integer.parseInt(arr[1]), arr[2]);
			break;
		}
		case CLIENTID :{
			clientID = Integer.parseInt(arr[1]);
			break;
		}
		case REMOVELISTENERS :{
			if ( clientID == Integer.parseInt(arr[2])){
				disableAllButtons();
				break;
			}
		}
		case ADDLISTENERS :{
			if ( clientID == Integer.parseInt(arr[2])){
				enableAllButtons();
				break;
			}
		}
		}
		return message;
	}
	/**
	 * If a player wants to forfeit the game then it displays the text to different users about winning/losing
	 * @param player Player 1 or 2 who forfeited the game
	 */
	private void forfeitGame( int player ){
		if ( player == clientID ){
			msg.setText("You lose by forfeiting the game ");
			msg.setBackground(RED);
		}
		else {
			msg.setText("Opponent forfeited, You won ");
			msg.setBackground(GREEN);
		}
	}
	/**
	 * This method will process the instructions sent by server whether the cards are matched or not 
	 * @param arr List of instructions sent 
	 */
	private void processInstructions( String[] arr ){
		//structure of array is { INSTRUCTIONS, clientID, button1, button2, MATCH/NON_MATCH }
		if ( Integer.parseInt(arr[4]) == MATCH ){//cards are matched 
			cardsMatched(arr[2], arr[3]);
			incrementScore(arr[1]);
		}
		else{
			cardsNotMatched( arr[2], arr[3]);
			playSound("fade_x.wav");
		}
	}
	/**
	 * This method sets the button1 and button 2 to disable since cards on these buttons are matched
	 * @param button1 Index of button1
	 * @param button2 Index of button2
	 */
	private void cardsMatched(String button1, String button2 ) {
		int b1 = Integer.parseInt(button1);
		int b2 = Integer.parseInt(button2);
		buttons[b1].setEnabled(false);
		buttons[b2].setEnabled(false);
		cardsMatched[b1] = MATCH;
		cardsMatched[b2] = MATCH;
	}
	/**
	 * This method will increment score of the player
	 * @param id ClientID of the player
	 */
	private void incrementScore(String id ) {
		int playerID = Integer.parseInt(id);
		if ( playerID == clientID ){
			showMessage(" Your cards Matched", GREEN);
		}
		else {
			showMessage("Opponent player matched cards", Color.YELLOW );
		}
		try {//so that player can see both the cards for a while 
			Thread.sleep(1300);
		} catch (InterruptedException e) {
			//do nothing
		}
		incrementScoreLabel( playerID );
	}
	/**
	 * This method will increment the score labels on top panel
	 * @param playerID Player whose score has to be incremented
	 */
	private void incrementScoreLabel(int playerID) {
		int score = getPreviousScore(playerID);
		score++;
		if ( playerID == 1 ){
			player1Score.setText("Player 1 \t Score = " + score );
		}
		else {
			player2Score.setText("Player 2 \t Score = " + score );
		}
		playSound("applause_y.wav");
	}
	/**
	 * This method sets the button1 and button 2 to default since cards on these buttons did not matched
	 * @param button1 Index of button1
	 * @param button2 Index of button2
	 */
	private void cardsNotMatched(String button1, String button2 ) {
		int b1 = Integer.parseInt(button1);
		int b2 = Integer.parseInt(button2);
		try {//so that player can see both the cards for a while 
			Thread.sleep(1300);
		} catch (InterruptedException e) {
			//do nothing
		}
		setImage( b1, DEFAULT_FILE_NAME);
		setImage( b2, DEFAULT_FILE_NAME);
	}
	/**
	 * This method is called when all the cards are matched. It decides who is the winner depending on 
	 * the score of each player 
	 */
	private void decideWinner(){
		int score1 = getPreviousScore(1);
		int score2 = getPreviousScore(2);
		if ( score1 > score2 ) {
			declareWinner(score1, score2 , 1, 2);
		}
		else if ( score1 < score2 ) {
			declareWinner(score2, score1, 2, 1);
		}
	}
	private void declareWinner( int highScore, int lowScore, int winnerID, int loserID ){
		if ( clientID == winnerID ){
			showMessage("You won the game by " + highScore + "-" + lowScore, GREEN);
		}
		else{
			showMessage("You lost the game.Final score is " + lowScore + "-" + highScore, RED);
		}
	}
	/**
	 * This method will get the previous score of current player from jlabel text
	 * @param player player entered 
	 * @return Score of current Player
	 */
	private int getPreviousScore(int player ) {
		String str ="";
		if ( player == 1){
			str = player1Score.getText();
		}
		else {
			str = player2Score.getText();
		}
		str = str.charAt(str.length()-1) + "";
		return Integer.parseInt(str);
	}
	/**
	 * This method creates the GUI for the game 
	 * @return JPanel which has all the components embedded to it 
	 */
	private JPanel createGUI() {
		mainPanel = new JPanel();
		upperPanel = createUpperPanel();
		buttonPanel = createButtonPanel();
		lowerPanel = createLowerPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(buttonPanel,BorderLayout.CENTER);
		mainPanel.add(upperPanel,BorderLayout.NORTH);
		mainPanel.add(lowerPanel,BorderLayout.SOUTH);
		return mainPanel;
	}
	/**
	 * This method creates a panel that is added to the bottom of the frame
	 * @return JPanel with a text area and a quit button
	 */
	private JPanel createLowerPanel() {
		JPanel panel = new JPanel();
		quitButton = new JButton("Quit");
		quitButton.addActionListener(
			(ActionEvent event) ->{
				playSound("click_x.wav");
				sendMessage("" + QUIT);
			}
		);
		connectionMsg = new JTextArea();
		connectionMsg.setEditable(false);
		chatWindow = new JTextArea();
		chatWindow.setEditable(false);
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(
         (ActionEvent event) -> {
						playSound("chime.wav");
						showChatMessage("My Message->" + event.getActionCommand());
						userText.setText("");
					}
         );
		panel.setLayout(new GridLayout(2,2));
		panel.add(connectionMsg);
		panel.add(quitButton);
		panel.add(chatWindow);
		panel.add( new JScrollPane(chatWindow));
		panel.add(userText);
		return panel;
	}

	/** This method creates a panel to put in top of screen 
	 * It will have 3 text fields, one showing the turn of the player and other 2 showing scores
	 * @return JPanel with 3 text fields 
	 */
	private JPanel createUpperPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		msg = new JTextArea("All the messages will be displayed here ");
		msg.setBackground(Color.GREEN);
		msg.setEditable(false);
		msg.setLineWrap(true);
		msg.setWrapStyleWord(true);
		player1Score = new JLabel("Player1 Score = 0");
		player2Score = new JLabel("Player2 Score = 0");
		JPanel shortPanel = new JPanel();
		shortPanel.setLayout(new GridLayout(1,3));
		shortPanel.add(msg);
		shortPanel.add(player1Score);
		shortPanel.add(player2Score);
		panel.add(shortPanel);
		panel.setBorder(new TitledBorder(new EtchedBorder(),"Display"));
		return panel;
	}
	/**
	 * This method will create 10 buttons, add them to a panel and return the panel 
	 * @return JPanel with 10 buttons on it 
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		int[] arr = {0,1,2,3,4,5,6,7,8,9};
		panel.setLayout(new GridLayout(2,5));
		for ( int i : arr ){
			final JButton button = new JButton();
			button.setVisible(true);
			button.setOpaque(true);
			buttons[i] = button;
			setImage(i, DEFAULT_FILE_NAME);
			button.addActionListener(new MyListener(i));
			panel.add(button);
		}
		return panel;
	}
	/**
	 * This method sets the image fileName on button at index in the button array
	 * @param index Index in buttons array
	 * @param fileName File name of the image 
	 */
	private void setImage(int index, String fileName) {
		try{
			Image image = ImageIO.read(Client.class.getResource(fileName));
			Image temp = image.getScaledInstance(70,70,3);
			buttons[index].setIcon(new ImageIcon (temp) );
		}
		catch( IllegalArgumentException e ){
			System.out.println( "Image not found for " + fileName );
		}
		catch( IOException e ){
			System.out.println( "Image not found for " + fileName );
		}
	}
	/**
	 * This method sends the string to server
	 * @param message Message to be sent usually an index of the button or QUIT
	 */
	private  void sendMessage(String message ){
		try{
			output.writeInt(Integer.parseInt(message));
			output.flush();
		}
		catch (IOException e){
			//Do nothing
		}
	}
	/**
	 * This method disables all the buttons so that control can be given to other player
	 */
	private void disableAllButtons(){
		quitButton.setEnabled(false);
		userText.setEditable(false);
		for ( int i = 0; i< 10 ; i++){
			if ( cardsMatched[i] == NON_MATCH ) {
				buttons[i].setEnabled(false);
			}
		}
		showMessage("Opponent's turn", Color.BLUE);
	}
	/**
	 * This method enables all the buttons so that control can be given to current player
	 */
	private void enableAllButtons(){
		quitButton.setEnabled(true);
		userText.setEditable(true);
		for ( int i = 0; i< 10 ; i++){
			if ( cardsMatched[i] == NON_MATCH ) {
				buttons[i].setEnabled(true);
			}
		}
		showMessage("Your turn", GREEN);
	}
	/**
	 * This class is attached to each JButton. It implements ActionListener.
	 * Whenever a button is pressed, it sends the index of that button to server
	 * @author harman Randhawa
	 *
	 */
	private class MyListener implements ActionListener{
		int num ;
		/**
		 * Constructor of this class
		 * @param num Index of pressed button 
		 */
		public MyListener (int num ){
			this.num = num ;
		}
		/**
		 * Override method that is called every time the button is pressed
		 */
		public void actionPerformed(ActionEvent arg0) {
			playSound("click_x.wav");
			sendMessage("" + this.num );
		}
	}
	private void showChatMessage(final String str ){
		playSound("chime.wav");
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						chatWindow.append("\n" + str);
					}
				}	
				);
	}
	/**
	 * This method is used to display the connection messages on the GUI 
	 * @param message Message to be displayed 
	 */
	private void showConnectionMessage( String message){
		connectionMsg.setText(message);
	}
	/**
	 * This method displays the messages on the GUI
	 * @param message Message to be displayed 
	 * @param color Background colour of the area where the message is displayed.
	 */
	private void showMessage( String message, Color color){
		msg.setText(message);
		msg.setBackground(color);
	}
	/**
	 * This method is called when the game is finished to close all the sockets between client and server
	 */
	private void closeConnections(){
		showConnectionMessage("Closing the streams and socket");
		try{
			output.close();
			input.close();
			connection.close();
		}
		catch( IOException e ){
			;
		}
	}
	/**
	 * This method plays the sound
	 * @param soundName Name of the file that has sound
	 */
	public void playSound(String soundName){
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundName).getAbsoluteFile( ));
			Clip clip = AudioSystem.getClip( );
			clip.open(audioInputStream);
			clip.start( );
		}
		catch(Exception ex){
			System.out.println("Error with playing sound.");
			ex.printStackTrace( );
		}
	}
}
