package Structure;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Article  implements Comparable{

	/**
	 * @param args
	 */
	
	public String title;
	public String url;
	public String source;
	public String content;
	public String time;
	
	Article()
	{
		title = "";
		url = "";
		source = "";
		content = "";
		time = "";
	}
	
	public int compare(Object arg0, Object arg1)
	{
		Article a = (Article)arg0;
		Article b = (Article)arg1;
		return a.time.compareTo(b.time);
	}
	
	public int compareTo(Object arg0)
	{
		Article b = (Article)arg0;
		return time.compareTo(b.time);
	}
	
	public Date getDate() throws ParseException
	{
		SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
		Date d = new Date(time);
		return d;
	}
	
	public static Article readArticle(BufferedReader reader) throws Exception
	{
		String line = reader.readLine();
		if (line == null || line.startsWith("</event>")) return null;
		Article ret = new Article();
		line = reader.readLine();
		ret.title = line.substring(7, line.length()-8);
		line = reader.readLine();
		ret.content = line.substring(9, line.length()-10);
		line = reader.readLine();
		ret.url = line.substring(5, line.length()-6);
		line = reader.readLine();
		ret.time = line.substring(6, line.length()-7);
		line = reader.readLine();
		ret.source = line.substring(8, line.length()-9);
		reader.readLine();
		return ret;
	}
	
	public void printArticle(PrintWriter writer) throws Exception
	{
		writer.println("<doc>");
		writer.println("<title>"+title+"</title>");
		writer.println("<content>"+content+"</content>");
		writer.println("<url>"+url+"</url>");
		writer.println("<time>"+time+"</time>");
		writer.println("<source>"+source+"</source>");
		writer.println("</doc>");
	}
	
	public static Date getDate(String input) throws ParseException
	{
		String s = input;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		if (input.length() > 10) s = input.substring(0,10);
		return format.parse(s);
	}
	
	public static int getDay(Date d1, Date d2)
	{
		return ((int) ((d1.getTime() - d2.getTime()) / (24 * 60 * 60 * 1000)));
	}
	
	public static int getDay(String input1, String input2) throws ParseException
	{
		Date d1 = getDate(input1);
		Date d2 = getDate(input2);
		return ((int) ((d1.getTime() - d2.getTime()) / (24 * 60 * 60 * 1000)));
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
