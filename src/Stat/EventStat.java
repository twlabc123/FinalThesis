package Stat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;

import Structure.Event;

/**
 * The function of this class is to get statistics from event cluster results.<br>
 * Not important.
 * @author twl
 *
 */
public class EventStat {

	Vector<Integer> eventScale;
	int[] eventScaleBound = {5,20,50,100,200};
	int lowBound = 50;
	int highBound = 20000;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EventStat es = new EventStat();
		es.stat("data/stat/news_lc_0.4.txt", "data/stat/eventscale0.4_"+es.lowBound+"_"+es.highBound+".txt");
	}
	EventStat()
	{
		eventScale = new Vector<Integer>();
	}
	
	public void stat(String input, String output)
	{
		try
		{
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			for (int i = 0; i<=eventScaleBound.length; i++)
			{
				eventScale.add(0);
			}
			Event e;
			while ((e = Event.readEvent(reader)) != null)
			{
				if (e.article.size() <= highBound && e.article.size() > lowBound)
				{
					for (int i = 0; i<e.article.size(); i++)
					{
						writer.println(e.article.elementAt(i).time.substring(0,10)+" "+e.article.elementAt(i).title);
					}
					writer.println("====");
				}
				Integer temp = eventScale.elementAt(getEventScaleIndex(e.article.size())) + 1;
				eventScale.set(getEventScaleIndex(e.article.size()), temp);
			}
			
			for (int i = 0; i<eventScale.size(); i++)
			{
				if (i < eventScaleBound.length) writer.print(eventScaleBound[i]);
				else writer.print("larger");
				writer.println(" "+eventScale.elementAt(i));
			}
			reader.close();
			writer.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	int getEventScaleIndex(int n)
	{
		for (int i = 0; i<eventScaleBound.length; i++)
		{
			if (n <= eventScaleBound[i]) return i;
		}
		return eventScaleBound.length;
	}

}
