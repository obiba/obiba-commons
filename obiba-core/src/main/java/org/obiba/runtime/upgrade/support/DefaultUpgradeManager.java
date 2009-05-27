package org.obiba.runtime.upgrade.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.obiba.core.util.ComparableComparator;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.UpgradeException;
import org.obiba.runtime.upgrade.UpgradeManager;
import org.obiba.runtime.upgrade.UpgradeStep;
import org.obiba.runtime.upgrade.VersionModifier;
import org.obiba.runtime.upgrade.VersionProvider;

public class DefaultUpgradeManager implements UpgradeManager {

  /**
   * Used to obtain and modify the current version.
   */
  private VersionModifier currentVersionProvider;

  /**
   * Used to obtain the current runtime version.
   */
  private VersionProvider runtimeVersionProvider;

  /**
   * A list of all available upgrade steps.
   */
  private List<UpgradeStep> upgradeSteps;

  /**
   * A list of listeners to be notified of step executions.
   */
  private List<UpgradeStepExecutionListener> stepListeners;

  /**
   * The comparator implementation to use for comparing two versions.
   */
  private Comparator<Version> versionComparator = new ComparableComparator<Version>();

  public void setCurrentVersionProvider(VersionModifier currentVersionProvider) {
    this.currentVersionProvider = currentVersionProvider;
  }

  public void setRuntimeVersionProvider(VersionProvider runtimeVersionProvider) {
    this.runtimeVersionProvider = runtimeVersionProvider;
  }

  public void setUpgradeSteps(List<UpgradeStep> upgradeSteps) {
    this.upgradeSteps = upgradeSteps;
  }

  public void setVersionComparator(Comparator<Version> versionComparator) {
    this.versionComparator = versionComparator;
  }

  public void setStepListeners(List<UpgradeStepExecutionListener> stepListeners) {
    this.stepListeners = stepListeners;
  }

  public void executeUpgrade() throws UpgradeException {

    List<UpgradeStep> steps = getApplicableSteps();
    for(UpgradeStep step : steps) {

      notifyBeforeStep(step);

      try {
        step.execute(getCurrentVersion());

        // Update the current version.
        currentVersionProvider.setVersion(step.getAppliesTo());

        notifyAfterStep(step);
      } catch(Exception e) {
        notifyFailedStep(step, e);

        throw new UpgradeException(e, step);
      }
    }
    currentVersionProvider.setVersion(getRuntimeVersion());
  }

  public Version getCurrentVersion() {
    return currentVersionProvider.getVersion();
  }

  public Version getRuntimeVersion() {
    return runtimeVersionProvider.getVersion();
  }

  /**
   * Returns true when {@code #getCurrentVersion()} is less than {@code #getRuntimeVersion()} using
   * {@code #versionComparator}.
   */
  public boolean requiresUpgrade() {
    return versionComparator.compare(getCurrentVersion(), getRuntimeVersion()) < 0;
  }

  protected void notifyBeforeStep(UpgradeStep step) {
    if(stepListeners != null) {
      for(UpgradeStepExecutionListener listener : stepListeners) {
        listener.onBeforeStep(step);
      }
    }
  }

  protected void notifyAfterStep(UpgradeStep step) {
    if(stepListeners != null) {
      for(UpgradeStepExecutionListener listener : stepListeners) {
        listener.onAfterStep(step);
      }
    }
  }

  protected void notifyFailedStep(UpgradeStep step, Exception e) {
    if(stepListeners != null) {
      for(UpgradeStepExecutionListener listener : stepListeners) {
        listener.onFailedStep(step, e);
      }
    }
  }

  /**
   * Extracts all applicable upgrade steps from the list of possible steps. An applicable step is a step instance that
   * has a {@code Version} that is greater or equal to the {@code Version} returned by {@code #getCurrentVersion()},
   * determined using the {@code #versionComparator}.
   * 
   * @return a new list containing all the applicable steps.
   */
  protected List<UpgradeStep> getApplicableSteps() {
    List<UpgradeStep> applicableSteps = new ArrayList<UpgradeStep>();
    for(UpgradeStep step : upgradeSteps) {
      int diff = versionComparator.compare(getCurrentVersion(), step.getAppliesTo());
      if(diff <= 0) {
        applicableSteps.add(step);
      }
    }

    // Make sure we apply upgrade steps in order of the version they apply to.
    Collections.sort(applicableSteps, new Comparator<UpgradeStep>() {
      public int compare(UpgradeStep step1, UpgradeStep step2) {
        return versionComparator.compare(step1.getAppliesTo(), step2.getAppliesTo());
      }
    });
    return applicableSteps;
  }
}
