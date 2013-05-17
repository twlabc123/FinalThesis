package Useless;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import Structure.Article;

public class Useless {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Useless.stat("data/news_split_sort.txt");
	}
	
	public static void stat(String input)
	{
		try {
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			Article a;
			HashMap<String, Integer> m = new HashMap<String, Integer>();
			while((a = Article.readArticle(reader)) != null)
			{
				String year = a.time.substring(0,4);
				if (m.containsKey(year))
				{
					Integer i = m.get(year);
					m.remove(year);
					m.put(year, i+1);
				}
				else
				{
					m.put(year, 1);
				}
			}
			for (int i = 1996; i<=2012; i++)
			{
				System.out.println(i+" : "+m.get(i+""));
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
