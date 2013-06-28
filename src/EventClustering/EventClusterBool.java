package EventClustering;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import DataPreprocess.StopWordFilter;
import Structure.*;
import System.ActiveEventModule;

public class EventClusterBool extends EventCluster {
	
	/**
	 * document frequency table
	 */
	HashMap<String, Integer> df;
	/**
	 * Total number of documents
	 */
	int docTotalNum;
	
	/**
	 * width of the time window
	 */
	static int Delta = 3;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//EventClusterTFIDF ec = new EventClusterTFIDF();
		//ec.leaderCluster("data/final/news.txt", "data/final/news_lc_test.txt");
	}
	
	public EventClusterBool()
	{
		df = new HashMap<String, Integer>();
		docTotalNum = 0;
		swf = new StopWordFilter();
		swf.load(StopWordFilter.StopWordDic);
	}
	
	/**
	 * Initialize all para
	 * @param aem ref to active event module
	 * @param output Output file path
	 * @throws Exception
	 */
	public EventClusterBool(ActiveEventModule aem, String output) throws Exception
	{
		df = new HashMap<String, Integer>();
		docTotalNum = 0;
		swf = new StopWordFilter();
		swf.load(StopWordFilter.StopWordDic);
		this.aem = aem;
		FileOutputStream stream = new FileOutputStream(output);
		OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
		writer = new PrintWriter(sw);
		eventNum = 0;
	}
	
	/**
	 * Use bool vector representation and leader cluster algorithm.
	 * @param docs the input documents
	 * @param activeEvent the current active event set
	 * @see EventCluster
	 */
	@Override
	public void processBatch(Vector<ArticleExtend> docs, Vector<ActiveEvent> activeEvent)
	{
		try
		{
			int deleteIndex = -1;// index of the last event that is out of the time window
			for (int i = 0; i < activeEvent.size(); i++)
			{
				activeEvent.elementAt(i).hasNewDoc = false;
			}
			for (int articleIndex = 0; articleIndex<docs.size(); articleIndex++)
			{
				// Update the tf,df table
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
					int delta = (int) ((d1.getTime() - d2.getTime()) / (24 * 60 * 60 * 1000));// time gap in days.
					
					// if the time gap is large enough, that means all the former event are out
					// of the time window and could be output.
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
				if (mergeTo < 0) // add to exist event
				{
					ActiveEvent e = new ActiveEvent();
					e.id = Event.TotalEventNum;
					Event.TotalEventNum++;
					e.addArticle(a, this.swf);
					activeEvent.add(e);
				}
				else // build a new event for this document
				{
					aem.removeChangedEventFromSubtopic(activeEvent.elementAt(mergeTo));
					a.tf = new HashMap<String, Integer>();//It is not a leader, so we don't habe to save tf-table.
					activeEvent.elementAt(mergeTo).addArticle(a, this.swf);
				}
			}
			
			// output the event that are out of the time window and delete them from the
			// activeEvent set.
			for (int j = 0; j<=deleteIndex; j++)
			{
				if (activeEvent.firstElement().article.size() >= Effective)// only save those are not too small
				{
					ActiveEvent ae = activeEvent.firstElement();
					ae.printEvent(writer);
					aem.addSummaryToSubtopic(ae);
				}
				activeEvent.remove(0);
				eventNum++;
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see EventCluster
	 */
	@Override
	public void finalOutput(Vector<ActiveEvent> activeEvent)
	{
		for (int j = 0; j<activeEvent.size(); j++)
		{
			if (activeEvent.elementAt(j).article.size() >= Effective)
			{
				ActiveEvent ae = activeEvent.elementAt(j);
				try {
					ae.printEvent(writer);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				aem.addSummaryToSubtopic(ae);
			}
		}
		writer.close();
	}
	
	/**
	 * Use sim = |A and B|/|A or B|
	 * @see EventCluster
	 */
	@Override
	public double similarity(ArticleExtend a, ArticleExtend b) { // if b == null, use bgtf;
		// TODO Auto-generated method stub
		double ret = 0;
		//double da = 0.0;
		//double db = 0.0;
		int mergeSize = 0;
		HashMap<String, Double> temp = new HashMap<String, Double>();
		for (String term : a.tf.keySet())
		{
			//double tempa = a.tf.get(term) * Math.log((double)docTotalNum/((double)df.get(term)+1));
			double tempa = 1;
			//da += tempa*tempa;
			temp.put(term, tempa);
			mergeSize++;
		}
		for (String term : b.tf.keySet())
		{
			//double tempb = btf.get(term) * Math.log((double)docTotalNum/((double)df.get(term)+1));
			double tempb = 1;
			//db += tempb*tempb;
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
