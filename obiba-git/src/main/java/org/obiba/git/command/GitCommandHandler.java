package org.obiba.git.command;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.obiba.git.GitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

@Component
public class GitCommandHandler {

  private static final Logger log = LoggerFactory.getLogger(GitCommandHandler.class);

  private static final List<RefSpec> DEFAULT_REF_SPEC = Lists.newArrayList( //
      new RefSpec("+refs/heads/*:refs/remotes/origin/*"), //
      new RefSpec("+refs/tags/*:refs/tags/*"), //
      new RefSpec("+refs/notes/*:refs/notes/*"));

  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

  private final LoadingCache<String, Lock> readLocks = CacheBuilder.newBuilder() //
      .build(new CacheLoader<String, Lock>() {
               @Override
               public Lock load(@SuppressWarnings("NullableProblems") String key) throws Exception {
                 log.debug("Create read lock for {}", key);
                 return readWriteLock.readLock();
               }
             }
      );

  private final LoadingCache<String, Lock> writeLocks = CacheBuilder.newBuilder() //
      .build(new CacheLoader<String, Lock>() {
               @Override
               public Lock load(@SuppressWarnings("NullableProblems") String key) throws Exception {
                 log.debug("Create write lock for {}", key);
                 return readWriteLock.writeLock();
               }
             }
      );

  public <T> T execute(GitCommand<T> command) {
    lock(command);
    Git git = null;
    try {

      log.debug("repositoryPath: {}", command.getRepositoryPath().getAbsolutePath());

      git = new Git(getLocalRepository(command.getRepositoryPath()));
      fetchAllRepository(git);
      return command.execute(git);

    } catch(IOException | GitAPIException e) {
      throw new GitException(e);
    } finally {
      if(git != null) git.close();
      unlock(command);
    }
  }

  private void lock(GitCommand<?> command) {
    log.debug("Lock for {}", command.getRepositoryPath().getAbsolutePath());
    getLock(command).lock();
  }

  private void unlock(GitCommand<?> command) {
    log.debug("Unlock for {}", command.getRepositoryPath().getAbsolutePath());
    getLock(command).unlock();
  }

  private synchronized Lock getLock(GitCommand<?> command) {
    try {
      String path = command.getRepositoryPath().getAbsolutePath();
      return command instanceof GitCommitCommand ? writeLocks.get(path) : readLocks.get(path);
    } catch(ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private Repository getLocalRepository(File repositoryPath) throws IOException, GitAPIException {
    if(!repositoryPath.exists()) {
      createBareRepository(repositoryPath);
    }

    CloneCommand clone = new CloneCommand();
    clone.setBare(false);
    clone.setCloneAllBranches(true);
    clone.setURI("file://" + repositoryPath.getAbsolutePath());
    clone.setDirectory(Files.createTempDir());
    return clone.call().getRepository();
  }

  private Repository createBareRepository(File repositoryPath) throws IOException {
    log.debug("Create bare repository for {}", repositoryPath.getAbsolutePath());
    Repository repository = new FileRepository(new File(repositoryPath, Constants.DOT_GIT));
    repository.create(true);
    return repository;
  }

  private FetchResult fetchAllRepository(Git git) throws GitAPIException {
    return git.fetch().setRefSpecs(DEFAULT_REF_SPEC).call();
  }

}
