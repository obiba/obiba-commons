package org.obiba.core.service.impl.db4o;

import java.io.Serializable;
import java.util.List;

import org.obiba.core.domain.IEntity;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.DefaultPersistenceManagerImpl;
import org.obiba.core.service.impl.hibernate.PersistenceManagerHibernateImpl;
import org.obiba.db4o.Db4oUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;


import com.db4o.ObjectContainer;
import com.db4o.ext.Db4oUUID;
import com.db4o.ext.ExtObjectContainer;
import com.db4o.query.Query;

@Transactional
public class PersistenceManagerDb4oImpl extends DefaultPersistenceManagerImpl {

  private final Logger log = LoggerFactory.getLogger(PersistenceManagerHibernateImpl.class);

  private ExtObjectContainer objectContainer;

  public void setObjectContainer(ObjectContainer objectContainer) {
    this.objectContainer = objectContainer.ext();
  }

  protected ExtObjectContainer container() {
    return objectContainer;
  }
  
  public void purge() {
    container().purge();
  }

  public void purge(Object entity) {
    container().purge(entity);
  }

  public int count(Object template) {
    return container().queryByExample(template).size();
  }

  public int count(Class<?> type) {
    return container().query(type).size();
  }

  public void delete(Object entity) {
    container().delete(entity);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> type, Serializable id) {
    Db4oUUID uuid = Db4oUtil.stringToUuid((String)id);
    if(uuid == null) {
      return null;
    }
    return (T)container().getByUUID(uuid);
  }

  public Serializable getId(Object o) {
    Db4oUUID uuid = container().getObjectInfo(o).getUUID();
    return Db4oUtil.uuidToString(uuid);
  }

  public <T> List<T> list(Class<T> type, PagingClause paging, SortingClause... clauses) {
    return container().query(type);
  }

  public <T> List<T> list(Class<T> type, SortingClause... clauses) {
    return container().query(type);
  }

  public <T> List<T> match(T template, PagingClause paging, SortingClause... clauses) {
    Query q = container().query();
    q.constrain(template).byExample();
    return q.execute();
  }

  public <T> List<T> match(T template, SortingClause... clauses) {
    return match(template, null, clauses);
  }

  @SuppressWarnings("unchecked")
  public <T> T matchOne(T template, SortingClause... clauses) {
    List<T> set = match(template, clauses);
    return (T) (set.size() > 0 ? set.get(0) : null);
  }

  public <T> T newInstance(Class<T> type) {
    try {
      return type.newInstance();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  public <T> T refresh(T entity) {
    container().refresh(entity, 1);
    return entity;
  }

  public <T> T save(T entity) {
    container().store(entity);
    if(entity instanceof IEntity) {
      IEntity ientity = (IEntity)entity;
      if(ientity.getId() == null) {
        Db4oUUID uuid = container().getObjectInfo(entity).getUUID();
        log.info("Generated UUID {} for entity {}", uuid, entity);
        if(uuid != null) {
          ientity.setId(Db4oUtil.uuidToString(uuid));
          container().store(entity);
        }
      }
    }
    return entity;
  }
  
}
