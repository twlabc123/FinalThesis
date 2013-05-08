package System;

import java.util.Vector;

import EventCluster.EventClusterTFIDF;
import Structure.ActiveEvent;
import Structure.Subtopic;
import TopicThreading.TfisfTime;

public class ActiveEventModule {

	Vector<ActiveEvent> activeEvent;
	EventClusterTFIDF ec;
	TfisfTime sa;
	
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
	
	public void removeChangedEventFromSubtopic(Vector<Subtopic> subtopic)
	{
		for (int i = 0; i<activeEvent.size(); i++)
		{
			ActiveEvent ae = activeEvent.elementAt(i);
			if (ae.hasNewDoc)
			{
				for (int j = 0; j<ae.stIndex.size(); j++)
				{
					int index = ae.stIndex.elementAt(j);
					Subtopic st = sa.subtopic.elementAt(index);
					for (int k = st.eventId.size(); k>=0; k--)
					{
						if (st.eventId.elementAt(k) == ae.id)
						{
							for (String term : ae.tf.keySet())
							{
								Integer temp = st.tf.get(term);
								st.tf.remove(term);
								temp -= ae.tf.get(term);
								if (temp > 0) st.tf.put(term, temp);
							}
							for (String term : ae.df.keySet())
							{
								Integer temp = st.df.get(term);
								st.df.remove(term);
								temp -= ae.df.get(term);
								if (temp > 0) st.df.put(term, temp);
							}
							st.eventId.remove(k);
							break;
						}
					}
				}
				ae.stIndex.clear();
			}
		}
	}
	
	public void linkEventToSubtopic(int eventId, int subtopicIndex) throws Exception
	{
		int a = 0;
		int b = activeEvent.size();
		int mid;
		while (a < b)
		{
			mid = (a+b)/2;
			int temp = activeEvent.elementAt(mid).id;
			if (temp == eventId)
			{
				a = b = mid;
			}
			else if (temp > eventId)
			{
				b = mid - 1;
			}
			else
			{
				a = mid + 1;
			}
		}
		
		if (a > b)
		{
			throw new Exception("Can't find eventId "+ eventId + " !");
		}
		else
		{
			activeEvent.elementAt(a).stIndex.add(subtopicIndex);
		}
	}
	
	public void delinkEventToSubtopic(int eventId, int subtopicIndex) throws Exception
	{
		int a = 0;
		int b = activeEvent.size();
		int mid;
		while (a < b)
		{
			mid = (a+b)/2;
			int temp = activeEvent.elementAt(mid).id;
			if (temp == eventId)
			{
				a = b = mid;
			}
			else if (temp > eventId)
			{
				b = mid - 1;
			}
			else
			{
				a = mid + 1;
			}
		}
		
		if (a > b)
		{
			throw new Exception("Can't find eventId "+ eventId + " !");
		}
		else
		{
			ActiveEvent ae = activeEvent.elementAt(a);
			for (int i = 0; i<ae.stIndex.size(); i++)
			{
				if (ae.stIndex.elementAt(i) == subtopicIndex)
				{
					ae.stIndex.remove(i);
					break;
				}
			}
		}
	}
	
	public void removeSubtopic(int stIndex)
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
