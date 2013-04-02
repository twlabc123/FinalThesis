package Structure;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Vector;

public class Event {
	
	public Vector<ArticleExtend> article;
	public String start;
	public String end;
	public HashMap<String, Integer> tf;
	public HashMap<String, Integer> df;
	
	public Event()
	{
		article = new Vector<ArticleExtend>();
		start = "";
		end = "";
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
		return ret;
	}

}
