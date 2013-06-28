package System;

import java.util.Vector;

import EventClustering.EventCluster;
import EventClustering.EventClusterBool;
import Structure.ActiveEvent;
import Structure.Subtopic;
import TopicThreading.TopicThreading;
import TopicThreading.Similarity.Tfisf;
import TopicThreading.Similarity.TfisfKeyword;

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
	EventCluster ec;
	//TfisfTime sa;
	/**
	 * The ref of the subtopic analysis module
	 */
	TopicThreading tt;
	
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
			Subtopic st = tt.getSubtopicById(id);
			if (st.summary.length() > 0) st.summary += "\n";
			String summary = ExtractSummary.ExtractEventSummary(ae, tt);
			st.summary += summary;
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
			Subtopic st = tt.getSubtopicById(id);
			st.removeEvent(ae, this);
		}
		ae.stId.clear();
		int i = 0;
		while(i<tt.subtopic.size())
		{
			if (tt.subtopic.elementAt(i).event.size() <= 0)
			{
				removeSubtopic(tt.subtopic.elementAt(i));
				tt.subtopic.remove(i);
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
					Subtopic st = tt.getSubtopicById(id);
					st.removeEvent(ae, this);
				}
				ae.stId.clear();
			}
		}
		int i = 0;
		// remove empty subtopics
		while(i<tt.subtopic.size())
		{
			if (tt.subtopic.elementAt(i).event.size() <= 0)
			{
				removeSubtopic(tt.subtopic.elementAt(i));
				tt.subtopic.remove(i);
				tt.stNum--;
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
	public void addEventToSubtopic(ActiveEvent ae, Subtopic st) throws Exception
	{
		ae.stId.add(st.id);
	}
	
	/**
	 * Remove the event from the subtopic
	 * @param eventId
	 * @param st
	 * @throws Exception
	 */
	public void removeEventFromSubtopic(int eventId, Subtopic st) throws Exception
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
