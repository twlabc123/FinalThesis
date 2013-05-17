package Structure;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import DataPrepare.StopWordFilter;

public class Event {
	
	public static int TotalEventNum = 0;
	public int id;
	public Vector<ArticleExtend> article;
	public String start;
	public String end;
	public long center;
	public HashMap<String, Integer> tf;
	public HashMap<String, Integer> df;
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
	
	public static Event readEvent(BufferedReader reader) throws Exception
	{
		String line = reader.readLine();
		if (line == null) return null;
		Event ret = new Event();
		ArticleExtend a;
		while ((a = ArticleExtend.readArticle(reader)) != null)
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
			temp += Article.getDate(article.elementAt(i).time).getTime()/3600/1000;
		}
		center = temp/article.size();
		String[] ss = a.content.split(" ");
		HashSet<String> temp2 = new HashSet<String>();
		for (int k = 0; k<ss.length; k++)
		{
			String term = ss[k];
			if (swf.isStopWord(term)) continue;
			//term = term.substring(0,term.lastIndexOf('/'));
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
	
	public void printEvent(PrintWriter writer) throws Exception
	{
		writer.println("<event>");
		for (int i = 0; i<article.size(); i++)
		{
			article.elementAt(i).printArticle(writer);
		}
		writer.println("</event>");
	}

}
