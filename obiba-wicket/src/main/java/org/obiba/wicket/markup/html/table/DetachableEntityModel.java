package org.obiba.wicket.markup.html.table;

import java.io.Serializable;

import org.apache.wicket.model.LoadableDetachableModel;
import org.obiba.core.service.EntityQueryService;


/**
 * Allows detaching/attaching the actual entity from/to the model to save memory.
 */
public class DetachableEntityModel extends LoadableDetachableModel {

  private static final long serialVersionUID = 1606621482493529188L;

  private Serializable id;

  private Class<?> type;

  private EntityQueryService service;

  public DetachableEntityModel(EntityQueryService service, Object o) {
    super(o);
    if(o == null) throw new IllegalArgumentException("model object cannot be null");
    this.service = service;
    this.service.refresh(o);
    this.id = service.getId(o);
    this.type = o.getClass();
  }

  @Override
  protected Object load() {
    return service.get(type, id);
  }

  @Override
  public boolean equals(Object obj) {
    if(obj instanceof DetachableEntityModel) {
      DetachableEntityModel rhs = (DetachableEntityModel)obj;
      return this.id.equals(rhs.id) && this.type.equals(rhs.type);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return 37 * this.id.hashCode() * this.type.hashCode();
  }
}