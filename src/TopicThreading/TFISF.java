package TopicThreading;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import DataPrepare.StopWordFilter;
import EventCluster.EventClusterTFIDF;
import Structure.ActiveEvent;
import Structure.Subtopic;
import System.ActiveEventModule;

public class TFISF {

	public Vector<Subtopic> subtopic;
	public HashMap<String, Integer> sf;
	public int stNum;
	public int bigStNum;
	public StopWordFilter swf;
	public PrintWriter writer;
	public Vector<Integer> docNums;//just for screen output
	public Vector<Integer> eventNums;//just for screen output
	public ActiveEventModule aem;
	
	double ThreadingThreshold = 0.4;
	int SummaryTitleNum = 10;
	int EffectiveDoc = 100;
	int EffectiveEvent = 10;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//TFISF t = new TFISF();
		//t.test("data/final/news_lc.txt", "data/final/news_lc_test_tfisf.txt");
	}
	
	TFISF()
	{
		subtopic = new Vector<Subtopic>();
		sf = new HashMap<String, Integer>();
		swf = new StopWordFilter();
		stNum = 0;
		bigStNum = 0;
		swf.load("data/stopwords.txt");
		docNums = new Vector<Integer>();
		eventNums = new Vector<Integer>();
		aem = null;
	}
	
	TFISF(ActiveEventModule a, String output) throws Exception
	{
		subtopic = new Vector<Subtopic>();
		sf = new HashMap<String, Integer>();
		swf = new StopWordFilter();
		stNum = 0;
		bigStNum = 0;
		swf.load("data/stopwords.txt");
		docNums = new Vector<Integer>();
		eventNums = new Vector<Integer>();
		aem = a;
		FileOutputStream stream = new FileOutputStream(output);
		OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
		writer = new PrintWriter(sw);
	}
	
	public void processBatch(Vector<ActiveEvent> activeEvent)
	{
		try
		{
			//aem.removeChangedEventFromSubtopic();
			for (int i = 0; i<activeEvent.size(); i++)
			{
				ActiveEvent ae = activeEvent.elementAt(i);
				if (ae.hasNewDoc && ae.article.size() >= EventClusterTFIDF.Effective)
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
	
	void addEvent(ActiveEvent e) throws Exception
	{
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
		}
		stNum++;
		computeSim(e);
		
		int i = 0;
		while (i < subtopic.size())
		{
			if (!subtopic.elementAt(i).active)
			{
				if (subtopic.elementAt(i).docNum >= EffectiveDoc
						|| subtopic.elementAt(i).event.size() >= EffectiveEvent)
				{
					subtopic.elementAt(i).printSubtopic(writer, this);
					bigStNum++;
				}
				aem.removeSubtopic(subtopic.elementAt(i));
				subtopic.remove(i);
				i--;
			}
			i++;
		}
	}
	
	void computeSim(ActiveEvent e) throws Exception
	{
		Vector<Integer> mergeIndex = new Vector<Integer>();
		double sim = 0;
		int mergeTo = -1;
		for (int i = 0; i<subtopic.size(); i++)
		{
			if (!subtopic.elementAt(i).active) continue;
			double tempsim = similarity(subtopic.elementAt(i), e);
			if (tempsim > ThreadingThreshold)
			{
				mergeIndex.add(i);
				if (tempsim > sim)
				{
					mergeTo = i;
					sim = tempsim;
				}
			}
		}
		if (mergeTo == -1)
		{
			Subtopic st = new Subtopic(e);
			subtopic.add(st);
			aem.linkEventToSubtopic(e, st);
		}
		else
		{
			/*for (int i = 0; i<mergeIndex.size(); i++)
			{
				merge(subtopic.elementAt(mergeIndex.elementAt(i)), e);
			}*/
			merge(subtopic.elementAt(mergeTo), e, sim);
		}
		
	}
	
	void merge(Subtopic a, ActiveEvent e, double sim) throws Exception
	{
		a.addEvent(e, sim);
		aem.linkEventToSubtopic(e, a);
		for (String term : e.tf.keySet())
		{
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
	
	public double similarity(Subtopic a, ActiveEvent b) throws Exception {// a should be the former subtopic and b should be the newer one.
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
		HashMap<String, Double> temp = new HashMap<String, Double>();
		for (String term : a.tf.keySet())
		{
			double tempa = a.tf.get(term) * Math.log((double)stNum/((double)sf.get(term)));
			//double tempa = 1.0;
			da += tempa*tempa;
			temp.put(term, tempa);
		}
		for (String term : b.tf.keySet())
		{
			double tempb = b.tf.get(term) * Math.log((double)stNum/((double)sf.get(term)));
			//double tempb = 1.0;
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
			if (s.startsWith("sim:")) continue;
			s = s.substring(11);
			int length = 0;
			double temp = 0;
			String[] terms = s.split(" ");
			for (String term : terms)
			{
				if (!sf.containsKey(term)) continue;
				length++;
				if (st.tf.containsKey(term))
				{
					double isf = (Math.log((double)stNum/((double)sf.get(term))) / Math.log(stNum));
					temp += st.tf.get(term) * isf;
				}
			}
			if (length != 0) temp /= length;
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
				if (terms[j].length() == 0) continue;
				ret += terms[j].substring(0, terms[j].lastIndexOf('/'));
			}
		}
		return ret;
	}
	
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
	
	public void finalOutput() throws Exception
	{
		for (int i = 0; i<subtopic.size(); i++)
		{
			if (subtopic.elementAt(i).docNum > EffectiveDoc
					|| subtopic.elementAt(i).event.size() >= EffectiveEvent)
			{
				subtopic.elementAt(i).printSubtopic(writer, this);
				bigStNum++;
			}
		}
		writer.close();
	}
	
	public Subtopic getSubtopicById(int id)
	{
		for (int i = 0; i<subtopic.size(); i++)
		{
			if (subtopic.elementAt(i).id == id) return subtopic.elementAt(i);
		}
		return null;
	}

}
