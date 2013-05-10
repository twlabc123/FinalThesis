package TopicThreading;

import java.util.HashMap;
import java.util.PriorityQueue;

import Structure.ActiveEvent;
import Structure.Subtopic;
import Structure.TermScore;
import System.ActiveEventModule;

public class MultiView extends TFISF {

	double Alpha = -0.01; // time fix with e^(-alpha*interval)
	int SubtopicKeyword = 50;
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
		long interval = e.center - a.center;
		if (interval < 0) interval = 0;
		//if (interval > 5) interval -= 5;
		double fix = Math.exp(interval*Alpha);
		if (fix < ThreadingThreshold*ThreadingThreshold) a.active = false;
		if (fix <= ThreadingThreshold) return 0;
		
		PriorityQueue<TermScore> pq = new PriorityQueue<TermScore>(this.SubtopicKeyword, TermScore.cp);
		
		for (String term : a.tf.keySet())
		{
			double isf;
			if (stNum == 1 || !sf.containsKey(term)) isf = 1;
			else isf = (Math.log((double)stNum/((double)sf.get(term))) / Math.log(stNum));
			double tempa = a.tf.get(term) * isf;
			pq.add(new TermScore(term, tempa));
		}
		HashMap<String, Double> temp = new HashMap<String, Double>();
		for (int i = 0; i<this.SubtopicKeyword; i++)
		{
			TermScore ts = pq.poll();
			//System.out.println(ts.term + " " + ts.score);
			temp.put(ts.term, ts.score);
			if (pq.isEmpty()) break;
		}
		
		for (String term : temp.keySet())
		{
			if (e.tf.containsKey(term)) ret += 1;
		}
		
		//ret /= Math.sqrt(da*db);
		ret /= temp.size();
		
		// Different from TFISF
		if (interval > 0)
		{
			ret *= fix;
		}
		
		return ret;
	}

}
