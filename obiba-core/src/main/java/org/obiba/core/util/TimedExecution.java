/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.core.util;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

/**
 *
 */
public class TimedExecution {

  private long start;

  private long executionTime;

  public TimedExecution start() {
    start = System.currentTimeMillis();
    return this;
  }

  public TimedExecution end() {
    executionTime = System.currentTimeMillis() - start;
    return this;
  }

  public String formatExecutionTime() {
    return PeriodFormat.getDefault().print(new Period(executionTime));
  }

}
