package EventCluster;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import DataPrepare.StopWordFilter;
import Structure.*;

public class EventClusterTFIDF extends EventCluster {

	PrintWriter writer;
	BufferedReader reader;
	HashMap<String, Integer> df;
	int docTotalNum;
	StopWordFilter swf;
	//Vector<ArticleExtend> leaders;// only for offline implementation
	//Vector<Vector<Article>> outputData;// only for offline implemetation
	
	double Threshold = 0.80;//cluster threshold
	int Effective = 5;//Event with more than Effective articles is effective
	int Delta = 3;//span of the time window
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EventClusterTFIDF ec = new EventClusterTFIDF();
		//ec.leaderCluster("data/final/news.txt", "data/final/news_lc_test.txt");
	}
	
	@Override
	public double similarity(ArticleExtend a, ArticleExtend b) { // if b == null, use bgtf;
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
		int mergeSize = 0;
		HashMap<String, Double> temp = new HashMap<String, Double>();
		for (String term : a.tf.keySet())
		{
			//double tempa = a.tf.get(term) * Math.log((double)docTotalNum/((double)df.get(term)+1));
			double tempa = 1;
			da += tempa*tempa;
			temp.put(term, tempa);
			mergeSize++;
		}
		for (String term : b.tf.keySet())
		{
			//double tempb = btf.get(term) * Math.log((double)docTotalNum/((double)df.get(term)+1));
			double tempb = 1;
			db += tempb*tempb;
			if (temp.containsKey(term))
			{
				ret += temp.get(term).doubleValue() * tempb;
			}
			else
			{
				mergeSize++;
			}
		}
		//ret /= Math.sqrt(da*db);
		ret /= mergeSize;
		return ret;
	}
	
	public EventClusterTFIDF()
	{
		df = new HashMap<String, Integer>();
		docTotalNum = 0;
		swf = new StopWordFilter();
		swf.load("data/stopwords.txt");
	}

	
	/*public void leaderCluster(String input, String output)// offline implementation
	{
		try
		{
			leaders = new Vector<ArticleExtend>();
			outputData = new Vector<Vector<Article>>();
			//this.load(input);
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			writer = new PrintWriter(sw);
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			ArticleExtend a;
			int count = 0;
			while ((a = ArticleExtend.readArticle(reader)) != null)
			{
				docTotalNum++;
				HashSet<String> termSet = new HashSet<String>();
				String[] term = a.content.split(" ");
				for (int i = 0; i<term.length; i++)
				{
					if (swf.isStopWord(term[i])) continue;
					if (a.tf.containsKey(term[i]))
					{
						Integer temp = a.tf.get(term[i]);
						a.tf.remove(term[i]);
						a.tf.put(term[i], temp+1);
					}
					else
					{
						a.tf.put(term[i], 1);
					}
					if (bgtf.containsKey(term[i]))
					{
						Integer temp = bgtf.get(term[i]);
						bgtf.remove(term[i]);
						bgtf.put(term[i], temp+1);
					}
					else
					{
						bgtf.put(term[i], 1);
					}
					if (!termSet.contains(term[i]))
					{
						termSet.add(term[i]);
						if (df.containsKey(term[i]))
						{
							Integer temp = df.get(term[i]);
							df.remove(term[i]);
							df.put(term[i], temp+1);
						}
						else
						{
							df.put(term[i],1);
						}
					}
				}
				
				//just for counting
				count++;
				if (count % 10 == 0)
				{
					System.out.println(count);
				}
				//if (count >= 1000) break;
				
				//Event cluster
				if (leaders.size() == 0)
				{
					leaders.add(a);
					Vector<Article> temp = new Vector<Article>();
					temp.add(a.getArticle());
					outputData.add(temp);
				}
				else
				{
					int mergeTo = -1;
					double max = -1;
					int deleteIndex = -1;
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Date d1 = format.parse(a.time.substring(0,10));
					for (int i = leaders.size()-1; i >= 0; i--)
					{
						Date d2 = format.parse(leaders.elementAt(i).time.substring(0,10));
						int delta = (int) ((d1.getTime() - d2.getTime()) / (24 * 60 * 60 * 1000));
						if (delta >= Delta)
						{
							deleteIndex = i;
							break;
						}
						double sim = similarity(a, leaders.elementAt(i));
						if (sim > Threshold && sim > max)
						{
							mergeTo = i;
							max = sim;
						}
					}
					if (mergeTo < 0)
					{
						leaders.add(a);
						Vector<Article> temp = new Vector<Article>();
						temp.add(a.getArticle());
						outputData.add(temp);
					}
					else
					{
						outputData.elementAt(mergeTo).add(a.getArticle());
					}
					for (int j = 0; j<=deleteIndex; j++)
					{
						if (outputData.elementAt(0).size() >= Effective)
						{
							writer.println("<event>");
							for (int k = 0; k<outputData.elementAt(0).size(); k++)
							{
								outputData.elementAt(0).elementAt(k).printArticle(writer);
							}
							writer.println("</event>");
						}
						leaders.remove(0);
						outputData.remove(0);
					}
				}
			}
			for (int i = 0; i<outputData.size(); i++)
			{
				writer.println("<event>");
				for (int j = 0; j<outputData.elementAt(i).size(); j++)
				{
					outputData.elementAt(i).elementAt(j).printArticle(writer);
				}
				writer.println("</event>");
			}	
			reader.close();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}*/
	
	public void processBatch(Vector<ArticleExtend> docs, Vector<ActiveEvent> activeEvent) throws Exception
	{
		int deleteIndex = -1;
		for (int articleIndex = 0; articleIndex<docs.size(); articleIndex++)
		{
			ArticleExtend a = docs.elementAt(articleIndex);
			docTotalNum++;
			HashSet<String> termSet = new HashSet<String>();
			String[] term = a.content.split(" ");
			for (int i = 0; i<term.length; i++)
			{
				if (swf.isStopWord(term[i])) continue;
				if (a.tf.containsKey(term[i]))
				{
					Integer temp = a.tf.get(term[i]);
					a.tf.remove(term[i]);
					a.tf.put(term[i], temp+1);
				}
				else
				{
					a.tf.put(term[i], 1);
				}
				if (!termSet.contains(term[i]))
				{
					termSet.add(term[i]);
					if (df.containsKey(term[i]))
					{
						Integer temp = df.get(term[i]);
						df.remove(term[i]);
						df.put(term[i], temp+1);
					}
					else
					{
						df.put(term[i],1);
					}
				}
			}
			
			for (int i = 0; i < activeEvent.size(); i++)
			{
				activeEvent.elementAt(i).hasNewDoc = false;
			}
			
			//Event cluster
			int mergeTo = -1;
			double max = -1;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date d1 = format.parse(a.time.substring(0,10));
			for (int i = activeEvent.size()-1; i >= 0; i--)
			{
				Date d2 = format.parse(activeEvent.elementAt(i).article.firstElement().time.substring(0,10));
				int delta = (int) ((d1.getTime() - d2.getTime()) / (24 * 60 * 60 * 1000));
				if (delta >= Delta)
				{
					if (articleIndex == 0) deleteIndex = i;
					break;
				}
				double sim = similarity(a, activeEvent.elementAt(i).article.firstElement());
				if (sim > Threshold && sim > max)
				{
					mergeTo = i;
					max = sim;
				}
			}
			if (mergeTo < 0)
			{
				ActiveEvent e = new ActiveEvent();
				e.id = Event.TotalEventNum;
				Event.TotalEventNum++;
				e.addArticle(a);
				activeEvent.add(e);
			}
			else
			{
				a.tf = new HashMap<String, Integer>();//It is not a leader, so we don't habe to save tf-table.
				activeEvent.elementAt(mergeTo).addArticle(a);
			}
		}
		for (int j = 0; j<=deleteIndex; j++)
		{
			activeEvent.remove(0);
		}
	}

}
