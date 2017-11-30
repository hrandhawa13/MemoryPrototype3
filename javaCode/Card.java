import java.awt.*;
import java.io.IOException;
import javax.swing.*;

/**
 * This class is for cards in the game. Each card has a unique value and unique icon.
 * @author harman Randhawa
 */
public class Card {
	private int value;
	private String iconFile;
	private ImageIcon cardIcon;
	private Point current;
	private static String[] fileNames = {"fish.gif","Nine.png", "phase1.png","phase2.jpeg","spade.png"};
	private boolean isShown;
	
	/**
	 * Default constructor with an integer value and string for file name 
	 * @param x This is the value of card. Each card has unique value
	 */
	public Card ( int x ){
		try{
		value = x;
		iconFile = fileNames[x];
		isShown=false;
		cardIcon = new ImageIcon(getClass().getResource(iconFile));
		}
		catch( NullPointerException e ){
			System.out.println( "Image not found for " + iconFile );
         System.exit(1);
			}
		catch ( ArrayIndexOutOfBoundsException e ){
			System.out.println (" You initialized card with wrong value ");
		}
		finally{}
	}
	/**
	 * This method returns the value of the card
	 * @return an integer value for the card
	 */
	public int getValue(){
		return value;
	}
	/**
	 * This method returns the string which contains the file name of the icon
	 * @return a string for the file name where the image is stored 
	 */
	public String getFileName(){
		return iconFile;
	}
	/**
	 * This method displays the card onto the screen 
	 * @param comp Component on which the card is drawn 
	 * @param g Graphics parameter used to draw the card
	 */
	public void draw(Component comp, Graphics g){
	      cardIcon.paintIcon(comp, g, current.x, current.y);
	   }
	/**
	 * This method sets whether the card is shown or not
	 * @param bool boolean depicting whether the card is shown or not 
	 */
	public void setShown(boolean bool ){
		isShown = bool;
	}
	/**
	 * This method returns whether the card is shown or not 
	 * @return Boolean whether the card is shown or not 
	 */
	public boolean getIsShown(){
		return isShown;
	}
	/**
	 * This method checks whether the 2 cards are same or not depending on the unique value that each card has
	 * @param other Second card to be checked
	 * @return True if they match false otherwise
	 */
	public boolean equals( Card other ){
		return this.value == other.getValue();
	}
}
