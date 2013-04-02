package EventCluster;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import DataPrepare.StopWordFilter;
import Structure.*;

public class EventClusterTFIDF extends EventCluster {

	ArrayList<ArticleExtend> data;
	HashMap<String, Integer> idf;
	HashMap<String, Integer> bgtf;// background tf
	int docTotalNum;
	StopWordFilter swf;
	Vector<ArticleExtend> leaders = new Vector<ArticleExtend>();
	Vector<Vector<Article>> outputData = new Vector<Vector<Article>>();
	double Threshold = 0.20;
	//double Same = 0.90;
	int Delta = 3;//span of the time window
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EventClusterTFIDF ec = new EventClusterTFIDF();
		//ec.filtNoise("data/news_split_sort_cut.txt", "data/news_split_sort_cut_filted.txt", "data/similarity.txt");
		//ec.leaderCluster("data/test/line1498.txt", "data/test/line1498_lc.txt");
		ec.leaderCluster("data/final/news.txt", "data/final/news_lc.txt");
	}
	
	@Override
	public double similarity(ArticleExtend a, ArticleExtend b) { // if b == null, use bgtf;
		// TODO Auto-generated method stub
		double ret = 0;
		double da = 0.0;
		double db = 0.0;
		HashMap<String, Integer> btf = (b == null) ? bgtf : b.tf;
		HashMap<String, Double> temp = new HashMap<String, Double>();
		for (String term : a.tf.keySet())
		{
			double tempa = a.tf.get(term) * Math.log((double)docTotalNum/((double)idf.get(term)+1));
			da += tempa*tempa;
			temp.put(term, tempa);
		}
		for (String term : btf.keySet())
		{
			double tempb = btf.get(term) * Math.log((double)docTotalNum/((double)idf.get(term)+1));
			db += tempb*tempb;
			if (temp.containsKey(term))
			{
				ret += temp.get(term).doubleValue() * tempb;
			}
		}
		ret /= Math.sqrt(da*db);
		return ret;
	}
	
	EventClusterTFIDF()
	{
		data = new ArrayList<ArticleExtend>();
		idf = new HashMap<String, Integer>();
		bgtf = new HashMap<String, Integer>();
		docTotalNum = 0;
		swf = new StopWordFilter();
		swf.load("data/sogou/tf.csv");
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
						if (idf.containsKey(term[i]))
						{
							Integer temp = idf.get(term[i]);
							idf.remove(term[i]);
							idf.put(term[i], temp+1);
						}
						else
						{
							idf.put(term[i],1);
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
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void leaderCluster(String input, String output)
	{
		try
		{
			leaders = new Vector<ArticleExtend>();
			outputData = new Vector<Vector<Article>>();
			//this.load(input);
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			ArticleExtend a;
			int count = 0;
			while ((a = ArticleExtend.readArticle(reader)) != null)
			{
				docTotalNum++;
				HashSet<String> termSet = new HashSet<String>();
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
						if (idf.containsKey(term[i]))
						{
							Integer temp = idf.get(term[i]);
							idf.remove(term[i]);
							idf.put(term[i], temp+1);
						}
						else
						{
							idf.put(term[i],1);
						}
					}
				}
				
				//just for counting
				count++;
				if (count % 10 == 0)
				{
					System.out.println(count);
				}
				//if (count >= 1000) break;
				
				//Event cluster
				if (leaders.size() == 0)
				{
					leaders.add(a);
					Vector<Article> temp = new Vector<Article>();
					temp.add(a.getArticle());
					outputData.add(temp);
				}
				else
				{
					int i;
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Date d1 = format.parse(a.time.substring(0,11));
					for (i = leaders.size()-1; i >= 0; i--)
					{
						Date d2 = format.parse(leaders.elementAt(i).time.substring(0,11));
						int delta = (int) ((d1.getTime() - d2.getTime()) / (24 * 60 * 60 * 1000));
						if (delta >= Delta)
						{
							for (int j = 0; j<=i; j++)
							{
								writer.println("<event>");
								for (int k = 0; k<outputData.elementAt(0).size(); k++)
								{
									outputData.elementAt(0).elementAt(k).printArticle(writer);
								}
								writer.println("</event>");
								leaders.remove(0);
								outputData.remove(0);
							}
							i = -1;
							break;
						}
						double sim = similarity(a, leaders.elementAt(i));
						if (sim > Threshold)
						{
							//leaders.elementAt(i).content += "\n<"+a.time+">"+a.content;
							outputData.elementAt(i).add(a.getArticle());
							break;
						}
					}
					if (i < 0)
					{
						leaders.add(a);
						Vector<Article> temp = new Vector<Article>();
						temp.add(a.getArticle());
						outputData.add(temp);
					}
				}
			}
			for (int i = 0; i<outputData.size(); i++)
			{
				writer.println("<event>");
				for (int j = 0; j<outputData.elementAt(i).size(); j++)
				{
					outputData.elementAt(i).elementAt(j).printArticle(writer);
				}
				writer.println("</event>");
			}	
			reader.close();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
