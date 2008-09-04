package org.obiba.wicket.markup.html.table;

import java.io.Serializable;

import org.apache.wicket.model.LoadableDetachableModel;
import org.obiba.core.domain.IEntity;
import org.obiba.core.service.EntityQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows detaching/attaching the actual entity from/to the model to save
 * memory.
 */
public class DetachableEntityModel extends LoadableDetachableModel {

	private static final long serialVersionUID = 1606621482493529188L;

	private static final Logger log = LoggerFactory
			.getLogger(DetachableEntityModel.class);

	private Serializable id;

	private Class<?> type;

	private EntityQueryService service;

	public DetachableEntityModel(EntityQueryService service, Object o) {
		super(o);
		if (o == null)
			throw new IllegalArgumentException("model object cannot be null");
		this.service = service;
		this.service.refresh(o);
		if (o instanceof IEntity)
			this.id = ((IEntity) o).getId();
		else
			this.id = service.getId(o);
		this.type = o.getClass();
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		log.trace(
				"DetachableEntityModel has detached instance id {} of type {}",
				id, type.getSimpleName());
	}

	@Override
	protected Object load() {
		log.trace("DetachableEntityModel is loading instance id {} of type {}",
				id, type.getSimpleName());
		return service.get(type, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DetachableEntityModel) {
			DetachableEntityModel rhs = (DetachableEntityModel) obj;
			return this.id.equals(rhs.id) && this.type.equals(rhs.type);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return 37 * this.id.hashCode() * this.type.hashCode();
	}
}