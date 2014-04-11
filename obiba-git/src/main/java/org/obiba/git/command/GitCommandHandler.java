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
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.obiba.git.GitException;
import org.obiba.git.NoSuchGitRepositoryException;
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
                 log.trace("Create read lock for {}", key);
                 return readWriteLock.readLock();
               }
             }
      );

  private final LoadingCache<String, Lock> writeLocks = CacheBuilder.newBuilder() //
      .build(new CacheLoader<String, Lock>() {
               @Override
               public Lock load(@SuppressWarnings("NullableProblems") String key) throws Exception {
                 log.trace("Create write lock for {}", key);
                 return readWriteLock.writeLock();
               }
             }
      );

  public <T> T execute(GitCommand<T> command) {
    lock(command);
    Git git = null;
    try {

      File repositoryPath = command.getRepositoryPath();
      git = new Git(getLocalRepository(repositoryPath));

      fetchAllRepository(git);
      return command.execute(git);

    } catch(IOException | GitAPIException e) {
      throw new GitException(e);
    } finally {
      if(git != null) git.close();
      unlock(command);
    }
  }

  public <T> T execute(GitReadCommand<T> command) {
    String path = command.getRepositoryPath().getAbsolutePath();

    if(!command.getRepositoryPath().exists() || !command.getRepositoryPath().isDirectory()) {
      throw new NoSuchGitRepositoryException(path);
    }

    readLock(path);
    Git git = null;
    try {
      git = new Git(getLocalRepository(command.getRepositoryPath()));
      fetchAllRepository(git);
      return command.execute(git.getRepository().getWorkTree());

    } catch(IOException | GitAPIException e) {
      throw new GitException(e);
    } finally {
      if(git != null) git.close();
      readUnlock(path);
    }
  }

  private void lock(GitCommand<?> command) {
    log.trace("Lock for {}", command.getRepositoryPath().getAbsolutePath());
    getLock(command).lock();
  }

  private void unlock(GitCommand<?> command) {
    log.trace("Unlock for {}", command.getRepositoryPath().getAbsolutePath());
    getLock(command).unlock();
  }

  private synchronized Lock getLock(GitCommand<?> command) {
    try {
      String path = command.getRepositoryPath().getAbsolutePath();
      return command instanceof GitWriteCommand ? writeLocks.get(path) : readLocks.get(path);
    } catch(ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private void readLock(String path) {
    try {
      log.trace("Read lock for {}", path);
      readLocks.get(path).lock();
    } catch(ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private void readUnlock(String path) {
    try {
      log.trace("Read unlock for {}", path);
      readLocks.get(path).unlock();
    } catch(ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private Repository getLocalRepository(File repositoryPath) throws IOException, GitAPIException {
    if(!repositoryPath.exists()) {
      createBareRepository(repositoryPath);
    }
    File localRepoDir = Files.createTempDir();

    CloneCommand clone = new CloneCommand();
    clone.setBare(false);
    clone.setCloneAllBranches(true);
    clone.setURI("file://" + repositoryPath.getAbsolutePath());
    clone.setDirectory(localRepoDir);
    Repository repository = clone.call().getRepository();

    log.debug("Clone {} to {}", repositoryPath.getAbsolutePath(), repository.getWorkTree().getAbsolutePath());
    return repository;
  }

  private Repository createBareRepository(File repositoryPath) throws IOException {
    log.debug("Create bare repository for {}", repositoryPath.getAbsolutePath());
    Repository repository = new FileRepository(repositoryPath);
    repository.create(true);
    return repository;
  }

  private FetchResult fetchAllRepository(Git git) throws GitAPIException {
    return git.fetch().setRefSpecs(DEFAULT_REF_SPEC).call();
  }

}
