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

  private long end;

  public TimedExecution start() {
    start = System.currentTimeMillis();
    return this;
  }

  public TimedExecution end() {
    if(start == 0) throw new IllegalStateException("Cannot end not started TimedExecution");
    end = System.currentTimeMillis();
    return this;
  }

  public String formatExecutionTime() {
    if(start == 0) throw new IllegalStateException("Cannot format not started TimedExecution");
    if(end == 0) throw new IllegalStateException("Cannot format not ended TimedExecution");
    return PeriodFormat.getDefault().print(new Period(start, end));
  }

}
