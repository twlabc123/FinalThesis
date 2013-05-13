package DataPrepare;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Structure.Article;

public class Sort {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Sort.sort("data/final/news_lite.txt", "data/final/news_lite_sorted.txt");
	}
	
	public static void sort(String input, String output)
	{
		try {
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			BufferedReader reader = new BufferedReader(sr);
			List array = new ArrayList<Article>();
			Article a;
			while ((a = Article.readArticle(reader)) != null)
			{
				array.add(a);
			}
			Collections.sort(array);
			Object o;
			for (int i = 0; i < array.size(); i++)
			{
				a = (Article)array.get(i);
				if (a.time.substring(0,4).compareTo("2012") >= 0)
				a.printArticle(writer);
			}
			reader.close();
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
