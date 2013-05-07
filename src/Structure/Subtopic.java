package Structure;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import TopicThreading.TFISF;

public class Subtopic {
	
	public HashMap<String, Integer> tf;
	public HashMap<String, Integer> df;
	public String start;
	public String end;
	public long center;
	public int docNum;
	public int eventNum;
	public String summary;
	public boolean active;
	
	public Subtopic()
	{
		tf = new HashMap<String, Integer>();
		df = new HashMap<String, Integer>();
		start = "";
		end = "";
		summary = "";
		docNum = 0;
		eventNum = 0;
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
