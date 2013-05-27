package System;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Vector;

import DataPrepare.StopWordFilter;
import Structure.Event;
import Structure.EventEdge;
import Structure.Subtopic;
import Structure.TermScore;

public class InteractDetection {
	
	static double SameEvent = 0.6;
	
	BufferedReader eventReader;
	BufferedReader subtopicReader;
	PrintWriter writer;
	HashMap<Integer, Event> event;
	Vector<Subtopic> subtopic;
	StopWordFilter swf;
	
	public InteractDetection(String eventInput, String subtopicInput, String output) throws Exception
	{
		FileInputStream istream = new FileInputStream(eventInput);
		InputStreamReader sr = new InputStreamReader(istream, "utf-8");
		eventReader = new BufferedReader(sr);
		FileInputStream istream2 = new FileInputStream(subtopicInput);
		InputStreamReader sr2 = new InputStreamReader(istream2, "utf-8");
		subtopicReader = new BufferedReader(sr2);
		Event e;
		event = new HashMap<Integer, Event>();
		while ((e = Event.readEvent(eventReader)) != null)
		{
			event.put(e.id, e);
		}
		eventReader.close();
		Subtopic st;
		subtopic = new Vector<Subtopic>();
		while ((st = Subtopic.readSubtopic(subtopicReader)) != null)
		{
			subtopic.add(st);
		}
		subtopicReader.close();
		swf = new StopWordFilter();
		swf.load("data/stopwords.txt");
	}
	
	public static void main(String[] args)
	{
		try
		{
			InteractDetection inter = new InteractDetection("data/final/online_lc.txt", "data/final/online_st.txt", "");
			inter.mergeEvent("data/final/online_lc_merge.txt", "data/final/online_st_merge.txt");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void mergeEvent(String eventOutput, String subtopicOutput) throws Exception
	{
		for (int i = 0; i<subtopic.size(); i++)
		{
			Subtopic st = subtopic.elementAt(i);
			String[] summary = st.summary.split("\n");
			st.summary = "";
			for (int j = 0; j<st.event.size(); j++)
			{
				String s1 = event.get(st.event.elementAt(j).id).start;
				for (int k = j+1; k<st.event.size(); k++)
				{
					String s2 = event.get(st.event.elementAt(k).id).start;
					if (s1.compareTo(s2) > 0)
					{
						EventEdge temp = st.event.elementAt(j);
						st.event.set(j, st.event.elementAt(k));
						st.event.set(k, temp);
						String tempS = summary[j];
						summary[j] = summary[k];
						summary[k] = tempS;
					}
				}
				if (st.summary.length() != 0) st.summary += "\n";
				st.summary += summary[j];
			}
			summary = st.summary.split("\n");
			st.summary = summary[0];
			Vector<Integer> del = new Vector<Integer>();
			for (int j = 1; j<st.event.size(); j++)
			{
				Event e = event.get(st.event.elementAt(j).id);
				Event last = event.get(st.event.elementAt(j-1).id);
				HashSet<String> terms1 = new HashSet<String>();
				HashSet<String> terms2 = new HashSet<String>();
				for (int k = 0; k<e.article.size(); k++)
				{
					String[] temp = e.article.elementAt(k).content.split(" ");
					for (String term : temp)
					{
						if (swf.isStopWord(term)) continue;
						terms1.add(term);
					}
				}
				for (int k = 0; k<last.article.size(); k++)
				{
					String[] temp = last.article.elementAt(k).content.split(" ");
					for (String term : temp)
					{
						if (swf.isStopWord(term)) continue;
						terms2.add(term);
					}
				}
				int count = 0;
				for (String term : terms1)
				{
					if (terms2.contains(term))
					{
						count++;
					}
				}
				double sim = (double)(count)/(double)(terms1.size()+terms2.size()-count);
				if (sim > SameEvent)
				{
					del.add(j);
				}
				else
				{
					if (st.summary.length() != 0) st.summary += "\n";
					st.summary += summary[j];
				}
			}
			
			for (int j = del.size() - 1; j >= 0; j--)
			{
				Event e = event.get(st.event.elementAt(del.elementAt(j)).id);
				Event last = event.get(st.event.elementAt(del.elementAt(j)-1).id);
				last.article.addAll(e.article);
				if (e.start.compareTo(last.start) < 0) last.start = e.start;
				if (e.end.compareTo(last.end) > 0) last.end = e.end;
				System.out.println(e.id + " " + last.id + " "+ summary[del.elementAt(j)]);
				event.remove(st.event.elementAt(del.elementAt(j)).id);
				st.event.remove(del.elementAt(j).intValue());
			}
		}
		PriorityQueue<Event> pq = new PriorityQueue<Event>(event.size(), this.cp);
		pq.addAll(event.values());
		FileOutputStream stream = new FileOutputStream(eventOutput);
		OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
		writer = new PrintWriter(sw);
		while(!pq.isEmpty())
		{
			Event e = pq.poll();
			e.printEvent(writer);
		}
		writer.close();
		stream = new FileOutputStream(subtopicOutput);
		sw = new OutputStreamWriter(stream, "utf-8");
		writer = new PrintWriter(sw);
		for (Subtopic st : subtopic)
		{
			st.printSubtopic(writer, null);
		}
		writer.close();
	}
	
	public static Comparator<Event> cp = new Comparator<Event>()
	{
        public int compare(Event o1, Event o2)
        {
            // TODO Auto-generated method stub  
        	return o1.start.compareTo(o2.start);
        }
	};

}
