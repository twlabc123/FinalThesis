package TopicThreading;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import DataPrepare.StopWordFilter;
import Structure.*;

public class BurstExtraction {
	
	Vector<Event> event;
	Vector<Burst> burst;
	HashMap<String, String> active;
	StopWordFilter swf;
	int WinSize = 7;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BurstExtraction be = new BurstExtraction();
		be.test("data/final/news.txt", "data/final/news_burst_test.txt");
	}
	
	BurstExtraction()
	{
		burst = new Vector<Burst>();
		active = new HashMap<String, String>();
		event = new Vector<Event>();
		swf = new StopWordFilter();
		swf.load("data/sogou/tf.csv");
	}
	
	public void test(String input, String output)
	{
		try
		{
			String START = "2012-01-03 00:00:00";
			String END = "";
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			ArticleExtend a;
			Vector<ArticleExtend> batchData = new Vector<ArticleExtend>();
			while ((a = ArticleExtend.readArticle(reader)) != null)
			{
				Date d = Article.getDate(a.time);
				if (Article.getDay(d, Article.getDate(START)) >= 7) break;
				batchData.add(a);
				END = a.time;
			}
			
			batch(batchData);

			int count = 0;
			while (a != null)
			{
				//System.out.println(batchData.firstElement().time);
				while (batchData.size() > 0 && Article.getDay(Article.getDate(a.time), Article.getDate(batchData.elementAt(0).time)) >= 7)
				{
					batchData.remove(0);
					if (batchData.size() > 0) START = batchData.elementAt(0).time;
				}
				if (batchData.size() == 0) START = a.time;
				System.out.println(START);
				while (a != null)
				{
					Date d = Article.getDate(a.time);
					if (Article.getDay(d, Article.getDate(START)) >= 7) break;
					batchData.add(a);
					END = a.time;
					a = ArticleExtend.readArticle(reader);
				}
				batch(batchData);
				count++;
				if (count >= 1000) break;
			}
			
			
			
			
			
			
			complete(END);
			for (int i = 0; i<burst.size(); i++)
			{
				burst.elementAt(i).printBurst(writer);
			}
			reader.close();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	public void batch(Vector<ArticleExtend> article)
	{
		HashMap<String, Integer> batchdf = new HashMap<String, Integer>();
		for (int i = 0; i<article.size(); i++)
		{
			ArticleExtend a = article.elementAt(i);
			String[] term = a.content.split(" ");
			HashSet<String> termSet = new HashSet<String>();
			for (int j = 0; j<term.length; j++)
			{
				if (swf.isStopWord(term[j])) continue;
				if (!termSet.contains(term))
				{
					if (batchdf.containsKey(term[j]))
					{
						Integer temp = batchdf.get(term[j]);
						batchdf.remove(term[j]);
						batchdf.put(term[j], temp+1);
					}
					else
					{
						batchdf.put(term[j], 1);
					}
					termSet.add(term[j]);
				}
			}
		}
		double Threshold = 0.95;
		HashSet<String> temp = new HashSet<String>();
		for (String term : active.keySet())
		{
			if (!batchdf.keySet().contains(term) || (double)batchdf.get(term)/(double)article.size() < Threshold)
			{
				Burst b = new Burst();
				b.term = term;
				b.start = active.get(term);
				b.end = article.lastElement().time;
				burst.add(b);
				temp.add(term);
				if (batchdf.keySet().contains(term)) batchdf.remove(term);// just for speeding
			}
			else
			{
				batchdf.remove(term);//just for speeding
			}
		}
		for (String term : temp)
		{
			active.remove(term);
		}
		for (String term : batchdf.keySet())
		{
			if ((double)batchdf.get(term)/(double)article.size() >= Threshold)
			{
				active.put(term, article.firstElement().time);
			}
		}
	}
	
	public void complete(String end)
	{
		for (String term : active.keySet())
		{
			Burst b = new Burst();
			b.term = term;
			b.start = active.get(term);
			b.end = end;
			burst.add(b);
		}
	}
	
	
	public void extractBurst()
	{
		
	}

}
