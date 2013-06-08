package Structure;

import java.util.Comparator;

/**
 * The representation of term and its score pair
 * @author twl
 *
 */
public class TermScore implements Comparable {
	
	public String term;
	public double score;
	
	public TermScore(String t, double s)
	{
		term = t;
		score = s;
	}
	
	public int compareTo(Object arg0)
	{
		// TODO Auto-generated method stub
		TermScore ts = (TermScore) arg0;
		if (score - ts.score > 0) return 1;
		else if (score - ts.score < 0) return -1;
		return 0;
	}
	
	public static Comparator<TermScore> cp = new Comparator<TermScore>()
	{  
        public int compare(TermScore o1, TermScore o2)
        {  
            // TODO Auto-generated method stub  
        	if (o1.score - o2.score > 0) return -1;
    		else if (o1.score - o2.score < 0) return 1;
    		return 0;
        } 
	};
}
