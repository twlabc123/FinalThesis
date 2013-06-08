package System;

import java.util.Vector;

import EventCluster.EventClusterTFIDF;
import Structure.ActiveEvent;
import Structure.Subtopic;
import TopicThreading.MultiView;

/**
 * The function of this class is to maintain a active event set and act as an
 * intermediary between the event cluster and the subtopic analysis.
 * @author twl
 *
 */
public class ActiveEventModule {

	/**
	 * The active event set
	 */
	Vector<ActiveEvent> activeEvent;
	/**
	 * The ref of the event cluster module
	 */
	EventClusterTFIDF ec;
	//TfisfTime sa;
	/**
	 * The ref of the subtopic analysis module
	 */
	MultiView sa;
	
	ActiveEventModule()
	{
		activeEvent = new Vector<ActiveEvent>(); 
	}
	
	/**
	 * Get an event by its id
	 * @param id
	 * @return
	 */
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
	
	/**
	 * Add the summary of the event to the subtopic.<br>
	 * The current summary String implementation is not good. Could use vector or
	 * others for better use.
	 * @param ae
	 */
	public void addSummaryToSubtopic(ActiveEvent ae)
	{
		for (int j = 0; j<ae.stId.size(); j++)
		{
			int id = ae.stId.elementAt(j);
			Subtopic st = sa.getSubtopicById(id);
			if (st.summary.length() > 0) st.summary += "\n";
			String summary = ExtractSummary.ExtractEventSummary(ae, sa);
			st.summary += summary;
//			for (int k = 0; k<ae.article.size(); k++)
//			{
//				st.summary += ae.article.elementAt(k).time.substring(0,10);
//				st.summary += " ";
//				st.summary += ae.article.elementAt(k).title;
//				st.summary += "\n";
//			}
//			for (int i = 0; i<st.event.size(); i++)
//			{
//				if (st.event.elementAt(i).id == ae.id)
//				{
//					st.summary += "sim: "+st.event.elementAt(i).value;
//				}
//			}
		}
	}
	
	/**
	 * Remove the event from all the subtopics that comtains it.
	 * @param ae
	 * @throws Exception
	 */
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
			if (sa.subtopic.elementAt(i).event.size() <= 0)
			{
				removeSubtopic(sa.subtopic.elementAt(i));
				sa.subtopic.remove(i);
				i--;
			}
			i++;
		}
	}
	
	/**
	 * Remove all the changed events from relating subtopics at once.
	 * @throws Exception
	 */
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
			if (sa.subtopic.elementAt(i).event.size() <= 0)
			{
				removeSubtopic(sa.subtopic.elementAt(i));
				sa.subtopic.remove(i);
				i--;
			}
			i++;
		}
	}
	
	/**
	 * Add a event to a subtopic and build a link from this event to the last event
	 * in the subtopic
	 * @param ae
	 * @param st
	 * @throws Exception
	 */
	public void linkEventToSubtopic(ActiveEvent ae, Subtopic st) throws Exception
	{
		ae.stId.add(st.id);
	}
	
	/**
	 * Remove the event from the subtopic
	 * @param eventId
	 * @param st
	 * @throws Exception
	 */
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
	
	/**
	 * Remove the subtopic from the active event set.
	 * @param st
	 */
	public void removeSubtopic(Subtopic st)
	{
		for (int i = 0; i<activeEvent.size(); i++)
		{
			ActiveEvent ae = activeEvent.elementAt(i);
			for (int j = 0; j<ae.stId.size(); j++)
			{
				if (ae.stId.elementAt(j) == st.id)
				{
					ae.stId.remove(j);
				}
			}
		}
	}
	
	

}
