package DataPrepare;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import Structure.ArticleExtend;

public class Filter {
	ArrayList<ArticleExtend> data;
	HashMap<String, Integer> df;
	HashMap<String, Integer> bgtf;// background tf
	int docTotalNum;
	StopWordFilter swf;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Filter f = new Filter();
		f.filtNoise("data/news_split_sort_cut.txt", "data/news_split_sort_cut_filted.txt", "data/similarity.txt");
	}
	public double similarity(ArticleExtend a, ArticleExtend b) { // if b == null, use bgtf;
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
		int mergeSize = 0;
		HashMap<String, Integer> btf = (b == null) ? bgtf : b.tf;
		HashMap<String, Double> temp = new HashMap<String, Double>();
		for (String term : a.tf.keySet())
		{
			//double tempa = a.tf.get(term) * Math.log((double)docTotalNum/((double)df.get(term)+1));
			double tempa = 1;
			da += tempa*tempa;
			temp.put(term, tempa);
			mergeSize++;
		}
		for (String term : btf.keySet())
		{
			//double tempb = btf.get(term) * Math.log((double)docTotalNum/((double)df.get(term)+1));
			double tempb = 1;
			db += tempb*tempb;
			if (temp.containsKey(term))
			{
				ret += temp.get(term).doubleValue() * tempb;
			}
			else
			{
				mergeSize++;
			}
		}
		//ret /= Math.sqrt(da*db);
		ret /= mergeSize;
		return ret;
	}
	
	Filter()
	{
		data = new ArrayList<ArticleExtend>();
		df = new HashMap<String, Integer>();
		bgtf = new HashMap<String, Integer>();
		docTotalNum = 0;
		swf = new StopWordFilter();
		swf.load("data/stopwords.txt");
	}
	
	public void load(String input)
	{
		try {
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			ArticleExtend a;
			int count = 0;
			while ((a = (ArticleExtend)ArticleExtend.readArticle(reader)) != null)
			{
				docTotalNum++;
				HashSet<String> termSet = new HashSet<String>();
				String[] term = a.content.split(" ");
				for (int i = 0; i<term.length; i++)
				{
					if (swf.isStopWord(term[i])) continue;
					if (bgtf.containsKey(term[i]))
					{
						Integer temp = bgtf.get(term[i]);
						bgtf.remove(term[i]);
						bgtf.put(term[i], temp+1);
					}
					else
					{
						bgtf.put(term[i], 1);
					}
					if (!termSet.contains(term[i]))
					{
						termSet.add(term[i]);
						if (df.containsKey(term[i]))
						{
							Integer temp = df.get(term[i]);
							df.remove(term[i]);
							df.put(term[i], temp+1);
						}
						else
						{
							df.put(term[i],1);
						}
					}
				}
				count++;
				if (count % 1000 == 0)
				{
					System.out.println(count);
				}
			}
			reader.close();
			System.out.println("Load Finish.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void filtNoise(String input, String output, String similarity)
	{
		try
		{
			this.load(input);
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			FileOutputStream stream2 = new FileOutputStream(similarity);
			OutputStreamWriter sw2 = new OutputStreamWriter(stream2, "utf-8");
			PrintWriter writer2 = new PrintWriter(sw2);
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			ArticleExtend a;
			int count = 0;
			while ((a = (ArticleExtend)ArticleExtend.readArticle(reader)) != null)
			{
				String[] term = a.content.split(" ");
				for (int i = 0; i<term.length; i++)
				{
					if (swf.isStopWord(term[i])) continue;
					if (a.tf.containsKey(term[i]))
					{
						Integer temp = a.tf.get(term[i]);
						a.tf.remove(term[i]);
						a.tf.put(term[i], temp+1);
					}
					else
					{
						a.tf.put(term[i], 1);
					}
				}
				if (this.similarity(a, null) > 0.1)
				{
					a.printArticle(writer);
				}
				else
				{
					writer2.println(a.title);
					writer2.println(this.similarity(a, null));
				}
				count++;
				if (count % 100 == 0)
				{
					System.out.println(count);
				}
				//break;
			}
			writer.close();
			writer2.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	

}
