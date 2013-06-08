package Structure;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import DataPrepare.StopWordFilter;

/**
 * The event representation class
 * @author twl
 *
 */
public class Event {
	
	/**
	 * Current total number of events.
	 */
	public static int TotalEventNum = 0;
	/**
	 * Unique id of the event
	 */
	public int id;
	/**
	 * Articles in this event
	 */
	public Vector<ArticleExtend> article;
	/**
	 * The start time of the event
	 */
	public String start;
	/**
	 * The end time of the event
	 */
	public String end;
	/**
	 * The time center of the event. (in hours)
	 */
	public long center;
	/**
	 * tf table of the event
	 */
	public HashMap<String, Integer> tf;
	/**
	 * df table of the event
	 */
	public HashMap<String, Integer> df;
	/**
	 * The flag of whether the event has new documents added in.<br>
	 * This is useful when the event is in active event set, because new documents
	 * could be added into during processing batches of the documents.
	 */
	public boolean hasNewDoc;
	
	public Event()
	{
		article = new Vector<ArticleExtend>();
		start = "";
		end = "";
		center = 0;
		tf = new HashMap<String, Integer>();
		df = new HashMap<String, Integer>();
		hasNewDoc = true;
	}
	
	@SuppressWarnings("unchecked")
	public Event(Event e)
	{
		article = e.article;
		start = e.start;
		end = e.end;
		center = e.center;
		tf = (HashMap<String, Integer>) e.tf.clone();
		df = (HashMap<String, Integer>) e.df.clone();
		hasNewDoc = e.hasNewDoc;
	}
	
	/**
	 * Read an event from input file
	 * @param reader
	 * @return
	 * @throws Exception
	 */
	public static Event readEvent(BufferedReader reader) throws Exception
	{
		String line = reader.readLine();
		if (line == null) return null;
		Event ret = new Event();
		line = reader.readLine();
		ret.id = Integer.parseInt(line.substring(4, line.length()-5));
		line = reader.readLine();
		ret.start = line.substring(7, line.length()-8);
		line = reader.readLine();
		ret.end = line.substring(5, line.length()-6);
		ArticleExtend a;
		while ((a = ArticleExtend.readArticle(reader)) != null)
		{
			ret.article.add(a);
		}
		
		return ret;
	}
	
	/**
	 * Build an event from articles
	 * @param doc
	 * @return
	 * @throws ParseException
	 */
	public static Event getEventFromArticle(Vector<ArticleExtend> doc) throws ParseException
	{
		Event ret = new Event();
		for (ArticleExtend a : doc)
		{
			ret.article.add(a);
		}
		ret.start = ret.article.firstElement().time;
		ret.end = ret.article.lastElement().time;
		long temp = 0;
		for (int i = 0; i<ret.article.size(); i++)
		{
			temp += Article.getDate(ret.article.elementAt(i).time).getTime()/3600/1000;
		}
		ret.center = temp/ret.article.size();
		return ret;
	}
	
	/**
	 * Add an article to the event
	 * @param a Article
	 * @param swf Stop word filter
	 * @throws ParseException
	 */
	public void addArticle(ArticleExtend a, StopWordFilter swf) throws ParseException
	{
		article.add(a);
		if (article.size() == 1)
		{
			start = end = a.time;
		}
		else if (a.time.compareTo(end) > 0)
		{
			end = a.time;
		}
		long temp = 0;
		for (int i = 0; i<article.size(); i++)
		{
			// Calculate time center
			temp += Article.getDate(article.elementAt(i).time).getTime()/3600/1000;
		}
		center = temp/article.size();
		String[] ss = a.content.split(" ");
		HashSet<String> temp2 = new HashSet<String>();
		// Update tf,df table
		for (int k = 0; k<ss.length; k++)
		{
			String term = ss[k];
			if (swf.isStopWord(term)) continue;
			if (tf.containsKey(term))
			{
				Integer tempI = tf.get(term);
				tf.remove(term);
				tf.put(term, tempI+1);
			}
			else
			{
				tf.put(term, 1);
			}
			
			if (!temp2.contains(term))
			{
				if (df.containsKey(term))
				{
					Integer tempI = df.get(term);
					df.remove(term);
					df.put(term, tempI+1);
				}
				else
				{
					df.put(term, 1);
				}
				temp2.add(term);
			}
		}
		
		hasNewDoc = true;
	}
	
	/**
	 * Print the event to the output file
	 * @param writer
	 * @throws Exception
	 */
	public void printEvent(PrintWriter writer) throws Exception
	{
		writer.println("<event>");
		writer.println("<id>"+id+"</id>");
		writer.println("<start>"+start+"</start>");
		writer.println("<end>"+end+"</end>");
		for (int i = 0; i<article.size(); i++)
		{
			article.elementAt(i).printArticle(writer);
		}
		writer.println("</event>");
	}

}
