package TopicThreading;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import DataPrepare.StopWordFilter;
import Structure.ArticleExtend;
import Structure.Event;
import Structure.Subtopic;

public class TFIDF {

	public Vector<Subtopic> subtopic;
	public HashMap<String, Integer> df;
	public int docTotalNum;
	public double InitClusterThreshold = 0.1;
	public StopWordFilter swf;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TFIDF t = new TFIDF();
		t.test("data/final/news_lc.txt", "data/final/news_lc_test.txt");
	}
	
	TFIDF()
	{
		subtopic = new Vector<Subtopic>();
		df = new HashMap<String, Integer>();
		docTotalNum = 0;
		swf = new StopWordFilter();
		swf.load("data/sogou/tf.csv");
	}
	
	public void test(String input, String output)
	{
		try
		{
			int count = 0;
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			Event e;
			Vector<Event> initData = new Vector<Event>();
			while ((e = Event.readEvent(reader)) != null)
			{
				initData.add(e);
				count++;
				if (count >= 100) break;
			}
			init(initData, writer);
			System.out.println("Threading");
			Threading(reader, writer);
			System.out.println("Threading finished");
			for (int j = 0; j<subtopic.size(); j++)
			{
				writer.println(subtopic.elementAt(j).summary+"\n<subtopic>");
				System.out.println(subtopic.elementAt(j).docNum);
			}
			System.out.println("finished");
			reader.close();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void init(Vector<Event> event, PrintWriter writer)
	{
		for (int i = 0; i<event.size(); i++)
		{
			Subtopic st = new Subtopic();
			Event e = event.elementAt(i);
			st.start = e.start;
			st.end = e.end;
			for (int j = 0; j<e.article.size(); j++)
			{
				docTotalNum++;
				ArticleExtend a = e.article.elementAt(j);
				st.docNum++;
				if (st.summary.length() != 0) st.summary += "\n"+a.title; else st.summary += a.title;
				String[] ss = a.content.split(" ");
				HashSet<String> temp = new HashSet<String>();
				for (int k = 0; k<ss.length; k++)
				{
					String term = ss[k];
					if (swf.isStopWord(term)) continue;
					if (st.tf.containsKey(term))
					{
						Integer tempI = st.tf.get(term);
						st.tf.remove(term);
						st.tf.put(term, tempI+1);
					}
					else
					{
						st.tf.put(term, 1);
					}
					if (!temp.contains(term))
					{
						if (st.df.containsKey(term))
						{
							Integer tempI = st.df.get(term);
							st.df.remove(term);
							st.df.put(term, tempI+1);
						}
						else
						{
							st.df.put(term, 1);
						}
						
						if (df.containsKey(term))
						{
							Integer tempI = df.get(term);
							df.remove(term);
							df.put(term, tempI+1);
						}
						else
						{
							df.put(term, 1);
						}
						temp.add(term);
					}
					
				}
			}
			subtopic.add(st);
		}
		System.out.println(docTotalNum);
		initCluster(writer);
	}
	
	public void Threading(BufferedReader reader, PrintWriter writer)
	{
		try
		{
			int count = 0;
			Event e;
			while ((e = Event.readEvent(reader)) != null )
			{
				Subtopic st = new Subtopic();
				st.start = e.start;
				st.end = e.end;
				for (int j = 0; j<e.article.size(); j++)
				{
					docTotalNum++;
					ArticleExtend a = e.article.elementAt(j);
					st.docNum++;
					if (st.summary.length() != 0) st.summary += "\n"+a.title; else st.summary += a.title;
					String[] ss = a.content.split(" ");
					HashSet<String> temp = new HashSet<String>();
					for (int k = 0; k<ss.length; k++)
					{
						String term = ss[k];
						if (swf.isStopWord(term)) continue;
						if (st.tf.containsKey(term))
						{
							Integer tempI = st.tf.get(term);
							st.tf.remove(term);
							st.tf.put(term, tempI+1);
						}
						else
						{
							st.tf.put(term, 1);
						}
						if (!temp.contains(term))
						{
							if (st.df.containsKey(term))
							{
								Integer tempI = st.df.get(term);
								st.df.remove(term);
								st.df.put(term, tempI+1);
							}
							else
							{
								st.df.put(term, 1);
							}
							
							if (df.containsKey(term))
							{
								Integer tempI = df.get(term);
								df.remove(term);
								df.put(term, tempI+1);
							}
							else
							{
								df.put(term, 1);
							}
							temp.add(term);
						}
						
					}
				}
				
				double sim = 0;
				int mergeTo = -1;
				for (int i = 0; i<subtopic.size(); i++)
				{
					double tempsim = similarity(subtopic.elementAt(i), st);
					if (tempsim > InitClusterThreshold && tempsim > sim)
					{
						mergeTo = i;
						sim = tempsim;
					}
				}
				
				if (mergeTo != -1)
				{
					merge(subtopic.elementAt(mergeTo), st);
				}
				else
				{
					subtopic.add(st);
				}
				count++;
				if (count % 100 == 0) System.out.println(count);
				//if (count > 1000) break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void initCluster(PrintWriter writer)
	{
		int i = 1;
		while (i < subtopic.size())
		{
			double sim = 0;
			int mergeTo = -1;
			for (int j = 0; j<i; j++)
			{
				double tempsim = similarity(subtopic.elementAt(i), subtopic.elementAt(j));
				if (tempsim > InitClusterThreshold && tempsim > sim)
				{
					mergeTo = j;
					sim = tempsim;
				}
			}
			
			if (mergeTo != -1)
			{
				merge(subtopic.elementAt(mergeTo), subtopic.elementAt(i));
				subtopic.remove(i);
				i--;
			}
			i++;
		}
//		for (int j = 0; j<subtopic.size(); j++)
//		{
//			writer.println(subtopic.elementAt(j).summary+"\n<subtopic>");
//			System.out.println(subtopic.elementAt(j).docNum);
//		}
//		System.out.println("finished");
	}
	
	public void merge(Subtopic a, Subtopic b)
	{
		for (String term : b.tf.keySet())
		{
			if (a.tf.containsKey(term))
			{
				Integer temp = a.tf.get(term);
				a.tf.remove(term);
				a.tf.put(term, temp+b.tf.get(term));
			}
			else
			{
				a.tf.put(term, b.tf.get(term));
			}
			if (a.df.containsKey(term))
			{
				Integer temp = a.df.get(term);
				a.df.remove(term);
				a.df.put(term, temp+b.df.get(term));
			}
			else
			{
				a.df.put(term, b.df.get(term));
			}
		}
		
		a.docNum += b.docNum;
		a.summary += "\n"+b.summary;
		if (a.start.compareTo(b.start) > 0) a.start = b.start;
		if (a.end.compareTo(b.end) < 0) a.end = b.end;
	}
	
	public double similarity(Subtopic a, Subtopic b) {
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
		HashMap<String, Double> temp = new HashMap<String, Double>();
		for (String term : a.tf.keySet())
		{
			double tempa = a.tf.get(term) * Math.log((double)docTotalNum/((double)df.get(term)+1));
			da += tempa*tempa;
			temp.put(term, tempa);
		}
		for (String term : b.tf.keySet())
		{
			double tempb = b.tf.get(term) * Math.log((double)docTotalNum/((double)df.get(term)+1));
			db += tempb*tempb;
			if (temp.containsKey(term))
			{
				ret += temp.get(term).doubleValue() * tempb;
			}
		}
		ret /= Math.sqrt(da*db);
		return ret;
	}
	
	

}
