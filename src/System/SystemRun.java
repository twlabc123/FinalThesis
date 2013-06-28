package System;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import Structure.ArticleExtend;
import TopicThreading.*;
import TopicThreading.Similarity.Tfisf;
import TopicThreading.Similarity.TfisfKeyword;
import Useless.Test;
import EventClustering.*;

/**
 * The entrance of the program
 * @author twl
 *
 */
public class SystemRun {
	
	EventCluster ec;
	TopicThreading tt;
	ActiveEventModule aem;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try
		{
			SystemRun tl = new SystemRun();
			//tl.run("data/final/test_sorted.txt");
			tl.run("data/final/FinalInput.txt");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Initialize the system input and output file. Also those 3 modules(EventClustring, TopicThreading and ActiveEventModule)
	 * @throws Exception
	 */
	SystemRun() throws Exception
	{
		aem = new ActiveEventModule();
		tt = new TopicThreading(aem, "data/final/online_st_new.txt", GlobalConstant.GlobalConstant.TFISFKEYWORD);
		ec = new EventClusterBool(aem, "data/final/online_lc_new.txt");
		aem.tt = tt;
		aem.ec = ec;
	}
	
	/**
	 * Begin to run.
	 * @param input
	 */
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
					// Each batch of documents contains one day's documents
					if (a.time.subSequence(0, 10).equals(batch.firstElement().time.subSequence(0, 10)))// next day
					{
						batch.add(a);
					}
					else
					{
						ec.processBatch(batch, aem.activeEvent);
						tt.processBatch(aem.activeEvent);
						batch.clear();
						batch.add(a);
					}
				}
				tt.docTotalNum++;
				count++;
				if (count % 100 == 0) System.out.println(count);
			}
			if (!batch.isEmpty())
			{
				ec.processBatch(batch, aem.activeEvent);
				tt.processBatch(aem.activeEvent);
			}
			ec.finalOutput(aem.activeEvent);
			tt.finalOutput();
			//System.out.println(ec.eventNum);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
