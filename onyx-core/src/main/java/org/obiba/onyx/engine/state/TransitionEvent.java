package org.obiba.onyx.engine.state;

/**
 * Transition event is the way performing the transition from on state to another of the stage state machine.
 * @author Yannick Marcon
 * 
 */
public class TransitionEvent implements Comparable<TransitionEvent> {

  /**
   * Successfull validation event.
   */
  public static final TransitionEvent VALID = new TransitionEvent("valid");
  
  /**
   * Failed validation event.
   */
  public static final TransitionEvent INVALID = new TransitionEvent("invalid");
  
  /**
   * Default start event (usually from the beginning to the in progress state).
   */
  public static final TransitionEvent START = new TransitionEvent("start");

  /**
   * Default cancel event (usually goes to beginning state)
   */
  public static final TransitionEvent CANCEL = new TransitionEvent("cancel");

  /**
   * Default complete event (usually from the in progress to the completed state).
   */
  public static final TransitionEvent COMPLETE = new TransitionEvent("complete");

  /**
   * Default skip event (usually from the biginning to the skipped state).
   */
  public static final TransitionEvent SKIP = new TransitionEvent("skip");

  /**
   * Default interrupt event (usually from the in progress to the interrupted state).
   */
  public static final TransitionEvent INTERRUPT = new TransitionEvent("interrupt");

  /**
   * Default resume event (usually from the interrupted to the in progress state).
   */
  public static final TransitionEvent RESUME = new TransitionEvent("resume");

  /**
   * The name of the event that will identify it.
   */
  private String name;

  public TransitionEvent(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  public int compareTo(TransitionEvent o) {
    return name.compareTo(o.name);
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof TransitionEvent) {
      return name.equals(((TransitionEvent) obj).name);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

}
