package TopicThreading;

import java.util.HashMap;
import java.util.PriorityQueue;

import Structure.ActiveEvent;
import Structure.Subtopic;
import Structure.TermScore;
import System.ActiveEventModule;

public class MultiView extends TFISF {

	static double Alpha = -0.01; // time fix with e^(-alpha*interval)
	public static int SubtopicKeyword = 30;
	public static double EntityBonus = 2;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public MultiView(ActiveEventModule aem, String output) throws Exception {
		// TODO Auto-generated constructor stub
		super(aem, output);
	}
	
	public double similarity(Subtopic a, ActiveEvent e) throws Exception {
		// TODO Auto-generated method stub
		double ret = 0;
		
		// Different from TFISF
		long interval = (e.center - a.center) / 24;
		if (interval < 0) interval = 0;
		double fix = Math.exp(interval*Alpha);
		if (fix < ThreadingThreshold*ThreadingThreshold) a.active = false;
		if (fix <= ThreadingThreshold) return 0;
		
		PriorityQueue<TermScore> pq = this.getKeyword(a);
		HashMap<String, Double> temp = new HashMap<String, Double>();
		double total = 0;
		for (int i = 0; i<this.SubtopicKeyword && !pq.isEmpty(); i++)
		{
			TermScore ts = pq.poll();
			temp.put(ts.term, ts.score);
			total += ts.score;
		}
		
		double local = 0;
		for (String term : temp.keySet())
		{
			if (e.tf.containsKey(term))
			{
				ret += 1;
				local += temp.get(term);
			}
		}
		
		//ret /= temp.size();
		ret = local / total;
		
		// Time Fix
		if (interval > 0)
		{
			ret *= fix;
		}
		
		return ret;
	}
	
	public String keyWords(Subtopic a) throws Exception
	{
		String ret = "";
		
		PriorityQueue<TermScore> pq = this.getKeyword(a);
		for (int i = 0; i<this.SubtopicKeyword && !pq.isEmpty(); i++)
		{
			TermScore ts = pq.poll();
			if (ret.length() != 0) ret += " ";
			ret += ts.term+" "+ts.score;
		}
		return ret;
	}
	
	public PriorityQueue<TermScore> getKeyword(Subtopic a)
	{
		PriorityQueue<TermScore> pq = new PriorityQueue<TermScore>(this.SubtopicKeyword, TermScore.cp);
		for (String term : a.tf.keySet())
		{
			double isf;
			if (stNum < 2 || !sf.containsKey(term))
			{
				if (term.endsWith(term)) isf = 0.5;
				else isf = 1;
			}
			else
			{
				int temp = sf.get(term);
				double rate = (double)(a.df.get(term))/(double)(a.docNum);
				isf = (Math.log((double)stNum/((double)temp)) / Math.log(stNum));
				if (term.endsWith("/nr") && rate < 0.5) isf = 0;
			}
			double tempa = a.tf.get(term) * isf;
			if (term.endsWith("/ns")
				|| term.endsWith("/nr")
				|| term.endsWith("/me")
				|| term.endsWith("/nz")
				|| term.endsWith("/nt"))
			{
				tempa *= EntityBonus;
			}
			
			if (tempa >= 1.00001)
			{
				pq.add(new TermScore(term, tempa));
			}
		}
		return pq;
	}

}
