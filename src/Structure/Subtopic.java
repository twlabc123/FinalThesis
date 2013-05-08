package Structure;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import TopicThreading.TFISF;

public class Subtopic {
	
	public HashMap<String, Integer> tf;
	public HashMap<String, Integer> df;
	public Vector<Integer> eventId;
	public String start;
	public String end;
	public long center;
	public int docNum;
	public int eventNum;
	public String summary;
	public boolean active;
	
	public Subtopic(ActiveEvent e)
	{
		tf = new HashMap<String, Integer>();
		df = new HashMap<String, Integer>();
		eventId = new Vector<Integer>();
		eventId.add(e.id);
		this.start = e.start;
		this.end = e.end;
		this.center = e.center;
		this.docNum = e.article.size();
		this.eventNum = 1;
		this.summary = "";
		this.active = true;
	}
	
	public Subtopic()
	{
		tf = new HashMap<String, Integer>();
		df = new HashMap<String, Integer>();
		eventId = new Vector<Integer>();
		eventId = new Vector<Integer>();
		start = "";
		end = "";
		docNum = 0;
		eventNum = 0;
		summary = "";
		active = true;
	}
	
	public void printSubtopic(PrintWriter writer, TFISF model)
	{
		writer.println("<subtopic>\n============");
		writer.println(model.extractSubtopicSummary(this));
		writer.println("============");
		writer.println(eventNum + " " + docNum + " " + start.substring(0,10) + " " + end.substring(0,10));
		Date d = new Date();
		d.setTime(center*3600*1000);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		writer.println(center + " " + format.format(d));
		writer.println(summary);
		writer.println("</subtopic>");
		model.docNums.add(docNum);
		model.eventNums.add(eventNum);
	}

}
