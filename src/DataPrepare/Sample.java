package DataPrepare;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class Sample {
	public static void main(String[] args)
	{
		Sample.sampleSogou("data/sogou/news_split.dat", "data/sogou/news_bg.txt");
		//Sample.sample("data/news.txt", "data/news_sample.txt");
	}
	public static void sample(String input, String output)
	{
		try
		{
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			BufferedReader reader = new BufferedReader(sr);
			String line;
			int RATE = 50;
			int count = 0; 
			while ((line = reader.readLine()) != null)
			{
				String title = reader.readLine();
				String content = reader.readLine();
				String url = reader.readLine();
				String time = reader.readLine();
				String source = reader.readLine();
				reader.readLine();
				count++;
				if (count == RATE)
				{
					writer.println("<doc>");
					writer.println(title);
					writer.println(content);
					writer.println(url);
					writer.println(time);
					writer.println(source);
					writer.println("</doc>");
					count = 0;
				}
				//break;
			}
			
			reader.close();
			writer.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void sampleSogou(String input, String output)
	{
		try
		{
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			BufferedReader reader = new BufferedReader(sr);
			String line;
			int RATE = 10;
			int count = 0; 
			while ((line = reader.readLine()) != null)
			{
				String url = reader.readLine();
				String docN = reader.readLine();
				String title = reader.readLine();
				String content = reader.readLine();
				reader.readLine();
				count++;
				if (count == RATE)
				{
					if (content.length() > 100
							&& !content.contains("根据/p 相关/n 法律/n 规定/n")//yahoo news
							&& !content.contains("引起/v 的/u 法律/n 责任/"))
					{
						writer.println(content.substring(9, content.length()-10));
					}
					count = 0;
				}
				//break;
			}
			
			reader.close();
			writer.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
