/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.runtime.upgrade.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import org.obiba.core.util.ComparableComparator;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.InstallStep;
import org.obiba.runtime.upgrade.UpgradeException;
import org.obiba.runtime.upgrade.UpgradeManager;
import org.obiba.runtime.upgrade.UpgradeStep;
import org.obiba.runtime.upgrade.VersionModifier;
import org.obiba.runtime.upgrade.VersionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("UnusedDeclaration")
public class DefaultUpgradeManager implements UpgradeManager {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(DefaultUpgradeManager.class);

  //
  // Instance Variables
  //

  /**
   * Used to obtain and modify the current version.
   */
  private VersionModifier currentVersionProvider;

  /**
   * Used to obtain the current runtime version.
   */
  private VersionProvider runtimeVersionProvider;

  /**
   * Strategy that determines if the manager should go through the list of install steps instead of the list of upgrade
   * steps.
   */
  private NewInstallationDetectionStrategy newInstallationDetectionStrategy;

  /**
   * A list of all required installation steps.
   */
  private final Collection<InstallStep> installSteps = new ArrayList<>();

  /**
   * A list of all available upgrade steps.
   */
  private final Collection<UpgradeStep> upgradeSteps = new ArrayList<>();

  /**
   * A list of listeners to be notified of step executions.
   */
  @Nullable
  private List<UpgradeManagerListener> stepListeners;

  /**
   * The comparator implementation to use for comparing two versions.
   */
  private Comparator<Version> versionComparator = new ComparableComparator<>();

