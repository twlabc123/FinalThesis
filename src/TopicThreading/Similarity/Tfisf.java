package TopicThreading.Similarity;

import java.util.HashMap;
import Structure.ActiveEvent;
import Structure.Subtopic;
import TopicThreading.TopicThreading;

/**
 * Similarity using tf-isf<br>
 * tf means term-frequency<br>
 * isf means invert-subtopic frequency = log(TotalDocNumber / termSubtopicFrequency).<br>
 * The subtopic frequency is more effective than document frequency in the representation of sutopic.<br>
 * We use the inner product of tf-isf vector of the subtopic and the event as the similarity.
 * @author twl
 *
 */
public class Tfisf extends Similarity{
	
	public Tfisf(TopicThreading tt)
	{
		this.tt = tt;
	}
	
	public double similarity(Subtopic a, ActiveEvent b) throws Exception {// a should be the former subtopic and b should be the newer one.
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
		HashMap<String, Double> temp = new HashMap<String, Double>();
		for (String term : a.tf.keySet())
		{
			if (!tt.sf.containsKey(term)) continue;
			double debugTemp2 = (double)tt.sf.get(term);
			double debugTemp =  Math.log((double)tt.stNum/(debugTemp2));
			double tempa = a.tf.get(term) * debugTemp;
			//double tempa = 1.0;
			da += tempa*tempa;
			temp.put(term, tempa);
		}
		for (String term : b.tf.keySet())
		{
			double tempb = b.tf.get(term) * Math.log((double)tt.stNum/((double)tt.sf.get(term)));
			//double tempb = 1.0;
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
