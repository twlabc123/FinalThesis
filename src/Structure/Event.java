package Structure;

import java.io.BufferedReader;
import java.io.PrintWriter;
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
	
	public Event()
	{
		article = new Vector<ArticleExtend>();
		start = "";
		end = "";
		center = 0;
		tf = new HashMap<String, Integer>();
		df = new HashMap<String, Integer>();
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
