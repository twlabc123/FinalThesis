package TopicThreading;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import DataPrepare.StopWordFilter;
import Structure.ArticleExtend;
import Structure.Burst;
import Structure.Event;
import Structure.Subtopic;

public class BurstSim extends TFISF {

	/**
	 * @param args
	 */
	
	public Vector<Burst> burst;
	public HashMap<String, Burst> active;
	public int burstIndex;
	public double InitClusterThreshold = 0.2;
	public double ThreadingThreshold = InitClusterThreshold;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BurstSim bs = new BurstSim();
		bs.test("data/final/news_lc.txt", "data/final/news_lc_test_burstsim.txt");
	}
	
	public BurstSim()
	{
		subtopic = new Vector<Subtopic>();
		sf = new HashMap<String, Integer>();
		swf = new StopWordFilter();
		swf.load("data/sogou/tf.csv");
		burst = new Vector<Burst>();
		active = new HashMap<String, Burst>();
		burstIndex = 0;
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
			int sum = 0;
			for (int j = 0; j<subtopic.size(); j++)
			{
				Subtopic st = subtopic.elementAt(j);
				if (st.docNum < 50) continue;
				sum++;
				writer.println("<subtopic>");
				writer.println(extractSubtopicSummary(st));
				writer.println(st.docNum + " " + st.start.substring(0,10) + " " + st.end.substring(0,10));
				writer.println(st.summary);
				writer.println("</subtopic>");
				System.out.println(st.docNum);
			}
			System.out.println("Total subtopics : "+subtopic.size());
			System.out.println("Big subtopics : "+sum);
			System.out.println("finished");
			reader.close();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void init(Vector<Event> event, PrintWriter writer) throws Exception
	{
		this.loadBurst("data/final/news_burst_test.txt");
		for (int i = 0; i<event.size(); i++)
		{
			Subtopic st = new Subtopic();
			Event e = event.elementAt(i);
			st.start = e.start;
			st.end = e.end;
			while (burstIndex < burst.size() && burst.elementAt(burstIndex).start.compareTo(st.end.substring(0,10))<=0)
			{
				active.put(burst.elementAt(burstIndex).term, burst.elementAt(burstIndex));
				burstIndex++;
			}
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
	
	public void initCluster(PrintWriter writer) throws Exception
	{
		int i = 1;
		while (i < subtopic.size())
		{
			double sim = 0;
			int mergeTo = -1;
			for (int j = 0; j<i; j++)
			{
				double tempsim = similarity(subtopic.elementAt(j), subtopic.elementAt(i));
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
				HashSet<String> remove = new HashSet<String>();
				for (String term : active.keySet())
				{
					if (active.get(term).end.compareTo(st.start) < 0)
					{
						remove.add(term);
					}
				}
				for (String term : remove)
				{
					active.remove(term);
				}
				while (burstIndex < burst.size() && burst.elementAt(burstIndex).start.compareTo(st.end.substring(0,10))<=0)
				{
					active.put(burst.elementAt(burstIndex).term, burst.elementAt(burstIndex));
					burstIndex++;
				}
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
	
	public void loadBurst(String input)
	{
		try {
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			String line;
			while ((line = reader.readLine()) != null)
			{
				Burst b = new Burst();
				b.term = line.split(" ")[0];
				b.start = line.split(" ")[1];
				b.end = line.split(" ")[2];
				burst.add(b);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isActive(String term, String start, String end)
	{
		if (!active.containsKey(term)) return false;
		if (start.compareTo(active.get(term).end) > 0) return false;
		if (end.compareTo(active.get(term).start) < 0) return false;
		return true;
	}
	
	public double similarity(Subtopic a, Subtopic b) throws Exception {// a should be the former subtopic and b should be the newer one.
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
		for (String term : active.keySet())
		{
			double tempa = 0;
			double tempb = 0;
			if (a.tf.containsKey(term) && isActive(term, a.start, a.end))
			{
				//tempa = a.tf.get(term) * Math.log((double)subtopic.size()/((double)sf.get(term)));//tf-isf
				tempa = 1.0;
			}
			if (b.tf.containsKey(term) && isActive(term, b.start, b.end))
			{
				//tempb = b.tf.get(term) * Math.log((double)subtopic.size()/((double)sf.get(term)));// tf-isf
				tempb = 1.0;
			}
			da += tempa;
			db += tempb;
			ret += tempa*tempb;
		}
		if (da*db != 0)	ret /= Math.sqrt(da*db);
		return ret;
	}
	
	

}
