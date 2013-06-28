package TopicThreading.Similarity;

import java.util.HashMap;
import Structure.ActiveEvent;
import Structure.Subtopic;
import TopicThreading.TopicThreading;

/**
 * Similarity using tf-idf<br>
 * tf means term-frequency<br>
 * idf means invert-document frequency = log(TotalDocNumber / termDocFrequency).<br>
 * We use the inner product of tf-isf vector of the subtopic and the event as the similarity.
 * @author twl
 *
 */
public class Tfidf extends Similarity {
	
	public Tfidf(TopicThreading tt)
	{
		this.tt = tt;
	}
	
	public double similarity(Subtopic a, ActiveEvent b) throws Exception {
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
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
			if (temp.containsKey(term))// compute the inner product
			{
				ret += temp.get(term).doubleValue() * tempb;
			}
		}
		ret /= Math.sqrt(da*db);
		return ret;
	}
	
}
