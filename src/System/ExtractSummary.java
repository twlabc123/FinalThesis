package System;

import java.util.HashSet;

import Structure.ActiveEvent;
import Structure.Event;
import TopicThreading.TopicThreading;
import TopicThreading.Similarity.Tfisf;

/**
 * The function of this class is to extract summary from sentences of an event
 * @author twl
 *
 */
public class ExtractSummary {
	
	/**
	 * A bonus factor for title
	 */
	static double TitleBonus = 1;
	
	/**
	 * Extract a summary from the event
	 * @param e
	 * @param tt provide the subtopic frequency
	 * @return
	 */
	public static String ExtractEventSummary(Event e, TopicThreading tt)
	{
		String ret = "";
		String sent = "";
		int docIndex = -1;
		double tempSum = 0;
		double maxScore = -1;
		// First choose the title with highest score as summary
		for (int i = 0; i<e.article.size(); i++)
		{
			String title = e.article.elementAt(i).title;
			tempSum = computeScore(title, e, tt);
			if (tempSum > maxScore)
			{
				ret = title;
				maxScore = tempSum;
				docIndex = i;
			}
		}
		// Then consider all the sentences
		String content = e.article.elementAt(docIndex).content;
		String[] terms = content.split(" ");
		int j = 0;
		while (j <= terms.length)
		{
			if (j >= terms.length || tt.swf.isSentenceEnd(terms[j]))
			{
				tempSum = computeScore(sent, e, tt);
				if (tempSum > maxScore)
				{
					ret = sent;
					maxScore = tempSum;
				}
			}
			else
			{
				sent += terms[j];
			}
			j++;
		}
		return ret;
	}
	
	/**
	 * Get the score of a sentence/title
	 * @param sent
	 * @param e
	 * @param tt
	 * @return
	 */
	static double computeScore(String sent, Event e, TopicThreading tt)
	{
		if (sent.split(" ").length >= 30) return -0.2;// too long
		int length = 0;
		double ret = 0;
		String[] terms = sent.split(" ");
		for (String term : terms)
		{
			if (!tt.sf.containsKey(term)) continue;
			length++;
			if (e.tf.containsKey(term))
			{
				double isf = (Math.log((double)tt.stNum/((double)tt.sf.get(term))) / Math.log(tt.stNum));
				ret += e.tf.get(term) * isf;
			}
		}
		if (length < 3) return -0.1;// too short
		if (length != 0) ret /= length;
		return ret;
	}
	
	/**
	 * Return true if the two String are too similar
	 * @param t1
	 * @param t2
	 * @return
	 */
	public static boolean simTitle(String t1, String t2)
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
		return temp >= 0.8;
	}
}
