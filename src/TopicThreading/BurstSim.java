package TopicThreading;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import DataPrepare.StopWordFilter;
import Structure.Burst;
import Structure.Subtopic;

public class BurstSim extends TFISF {

	/**
	 * @param args
	 */
	
	public Vector<Burst> burst;
	public HashMap<String, Burst> active;
	public int burstIndex;
	public double ThreadingThreshold = 0.3;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public BurstSim()
	{
		subtopic = new Vector<Subtopic>();
		sf = new HashMap<String, Integer>();
		swf = new StopWordFilter();
		swf.load("data/sogou/tf.csv");
		burst = new Vector<Burst>();
		active = new HashMap<String, Burst>();
		burstIndex = -1;
	}
	
	public void loadBurst(String input)
	{
		try {
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			String line;
			while ((line = reader.readLine()) != null)
			{
				Burst b = new Burst();
				b.term = line.split(" ")[0];
				b.start = line.split(" ")[1];
				b.end = line.split(" ")[2];
				burst.add(b);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isActive(String term, String start, String end)
	{
		if (!active.containsKey(term)) return false;
		if (start.compareTo(active.get(term).end) > 0) return false;
		if (end.compareTo(active.get(term).start) < 0) return false;
		return true;
	}
	
	public double similarity(Subtopic a, Subtopic b) throws Exception {// a should be the former subtopic and b should be the newer one.
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
		HashMap<String, Double> temp = new HashMap<String, Double>();
		for (String term : a.tf.keySet())
		{
			if (!isActive(term, a.start, a.end)) continue;
			//double tempa = a.tf.get(term) * Math.log((double)subtopic.size()/((double)sf.get(term)));//tf-isf
			double tempa = 1; // 0-1
			da += tempa*tempa;
			temp.put(term, tempa);
		}
		for (String term : b.tf.keySet())
		{
			if (!isActive(term, b.start, b.end)) continue;
			//double tempb = b.tf.get(term) * Math.log((double)subtopic.size()/((double)sf.get(term)));// tf-isf
			double tempb = 1; // 0-1
			db += tempb*tempb;
			if (temp.containsKey(term))
			{
				ret += temp.get(term).doubleValue() * tempb;
			}
		}
		
		ret /= Math.sqrt(da*db);
		return ret;
	}
	
	

}
