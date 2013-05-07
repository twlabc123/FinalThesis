package TopicThreading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;

import Structure.Article;
import Structure.ArticleExtend;
import Structure.Event;
import Structure.Subtopic;

public class TfisfTime extends TFISF {
	
	
	double alpha = -0.02; // time fix with e^(-alpha*interval)

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TfisfTime t = new TfisfTime();
		t.ThreadingThreshold *= Math.exp(t.alpha);
		t.test("data/final/news_lc_test.txt", "data/final/news_lc_test_tfisf_time_merge_2.txt");
	}
	
	public double similarity(Subtopic a, Subtopic b) throws Exception {
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
		
		// Different from TFISF
		long interval = b.center - a.center;
		if (interval < 0) interval = 0;
		//if (interval > 5) interval -= 5;
		double fix = Math.exp(interval*alpha);
		if (fix < ThreadingThreshold*ThreadingThreshold) a.active = false;
		if (fix <= ThreadingThreshold) return 0;
		
		
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
		
		//new java.util.Scanner(System.in).nextLine();
		
		
		// Different from TFISF
		if (interval > 0)
		{
			ret *= fix;
		}
		
		return ret;
	}

}
