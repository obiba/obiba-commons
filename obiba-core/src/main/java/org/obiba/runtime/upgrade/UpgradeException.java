package org.obiba.runtime.upgrade;

public class UpgradeException extends Exception {

  private static final long serialVersionUID = -5972984899869970156L;

  private final String stepDescription;

  private final transient UpgradeStep step;

  public UpgradeException(Exception cause) {
    super(cause);
    this.step = null;
    this.stepDescription = null;
  }

  public UpgradeException(Exception cause, UpgradeStep step) {
    super(cause);
    if(step == null) throw new IllegalArgumentException("step cannot be null");
    this.step = step;
    this.stepDescription = step.getDescription();
  }

  public UpgradeStep getStep() {
    return step;
  }

  public String getStepDescription() {
    return stepDescription;
  }

}
