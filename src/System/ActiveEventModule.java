package System;

import java.util.Vector;

import Structure.ActiveEvent;
import Structure.Subtopic;

public class ActiveEventModule {

	Vector<ActiveEvent> activeEvent;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	ActiveEventModule()
	{
		activeEvent = new Vector<ActiveEvent>(); 
	}
	
	public void updateEvent(Vector<Subtopic> subtopic)
	{
		for (int i = 0; i<activeEvent.size(); i++)
		{
			ActiveEvent ae = activeEvent.elementAt(i);
			if (ae.hasNewDoc)
			{
				for (int j = 0; j<ae.stIndex.size(); j++)
				{
					int index = ae.stIndex.elementAt(j);
					
				}
			}
		}
	}
	
	public void deactiveSubtopic(int stIndex)
	{
		for (int i = 0; i<activeEvent.size(); i++)
		{
			ActiveEvent ae = activeEvent.elementAt(i);
			for (int j = 0; j<ae.stIndex.size(); j++)
			{
				if (ae.stIndex.elementAt(j) > stIndex)
				{
					ae.stIndex.set(j, ae.stIndex.elementAt(j)-1);
				}
				else if (ae.stIndex.elementAt(j) == stIndex)
				//If the events always become inactive later than subtopics, then
				//no currently active events can affect the subtopic that is being deleted,
				//so this situation should not happen theoretically.
				{
					ae.stIndex.remove(j);
					//if this situation indeed happens, we simply delete the subtopic ref from the event...
				}
			}
		}
	}

}
