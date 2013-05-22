package System;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import DataPrepare.StopWordFilter;
import Structure.Event;
import Structure.Subtopic;

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
			inter.mergeEvent();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void mergeEvent()
	{
		for (int i = 0; i<subtopic.size(); i++)
		{
			Subtopic st = subtopic.elementAt(i);
			String[] summary = st.summary.split("\n");
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
					System.out.println("==========");
					System.out.println(e.id + " " + summary[j]);
					System.out.println(last.id + " " + summary[j-1]);
					
				}
			}
		}
	}

}
