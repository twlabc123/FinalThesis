package Useless;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;

import DataPreprocess.Sort;
import Structure.Article;
import Structure.ArticleExtend;
import Structure.Event;
/**
 * This class is for any useless function.<br>
 * Totally not important.
 * @author ailab
 *
 */
public class Useless {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Useless.stat("data/final/lc_test.txt","data/final/test.txt");
		Sort.sort("data/final/test.txt", "data/final/test_sorted.txt");
	}
	
	public static void stat(String input, String output)
	{
		try
		{
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			Event e;
			while ((e = Event.readEvent(reader)) != null)
			{
				for (ArticleExtend a : e.article)
				{
					a.printArticle(writer);
				}
			}
			reader.close();
			writer.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}

}
