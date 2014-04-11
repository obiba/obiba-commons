/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.git.command;

import org.eclipse.jgit.transport.PushResult;

public interface GitWriteCommand extends GitCommand<Iterable<PushResult>> {

  void setCommitMessage(String commitMessage);

  void setAuthorName(String authorName);

  void setAuthorEmail(String authorEmail);
}

