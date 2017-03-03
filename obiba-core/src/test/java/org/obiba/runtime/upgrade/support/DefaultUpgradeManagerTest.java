/*
 *
 *  * Copyright (c) 2017 OBiBa. All rights reserved.
 *  *
 *  * This program and the accompanying materials
 *  * are made available under the terms of the GNU Public License v3.0.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.obiba.runtime.upgrade.support;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.runners.MockitoJUnitRunner;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.UpgradeException;
import org.obiba.runtime.upgrade.UpgradeStep;
import org.obiba.runtime.upgrade.VersionModifier;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultUpgradeManagerTest {

  private DefaultUpgradeManager defaultUpgradeManager;

  @Before
  public void setUp() throws Exception {

    defaultUpgradeManager = new DefaultUpgradeManager();
    defaultUpgradeManager.setCurrentVersionProvider(versionModifier(1, 5, 0));
    defaultUpgradeManager.setRuntimeVersionProvider(versionModifier(2, 1, 0));
    defaultUpgradeManager.setNewInstallationDetectionStrategy(notANewInstallation());
  }

  @Test
  public void when_no_upgrade_step__do_nothing() throws UpgradeException {
    defaultUpgradeManager.executeUpgrade();
  }

  @Test
  public void when_many_upgradeSetp__execute_in_correct_order() throws UpgradeException {

    UpgradeStep upgrade1 = upgradeStep(new Version(1, 6, 0));
    UpgradeStep upgrade2 = upgradeStep(new Version(2, 0, 0));
    UpgradeStep upgrade3 = upgradeStep(new Version(1, 7, 0));
    defaultUpgradeManager.setUpgradeSteps(asList(upgrade1, upgrade2, upgrade3));
    InOrder inOrder = inOrder(upgrade1, upgrade2, upgrade3);

    defaultUpgradeManager.executeUpgrade();

    inOrder.verify(upgrade1).execute(any(Version.class));
    inOrder.verify(upgrade3).execute(any(Version.class));
    inOrder.verify(upgrade2).execute(any(Version.class));
  }

  @Test
  public void when_disabled_step__dont_execute_disabled_steps() throws UpgradeException {

    UpgradeStep upgrade1 = upgradeStep(new Version(1, 6, 0));
    UpgradeStep upgrade2 = upgradeStepDisabled(new Version(2, 0, 0));
    UpgradeStep upgrade3 = upgradeStep(new Version(1, 7, 0));
    defaultUpgradeManager.setUpgradeSteps(asList(upgrade1, upgrade2, upgrade3));

    defaultUpgradeManager.executeUpgrade();

    verify(upgrade2, never()).execute(any(Version.class));
  }

  @Test
  public void when_upgrade_step__older_than_migration_start_version__ignore_these_steps() throws UpgradeException {

    UpgradeStep upgrade1 = upgradeStep(new Version(1, 6, 0));
    UpgradeStep upgrade2 = upgradeStepDisabled(new Version(1, 7, 0));
    defaultUpgradeManager.setUpgradeSteps(asList(upgrade1, upgrade2));

    defaultUpgradeManager.executeUpgrade();

    verify(upgrade1).execute(any(Version.class));
    verify(upgrade2, never()).execute(any(Version.class));
  }

  private UpgradeStep upgradeStep(Version version) {
    UpgradeStep upgrade1 = mock(UpgradeStep.class);
    when(upgrade1.getAppliesTo()).thenReturn(version);
    when(upgrade1.mustBeApplied(any(Version.class), any(Version.class))).thenReturn(true);
    return upgrade1;
  }

  private UpgradeStep upgradeStepDisabled(Version version) {
    UpgradeStep upgradeStep = upgradeStep(version);
    when(upgradeStep.mustBeApplied(any(Version.class), any(Version.class))).thenReturn(false);
    return upgradeStep;
  }

  private VersionModifier versionModifier(int major, int minor, int micro) {
    return new VersionModifier() {
      @Override
      public void setVersion(Version version) {
      }

      @Override
      public Version getVersion() {
        return new Version(major, minor, micro);
      }
    };
  }

  private NewInstallationDetectionStrategy notANewInstallation() {
    return runtimeVersionProvider -> false;
  }
}
