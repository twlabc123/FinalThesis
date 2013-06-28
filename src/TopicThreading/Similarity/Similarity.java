package TopicThreading.Similarity;

import Structure.ActiveEvent;
import Structure.Subtopic;
import TopicThreading.TopicThreading;

/**
 * This is the base class for computing similarity between an event and a subject.
 * @author twl
 *
 */
public abstract class Similarity {
	
	/**
	 * The reference to the topic threading module
	 */
	TopicThreading tt;
	
	public abstract double similarity(Subtopic a, ActiveEvent b) throws Exception;

}
