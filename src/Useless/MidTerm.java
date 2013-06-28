package Useless;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import Structure.Article;
import Structure.Subtopic;
/**
 * The function of this class is to get statistics from event cluster results.<br>
 * Not important.
 * @author twl
 *
 */
public class MidTerm {
	
	public int stNum;
	public int bigStNum;
	public int[] bound = {5,20,50,100,500,1000,2000,4000};
	public int[] span = {5,10,30,90,180,270};
	public Vector<Subtopic> subtopic;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		File dir = new File("data/mid");
//		File[] files = dir.listFiles();
//		for (int i = 0; i<files.length; i++)
//		{
//			if (files[i].getName().startsWith("stat")) continue;
//			System.out.println(files[i].getName());
//			MidTerm mt = new MidTerm();
//			String input = files[i].getName();
//			mt.run("data/mid/"+input, "data/mid/stat_"+input);
//		}
		MidTerm mt = new MidTerm();
		mt.run("data/final/news_lc_test_tfisf_time_merge_2.txt", "data/stat/stat_news_lc_test_tfisf_time_merge_2.txt");
	}
	
	public MidTerm()
	{
		stNum = 0;
		bigStNum = 0;
		subtopic = new Vector<Subtopic>();
	}
	
	public void run(String input, String output)
	{
		try
		{
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.startsWith("<subtopic>"))
				{
					reader.readLine();
					Subtopic st = new Subtopic();
					line = reader.readLine();
					while (!line.startsWith("======"))
					{
						if (st.summary.length() != 0) st.summary += "\n";
						st.summary += line;
						line = reader.readLine();
					}
					line = reader.readLine();
					//st.eventId = new Integer.parseInt(line.split(" ")[0]);
					st.docNum = Integer.parseInt(line.split(" ")[1]);
					st.start = line.split(" ")[2];
					st.end = line.split(" ")[3];
					line = reader.readLine();
					st.center = Integer.parseInt(line.split(" ")[0]);
					line = reader.readLine();
					while (!line.startsWith("</subtopic>"))
					{
						line = reader.readLine();
					}
					subtopic.add(st);
				}
				else
				{
					stNum = Integer.parseInt(line.substring(18));
					line = reader.readLine();
					bigStNum = Integer.parseInt(line.substring(16));
				}
			}
			System.out.println("Load finish");
			
			writer.println("total "+stNum);
			writer.println("big "+bigStNum);
			
			Vector<Integer> scale = new Vector<Integer>();
			for (int i = 0; i<=bound.length; i++)
			{
				scale.add(0);
			}
			for (int i = 0; i<subtopic.size(); i++)
			{
				Integer temp = scale.elementAt(scaleIndex(subtopic.elementAt(i).docNum)) + 1;
				scale.set(scaleIndex(subtopic.elementAt(i).docNum), temp);
			}
			for (int i = 0; i<scale.size(); i++)
			{
				if (i<bound.length)
				{
					writer.print(bound[i]);
				}
				else
				{
					writer.print("larger");
				}
				writer.println(" "+scale.elementAt(i));
			}
			
			Date d = Article.getDate("2012-01-01 01:00:00");
			int base = (int)(d.getTime()/24/3600/1000);
			Vector<Integer> time = new Vector<Integer>();
			for (int i = 0; i<365; i++)
			{
				time.add(0);
			}
			for (int i = 0; i<subtopic.size(); i++)
			{
				Subtopic st = subtopic.elementAt(i);
				int start = (int)(Article.getDate(st.start).getTime()/24/3600/1000) - base;
				int end = (int)(Article.getDate(st.end).getTime()/24/3600/1000) - base;
				for (int j = start; j<=end; j++)
				{
					Integer temp = time.elementAt(j) + 1;
					time.set(j, temp);
				}
				if (end - start >= 180) System.out.println(st.summary+"\n");
			}
			for (int i = 0; i<365; i++)
			{
				Date temp = new Date();
				temp.setTime(((long)(i+base+1))*24*3600*1000);
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				String date = format.format(temp);
				writer.println(date+" "+time.elementAt(i));
			}
			
			Vector<Integer> spanStat = new Vector<Integer>();
			for (int i = 0; i<=span.length; i++)
			{
				spanStat.add(0);
			}
			for (int i = 0; i<subtopic.size(); i++)
			{
				Subtopic st = subtopic.elementAt(i);
				int start = (int)(Article.getDate(st.start).getTime()/24/3600/1000);
				int end = (int)(Article.getDate(st.end).getTime()/24/3600/1000);
				int interval = end - start + 1;
				Integer temp = spanStat.elementAt(spanIndex(interval)) + 1;
				spanStat.set(spanIndex(interval), temp);
			}
			for (int i = 0; i<spanStat.size(); i++)
			{
				if (i<span.length)
				{
					writer.print(span[i]);
				}
				else
				{
					writer.print("larger");
				}
				writer.println(" "+spanStat.elementAt(i));
			}
			
			reader.close();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int scaleIndex(int n)
	{
		for (int i = 0; i<bound.length; i++)
		{
			if (n <= bound[i]) return i;
		}
		return bound.length;
	}
	
	public int spanIndex(int n)
	{
		for (int i = 0; i<span.length; i++)
		{
			if (n <= span[i]) return i;
		}
		return span.length;
	}

}
