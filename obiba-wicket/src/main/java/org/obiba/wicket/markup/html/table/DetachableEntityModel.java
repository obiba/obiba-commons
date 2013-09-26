package org.obiba.wicket.markup.html.table;

import java.io.Serializable;

import org.apache.wicket.model.LoadableDetachableModel;
import org.obiba.core.domain.IEntity;
import org.obiba.core.service.EntityQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows detaching/attaching the actual entity from/to the model to save memory.
 */
public class DetachableEntityModel<T> extends LoadableDetachableModel<T> {

  private static final long serialVersionUID = 1606621482493529188L;

  private static final Logger log = LoggerFactory.getLogger(DetachableEntityModel.class);

  private final Serializable id;

  private final Class<T> type;

  private final EntityQueryService service;

  public DetachableEntityModel(EntityQueryService service, T o) {
    super(o);
    if(o == null) throw new IllegalArgumentException("model object cannot be null");
    this.service = service;
    getEntityQueryService().refresh(o);
    if(o instanceof IEntity) {
      id = ((IEntity) o).getId();
    } else {
      id = getEntityQueryService().getId(o);
    }
    type = (Class<T>) o.getClass();
  }

  @Override
  protected void onDetach() {
    super.onDetach();
    log.trace("DetachableEntityModel has detached instance id {} of type {}", id, type.getSimpleName());
  }

  @Override
  protected T load() {
    log.trace("DetachableEntityModel is loading instance id {} of type {}", id, type.getSimpleName());
    return getEntityQueryService().get(type, id);
  }

  /**
   * Returns the {@code EntityQueryService} used to load the entity. Extending classes can override the strategy for
   * obtaining the service.
   *
   * @return the instance of {@code EntityQueryService} to use for loading the entity.
   */
  protected EntityQueryService getEntityQueryService() {
    return service;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof DetachableEntityModel) {
      DetachableEntityModel<T> rhs = (DetachableEntityModel<T>) obj;
      return id.equals(rhs.id) && type.equals(rhs.type);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return 37 * id.hashCode() * type.hashCode();
  }
}