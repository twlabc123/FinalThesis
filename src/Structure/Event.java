package Structure;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public class Event {
	
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
	
	public void addArticle(ArticleExtend a) throws ParseException
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
