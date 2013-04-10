package TopicThreading;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

import Structure.Article;
import Structure.Subtopic;

public class TfisfTime extends TFISF {
	
	
	double alpha = -0.5; // time fix with e^(-alpha*interval)

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TfisfTime t = new TfisfTime();
		t.test("data/final/news_lc.txt", "data/final/news_lc_test_tfisf_time.txt");
	}
	
	public double similarity(Subtopic a, Subtopic b) throws Exception {
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
		HashMap<String, Double> temp = new HashMap<String, Double>();
		for (String term : a.tf.keySet())
		{
			double tempa = a.tf.get(term) * Math.log((double)subtopic.size()/((double)sf.get(term)));
			da += tempa*tempa;
			temp.put(term, tempa);
		}
		for (String term : b.tf.keySet())
		{
			double tempb = b.tf.get(term) * Math.log((double)subtopic.size()/((double)sf.get(term)));
			db += tempb*tempb;
			if (temp.containsKey(term))
			{
				ret += temp.get(term).doubleValue() * tempb;
			}
		}
		
		ret /= Math.sqrt(da*db);
		
		int interval = Article.getDay(Article.getDate(a.end), Article.getDate(b.start));
		if (interval > 0)
		{
			ret *= Math.exp(interval*alpha);
		}
		
		return ret;
	}

}
