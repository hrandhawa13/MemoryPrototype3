import java.io.*;
import java.net.*;
import javax.swing.*;

/**
 * This class is dedicated for server. It will have all the necessary data required for the game.
 * It will make a connection with different clients, and whenever there are 2 clients it will start a thread dedicated for those 2 players.
 * Clients will exchange data to each other through the server. There are few protocols that both the client and server has to follow. 
 * @author harman Randhawa
 *
 */
public class Server extends JFrame implements Protocols{

	private static final long serialVersionUID = 1L;
	public JTextArea displayArea;
	private ServerSocket server;
	private Socket connectionClient1;
	private Socket connectionClient2;

	/**
	 * Main method that calls the server to start running
	 * @param args Default parameters from console
	 */
	public static void main (String[] args){
		new Server().startRunning();
	}
	/**
	 * Constructor for the class that creates the frame where all the messages are displayed
	 */
	public Server(){
		displayArea = new JTextArea();
		displayArea.setEditable(false);
		add( new JScrollPane(displayArea));
		setSize(300,150);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	/**
	 * This method will start running the server, wait for 2 connections
	 * and then start the game once 2 connections are there.
	 */
	public void startRunning(){
		try{
			while( true ){
				server = new ServerSocket(PORT);
				try{
					waitForClient(1);
					waitForClient(2);
					new ServerThread(connectionClient1, connectionClient2, this).start();
				}
				catch( EOFException e ){
					showMessage("\nServer ended the connection");
				}
				finally {
					server.close();
				}
			}
		}
		catch( IOException e ){
		}
	}
	/**
	 * This method waits for client to connect. Once the client is available it connects with it.
	 * @param num It is the client number
	 * @throws IOException Throws this exception if it cannot connect to a client
	 */
	private void waitForClient( int num ) throws IOException{
		showMessage("\n Waiting for Client " + num + " to connect ...");
		if ( num == 1 ){
			connectionClient1 = server.accept();
			showMessage("\nNow Connected to " + connectionClient1.getInetAddress().getHostName() + "/" +connectionClient1.getInetAddress().getHostAddress());
		}
		else {
			connectionClient2 = server.accept();
			showMessage("\nNow Connected to " + connectionClient2.getInetAddress().getHostName() + "/" +connectionClient2.getInetAddress().getHostAddress());
		}
	}
	/**
	 * This message displays a message in the frame window and updates the frame window
	 * @param str String to be displayed
	 */
	private void showMessage ( final String str ){
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						displayArea.append(str);
					}
				}	
      );
	}

}