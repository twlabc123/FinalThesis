package DataPrepare;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Merge {
	
	public static void main(String args[])
	{
		Merge.Merge2File("data/news_1.txt", "data/news_2.txt", "data/news_lite.txt");
	}
	
	static void Merge2File(String input1, String input2, String output)
	{
		try
		{
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			FileInputStream istream = new FileInputStream(input1);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			BufferedReader reader = new BufferedReader(sr);
			String line;
			while ((line = reader.readLine()) != null)
			{
				String title = reader.readLine().substring(6);	
				String url = reader.readLine().substring(4);
				String source = reader.readLine().substring(7);
				String time = reader.readLine().substring(5);
				String content = reader.readLine().substring(5);
				String type = reader.readLine().substring(14);
				reader.readLine();
				if (type.equals("news") && !source.contains("娱乐"))
				{
					writer.println("<doc>");
					title = title.replaceAll(" ", "");
					title = title.replaceAll("　", "");
					writer.println("<title>"+title+"</title>");
					content = content.replaceAll(" ", "");
					content = content.replaceAll("　", "");
					writer.println("<content>"+content+"</content>");
					writer.println("<url>"+url+"</url>");
					writer.println("<time>"+Merge.dateFormat(time)+"</time>");
					writer.println("<source>"+source+"</source>");
					writer.println("</doc>");
					//break;
				}
			}
			
			reader.close();
			istream = new FileInputStream(input2);
			sr = new InputStreamReader(istream, "utf-8");
			reader = new BufferedReader(sr);
			reader.readLine();
			while ((line = reader.readLine()) != null)
			{
				String title = reader.readLine();
				title = title.replaceAll(" ", "");
				title = title.replaceAll("　", "");
				String content = reader.readLine();
				content = content.replaceAll(" ", "");
				content = content.replaceAll("　", "");
				String type = reader.readLine();
				String url = reader.readLine();
				String time = reader.readLine();
				String source = reader.readLine();
				reader.readLine();
				if (type.equals("<type>1</type>")) continue;
				writer.println("<doc>");
				writer.println(title);
				writer.println(content);
				writer.println(url);
				writer.println(time);
				writer.println(source);
				writer.println("</doc>");
			}
			writer.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	static String dateFormat(String date)
	{
		String[] ss = date.split(" ");
		String ret = ss[5]+"-";
		if (ss[1].equals("Jan"))
		{
			ret += "01";
		}
		else if (ss[1].equals("Feb"))
		{
			ret += "02";
		}
		else if (ss[1].equals("Mar"))
		{
			ret += "03";
		}
		else if (ss[1].equals("Apr"))
		{
			ret += "04";
		}
		else if (ss[1].equals("May"))
		{
			ret += "05";
		}
		else if (ss[1].equals("Jun"))
		{
			ret += "06";
		}
		else if (ss[1].equals("Jul"))
		{
			ret += "07";
		}
		else if (ss[1].equals("Aug"))
		{
			ret += "08";
		}
		else if (ss[1].equals("Sep"))
		{
			ret += "09";
		}
		else if (ss[1].equals("Oct"))
		{
			ret += "10";
		}
		else if (ss[1].equals("Nov"))
		{
			ret += "11";
		}
		else if (ss[1].equals("Dec"))
		{
			ret += "12";
		}
		else
		{
			System.out.println(ss[1]);
		}
		ret += "-";
		if (ss[2].length() < 2) ret += "0";
		ret += ss[2]+" "+ss[3];
		return ret;
	}

}
