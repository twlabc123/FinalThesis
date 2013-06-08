package Structure;

/**
 * The event-subtopic edge representation class
 * @author twl
 *
 */
public class EventEdge
{
	/**
	 * the id of the event
	 */
	public int id;
	/**
	 * the value of the edge
	 */
	public double value;
	
	public EventEdge(int id, double value)
	{
		this.id = id;
		this.value = value;
	}
}