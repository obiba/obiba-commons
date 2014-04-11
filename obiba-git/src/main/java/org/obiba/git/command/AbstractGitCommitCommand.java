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

import java.io.File;

import javax.validation.constraints.NotNull;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import com.google.common.base.Strings;

/**
 * Base class for all  GIT commands. All subclasses are immutable and must be created by their respective builders
 *
 * @param <T> type of builder
 */
public abstract class AbstractGitCommitCommand<T> extends AbstractGitCommand<T> {

  private static final Logger log = LoggerFactory.getLogger(AbstractGitCommitCommand.class);

  private static final String DEFAULT_AUTHOR_NAME = "Anonymous";

  private static final String DEFAULT_AUTHOR_EMAIL = "anonymous@obiba.org";

  private static final String SHIRO_SECURITY_UTILS_CLASS = "org.apache.shiro.SecurityUtils";

  private String authorName;

  private String authorEmail;

  private final String commitMessage;

  public AbstractGitCommitCommand(@NotNull File repositoryPath, String commitMessage) {
    super(repositoryPath);
    this.commitMessage = commitMessage;
  }

  protected AbstractGitCommitCommand(@NotNull File repositoryPath, String commitMessage, String authorName,
      String authorEmail) {
    this(repositoryPath, commitMessage);
    this.authorName = authorName;
    this.authorEmail = authorEmail;
  }

  protected Iterable<PushResult> commitAndPush(Git git) throws GitAPIException {
    String name = getAuthorName();
    String email = getAuthorEmail();
    log.debug("Commit with author: {} - {} and message: {}", name, email, getCommitMessage());
    git.commit() //
        .setAuthor(name, email) //
        .setCommitter(name, email) //
        .setMessage(getCommitMessage()) //
        .call();
    return git.push().setPushAll().setRemote("origin").call();
  }

  public void setAuthorName(String authorName) {
    this.authorName = authorName;
  }

  public void setAuthorEmail(String authorEmail) {
    this.authorEmail = authorEmail;
  }

  protected String getAuthorName() {
    if(!Strings.isNullOrEmpty(authorName)) return authorName;
    if(ClassUtils.isPresent(SHIRO_SECURITY_UTILS_CLASS, null)) {
      Subject subject = SecurityUtils.getSubject();
      return subject != null && subject.getPrincipal() != null
          ? subject.getPrincipal().toString()
          : DEFAULT_AUTHOR_NAME;
    }
    return DEFAULT_AUTHOR_NAME;
  }

  protected String getAuthorEmail() {
    return Strings.isNullOrEmpty(authorEmail) ? DEFAULT_AUTHOR_EMAIL : authorEmail;
  }

  public String getCommitMessage() {
    return commitMessage;
  }
}
