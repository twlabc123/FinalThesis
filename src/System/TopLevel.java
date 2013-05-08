package System;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import Structure.Article;
import Structure.ArticleExtend;
import TopicThreading.TfisfTime;
import EventCluster.EventClusterTFIDF;

public class TopLevel {
	
	EventClusterTFIDF ec;
	TfisfTime sa;
	ActiveEventModule aem;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
	
	TopLevel()
	{
		aem = new ActiveEventModule();
		sa = new TfisfTime(aem);
		ec = new EventClusterTFIDF();
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
						batch.add(a);
					}
				}
				count++;
				if (count % 1000 == 0) System.out.println(count);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
