package TopicThreading.Similarity;

import java.util.HashMap;
import java.util.PriorityQueue;

import Structure.ActiveEvent;
import Structure.Subtopic;
import Structure.TermScore;
import TopicThreading.TopicThreading;

/**
 * Similarity using keywords.<br>
 * First compute the score for each term.<br>
 * Then choose the keywords with high scores.<br>
 * Finally check if the event has these keywords in its term set and compute the similarity based on the scores.
 * @author twl
 *
 */
public class TfisfKeyword extends Similarity {
	/**
	 * time fix with e^(-alpha*interval)
	 */
	static double ALPHA = -0.005;
	/**
	 * The number of keywords to represent the subtopic
	 */
	public static int SubtopicKeyword = 30;
	/**
	 * The bonus factor for entities when computing term scores
	 */
	public static double EntityBonus = 2;
	
	public TfisfKeyword(TopicThreading tt)
	{
		this.tt = tt;
	}
	
	public double similarity(Subtopic a, ActiveEvent e) throws Exception {
		// TODO Auto-generated method stub
		double ret = 0;
		
		// Different from TFISF
		long interval = (e.center - a.center) / 24;// interval in days
		if (interval < 0) interval = 0;
		double fix = Math.exp(interval*ALPHA);
		if (fix < TopicThreading.ThreadingThreshold*TopicThreading.ThreadingThreshold) a.active = false;// find the inactive subtopic @see TfidfTime
		if (fix <= TopicThreading.ThreadingThreshold) return 0;
		
		// Compute the scores for each term.
		// Use a priority queue as the container.
		PriorityQueue<TermScore> pq = this.getKeyword(a);
		HashMap<String, Double> temp = new HashMap<String, Double>();
		double total = 0;
		
		// Find the first SubtopicKeyword terms as the keywords of the subtopic.
		for (int i = 0; i<TfisfKeyword.SubtopicKeyword && !pq.isEmpty(); i++)
		{
			TermScore ts = pq.poll();
			temp.put(ts.term, ts.score);
			total += ts.score;
		}
		
		double local = 0;
		for (String term : temp.keySet())
		{
			if (e.tf.containsKey(term) && e.tf.get(term) >= e.article.size())
			{
				local += temp.get(term);
			}
		}
		
		ret = local / total;
		
		// Time Fix
		if (interval > 0)
		{
			ret *= fix;
		}
		
		return ret;
	}
	
	/**
	 * Extract keywords from the subtopic.<br>
	 * It returns a String with the format "t_1 score(t_1) t_2 score(t_2) ..."
	 * @param a
	 * @return
	 * @throws Exception
	 */
	public String keyWords(Subtopic a) throws Exception
	{
		String ret = "";
		
		PriorityQueue<TermScore> pq = this.getKeyword(a);
		for (int i = 0; i<TfisfKeyword.SubtopicKeyword && !pq.isEmpty(); i++)
		{
			TermScore ts = pq.poll();
			if (ret.length() != 0) ret += " ";
			ret += ts.term+" "+ts.score;
		}
		return ret;
	}
	
	/**
	 * Get the scores of all the terms in the subtopic.<br>
	 * Use a priority queue.
	 * @param a
	 * @return
	 */
	public PriorityQueue<TermScore> getKeyword(Subtopic a)
	{
		PriorityQueue<TermScore> pq = new PriorityQueue<TermScore>(TfisfKeyword.SubtopicKeyword, TermScore.cp);
		for (String term : a.tf.keySet())
		{
			double isf;
			if (tt.stNum < 2 || !tt.sf.containsKey(term))
			{
				if (term.endsWith(term)) isf = 0.5;
				else isf = 1;
			}
			else
			{
				int temp = tt.sf.get(term);
				double rate = (double)(a.df.get(term))/(double)(a.docNum);
				isf = (Math.log((double)tt.stNum/((double)temp)) / Math.log(tt.stNum));
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
			
			// only consider those terms with tfisf-value greater than 1.0
			if (tempa >= 1.00001)
			{
				pq.add(new TermScore(term, tempa));
			}
		}
		return pq;
	}

}
