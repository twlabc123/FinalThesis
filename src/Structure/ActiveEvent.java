package Structure;

import java.util.Vector;

public class ActiveEvent extends Event {
	
	public Vector<Integer> stIndex;//the indexes of the subtopics that contains the event
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
	
	public ActiveEvent()
	{
		super();
		stIndex = new Vector<Integer>();
	}
	
	public ActiveEvent(Event e)
	{
		super(e);
		stIndex = new Vector<Integer>();
	}

}
