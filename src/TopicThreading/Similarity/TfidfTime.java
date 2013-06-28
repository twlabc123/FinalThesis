package TopicThreading.Similarity;

import java.util.HashMap;

import Structure.ActiveEvent;
import Structure.Subtopic;
import TopicThreading.TopicThreading;

/**
 * Use tf-idf and time represent to compute the similarity.<br>
 * The general similarity = tfidf-sim * time-sim;<br>
 * time-sim = exp(-alpha * (t_e - t_s));
 * @author twl
 *
 */
public class TfidfTime extends Similarity {
	
	static double ALPHA = -0.01; // time fix with e^(-alpha*interval)
	
	public TfidfTime(TopicThreading tt)
	{
		this.tt = tt;
	}
	
	public double similarity(Subtopic a, ActiveEvent b) throws Exception {
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
		
		long interval = b.center - a.center;
		if (interval < 0) interval = 0;
		double fix = Math.exp(interval*ALPHA);
		// if the time-sim is small enough, it means this subtopic is too old and should be remove from the active set
		if (fix < TopicThreading.ThreadingThreshold*TopicThreading.ThreadingThreshold) a.active = false;// Threshold*Threshold is just for making system more robust.
		if (fix <= TopicThreading.ThreadingThreshold) return 0;
		
		//The tf-idf similarity
		HashMap<String, Double> temp = new HashMap<String, Double>();
		for (String term : a.tf.keySet())
		{
			double tempa = a.tf.get(term) * Math.log((double)tt.docTotalNum/((double)tt.df.get(term)));
			da += tempa*tempa;
			temp.put(term, tempa);
		}
		for (String term : b.tf.keySet())
		{
			double tempb = b.tf.get(term) * Math.log((double)tt.docTotalNum/((double)tt.df.get(term)));
			db += tempb*tempb;
			if (temp.containsKey(term))
			{
				ret += temp.get(term).doubleValue() * tempb;
			}
		}
		ret /= Math.sqrt(da*db);
		
		if (interval > 0)// if the subtopic is even later than the event, then there is no need to compute the time-sim
		{
			ret *= fix;
		}
		
		return ret;
	}

}
