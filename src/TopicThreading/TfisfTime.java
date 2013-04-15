package TopicThreading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;

import Structure.Article;
import Structure.ArticleExtend;
import Structure.Event;
import Structure.Subtopic;

public class TfisfTime extends TFISF {
	
	
	double alpha = -0.5; // time fix with e^(-alpha*interval)

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TfisfTime t = new TfisfTime();
		t.test("data/final/news_lc_merge_2.txt", "data/final/news_lc_test_tfisf_time_merge_2.txt");
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
				int index = 0;
				while (index < subtopic.size())
				{
					int interval = Article.getDay(Article.getDate(subtopic.elementAt(index).end), Article.getDate(st.start));
					if (interval > 7 && subtopic.elementAt(index).docNum < 50)
					{
						subtopic.remove(index);
					}
					else
					{
						double tempsim = similarity(subtopic.elementAt(index), st);
						if (tempsim > ThreadingThreshold && tempsim > sim)
						{
							mergeTo = index;
							sim = tempsim;
						}
						index++;
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
	
	public double similarity(Subtopic a, Subtopic b) throws Exception {
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
		
		// Different from TFISF
		int interval = Article.getDay(Article.getDate(a.end), Article.getDate(b.start));
		double fix = Math.exp(interval*alpha);
		if (fix <= ThreadingThreshold) return 0;
		
		
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
		}
		
		ret /= Math.sqrt(da*db);
		
		
		// Different from TFISF
		if (interval > 0)
		{
			ret *= fix;
		}
		
		return ret;
	}

}
