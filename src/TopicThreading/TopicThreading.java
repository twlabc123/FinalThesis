package TopicThreading;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import DataPreprocess.StopWordFilter;
import EventClustering.EventClusterBool;
import GlobalConstant.GlobalConstant;
import Structure.ActiveEvent;
import Structure.Subtopic;
import System.ActiveEventModule;
import TopicThreading.Similarity.*;

/**
 * The function of this class is to cluster events into subtopic using online clustring algorithm.<br>
 * The similarity algorithm could be replaced with other models or algorithms.
 * @author twl
 *
 */
public class TopicThreading {

	/**
	 * Subtopic set
	 */
	public Vector<Subtopic> subtopic;
	/**
	 * Term subtopic frequency table
	 */
	public HashMap<String, Integer> sf;
	/**
	 * The total number of subtopics
	 */
	public int stNum;
//	/**
//	 * The total number of big subtopics.<br>
//	 * Has been abandoned
//	 */
//	public int bigStNum;
	/**
	 * Stop word filter
	 */
	public StopWordFilter swf;
	/**
	 * Writer of the output file
	 */
	public PrintWriter writer;
	/**
	 * Ref of active event module
	 */
	public ActiveEventModule aem;
	/**
	 * df table
	 */
	public HashMap<String, Integer> df;
	/**
	 * Total doc number
	 */
	public int docTotalNum;
	/**
	 * Similarity computing model
	 */
	public Similarity simModel;
	
	/**
	 * The threshold of similarity for add an event to an exited subtopic
	 */
	public static double ThreadingThreshold = 0.3;
	/**
	 * Only subtopic with 5(or more) events or 100(or more) documents are effective
	 */
	static int EffectiveEvent = 5;
	/**
	 * Only subtopic with 5(or more) events or 100(or more) documents are effective
	 */
	static int EffectiveDoc = 100;
	
	TopicThreading()
	{
		subtopic = new Vector<Subtopic>();
		sf = new HashMap<String, Integer>();
		swf = new StopWordFilter();
		stNum = 0;
		swf.load(StopWordFilter.StopWordDic);
		aem = null;
		df = new HashMap<String, Integer>();
		docTotalNum = 0;
		simModel = new Tfisf(this);
	}
	
	public TopicThreading(ActiveEventModule a, String output, String model) throws Exception
	{
		subtopic = new Vector<Subtopic>();
		sf = new HashMap<String, Integer>();
		swf = new StopWordFilter();
		stNum = 0;
		swf.load(StopWordFilter.StopWordDic);
		aem = a;
		FileOutputStream stream = new FileOutputStream(output);
		OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
		writer = new PrintWriter(sw);
		df = new HashMap<String, Integer>();
		docTotalNum = 0;
		
		if (model.equals(GlobalConstant.TFISF))
		{
			simModel = new Tfisf(this);
		}
		else if (model.equals(GlobalConstant.TFIDF))
		{
			simModel = new Tfidf(this);
		}
		else if (model.equals(GlobalConstant.TFIDFTIME))
		{
			simModel = new TfidfTime(this);
		}
		else if (model.equals(GlobalConstant.TFISFTIME))
		{
			simModel = new TfisfTime(this);
		}
		else if (model.equals(GlobalConstant.TFISFKEYWORD))
		{
			simModel = new TfisfKeyword(this);
		}
		else
		{
			throw new Exception("Wrong type");
		}
	}
	
	public void processBatch(Vector<ActiveEvent> activeEvent)
	{
		try
		{
			//aem.removeChangedEventFromSubtopic();
			for (int i = 0; i<activeEvent.size(); i++)
			{
				ActiveEvent ae = activeEvent.elementAt(i);
				if (ae.hasNewDoc && ae.article.size() >= EventClusterBool.Effective)
				{
					addEvent(ae);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a event to TopicTheading module
	 * @param e
	 * @throws Exception
	 */
	void addEvent(ActiveEvent e) throws Exception
	{
		// Update the df & sf table
		for (String term : e.tf.keySet())
		{
			if (sf.containsKey(term))
			{
				Integer temp = sf.get(term);
				sf.remove(term);
				sf.put(term, temp+1);
			}
			else
			{
				sf.put(term, 1);
			}
			
			if (df.containsKey(term))
			{
				Integer temp = df.get(term);
				df.remove(term);
				df.put(term, temp+e.df.get(term));
			}
			else
			{
				df.put(term, 1);
			}
		}
		stNum++;
		
		computeAndMerge(e);
		
		// output and remove the inactive subtopics
		int i = 0;
		while (i < subtopic.size())
		{
			if (!subtopic.elementAt(i).active)
			{
				if (subtopic.elementAt(i).docNum >= EffectiveDoc
						|| subtopic.elementAt(i).event.size() >= EffectiveEvent)
				{
					subtopic.elementAt(i).printSubtopic(writer, this);
				}
				aem.removeSubtopic(subtopic.elementAt(i));
				subtopic.remove(i);
				i--;
			}
			i++;
		}
	}
	
	/**
	 * Compute the similarity and merge those event with high similarities into the subtopic.
	 * @param e
	 * @throws Exception
	 */
	void computeAndMerge(ActiveEvent e) throws Exception
	{
		Vector<Integer> mergeIndex = new Vector<Integer>();
		double sim = 0;
		int mergeTo = -1;
		for (int i = 0; i<subtopic.size(); i++)
		{
			if (!subtopic.elementAt(i).active) continue;
			double tempsim = simModel.similarity(subtopic.elementAt(i), e);
			if (tempsim > ThreadingThreshold)// choose the greatest one
			{
				mergeIndex.add(i);
				if (tempsim > sim)
				{
					mergeTo = i;
					sim = tempsim;
				}
			}
		}
		if (mergeTo == -1)// set up a new subtopic for this event
		{
			Subtopic st = new Subtopic(e);
			subtopic.add(st);
			aem.addEventToSubtopic(e, st);
		}
		else// merge it into a subtopic
		{
			merge(subtopic.elementAt(mergeTo), e, sim);
		}
		
	}
	
	void merge(Subtopic a, ActiveEvent e, double sim) throws Exception
	{
		a.addEvent(e, sim);// add the event to the subtopic
		aem.addEventToSubtopic(e, a);// add reference
		for (String term : e.tf.keySet())
		{
			// Update the sf table. Remove the terms in the event
			if (sf.containsKey(term))
			{
				if (a.tf.containsKey(term))
				{
					Integer temp = sf.get(term);
					sf.remove(term);
					if (temp - 1 > 0)	sf.put(term, temp-1);
				}
			}
		}
		stNum--;
	}
	
	
	/**
	 * Compute similarity of 2 titles
	 * @param t1
	 * @param t2
	 * @return
	 */
	boolean simTitle(String t1, String t2)
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
	
	/**
	 * Output all the subtopic in active sets
	 * @throws Exception
	 */
	public void finalOutput() throws Exception
	{
		for (int i = 0; i<subtopic.size(); i++)
		{
			if (subtopic.elementAt(i).docNum > EffectiveDoc
					|| subtopic.elementAt(i).event.size() >= EffectiveEvent)
			{
				subtopic.elementAt(i).printSubtopic(writer, this);
			}
		}
		writer.close();
	}
	
	/**
	 * Get a subtopic by its id
	 * @param id
	 * @return
	 */
	public Subtopic getSubtopicById(int id)
	{
		for (int i = 0; i<subtopic.size(); i++)
		{
			if (subtopic.elementAt(i).id == id) return subtopic.elementAt(i);
		}
		return null;
	}

}
