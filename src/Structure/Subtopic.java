package Structure;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import System.ActiveEventModule;
import TopicThreading.TFISF;

public class Subtopic {
	
	
	public static int ID = 0;
	public int id;
	public HashMap<String, Integer> tf;
	public HashMap<String, Integer> df;
	public Vector<Integer> eventId;
	public String start;
	public String end;
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
		eventId = new Vector<Integer>();
		eventId.add(e.id);
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
		eventId = new Vector<Integer>();
		eventId = new Vector<Integer>();
		start = "";
		end = "";
		docNum = 0;
		summary = "";
		active = true;
	}
	
	public void printSubtopic(PrintWriter writer, TFISF model)
	{
		writer.println("<subtopic>\n============");
		writer.println(model.extractSubtopicSummary(this));
		writer.println("============");
		//writer.println(id);
		writer.println(eventId.size() + " " + docNum);
		writer.println(start.substring(0,10) + " " + end.substring(0,10));
		Date d = new Date();
		d.setTime(center*3600*1000);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		//writer.println(format.format(d));
		writer.println(summary);
		writer.println("</subtopic>");
		model.docNums.add(docNum);
		model.eventNums.add(eventId.size());
	}
	
	public void addEvent(ActiveEvent e) throws Exception
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
		a.eventId.add(e.id);
		
		if (a.start.compareTo(e.start) > 0) a.start = e.start;
		if (a.end.compareTo(e.end) < 0) a.end = e.end;
		a.center = (a.center*a.docNum + e.center*e.article.size())/(a.docNum + e.article.size());
		a.docNum += e.article.size();
	}
	
	public void removeEvent(ActiveEvent ae, ActiveEventModule aem)
	{
		for (int k = eventId.size()-1; k>=0; k--)
		{
			if (eventId.elementAt(k) == ae.id)
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
				eventId.remove(k);
				break;
			}
		}
		if (eventId.size() > 0)
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
			for (int i = 0; i<eventId.size(); i++)
			{
				ActiveEvent tempae = aem.getEventById(eventId.elementAt(i));
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
