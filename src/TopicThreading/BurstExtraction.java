package TopicThreading;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
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
	HashMap<String, Integer> df;
	int docNum;
	HashMap<String, String> active;
	StopWordFilter swf;
	int WinSize = 7;
	double Threshold = 3;
	int BdfThreshold = 50;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BurstExtraction be = new BurstExtraction();
		be.avgDf("data/final/news.txt");
		be.test("data/final/news.txt", "data/final/news_burst_test.txt");
	}
	
	BurstExtraction()
	{
		burst = new Vector<Burst>();
		active = new HashMap<String, String>();
		event = new Vector<Event>();
		swf = new StopWordFilter();
		swf.load("data/sogou/tf.csv");
		df = new HashMap<String, Integer>();
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
			merge();
			
			for (int i = 0; i<burst.size(); i++)
			{
				for (int j = i+1; j<burst.size(); j++)
				{
					Burst b1 = burst.elementAt(i);
					Burst b2 = burst.elementAt(j);
					if ((b1.start+b1.end).compareTo(b2.start+b2.end) > 0)
					{
						burst.set(i, b2);
						burst.set(j, b1);
					}
				}
			}
			
			
			for (int i = 0; i<burst.size(); i++)
			{
				if (df.get(burst.elementAt(i).term) > 50) burst.elementAt(i).printBurst(writer);
			}
			reader.close();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	public void batch(Vector<ArticleExtend> article) throws Exception
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
		HashSet<String> temp = new HashSet<String>();
		for (String term : active.keySet())
		{
			if (!batchdf.keySet().contains(term) || !isBurst(batchdf.get(term), article.size(), term))
			{
				Burst b = new Burst();
				b.term = term;
				b.start = active.get(term);
				Date d = Article.getDate(article.firstElement().time);
				d.setTime(d.getTime()+0*3600*24);
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				b.end = format.format(d);
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
			if (isBurst(batchdf.get(term), article.size(), term))
			{
				Date d = Article.getDate(article.firstElement().time);
				d.setTime(d.getTime()+0*3600*24);
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				active.put(term, format.format(d));
			}
		}
	}
	
	boolean isBurst(int bdf, int articleSize, String term)
	{
		double local = (double)bdf/(double)articleSize;
		double global = (double)df.get(term)/(double)docNum;
		int DynamicThreshold = (BdfThreshold < articleSize) ? BdfThreshold : articleSize;
		return (local/global >= Threshold && bdf >= DynamicThreshold);
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
	
	public void merge() throws Exception // merge very close burst
	{
		HashMap<String, Integer> lastIndex = new HashMap<String, Integer>();
		int i = 0;
		while (i < burst.size())
		{
			Burst b = burst.elementAt(i);
			String term = b.term;
			if (lastIndex.containsKey(term))
			{
				Burst last = burst.elementAt(lastIndex.get(term));
				int interval = Article.getDay(b.start, last.end);
				if (interval <= 2)
				{
					last.end = b.end;
					burst.set(lastIndex.get(term), last);
					burst.remove(i);
					i--;
				}
				else
				{
					lastIndex.remove(term);
					lastIndex.put(term, i);
				}
			}
			else
			{
				lastIndex.put(term, i);
			}
			i++;
		}
	}
	
	public void avgDf(String input)
	{
		try
		{
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			ArticleExtend a;
			while ((a = ArticleExtend.readArticle(reader)) != null)
			{
				HashSet<String> tempSet = new HashSet<String>();
				String[] ss = a.content.split(" ");
				for (int i = 0; i<ss.length; i++)
				{
					String term = ss[i];
					if (swf.isStopWord(term)) continue;
					if (!tempSet.contains(term))
					{
						if (df.containsKey(term))
						{
							Integer temp = df.get(term);
							df.remove(term);
							df.put(term, temp+1);
						}
						else
						{
							df.put(term, 1);
						}
					}
				}
				docNum++;
				if (docNum % 100 == 0) System.out.println(docNum);
			}
			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
