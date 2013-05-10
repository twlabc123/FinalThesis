package Structure;

import java.util.Vector;

public class ActiveEvent extends Event {
	
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