  //
  // Constructors
  //

  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
      justification = "XML deserialization")
  public DefaultUpgradeManager() {

  }

  //
  // UpgradeManager Methods
  //

  @SuppressWarnings("ConstantConditions")
  @Override
  public void executeUpgrade() throws UpgradeException {
    if(currentVersionProvider == null) throw new IllegalStateException("currentVersionProvider is required");
    if(runtimeVersionProvider == null) throw new IllegalStateException("runtimeVersionProvider is required");
    if(newInstallationDetectionStrategy == null) {
      throw new IllegalStateException("newInstallationDetectionStrategy is required");
    }
    if(installSteps == null) throw new IllegalStateException("newInstallationDetectionStrategy is required");
    if(upgradeSteps == null) throw new IllegalStateException("newInstallationDetectionStrategy is required");

    // Is this a new installation, or an upgrade of an existing installation?
    if(newInstallationDetectionStrategy.isNewInstallation(runtimeVersionProvider)) {
      executeNewInstallation();
    } else {
      upgradeExistingInstallation();
    }

    // After completing the installation/upgrade, set the current version to the
    // runtime version (i.e., the version installed or upgraded to).
    log.info("Setting version to : {}", getRuntimeVersion());
    currentVersionProvider.setVersion(getRuntimeVersion());
  }

  private void upgradeExistingInstallation() throws UpgradeException {
    for(UpgradeStep step : getApplicableSteps()) {
      try {
        notifyBeforeStep(step);

        step.execute(getCurrentVersion());

        // Update the current version.
        currentVersionProvider.setVersion(step.getAppliesTo());

        notifyAfterStep(step);
      } catch(Exception e) {
        notifyFailedStep(step, e);

        throw new UpgradeException(e);
      }
    }
  }

  private void executeNewInstallation() throws UpgradeException {
    for(InstallStep installStep : installSteps) {
      try {
        notifyBeforeStep(installStep);
        installStep.execute(getCurrentVersion());
        notifyAfterStep(installStep);
      } catch(Exception e) {
        notifyFailedStep(installStep, e);
        throw new UpgradeException(e);
      }
    }
  }

  @Override
  public Version getCurrentVersion() {
    return currentVersionProvider.getVersion();
  }

  @Override
  public Version getRuntimeVersion() {
    return runtimeVersionProvider.getVersion();
  }

  /**
   * Returns true when {@code #getCurrentVersion()} is less than {@code #getRuntimeVersion()} using {@code
   * #versionComparator}.
   */
  @Override
  public boolean requiresUpgrade() {
    return versionComparator.compare(getCurrentVersion(), getRuntimeVersion()) < 0;
  }

  //
  // Methods
  //

  public void setCurrentVersionProvider(VersionModifier currentVersionProvider) {
    //noinspection ConstantConditions
    if(currentVersionProvider == null) throw new IllegalArgumentException("currentVersionProvider cannot be null");
    this.currentVersionProvider = currentVersionProvider;
  }

  public void setRuntimeVersionProvider(VersionProvider runtimeVersionProvider) {
    //noinspection ConstantConditions
    if(runtimeVersionProvider == null) throw new IllegalArgumentException("runtimeVersionProvider cannot be null");
    this.runtimeVersionProvider = runtimeVersionProvider;
  }

  public void setNewInstallationDetectionStrategy(
      NewInstallationDetectionStrategy newInstallationDetectionStrategy) {
    //noinspection ConstantConditions
    if(newInstallationDetectionStrategy == null) {
      throw new IllegalArgumentException("newInstallationDetectionStrategy cannot be null");
    }
    this.newInstallationDetectionStrategy = newInstallationDetectionStrategy;
  }

  public void setInstallSteps(@Nullable Collection<InstallStep> installSteps) {
    if(installSteps != null) {
      this.installSteps.clear();
      this.installSteps.addAll(installSteps);
    }
  }

  public void setUpgradeSteps(@Nullable Collection<UpgradeStep> upgradeSteps) {
    if(upgradeSteps != null) {
      this.upgradeSteps.clear();
      this.upgradeSteps.addAll(upgradeSteps);
    }
  }

  public void setVersionComparator(Comparator<Version> versionComparator) {
    //noinspection ConstantConditions
    if(versionComparator == null) throw new IllegalArgumentException("versionComparator cannot be null");
    this.versionComparator = versionComparator;
  }

  public void setStepListeners(@Nullable List<UpgradeManagerListener> stepListeners) {
    this.stepListeners = stepListeners;
  }

  protected void notifyBeforeStep(InstallStep step) {
    if(stepListeners != null) {
      for(UpgradeManagerListener listener : stepListeners) {
        listener.onBeforeStep(step);
      }
    }
  }

  protected void notifyAfterStep(InstallStep step) {
    if(stepListeners != null) {
      for(UpgradeManagerListener listener : stepListeners) {
        listener.onAfterStep(step);
      }
    }
  }

  protected void notifyFailedStep(InstallStep step, Exception e) {
    if(stepListeners != null) {
      for(UpgradeManagerListener listener : stepListeners) {
        listener.onFailedStep(step, e);
      }
    }
  }

  protected void notifyBeforeStep(UpgradeStep step) {
    if(stepListeners != null) {
      for(UpgradeManagerListener listener : stepListeners) {
        listener.onBeforeStep(step);
      }
    }
  }

  protected void notifyAfterStep(UpgradeStep step) {
    if(stepListeners != null) {
      for(UpgradeManagerListener listener : stepListeners) {
        listener.onAfterStep(step);
      }
    }
  }

  protected void notifyFailedStep(UpgradeStep step, Exception e) {
    if(stepListeners != null) {
      for(UpgradeManagerListener listener : stepListeners) {
        listener.onFailedStep(step, e);
      }
    }
  }

  /**
   * Extracts all applicable upgrade steps from the list of possible steps. An applicable step is a step instance that
   * has a {@code Version} that is greater than {@code Version} returned by {@code #getCurrentVersion()}, determined
   * using the {@code #versionComparator}.
   *
   * @return a new list containing all the applicable steps.
   */
  protected List<UpgradeStep> getApplicableSteps() {

    Version currentVersion = getCurrentVersion();

    return upgradeSteps.stream()
            .filter(isNewerThanCurrentVersion(currentVersion))
            .filter((step) -> step.mustBeApplied(getCurrentVersion(), getRuntimeVersion()))
            .sorted((step1, step2) -> versionComparator.compare(step1.getAppliesTo(), step2.getAppliesTo()))
            .collect(toList());
  }

  private Predicate<UpgradeStep> isNewerThanCurrentVersion(Version currentVersion) {
    return (step) -> versionComparator.compare(step.getAppliesTo(), currentVersion) > 0;
  }
}
