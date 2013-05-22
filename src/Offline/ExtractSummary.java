package Offline;

import java.util.HashSet;

import Structure.ActiveEvent;
import Structure.Event;
import TopicThreading.TFISF;

public class ExtractSummary {
	
	static double TitleBonus = 2;

	public static String ExtractEventSummary(Event e, TFISF sa)
	{
		String ret = "";
		String sent = "";
		int docIndex = -1;
		double tempSum = 0;
		double maxScore = -1;
		for (int i = 0; i<e.article.size(); i++)
		{
			String title = e.article.elementAt(i).title;
			tempSum = computeScore(title, e, sa);
			if (tempSum > maxScore)
			{
				ret = title;
				maxScore = tempSum;
				docIndex = i;
			}
		}
		String content = e.article.elementAt(docIndex).content;
		String[] terms = content.split(" ");
		int j = 0;
		while (j <= terms.length)
		{
			if (j >= terms.length || sa.swf.isSentenceEnd(terms[j]))
			{
				tempSum = computeScore(sent, e, sa);
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
	
	static double computeScore(String sent, Event e, TFISF sa)
	{
		if (sent.split(" ").length >= 30) return -0.2;
		int length = 0;
		double ret = 0;
		String[] terms = sent.split(" ");
		for (String term : terms)
		{
			if (!sa.sf.containsKey(term)) continue;
			length++;
			if (e.tf.containsKey(term))
			{
				double isf = (Math.log((double)sa.stNum/((double)sa.sf.get(term))) / Math.log(sa.stNum));
				ret += e.tf.get(term) * isf;
			}
		}
		if (length < 3) return -0.1;
		if (length != 0) ret /= length;
		return ret;
	}
	
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
