package EventCluster;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import DataPrepare.StopWordFilter;
import Structure.*;
import System.ActiveEventModule;

public class EventClusterTFIDF extends EventCluster {

	PrintWriter writer;
	BufferedReader reader;
	HashMap<String, Integer> df;
	int docTotalNum;
	StopWordFilter swf;
	ActiveEventModule aem;
	//Vector<ArticleExtend> leaders;// only for off-line implementation
	//Vector<Vector<Article>> outputData;// only for off-line implementation
	
	double Threshold = 0.80;//cluster threshold
	public static int Effective = 2;//Event with more than Effective articles is effective
	int Delta = 3;//span of the time window
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//EventClusterTFIDF ec = new EventClusterTFIDF();
		//ec.leaderCluster("data/final/news.txt", "data/final/news_lc_test.txt");
	}
	
	public EventClusterTFIDF()
	{
		df = new HashMap<String, Integer>();
		docTotalNum = 0;
		swf = new StopWordFilter();
		swf.load("data/stopwords.txt");
	}
	
	public EventClusterTFIDF(ActiveEventModule aem, String output) throws Exception
	{
		df = new HashMap<String, Integer>();
		docTotalNum = 0;
		swf = new StopWordFilter();
		swf.load("data/stopwords.txt");
		this.aem = aem;
		FileOutputStream stream = new FileOutputStream(output);
		OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
		writer = new PrintWriter(sw);
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
		for (int i = 0; i < activeEvent.size(); i++)
		{
			activeEvent.elementAt(i).hasNewDoc = false;
		}
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
				e.addArticle(a, this.swf);
				activeEvent.add(e);
			}
			else
			{
				aem.removeChangedEventFromSubtopic(activeEvent.elementAt(mergeTo));
				a.tf = new HashMap<String, Integer>();//It is not a leader, so we don't habe to save tf-table.
				activeEvent.elementAt(mergeTo).addArticle(a, this.swf);
			}
		}
		
		for (int j = 0; j<=deleteIndex; j++)
		{
			if (activeEvent.firstElement().article.size() >= Effective)
			{
				ActiveEvent ae = activeEvent.firstElement();
				writer.println("<event>");
				/*writer.println("<id>"+ae.id+"</id>");
				writer.println("<start>"+ae.start+"</start>");
				writer.println("<end>"+ae.end+"</end>");
				for (int i = 0; i<ae.article.size(); i++)
				{
					writer.print(ae.article.elementAt(i).time.substring(0,10)+" ");
					writer.println(ae.article.elementAt(i).title);
				}*/
				ae.printEvent(writer);
				writer.println("</event>");
				aem.addSummaryToSubtopic(ae);
			}
			activeEvent.remove(0);
		}
	}
	
	public void finalOutput(Vector<ActiveEvent> activeEvent)
	{
		for (int j = 0; j<activeEvent.size(); j++)
		{
			if (activeEvent.elementAt(j).article.size() >= Effective)
			{
				ActiveEvent ae = activeEvent.elementAt(j);
				writer.println("<event>");
				writer.println("<id>"+ae.id+"</id>");
				writer.println("<start>"+ae.start+"</start>");
				writer.println("<end>"+ae.end+"</end>");
				writer.println("<center>"+ae.center+"</center>");
				for (int i = 0; i<ae.article.size(); i++)
				{
					writer.print(ae.article.elementAt(i).time.substring(0,10)+" ");
					writer.println(ae.article.elementAt(i).title);
				}
				writer.println("</event>");
				aem.addSummaryToSubtopic(ae);
			}
		}
		writer.close();
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

}
