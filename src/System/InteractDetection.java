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
import Structure.Article;
import Structure.Event;
import Structure.EventEdge;
import Structure.Subtopic;
import Structure.TermScore;
import TopicThreading.MultiView;

/**
 * The function of this class is to extract interact between events from different
 * subtopics off-line.
 * @author twl
 *
 */
public class InteractDetection {
	/**
	 * The threshold of similarity that determines whether 2 adjacent event are the same event.
	 */
	static double SameEvent = 0.6;
	/**
	 * The threshold of similarity that determines whether 2 events from different subtopics
	 * have interact.
	 */
	static double Interact = 0.5;
	/**
	 * The number of keywords that are used to describe the event.
	 */
	static int EventKeyword = 30;
	/**
	 * The bonus factor for entities when compute term scores
	 */
	static double EntityBonus = 2;
	/**
	 * The width of the time window.
	 */
	static int WindowWidth = 5;
	
	/**
	 * The reader of the event input file
	 */
	BufferedReader eventReader;
	/**
	 * The reader of the subtopic input file
	 */
	BufferedReader subtopicReader;
	/**
	 * The writer of the output file
	 */
	PrintWriter writer;
	/**
	 * The event set
	 */
	HashMap<Integer, Event> event;
	/**
	 * The subtopic set
	 */
	Vector<Subtopic> subtopic;
	/**
	 * The stop word filter
	 */
	StopWordFilter swf;
	/**
	 * The term event frequency table
	 */
	HashMap<String, Integer> ef;
	/**
	 * The total number of the events
	 */
	int eventNum;
	
	/**
	 * Initialize all para
	 * @param eventInput
	 * @param subtopicInput
	 * @throws Exception
	 */
	public InteractDetection(String eventInput, String subtopicInput) throws Exception
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
			InteractDetection inter = new InteractDetection("data/final/online_lc.txt", "data/final/online_st.txt");
			inter.mergeEvent("data/final/online_lc_merge.txt", "data/final/online_st_merge.txt");
			inter = new InteractDetection("data/final/online_lc_merge.txt", "data/final/online_st_merge.txt");
			inter.loadEf();
			inter.detect("data/final/inter.txt");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Merge vary similar adjacent events.<br>
	 * It's just a pro-process.
	 * @param eventOutput
	 * @param subtopicOutput
	 * @throws Exception
	 */
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
				double sim = simpleSim(e, last);
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
		
