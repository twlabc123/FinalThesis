package DataPrepare;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;

import Structure.ArticleExtend;
import Structure.Event;
import Structure.Subtopic;

public class Ngram {
	
	public class neighbor
	{
		public HashMap<String, Integer> left;
		public HashMap<String, Integer> right;
	}
	
	public HashMap<String, Integer> tf;
	public HashMap<String, Integer> ntf;
	public StopWordFilter swf;
	double Threshold = 0.9;
	double AnyThreshold = 0.8;
	double EachThreshold = 1.2;
	int freThreshold = 0;
	
	
	public HashMap<String, String> dic;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Ngram b = new Ngram();
		int n = 2;
		//b.extract("data/final/news_lc_merge.txt", "data/ngram/"+n+"gramdic_"+b.Threshold+"_"+b.AnyThreshold+"_"+b.EachThreshold+".txt", n);
		b.loadDic("data/ngram/2gramdic_0.9_0.8_1.2_2.txt");
		//b.merge("data/final/news_lc_merge.txt", "data/final/news_lc_merge_2.txt");
		b.mergeArticle("data/final/news_merge.txt", "data/final/news_merge_2.txt");
		
	}
	
	Ngram()
	{
		tf = new HashMap<String, Integer>();
		ntf = new HashMap<String, Integer>();
		dic = new HashMap<String, String>();
		swf = new StopWordFilter();
		swf.load("data/sogou/tf.csv");
	}
	
	public void extract(String input, String output, int N)
	{
		try
		{
			int count = 0;
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			Event e;
			Vector<Event> initData = new Vector<Event>();
			while ((e = Event.readEvent(reader)) != null)
			{
				for (int i = 0; i<e.article.size(); i++)
				{
					ArticleExtend a = e.article.elementAt(i);
					String[] ss = a.content.split(" ");
					for (int j = 0; j<ss.length; j++)
					{
						String term = ss[j];
						if (swf.notWord(term)) continue;
						if (tf.containsKey(term))
						{
							Integer temp = tf.get(term);
							tf.remove(term);
							tf.put(term, temp+1);
						}
						else
						{
							tf.put(term, 1);
						}
						
						
						if (j+N-1 < ss.length)
						{
							String nterm = term;
							boolean b = true;
							for (int k = j+1; k<=j+N-1; k++)
							{
								nterm += " " + ss[k];
								if (swf.notWord(ss[k])) b = false;
							}
							if (b)
							{
								if (ntf.containsKey(nterm))
								{
									Integer temp = ntf.get(nterm);
									ntf.remove(nterm);
									ntf.put(nterm, temp+1);
								}
								else
								{
									ntf.put(nterm, 1);
								}
							}
						}
					}
				}
				
				count++;
				if (count % 100 == 0) System.out.println(count);
			}
			System.out.println("Outputing");
			Vector<String> nterms = new Vector<String>();
			for (String term : ntf.keySet())
			{
				if (ntf.get(term) < freThreshold) continue;
				//System.out.println(term);
				String[] t = term.split(" ");
				Vector<Double> r = new Vector<Double>();
				double rate = -0.1;;
				boolean any = true;
				boolean each = true;
				for (int i = 0; i<N; i++)
				{
					double temp = (double)ntf.get(term)/(double)tf.get(t[i]);
					r.add(temp);
					if (rate < temp) rate = temp;
					if (temp < AnyThreshold) any = false;
					if (temp < EachThreshold) each = false;
				}
				java.text.DecimalFormat df=new java.text.DecimalFormat("#.###");
				if ((any && rate > Threshold) || each)
				{
					String nterm = "";
					String out = "";
					for (int i = 0; i<r.size(); i++)
					{
						out += " "+t[i]+":"+df.format(r.elementAt(i));
						nterm += t[i].substring(0, t[i].indexOf("/"));
					}
					nterms.add(term+"@$@"+out);
					//writer.println(nterm+out);
				}
			}
			for (int i = 0; i<nterms.size(); i++)
			{
				String termI = nterms.elementAt(i);
				for (int j = i+1; j<nterms.size(); j++)
				{
					String termJ = nterms.elementAt(j);
					if (ntf.get(termI.substring(0, termI.indexOf("@$@"))) < ntf.get(termJ.substring(0, termJ.indexOf("@$@"))))
					{
						nterms.set(i, termJ);
						nterms.set(j, termI);
						String temp = termI;
						termI = termJ;
						termJ = temp;
					}
				}
				
				if (!(termI.substring(0, termI.indexOf("@$@")).split(" ")[0].endsWith("/nr") &&
						termI.substring(0, termI.indexOf("@$@")).split(" ")[1].endsWith("/nr")))
				{
					writer.println(termI+"---"+ntf.get(termI.substring(0, termI.indexOf("@$@"))));
				}
			}
			
			System.out.println("Finished");
			reader.close();		
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadDic(String input)
	{
		try
		{
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			String line;
			while ((line = reader.readLine()) != null)
			{
				String key = line.substring(0, line.indexOf("@$@"));
				String value = key.split(" ")[0].substring(0, key.split(" ")[0].indexOf("/")) + key.split(" ")[1].substring(0, key.split(" ")[1].indexOf("/"))+"/me";
				//System.out.println(key+" --> "+value);
				dic.put(key, value);
			}
			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void merge(String input, String output)
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
			int count = 0;
			while ((e = Event.readEvent(reader)) != null)
			{
				ArticleExtend a = null;
				for (int i = 0; i<e.article.size(); i++)
				{
					a = e.article.elementAt(i);
					for (String term : dic.keySet())
					{
						a.title = a.title.replaceAll(term, dic.get(term));
						a.content = a.content.replaceAll(term, dic.get(term));
					}
				}
				e.printEvent(writer);
				count++;
				if (count % 100 == 0) System.out.println(count);
			}
			reader.close();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void mergeArticle(String input, String output)
	{
		try
		{
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
				for (String term : dic.keySet())
				{
					a.title = a.title.replaceAll(term, dic.get(term));
					a.content = a.content.replaceAll(term, dic.get(term));
				}
				a.printArticle(writer);
				count++;
				if (count % 100 == 0) System.out.println(count);
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
