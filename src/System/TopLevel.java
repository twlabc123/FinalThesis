package System;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import Structure.ArticleExtend;
import TopicThreading.MultiView;
import EventCluster.EventClusterTFIDF;

/**
 * Top level of this system
 * @author twl
 *
 */
public class TopLevel {
	
	EventClusterTFIDF ec;
	//TfisfTime sa;
	MultiView sa;
	ActiveEventModule aem;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try
		{
			TopLevel tl = new TopLevel();
			tl.run("data/source/news_merge_2.txt");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	TopLevel() throws Exception
	{
		aem = new ActiveEventModule();
		sa = new MultiView(aem, "data/final/online_st.txt");
		ec = new EventClusterTFIDF(aem, "data/final/online_lc.txt");
		aem.sa = sa;
		aem.ec = ec;
	}
	
	public void run(String input)
	{
		try
		{
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			Vector<ArticleExtend> batch = new Vector<ArticleExtend>();
			ArticleExtend a;
			int count = 0;
			while ((a = ArticleExtend.readArticle(reader)) != null)
			{
				if (batch.size() == 0)
				{
					batch.add(a);
				}
				else
				{
					if (a.time.subSequence(0, 10).equals(batch.firstElement().time.subSequence(0, 10)))
					{
						batch.add(a);
					}
					else
					{
						ec.processBatch(batch, aem.activeEvent);
						sa.processBatch(aem.activeEvent);
						batch.clear();
						//if (count > 1000) break;
						batch.add(a);
					}
				}
				count++;
				if (count % 100 == 0) System.out.println(count);
			}
			if (!batch.isEmpty())
			{
				ec.processBatch(batch, aem.activeEvent);
				sa.processBatch(aem.activeEvent);
			}
			ec.finalOutput(aem.activeEvent);
			sa.finalOutput();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
