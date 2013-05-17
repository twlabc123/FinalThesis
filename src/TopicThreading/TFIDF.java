package TopicThreading;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import DataPrepare.StopWordFilter;
import Structure.ArticleExtend;
import Structure.Event;
import Structure.Subtopic;

public class TFIDF extends TFISF {

	public Vector<Subtopic> subtopic;
	public HashMap<String, Integer> df;
	public int docTotalNum;
	public double InitClusterThreshold = 0.1;
	public StopWordFilter swf;
	public int SummaryTitleNum = 3;
	
	
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
		//swf.load("data/sogou/tf.csv");
		swf.load("data/stopwords.txt");
	}
	
	public void test(String input, String output)
	{
		try
		{
			int count = 0;
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			writer = new PrintWriter(sw);
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
			init(initData);
			System.out.println("Threading");
			Threading(reader);
			System.out.println("Threading finished");
			for (int j = 0; j<subtopic.size(); j++)
			{
				Subtopic st = subtopic.elementAt(j);
				if (st.docNum >= 20) bigStNum++;
				st.printSubtopic(writer, this);
				System.out.println(st.docNum);
			}
			writer.println("Total subtopics : "+stNum);
			writer.println("Big subtopics : "+bigStNum);
			int biggest = -1;
			for (int i = 0; i<docNums.size(); i++)
			{
				System.out.println(docNums.elementAt(i));
				if (docNums.elementAt(i) > biggest) biggest = docNums.elementAt(i);
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
	
	public void init(Vector<Event> event)
	{
		for (int i = 0; i<event.size(); i++)
		{
			Subtopic st = new Subtopic();
			Event e = event.elementAt(i);
			st.start = e.start;
			st.end = e.end;
			st.center = e.center;
			for (int j = 0; j<e.article.size(); j++)
			{
				docTotalNum++;
				ArticleExtend a = e.article.elementAt(j);
				st.docNum++;
				if (st.summary.length() != 0) st.summary += "\n";
				st.summary += a.time.substring(0,10) + " " + a.title;
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
		//System.out.println(docTotalNum);
		initCluster();
	}
	
	public void Threading(BufferedReader reader)
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
				st.center = e.center;
				for (int j = 0; j<e.article.size(); j++)
				{
					docTotalNum++;
					ArticleExtend a = e.article.elementAt(j);
					st.docNum++;
					if (st.summary.length() != 0) st.summary += "\n";
					st.summary += a.time.substring(0,10) + " " + a.title;
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
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void initCluster()
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
		
		if (a.start.compareTo(b.start) > 0) a.start = b.start;
		if (a.end.compareTo(b.end) < 0) a.end = b.end;
		a.center = (a.center*a.docNum + b.center*b.docNum)/(a.docNum + b.docNum);
		a.docNum += b.docNum;
		a.summary += "\n"+b.summary;
	}
	
	public double similarity(Subtopic a, Subtopic b) {
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
		HashMap<String, Double> temp = new HashMap<String, Double>();
		for (String term : a.tf.keySet())
		{
			double tempa = a.tf.get(term) * Math.log((double)docTotalNum/((double)df.get(term)));
			da += tempa*tempa;
			temp.put(term, tempa);
		}
		for (String term : b.tf.keySet())
		{
			double tempb = b.tf.get(term) * Math.log((double)docTotalNum/((double)df.get(term)));
			db += tempb*tempb;
			if (temp.containsKey(term))
			{
				ret += temp.get(term).doubleValue() * tempb;
			}
		}
		ret /= Math.sqrt(da*db);
		return ret;
	}
	
	public String extractSubtopicSummary(Subtopic st)
	{
		String[] ss = st.summary.split("\n");
		String ret = "";
		Vector<String> summary = new Vector<String>();
		Vector<Double> value = new Vector<Double>();
		for (String s : ss)
		{
			s = s.substring(11);
			double temp = 0;
			String[] terms = s.split(" ");
			//s = "";
			for (String term : terms)
			{
				if (st.tf.containsKey(term))
				{
					temp += st.tf.get(term) * Math.log((double)docTotalNum/((double)df.get(term)));
				}
				//s += term.substring(0,term.lastIndexOf('/'));
			}
			if (terms.length != 0) temp /= terms.length;
			int i = 0;
			for (i = 0; i<summary.size(); i++)
			{
				if (simTitle(s,summary.elementAt(i)))
				{
					i = -1;
					break;
				}
				if (temp > value.elementAt(i)) break;
				
			}
			if (i == -1) continue;
			if (i >= summary.size())
			{
				if (summary.size() < SummaryTitleNum)
				{
					summary.add(s);
					value.add(temp);
				}
			}
			else
			{
				summary.insertElementAt(s, i);
				value.insertElementAt(temp, i);
				if (summary.size() > SummaryTitleNum)
				{
					summary.remove(summary.size()-1);
					value.remove(value.size()-1);
				}
			}
		}

		for (int i = 0; i<summary.size(); i++)
		{
			if (i != 0) ret += "\n";
			String[] terms = summary.elementAt(i).split(" ");
			for (int j = 0; j<terms.length; j++)
			{
				ret += terms[j].substring(0, terms[j].lastIndexOf('/'));
			}
		}
		return ret;
	}
	
	public boolean simTitle(String t1, String t2)
	{
		HashSet<String> term1 = new HashSet<String>();
		HashSet<String> term2 = new HashSet<String>();
		String[] ss;
		ss = t1.split(" ");
		for (String term : ss)
		{
			term1.add(term);
		}
		ss = t2.split(" ");
		for (String term : ss)
		{
			term2.add(term);
		}
		double temp = 0;
		for (String term : term1)
		{
			if (term2.contains(term)) temp += 1;
		}
		temp /= Math.sqrt(term1.size()*term2.size());
		return temp >= 0.5;
	}
	
	

}
