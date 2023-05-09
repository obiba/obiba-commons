/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.git.command;

import java.io.File;

import javax.annotation.Nullable;
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
 */
public abstract class AbstractGitWriteCommand extends AbstractGitCommand<Iterable<PushResult>>
    implements GitWriteCommand {

  private static final Logger log = LoggerFactory.getLogger(AbstractGitWriteCommand.class);

  public static final String DEFAULT_AUTHOR_NAME = "Anonymous";

  public static final String DEFAULT_AUTHOR_EMAIL = "anonymous@obiba.org";

  private static final String SHIRO_SECURITY_UTILS_CLASS = "org.apache.shiro.SecurityUtils";

  private String authorName;

  private String authorEmail;

  private String commitMessage;

  protected AbstractGitWriteCommand(@NotNull File repositoryPath, @Nullable File workPath, String commitMessage) {
    super(repositoryPath, workPath);
    // Current JGit API does not account for empty string and ignores an empty commit message
    this.commitMessage = Strings.emptyToNull(commitMessage);
  }

  protected Iterable<PushResult> commitAndPush(Git git) throws GitAPIException {
    String name = getAuthorName();
    String email = getAuthorEmail();
    log.debug("Commit: {} <{}> - {}", name, email, getCommitMessage());
    git.commit() //
        .setAuthor(name, email) //
        .setCommitter(name, email) //
        .setMessage(getCommitMessage()) //
        .call();
    return git.push().setPushAll().setRemote("origin").call();
  }

  @Override
  public void setCommitMessage(String commitMessage) {
    this.commitMessage = commitMessage;
  }

  @Override
  public void setAuthorName(String authorName) {
    this.authorName = authorName;
  }

  @Override
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
