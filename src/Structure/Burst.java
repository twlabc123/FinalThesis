package Structure;

import java.io.PrintWriter;

public class Burst {
	public String term;
	public String start;
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
