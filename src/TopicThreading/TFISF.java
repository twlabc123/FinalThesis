package TopicThreading;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class TFISF {

	public Vector<Subtopic> subtopic;
	public HashMap<String, Integer> sf;
	//public int stTotalNum;
	public double InitClusterThreshold = 0.1;
	public double ThreadingThreshold = InitClusterThreshold;
	public StopWordFilter swf;
	public int TestSample = 7000; // just for test
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TFISF t = new TFISF();
		t.test("data/final/news_lc.txt", "data/final/news_lc_test_tdisf.txt");
	}
	
	TFISF()
	{
		subtopic = new Vector<Subtopic>();
		sf = new HashMap<String, Integer>();
		//stTotalNum = 0;
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
			System.out.println(sf.get("钓鱼岛/ns")+"/"+subtopic.size());
			//System.out.println(sf.get("游行/n")+"/"+subtopic.size());
			//System.out.println(sf.get("游行/v")+"/"+subtopic.size());
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
	
	public void init(Vector<Event> event, PrintWriter writer) throws IOException
	{
		for (int i = 0; i<event.size(); i++)
		{
			Subtopic st = new Subtopic();
			Event e = event.elementAt(i);
			st.start = e.start;
			st.end = e.end;
			//stTotalNum++;
			HashSet<String> temp = new HashSet<String>();
			for (int j = 0; j<e.article.size(); j++)
			{
				ArticleExtend a = e.article.elementAt(j);
				st.docNum++;
				if (st.summary.length() != 0) st.summary += "\n"+a.title; else st.summary += a.title;
				String[] ss = a.content.split(" ");
				HashSet<String> temp2 = new HashSet<String>();
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
						if (sf.containsKey(term))
						{
							Integer tempI = sf.get(term);
							sf.remove(term);
							sf.put(term, tempI+1);
						}
						else
						{
							sf.put(term, 1);
						}
						temp.add(term);
					}
					
					if (!temp2.contains(term))
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
						temp2.add(term);
					}
					
				}
			}
			subtopic.add(st);
		}
		initCluster(writer);
	}
	
	public void initCluster(PrintWriter writer) throws IOException
	{
		int i = 1;
		while (i < subtopic.size())
		{
			double sim = 0;
			int mergeTo = -1;
			for (int j = 0; j<i; j++)
			{
				double tempsim = similarity(subtopic.elementAt(i), subtopic.elementAt(j));
				writer.println(subtopic.elementAt(i).summary + " " + subtopic.elementAt(j).summary + " " +tempsim);
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
				//stTotalNum++;
				HashSet<String> temp = new HashSet<String>();
				for (int j = 0; j<e.article.size(); j++)
				{
					
					ArticleExtend a = e.article.elementAt(j);
					st.docNum++;
					if (st.summary.length() != 0) st.summary += "\n"+a.title; else st.summary += a.title;
					String[] ss = a.content.split(" ");
					HashSet<String> temp2 = new HashSet<String>();
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
							if (sf.containsKey(term))
							{
								Integer tempI = sf.get(term);
								sf.remove(term);
								sf.put(term, tempI+1);
							}
							else
							{
								sf.put(term, 1);
							}
							temp.add(term);
						}
						
						if (!temp2.contains(term))
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
							temp2.add(term);
						}
						
					}
				}
				
				double sim = 0;
				int mergeTo = -1;
				for (int i = 0; i<subtopic.size(); i++)
				{
					double tempsim = similarity(subtopic.elementAt(i), st);
					if (tempsim > ThreadingThreshold && tempsim > sim)
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
				if (count > TestSample) break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	public void merge(Subtopic a, Subtopic b) // merge b into a, but not responsible to delete/remove b. User should delete/remove b in calling function
	{
		for (String term : b.tf.keySet())
		{
			if (sf.containsKey(term))
			{
				if (a.tf.containsKey(term))
				{
					Integer temp = sf.get(term);
					sf.remove(term);
					sf.put(term, temp-1);
				}
			}
			
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
	
	public double similarity(Subtopic a, Subtopic b) throws IOException {
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
		HashMap<String, Double> temp = new HashMap<String, Double>();
		for (String term : a.tf.keySet())
		{
			double tempa = a.tf.get(term) * Math.log((double)subtopic.size()/((double)sf.get(term)));
			da += tempa*tempa;
			temp.put(term, tempa);
		}
		for (String term : b.tf.keySet())
		{
			double tempb = b.tf.get(term) * Math.log((double)subtopic.size()/((double)sf.get(term)));
			db += tempb*tempb;
			if (temp.containsKey(term))
			{
				ret += temp.get(term).doubleValue() * tempb;
			}
//			System.out.println(Math.log((double)subtopic.size()/((double)sf.get(term))+1));
//			if (sf.get(term) == 0)
//			{
//				System.out.println(term);
//				System.in.read();
//			}
		}
		
		ret /= Math.sqrt(da*db);
		return ret;
	}
	
	

}
