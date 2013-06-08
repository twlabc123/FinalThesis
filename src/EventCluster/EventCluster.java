package EventCluster;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Vector;

import DataPrepare.StopWordFilter;
import Structure.*;
import System.ActiveEventModule;

/**
 * This is an abstract class for event clustering module.<br>
 * Event clustering module uses raw document data and the active event set of active event module
 * to cluster documents into events.
 * @author twl
 *
 */
public abstract class EventCluster {
	/**
	 * Output file writer
	 */
	PrintWriter writer;
	/**
	 * Input file reader
	 */
	BufferedReader reader;
	/**
	 * Stop word filter
	 */
	StopWordFilter swf;
	/**
	 * Reference to ActiveEventModule
	 */
	ActiveEventModule aem;
	
	/**
	 * Similarity threshold for adding document to an event
	 */
	static double Threshold = 0.80;//cluster threshold
	/**
	 * Only events with more than {@value} are effective
	 */
	public static int Effective = 5;//Event with more than Effective articles is effective
	
	/**
	 * Cluster a batch of documents into event. This will update the active event set.
	 * @param docs the input documents
	 * @param activeEvent the current active event set
	 */
	public abstract void processBatch(Vector<ArticleExtend> docs, Vector<ActiveEvent> activeEvent);
	/**
	 * Output the result when the whole process is over
	 * @param activeEvent the final active event set
	 */
	public abstract void finalOutput(Vector<ActiveEvent> activeEvent);
	/**
	 * Compute the similarity of two articles.
	 * @param a first article
	 * @param b second article
	 * @return
	 */
	public abstract double similarity(ArticleExtend a, ArticleExtend b);
	
	

}
