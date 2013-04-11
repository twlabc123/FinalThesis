package Structure;

import java.util.HashMap;

public class Subtopic {
	
	public HashMap<String, Integer> tf;
	public HashMap<String, Integer> df;
	public String start;
	public String end;
	public int docNum;
	public String summary;
	public double active;
	
	public Subtopic()
	{
		tf = new HashMap<String, Integer>();
		df = new HashMap<String, Integer>();
		start = "";
		end = "";
		summary = "";
		docNum = 0;
		active = 1.0;
	}

}
