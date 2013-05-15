package DataPrepare;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import Structure.Article;
import Structure.ArticleExtend;
import Structure.Event;
import Structure.Subtopic;

public class Ngram {
	
	class Pair
	{
		int f;
		int t;
		
		Pair(int f, int t)
		{
			this.f = f;
			this.t = t;
		}
	}
	
	public HashMap<String, Pair> ntf;
	public StopWordFilter swf;
	double Threshold = 0.90;
	int freThreshold = 300;
	
	
	public HashMap<String, String> dic;
	Vector<String> keyTerm;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Ngram b = new Ngram();
		int n = 2;
		//b.extract(args[0], args[1], n);
		b.loadDic("data/ngram/dic_1.txt");
		b.mergeArticle("data/news_split_sort_cut_filted.txt", "data/source/news_merge_1.txt");
		b = new Ngram();
		b.loadDic("data/ngram/dic_2.txt");
		b.loadSyn("profile/synonym.txt");
		b.mergeArticle("data/source/news_merge_1.txt", "data/source/news_merge_2.txt");
	}
	
	Ngram()
	{
		ntf = new HashMap<String, Pair>();
		dic = new HashMap<String, String>();
		keyTerm = new Vector<String>();
		swf = new StopWordFilter();
		swf.load("data/stopwords.txt");
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
			ArticleExtend a;
			while ((a = ArticleExtend.readArticle(reader)) != null)
			{
				String[] ss = a.content.split(" ");
				HashSet<String> terms = new HashSet<String>();
				HashSet<String> nterms = new HashSet<String>();
				for (int j = 0; j<ss.length; j++)
				{
					String term = ss[j];
					if (swf.notWord(term)) continue;
					
					terms.add(term);
					
					if (j+N-1 < ss.length)
					{
						String nterm = term;
						boolean b = true;
						for (int k = j+1; k<=j+N-1; k++)
						{
							nterm += " " + ss[k];
							if (swf.notWord(ss[k])) b = false;
						}
						if (b && !nterms.contains(nterm))
						{
							if (ntf.containsKey(nterm))
							{
								int f = ntf.get(nterm).f;
								int t = ntf.get(nterm).t;
								ntf.remove(nterm);
								ntf.put(nterm, new Pair(f+1, t));
							}
							else
							{
								ntf.put(nterm, new Pair(1, 0));
							}
							nterms.add(nterm);
						}
					}
				}
				
				nterms = new HashSet<String>();
				for (String nterm : ntf.keySet())
				{
					String term1 = nterm.split(" ")[0];
					String term2 = nterm.split(" ")[1];
					if (terms.contains(term1) && terms.contains(term2))
					{
						nterms.add(nterm);
					}
				}
				
				for (String nterm : nterms)
				{
					int f = ntf.get(nterm).f;
					int t = ntf.get(nterm).t;
					ntf.remove(nterm);
					ntf.put(nterm, new Pair(f, t+1));
				}
				
				count++;
				nterms = new HashSet<String>();
				if (count % 100 == 0) System.out.println(count);
				if (count % 500 == 0)
				{
					for (String nterm : ntf.keySet())
					{
						double r = (double)ntf.get(nterm).f/(double)ntf.get(nterm).t;
						if (ntf.get(nterm).f < 20 || r < Threshold/2)
						{
							nterms.add(nterm);
						}
					}
				}
				
				for (String nterm : nterms)
				{
					ntf.remove(nterm);
				}
			}
			System.out.println("Outputing");
			Vector<String> nterms = new Vector<String>();
			for (String term : ntf.keySet())
			{
				java.text.DecimalFormat df=new java.text.DecimalFormat("#.###");
				//System.out.println(term);
				double r = (double)ntf.get(term).f/(double)ntf.get(term).t;
				if (r >= Threshold && ntf.get(term).f >= freThreshold)
				{
					nterms.add(term+"@$@"+r+"@$@"+ntf.get(term).f+"/"+ntf.get(term).t);
				}
			}
			for (int i = 0; i<nterms.size(); i++)
			{
				String termI = nterms.elementAt(i);
				for (int j = i+1; j<nterms.size(); j++)
				{
					String termJ = nterms.elementAt(j);
					if (Double.parseDouble(termI.substring(termI.indexOf("@$@")+3,termI.lastIndexOf("@$@")))
							< Double.parseDouble(termJ.substring(termJ.indexOf("@$@")+3,termJ.lastIndexOf("@$@"))))
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
					writer.println(termI);
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
				System.out.println(key);
				String value = key.split(" ")[0].substring(0, key.split(" ")[0].indexOf("/")) + key.split(" ")[1].substring(0, key.split(" ")[1].indexOf("/"))+"/me";
				//System.out.println(key+" --> "+value);
				keyTerm.add(key);
				dic.put(key, value);
			}
			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadSyn(String input)
	{
		try
		{
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			String line;
			while ((line = reader.readLine()) != null)
			{
				String key = line.split(" ")[0];
				String value = line.split(" ")[1];
				keyTerm.add(key);
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
				if (a.url.startsWith("http%3A%2F%2Fent")) continue;
				if (a.url.startsWith("http://ent")) continue;
				if (a.source.contains("娱乐")) continue;
				if (a.source.length() > 20) a.source = "";
				for (String term : keyTerm)
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
