package Structure;

import java.util.Vector;

/**
 * The active event representation class<br>
 * Add an vector of the indexes of the subtopics that contain this event.
 * @author twl
 *
 */
public class ActiveEvent extends Event {
	/**
	 * The vector of the indexes of the subtopics that contain this event.
	 */
	public Vector<Integer> stId;//the indexes of the subtopics that contains the event
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
	
	public ActiveEvent()
	{
		super();
		stId = new Vector<Integer>();
	}
	
	public ActiveEvent(Event e)
	{
		super(e);
		stId = new Vector<Integer>();
	}

}
