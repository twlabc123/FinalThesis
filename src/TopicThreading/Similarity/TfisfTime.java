package TopicThreading.Similarity;

import java.util.HashMap;
import Structure.ActiveEvent;
import Structure.Subtopic;
import TopicThreading.TopicThreading;

/**
 * Similar to TF-isf. Only add time fix
 * @author twl
 *
 */
public class TfisfTime extends Similarity {
	
	/**
	 * time fix with e^(-alpha*interval)
	 */
	static double ALPHA = -0.01;
	
	public TfisfTime(TopicThreading tt)
	{
		this.tt = tt;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//TfisfTime t = new TfisfTime();
		//t.test("data/final/news_lc_test.txt", "data/final/news_lc_test_tfisf_time_merge_2.txt");
	}
	
	public double similarity(Subtopic a, ActiveEvent e) throws Exception {
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
		
		// Different from TFISF
		long interval = e.center - a.center;
		if (interval < 0) interval = 0;
		double fix = Math.exp(interval*ALPHA);
		if (fix < TopicThreading.ThreadingThreshold*TopicThreading.ThreadingThreshold) a.active = false;// @ TfidfTime
		if (fix <= TopicThreading.ThreadingThreshold) return 0;
		
		
		HashMap<String, Double> temp = new HashMap<String, Double>();
		for (String term : a.tf.keySet())
		{
			Integer tempsf = tt.sf.get(term);
			if (tempsf == null) tempsf = 1; 
			double tempa = a.tf.get(term) * Math.log((double)tt.stNum/tempsf);
			//double tempa = 1.0;
			da += tempa*tempa;
			temp.put(term, tempa);
		}
		for (String term : e.tf.keySet())
		{
			Integer tempsf = tt.sf.get(term);
			if (tempsf == null) tempsf = 1; 
			double tempb = e.tf.get(term) * Math.log((double)tt.stNum/tempsf);
			//double tempb = 1.0;
			db += tempb*tempb;
			if (temp.containsKey(term))//compute the inner product
			{
				ret += temp.get(term).doubleValue() * tempb;
			}
		}
		
		ret /= Math.sqrt(da*db);
		
		// Different from TFISF
		if (interval > 0)
		{
			ret *= fix;
		}
		
		return ret;
	}

}