		// Sort event by time order.
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
		for(int i = 0; i<subtopic.size(); i++)
		{
			Subtopic s1 = subtopic.elementAt(i);
			for (int j = i+1; j<subtopic.size(); j++)
			{
				Subtopic s2 = subtopic.elementAt(j);
				if (s1.event.firstElement().id > s2.event.firstElement().id)
				{
					Subtopic temp = s1;
					subtopic.set(i, s2);
					subtopic.set(j, temp);
				}
			}
		}
		for (Subtopic st : subtopic)
		{
			st.printSubtopic(writer, null);
		}
		writer.close();
	}
	
	/**
	 * Extract interact
	 * @param output
	 * @throws Exception
	 */
	public void detect(String output) throws Exception
	{
		FileOutputStream stream = new FileOutputStream(output);
		OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
		writer = new PrintWriter(sw);
		
		for (int i = 0; i<subtopic.size(); i++)
		{
			subtopic.elementAt(i).start = event.get(subtopic.elementAt(i).event.firstElement().id).start;
			String end = "2011";
			for (int j = 0; j<subtopic.elementAt(i).event.size(); j++)
			{
				String e = event.get(subtopic.elementAt(i).event.elementAt(j).id).end;
				if (e.compareTo(end) > 0) end = e;
			}
			subtopic.elementAt(i).end = end;
		}
		
		for (int i = 0; i<subtopic.size(); i++)
		{
			Subtopic st = subtopic.elementAt(i);
			String[] sum = st.summary.split("\n");
			for (int j = 0; j<st.event.size(); j++)
			{
				Event e = event.get(st.event.elementAt(j).id);
				for (int ii = 0; ii<subtopic.size(); ii++)
				{
					if (ii == i) continue;
					Subtopic st2 = subtopic.elementAt(ii);
					String[] sum2 = st2.summary.split("\n");
					if (st2.start.compareTo(e.start)<=0 ||
							st2.end.compareTo(e.end) >= 0)
					{
						for (int jj = 0; jj<st2.event.size(); jj++)
						{
							Event e2 = event.get(st2.event.elementAt(jj).id);
							// Only when e2 is in e's time window and e2 is early than e
							if (Article.getDay(e.start, e2.end) <= WindowWidth &&
									e.start.compareTo(e2.start) >= 0)
							{
								double sim = complexSim(e2, e);
								if (sim > Interact)
								{
									writer.println("<interact>");
									writer.println("<from>"+e.id+"</from>");
									writer.println("<to>"+e2.id+"</to>");
									writer.println("</interact>");
									System.out.println(sum[j]+" ---> "+sum2[jj]);
								}
							}
						}
					}
				}
			}
		}
		writer.close();
	}
	
	/**
	 * Load and build term event frequency table
	 */
	public void loadEf()
	{
		eventNum = 0;
		ef = new HashMap<String, Integer>();
		for (int i = 0; i<subtopic.size(); i++)
		{
			Subtopic st = subtopic.elementAt(i);
			for (int j = 0; j<st.event.size(); j++)
			{
				Event e = event.get(st.event.elementAt(j).id);
				eventNum++;
				HashSet<String> temp = new HashSet<String>();
				for (int k = 0; k<e.article.size(); k++)
				{
					String[] terms = e.article.elementAt(k).content.split(" ");
					for (String term : terms)
					{
						if (!temp.contains(term))
						{
							if (ef.containsKey(term))
							{
								Integer tempInt = ef.get(term);
								ef.remove(term);
								ef.put(term, tempInt+1);
							}
							else
							{
								ef.put(term, 1);
							}
							temp.add(term);
						}
						
						if (e.tf.containsKey(term))
						{
							Integer tempInt = e.tf.get(term);
							e.tf.remove(term);
							e.tf.put(term, tempInt+1);
						}
						else
						{
							e.tf.put(term, 1);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Simple similarity for merge
	 * @param e1
	 * @param e2
	 * @return
	 */
	public double simpleSim(Event e1, Event e2)
	{
		HashSet<String> terms1 = new HashSet<String>();
		HashSet<String> terms2 = new HashSet<String>();
		for (int k = 0; k<e1.article.size(); k++)
		{
			String[] temp = e1.article.elementAt(k).content.split(" ");
			for (String term : temp)
			{
				if (swf.isStopWord(term)) continue;
				terms1.add(term);
			}
		}
		for (int k = 0; k<e2.article.size(); k++)
		{
			String[] temp = e2.article.elementAt(k).content.split(" ");
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
		return sim;
	}
	
	/**
	 * Complex similarity using keywords presentation
	 * @param e1
	 * @param e2
	 * @return
	 */
	public double complexSim(Event e1, Event e2)
	{
		double ret = 0;
		
		PriorityQueue<TermScore> pq = this.getKeyword(e1);
		HashMap<String, Double> temp = new HashMap<String, Double>();
		double total = 0;
		String ss = "";
		for (int i = 0; i<EventKeyword && !pq.isEmpty(); i++)
		{
			TermScore ts = pq.poll();
			temp.put(ts.term, ts.score);
			ss += ts.term+" ";
			total += ts.score;
		}
		//System.out.println(ss);
		
		double local = 0;
		for (String term : temp.keySet())
		{
			if (e2.tf.containsKey(term))
			{
				ret += 1;
				local += temp.get(term);
			}
		}
		ret = local / total;
		
		return ret;
	}
	
	public PriorityQueue<TermScore> getKeyword(Event a)
	{
		PriorityQueue<TermScore> pq = new PriorityQueue<TermScore>(EventKeyword, TermScore.cp);
		for (String term : a.tf.keySet())
		{
			double isf;
			isf = (Math.log((double)eventNum/((double)ef.get(term))));
			double tempa = a.tf.get(term) * isf;
			if (term.endsWith("/ns")
				|| term.endsWith("/nr")
				|| term.endsWith("/me")
				|| term.endsWith("/nz")
				|| term.endsWith("/nt"))
			{
				tempa *= EntityBonus;
			}
			
			if (tempa >= 1.00001)
			{
				pq.add(new TermScore(term, tempa));
			}
		}
		return pq;
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
