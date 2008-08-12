package org.obiba.onyx.engine.state;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;

/**
 * Base class for Stage states.
 * @author Yannick Marcon
 * 
 */
public abstract class AbstractStageState implements IStageExecution {

  private ITransitionEventSink eventSink;

  private Stage stage;

  protected List<ActionDefinition> actionDefinitions = new ArrayList<ActionDefinition>();
  
  protected List<ActionDefinition> systemActionDefinitions = new ArrayList<ActionDefinition>();

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public Stage getStage() {
    return stage;
  }

  protected void castEvent(TransitionEvent event) {
    eventSink.castEvent(event);
  }

  public void setEventSink(ITransitionEventSink eventSink) {
    this.eventSink = eventSink;
  }

  protected void addAction(ActionDefinition action) {
    actionDefinitions.add(action);
  }

  public List<ActionDefinition> getActionDefinitions() {
    return actionDefinitions;
  }
  
  public ActionDefinition getActionDefinition(ActionType type) {
    for (ActionDefinition def : actionDefinitions) {
      if (def.getType().equals(type))
        return def;
    }
    return null;
  }
  
  public ActionDefinition getSystemActionDefinition(ActionType type) {
    for (ActionDefinition def : systemActionDefinitions) {
      if (def.getType().equals(type))
        return def;
    }
    return null;
  }
  
  protected void addSystemAction(ActionDefinition action) {
    systemActionDefinitions.add(action);
  }

  public List<ActionDefinition> getSystemActionDefinitions() {
    return systemActionDefinitions;
  }

  public void execute(Action action) {
  }

  public void interrupt(Action action) {
  }

  public void skip(Action action) {
  }

  public void stop(Action action) {
  }

  public void complete(Action action) {
  }

  public Component getWidget(String id) {
    return null;
  }

  public boolean isCompleted() {
    return false;
  }

  public boolean isFinal() {
    return false;
  }

  public boolean isInteractive() {
    return false;
  }
  
  public String getMessage() {
    return "";
  }
}