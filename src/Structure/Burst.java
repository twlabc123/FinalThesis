package Structure;

import java.io.PrintWriter;

/**
 * The burst representation<br>
 * not used finally.
 * @author twl
 *
 */
public class Burst {
	/**
	 * The term of the burst
	 */
	public String term;
	/**
	 * The start time of the burst
	 */
	public String start;
	/**
	 * The end time of the burst
	 */
	public String end;
	
	public Burst()
	{
		term = "";
		start = "";
		end = "";
	}
	
	public void printBurst(PrintWriter writer) throws Exception
	{
		writer.println(term+" "+start.substring(0,10)+" "+end.substring(0,10));
	}
	
	
	

}
