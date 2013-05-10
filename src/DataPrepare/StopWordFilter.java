package DataPrepare;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import Structure.Article;
import Structure.ArticleExtend;

public class StopWordFilter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		StopWordFilter s = new StopWordFilter();
		s.load("data/sogou/tf.csv");
		s.run("data/news_split_sort_cut.txt", "data/tf.csv");
		//s.runSogou("data/sogou/news_bg.txt", "data/sogou/tf.csv");
	}
	
	
	HashSet<String> stopWord;
	static int SIZE = 200;
	
	public StopWordFilter()
	{
		stopWord = new HashSet<String>();
	}
	
	public void load(String input)
	{
		try
		{
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			String line;
			int count = 0;
			reader.readLine();
			while ((line = reader.readLine()) != null)
			{
				stopWord.add(line.split(",")[0]);
				count++;
				if (count >= SIZE) break;
			}
			System.out.println("Load Stopwords finished");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean notWord(String term)
	{
		boolean ret = term.endsWith("/w") || term.length() <= 2;
		return ret;
	}
	
	public boolean isEntity(String term)
	{
		boolean ret = term.endsWith("/ns");
		ret = ret || term.endsWith("/nt");
		ret = ret || term.endsWith("/nr");
		ret = ret || term.endsWith("/nz");
		ret = ret || term.endsWith("/j");
		ret = ret || term.endsWith("/me");
		return ret;
	}
	
	public boolean isStopWord(String term)
	{
		boolean ret = stopWord.contains(term) || term.endsWith("/w") || term.length() == 0 || term.endsWith("/m");
		ret = ret || term.endsWith("/p") || term.endsWith("/q") || term.endsWith("/c") || term.endsWith("/d");
		ret = ret || term.endsWith("/r") || term.endsWith("/t") || term.endsWith("/k") || term.endsWith("/y");
		ret = ret || (term.endsWith("/nx") && term.length() == 4) || term.endsWith("/f") || term.endsWith("/nz");
		ret = ret || term.endsWith("/b") || term.endsWith("/i");
		return ret;
		//Filt single numbers. They should be
		//covered by the stopword-list generated by
		//sogou data. However, sogou uses SBC case.
	}
	
	public void run(String input, String output)//generate tf table from diaoyudao news data
	{
		HashMap<String, Integer> tf = new HashMap<String, Integer>();
		try {
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			Article a;
			while ((a = Article.readArticle(reader)) != null)
			{
				String[] term = a.content.split(" ");
				for (int i = 0; i<term.length; i++)
				{
					if (term[i].endsWith("/w") || term[i].length() == 0) continue;
					if (stopWord.contains(term[i])) continue;
					if (term[i].endsWith("/m") && term[i].length() == 3) continue;//Filt single numbers. They should be
																				  //covered by the stopword-list generated by
																				  //sogou data. However, sogou uses SBC case.
					if (tf.containsKey(term[i]))
					{
						Integer temp = tf.get(term[i]);
						tf.remove(term[i]);
						tf.put(term[i], temp+1);
					}
					else
					{
						tf.put(term[i], 1);
					}
				}
			}
			ArrayList<Map.Entry<String,Integer>> array = new ArrayList<Map.Entry<String,Integer>>(tf.entrySet());    
	        Collections.sort(array, new Comparator<Map.Entry<String, Integer>>() {      
	            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
	                return (o2.getValue().intValue() - o1.getValue().intValue());      
	            }      
	        });
			writer.println("term,frequency");
			for (int i = 0; i<array.size(); i++)
			{
				writer.println(array.get(i).getKey()+","+array.get(i).getValue());
			}
			reader.close();
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void runSogou(String input, String output)//generate tf table from sogou sampled background news data
	{
		HashMap<String, Integer> tf = new HashMap<String, Integer>();
		try {
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] term = line.split(" ");
				for (int i = 0; i<term.length; i++)
				{
					if (term[i].endsWith("/w")) continue;
					if (tf.containsKey(term[i]))
					{
						Integer temp = tf.get(term[i]);
						tf.remove(term[i]);
						tf.put(term[i], temp+1);
					}
					else
					{
						tf.put(term[i], 1);
					}
				}
			}
			ArrayList<Map.Entry<String,Integer>> array = new ArrayList<Map.Entry<String,Integer>>(tf.entrySet());    
	        Collections.sort(array, new Comparator<Map.Entry<String, Integer>>() {      
	            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
	                return (o2.getValue().intValue() - o1.getValue().intValue());      
	            }      
	        });
			writer.println("term,frequency");
			for (int i = 0; i<array.size(); i++)
			{
				writer.println(array.get(i).getKey()+","+array.get(i).getValue());
			}
			reader.close();
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
