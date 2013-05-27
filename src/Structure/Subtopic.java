package Structure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;

import System.ActiveEventModule;
import System.ExtractSummary;
import TopicThreading.MultiView;
import TopicThreading.TFISF;

public class Subtopic {
	
	public static int ID = 0;
	public int id;
	public HashMap<String, Integer> tf;
	public HashMap<String, Integer> df;
	public Vector<EventEdge> event;
	public String start;
	public String end;
	public String keyword;
	public long center;
	public int docNum;
	public String summary;
	public boolean active;
	
	public Subtopic(ActiveEvent e)
	{
		id = ID;
		ID++;
		tf = new HashMap<String, Integer>();
		tf.putAll(e.tf);
		df = new HashMap<String, Integer>();
		df.putAll(e.df);
		event = new Vector<EventEdge>();
		event.add(new EventEdge(e.id, 1));
		this.start = e.start;
		this.end = e.end;
		this.center = e.center;
		this.docNum = e.article.size();
		this.summary = "";
		this.active = true;
	}
	
	public Subtopic()
	{
		id = ID;
		ID++;
		tf = new HashMap<String, Integer>();
		df = new HashMap<String, Integer>();
		event = new Vector<EventEdge>();
		start = "";
		end = "";
		docNum = 0;
		summary = "";
		active = true;
	}
	
	public static Subtopic readSubtopic(BufferedReader reader) throws IOException
	{
		Subtopic ret = new Subtopic();
		String line = reader.readLine();
		if (line == null) return null;
		line = reader.readLine();
		ret.id = Integer.parseInt(line.substring(4, line.length()-5));
		line = reader.readLine();
		ret.keyword = line.substring(10, line.length()-11);
		line = reader.readLine();
		int eventNum = Integer.parseInt(line.substring(10, line.length()-11));
		for (int i = 0; i<eventNum; i++)
		{
			line = reader.readLine();
			line = reader.readLine();
			int id = Integer.parseInt(line.substring(4,line.length()-5));
			line = reader.readLine();
			double value = Double.parseDouble(line.substring(7,line.length()-8));
			ret.event.add(new EventEdge(id, value));
			line = reader.readLine();
			if (ret.summary.length() != 0) ret.summary += "\n";
			ret.summary += line.substring(9, line.length()-10);
			line = reader.readLine();
		}
		line = reader.readLine();
		return ret;
	}
	
	public void printSubtopic(PrintWriter writer, TFISF model) throws Exception
	{
		writer.println("<subtopic>");
		writer.println("<id>"+id+"</id>");
		if (model != null) writer.println("<keywords>"+((MultiView)model).keyWords(this)+"</keywords>");
		else writer.println("<keywords>"+this.keyword+"</keywords>");
		writer.println("<eventnum>"+event.size()+"</eventnum>");
		String[] summaries = summary.split("\n");
		for (int i = 0; i<event.size(); i++)
		{
			writer.println("<event>");
			writer.println("<id>"+event.elementAt(i).id+"</id>");
			writer.println("<value>"+event.elementAt(i).value+"</value>");
			String sum = "";
			for (String temp : summaries[i].split(" "))
			{
				if (temp.contains("/"))
				{
					sum += temp.substring(0, temp.lastIndexOf("/"));
				}
				else
				{
					sum += temp;
				}
			}
			writer.println("<summary>"+sum+"</summary>");
			writer.println("</event>");
		}
		writer.println("</subtopic>");
		writer.flush();
		
	}
	
	public void addEvent(ActiveEvent e, double sim) throws Exception
	{
		Subtopic a = this;
		for (String term : e.tf.keySet())
		{
			if (a.tf.containsKey(term))
			{
				Integer temp = a.tf.get(term);
				a.tf.remove(term);
				a.tf.put(term, temp+e.tf.get(term));
			}
			else
			{
				a.tf.put(term, e.tf.get(term));
			}
			
			if (a.df.containsKey(term))
			{
				Integer temp = a.df.get(term);
				a.df.remove(term);
				a.df.put(term, temp+e.df.get(term));
			}
			else
			{
				a.df.put(term, e.df.get(term));
			}
		}
		a.event.add(new EventEdge(e.id, sim));
		
		if (a.start.compareTo(e.start) > 0) a.start = e.start;
		if (a.end.compareTo(e.end) < 0) a.end = e.end;
		a.center = (a.center*a.docNum + e.center*e.article.size())/(a.docNum + e.article.size());
		a.docNum += e.article.size();
	}
	
	public void removeEvent(ActiveEvent ae, ActiveEventModule aem)
	{
		for (int k = event.size()-1; k>=0; k--)
		{
			if (event.elementAt(k).id == ae.id)
			{
				for (String term : ae.tf.keySet())
				{
					if (tf.containsKey(term))
					{
						Integer temp = tf.get(term);
						tf.remove(term);
						temp -= ae.tf.get(term);
						if (temp > 0) tf.put(term, temp);
					}
				}
				for (String term : ae.df.keySet())
				{
					if (df.containsKey(term))
					{
						Integer temp = df.get(term);
						df.remove(term);
						temp -= ae.df.get(term);
						if (temp > 0) df.put(term, temp);
					}
				}
				event.remove(k);
				break;
			}
		}
		if (event.size() > 0)
		{
			boolean resetStart = start.equals(ae.start);
			if (resetStart) start = "5012-01-01";
			boolean resetEnd = end.equals(ae.end);
			if (resetEnd) end = "0000-01-01";
			long temp = center * docNum;
			temp -= ae.center * ae.article.size();
			docNum -= ae.article.size();
			temp /= docNum;
			center = temp;
			for (int i = 0; i<event.size(); i++)
			{
				ActiveEvent tempae = aem.getEventById(event.elementAt(i).id);
				if (tempae == null) continue;
				if (resetStart)
				{
					if (tempae.start.compareTo(start) < 0) start = tempae.start;
				}
				if (resetEnd)
				{
					if (tempae.end.compareTo(end) > 0) end = tempae.end;
				}
			}
		}
		
	}

}
