package System;

import java.util.Vector;

import EventCluster.EventClusterTFIDF;
import Structure.ActiveEvent;
import Structure.Subtopic;
import TopicThreading.MultiView;
import TopicThreading.TfisfTime;

public class ActiveEventModule {

	Vector<ActiveEvent> activeEvent;
	EventClusterTFIDF ec;
	//TfisfTime sa;
	MultiView sa;
	
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
	
	public ActiveEvent getEventById(int id)
	{
		for (int i = 0; i<activeEvent.size(); i++)
		{
			if (activeEvent.elementAt(i).id == id)
			{
				return activeEvent.elementAt(i);
			}
		}
		return null;
	}
	
	public void addSummaryToSubtopic(ActiveEvent ae)
	{
		for (int j = 0; j<ae.stId.size(); j++)
		{
			int id = ae.stId.elementAt(j);
			Subtopic st = sa.getSubtopicById(id);
			for (int k = 0; k<ae.article.size(); k++)
			{
				if (st.summary.length() != 0) st.summary += "\n";
				st.summary += ae.article.elementAt(k).time.substring(0,10);
				st.summary += " ";
				st.summary += ae.article.elementAt(k).title;
			}
		}
	}
	
	public void removeChangedEventFromSubtopic(ActiveEvent ae) throws Exception
	{
		for (int j = 0; j<ae.stId.size(); j++)
		{
			int id = ae.stId.elementAt(j);
			Subtopic st = sa.getSubtopicById(id);
			st.removeEvent(ae, this);
		}
		ae.stId.clear();
		int i = 0;
		while(i<sa.subtopic.size())
		{
			if (sa.subtopic.elementAt(i).eventId.size() <= 0)
			{
				removeSubtopic(sa.subtopic.elementAt(i));
				sa.subtopic.remove(i);
				i--;
			}
			i++;
		}
	}
	
	public void removeChangedEventFromSubtopic() throws Exception
	{
		for (int i = 0; i<activeEvent.size(); i++)
		{
			ActiveEvent ae = activeEvent.elementAt(i);
			if (ae.hasNewDoc)
			{
				for (int j = 0; j<ae.stId.size(); j++)
				{
					int id = ae.stId.elementAt(j);
					Subtopic st = sa.getSubtopicById(id);
					st.removeEvent(ae, this);
				}
				ae.stId.clear();
			}
		}
		int i = 0;
		while(i<sa.subtopic.size())
		{
			if (sa.subtopic.elementAt(i).eventId.size() <= 0)
			{
				removeSubtopic(sa.subtopic.elementAt(i));
				sa.subtopic.remove(i);
				i--;
			}
			i++;
		}
	}
	
	public void linkEventToSubtopic(ActiveEvent ae, Subtopic st) throws Exception
	{
		ae.stId.add(st.id);
	}
	
	public void delinkEventToSubtopic(int eventId, Subtopic st) throws Exception
	{
		ActiveEvent ae = getEventById(eventId);
		int i = 0;
		while (i<ae.stId.size())
		{
			if (ae.stId.elementAt(i) == st.id)
			{
				ae.stId.remove(i);
				i--;
			}
			i++;
		}
	}
	
	public void removeSubtopic(Subtopic st)
	{
		for (int i = 0; i<activeEvent.size(); i++)
		{
			ActiveEvent ae = activeEvent.elementAt(i);
			for (int j = 0; j<ae.stId.size(); j++)
			{
				if (ae.stId.elementAt(j) == st.id)
				//If the events always become inactive later than subtopics, then
				//no currently active events can affect the subtopic that is being deleted,
				//so this situation should not happen theoretically.
				{
					ae.stId.remove(j);
					//if this situation indeed happens, we simply delete the subtopic ref from the event...
				}
			}
		}
	}
	
	

}
