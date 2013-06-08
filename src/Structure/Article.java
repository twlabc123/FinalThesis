package Structure;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The article(document) representation class
 * @author twl
 *
 */
public class Article  implements Comparable{
	
	/**
	 * The title of the article
	 */
	public String title;
	/**
	 * The URL of the article
	 */
	public String url;
	/**
	 * The source of the article
	 */
	public String source;
	/**
	 * The content of the article
	 */
	public String content;
	/**
	 * The time of the article<br>
	 * In yyyy-MM-dd kk:mm:ss format
	 */
	public String time;
	
	Article()
	{
		title = "";
		url = "";
		source = "";
		content = "";
		time = "";
	}
	
	/**
	 * Compare articles by time order.
	 * @param arg0
	 * @param arg1
	 * @return Positive for a is later than b; 0 for same; Negative for a is earlier than b.
	 */
	public int compare(Object arg0, Object arg1)
	{
		Article a = (Article)arg0;
		Article b = (Article)arg1;
		return a.time.compareTo(b.time);
	}
	
	/**
	 * @see compare
	 */
	public int compareTo(Object arg0)
	{
		Article b = (Article)arg0;
		return time.compareTo(b.time);
	}
	
	/**
	 * Get Date instance from its String format date.
	 * @return
	 * @throws ParseException
	 */
	public Date getDate() throws ParseException
	{
		SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
		Date d = new Date(time);
		return d;
	}
	
	/**
	 * Read an article from the input file.
	 * @param reader
	 * @return
	 * @throws Exception
	 */
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
	
	/**
	 * Print the article to the output file data
	 * @param writer
	 * @throws Exception
	 */
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
	
	/**
	 * Get date from any input String format date(not only the instance's own)
	 * @param input String format time. Should start with "yyyy-MM-dd"
	 * @return
	 * @throws ParseException
	 */
	public static Date getDate(String input) throws ParseException
	{
		String s = input;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		if (input.length() > 10) s = input.substring(0,10);
		return format.parse(s);
	}
	
	/**
	 * Get the interval in days between d1 and d2.(Date version)<br>
	 * If d1 is later, the return value could be negative.
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static int getDay(Date d1, Date d2)
	{
		return ((int) ((d1.getTime() - d2.getTime()) / (24 * 60 * 60 * 1000)));
	}
	
	/**
	 * Get the interval in days between d1 and d2.(String version)<br>
	 * If d1 is later, the return value could be negative.
	 * @param input1
	 * @param input2
	 * @return
	 * @throws ParseException
	 */
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
