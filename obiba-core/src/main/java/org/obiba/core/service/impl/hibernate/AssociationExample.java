/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.service.impl.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.annotations.common.util.StringHelper;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;

/**
 * A copy of Hibernate's Example class, with modifications that allow you to
 * include many-to-one and one-to-one associations in Query By Example (QBE)
 * queries.
 *
 * @author original code by Gavin King, modified by jkelly
 * @see <a href="http://forum.hibernate.org/viewtopic.php?t=942872">Example and association class </a>
 */
@SuppressWarnings("UnusedDeclaration")
public class AssociationExample implements Criterion {

  private static final long serialVersionUID = 1545101873498985131L;

  private final Object entity;

  private final Collection<String> excludedProperties = new HashSet<String>();

  private PropertySelector selector;

  private boolean isLikeEnabled;

  private boolean isIgnoreCaseEnabled;

  private MatchMode matchMode;

  private boolean includeAssociations = true;

  /**
   * A strategy for choosing property values for inclusion in the query
   * criteria
   */

  public interface PropertySelector {
    boolean include(Object propertyValue, String propertyName, Type type);
  }

  private static final PropertySelector NOT_NULL = new NotNullPropertySelector();

  private static final PropertySelector ALL = new AllPropertySelector();

  private static final PropertySelector NOT_NULL_OR_ZERO = new NotNullOrZeroPropertySelector();

  static final class AllPropertySelector implements PropertySelector, Serializable {

    private static final long serialVersionUID = -4563517949036036320L;

    @Override
    public boolean include(Object object, String propertyName, Type type) {
      return true;
    }
  }

  static final class NotNullPropertySelector implements PropertySelector, Serializable {

    private static final long serialVersionUID = -1409817364720711277L;

    @Override
    public boolean include(Object object, String propertyName, Type type) {
      return object != null;
    }
  }

  static final class NotNullOrZeroPropertySelector implements PropertySelector, Serializable {

    private static final long serialVersionUID = -2071586786806088292L;

    @Override
    public boolean include(Object object, String propertyName, Type type) {
      return object != null && (!(object instanceof Number) || ((Number) object).longValue() != 0);
    }
  }

  /**
   * Set the property selector
   */
  public AssociationExample setPropertySelector(
      @SuppressWarnings("ParameterHidesMemberVariable") PropertySelector selector) {
    this.selector = selector;
    return this;
  }

  /**
   * Exclude zero-valued properties
   */
  public AssociationExample excludeZeroes() {
    setPropertySelector(NOT_NULL_OR_ZERO);
    return this;
  }

  /**
   * Don't exclude null or zero-valued properties
   */
  public AssociationExample excludeNone() {
    setPropertySelector(ALL);
    return this;
  }

  /**
   * Use the "like" operator for all string-valued properties
   */
  public AssociationExample enableLike(@SuppressWarnings("ParameterHidesMemberVariable") MatchMode matchMode) {
    isLikeEnabled = true;
    this.matchMode = matchMode;
    return this;
  }

  /**
   * Use the "like" operator for all string-valued properties
   */
  public AssociationExample enableLike() {
    return enableLike(MatchMode.EXACT);
  }

  /**
   * Ignore case for all string-valued properties
   */
  public AssociationExample ignoreCase() {
    isIgnoreCaseEnabled = true;
    return this;
  }

  /**
   * Exclude a particular named property
   */
  public AssociationExample excludeProperty(String name) {
    excludedProperties.add(name);
    return this;
  }

  /**
   * Create a new instance, which includes all non-null properties
   * by default
   *
   * @param entity
   * @return a new instance of <tt>Example</tt>
   */
  public static AssociationExample create(Object entity) {
    if(entity == null) throw new NullPointerException("null AssociationExample");
    return new AssociationExample(entity, NOT_NULL);
  }

  protected AssociationExample(Object entity, PropertySelector selector) {
    this.entity = entity;
    this.selector = selector;
  }

  public String toString() {
    return "example (" + entity + ')';
  }

  private boolean isPropertyIncluded(Object value, String name, Type type) {
    return !excludedProperties.contains(name) &&
        selector.include(value, name, type) &&
        (!type.isAssociationType() || type.isAssociationType() &&
            includeAssociations &&
            !type.isCollectionType());
  }

  @Override
  public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {

    StringBuffer buf = new StringBuffer("(");
    EntityPersister meta = criteriaQuery.getFactory().getEntityPersister(criteriaQuery.getEntityName(criteria));
    String[] propertyNames = meta.getPropertyNames();
    Type[] propertyTypes = meta.getPropertyTypes();
    //TODO: get all properties, not just the fetched ones!
    Object[] propertyValues = meta.getPropertyValues(entity);
    for(int i = 0; i < propertyNames.length; i++) {
      Object propertyValue = propertyValues[i];
      String propertyName = propertyNames[i];

      boolean isPropertyIncluded = i != meta.getVersionProperty() &&
          isPropertyIncluded(propertyValue, propertyName, propertyTypes[i]);
      if(isPropertyIncluded) {
        if(propertyTypes[i].isComponentType()) {
          appendComponentCondition(propertyName, propertyValue, (CompositeType) propertyTypes[i], criteria,
              criteriaQuery, buf);
        } else {
          appendPropertyCondition(propertyName, propertyValue, criteria, criteriaQuery, buf);
        }
      }
    }
    if(buf.length() == 1) buf.append("1=1"); //yuck!
    return buf.append(')').toString();
  }

  private static final Object[] TYPED_VALUES = new TypedValue[0];

  @Override
  public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {

    EntityPersister meta = criteriaQuery.getFactory().getEntityPersister(criteriaQuery.getEntityName(criteria));
    String[] propertyNames = meta.getPropertyNames();
    Type[] propertyTypes = meta.getPropertyTypes();
    //TODO: get all properties, not just the fetched ones!
    Object[] values = meta.getPropertyValues(entity);
    List<TypedValue> list = new ArrayList<TypedValue>();
    for(int i = 0; i < propertyNames.length; i++) {
      Object value = values[i];
      Type type = propertyTypes[i];
      String name = propertyNames[i];

      boolean isPropertyIncluded = i != meta.getVersionProperty() && isPropertyIncluded(value, name, type);

      if(isPropertyIncluded) {
        if(propertyTypes[i].isComponentType()) {
          addComponentTypedValues(name, value, (CompositeType) type, list, criteria, criteriaQuery);
        } else {
          addPropertyTypedValue(value, type, list);
        }
      }
    }
    return list.toArray(new TypedValue[list.size()]);
  }

  private EntityMode getEntityMode(Criteria criteria, CriteriaQuery criteriaQuery) {
    EntityPersister meta = criteriaQuery.getFactory().getEntityPersister(criteriaQuery.getEntityName(criteria));
    EntityMode entityMode = meta.getEntityMode();
    if(entityMode == null) {
      throw new ClassCastException(entity.getClass().getName());
    }
    return entityMode;
  }

  protected void addPropertyTypedValue(Object value, Type type, Collection<TypedValue> list) {
    if(value != null) {
      if(value instanceof String) {
        String string = (String) value;
        if(isIgnoreCaseEnabled) string = string.toLowerCase();
        if(isLikeEnabled) string = matchMode.toMatchString(string);
        list.add(new TypedValue(type, string, null));
      } else {
        list.add(new TypedValue(type, value, null));
      }
    }
  }

  protected void addComponentTypedValues(String path, Object component, CompositeType type, List<TypedValue> list,
      Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {

    if(component != null) {
      String[] propertyNames = type.getPropertyNames();
      Type[] subtypes = type.getSubtypes();
      Object[] values = type.getPropertyValues(component, getEntityMode(criteria, criteriaQuery));
      for(int i = 0; i < propertyNames.length; i++) {
        Object value = values[i];
        Type subtype = subtypes[i];
        String subPath = StringHelper.qualify(path, propertyNames[i]);
        if(isPropertyIncluded(value, subPath, subtype)) {
          if(subtype.isComponentType()) {
            addComponentTypedValues(subPath, value, (CompositeType) subtype, list, criteria, criteriaQuery);
          } else {
            addPropertyTypedValue(value, subtype, list);
          }
        }
      }
    }
  }

  protected void appendPropertyCondition(String propertyName, Object propertyValue, Criteria criteria, CriteriaQuery cq,
      StringBuffer buf) throws HibernateException {
    Criterion criterion;
    if(propertyValue != null) {
      boolean isString = propertyValue instanceof String;
      SimpleExpression se = isLikeEnabled && isString
          ? Restrictions.like(propertyName, propertyValue)
          : Restrictions.eq(propertyName, propertyValue);
      criterion = isIgnoreCaseEnabled && isString ? se.ignoreCase() : se;
    } else {
      criterion = Restrictions.isNull(propertyName);
    }
    String critCondition = criterion.toSqlString(criteria, cq);
    if(buf.length() > 1 && critCondition.trim().length() > 0) buf.append(" and ");
    buf.append(critCondition);
  }

  protected void appendComponentCondition(String path, Object component, CompositeType type, Criteria criteria,
      CriteriaQuery criteriaQuery, StringBuffer buf) throws HibernateException {

    if(component != null) {
      String[] propertyNames = type.getPropertyNames();
      Object[] values = type.getPropertyValues(component, getEntityMode(criteria, criteriaQuery));
      Type[] subtypes = type.getSubtypes();
      for(int i = 0; i < propertyNames.length; i++) {
        String subPath = StringHelper.qualify(path, propertyNames[i]);
        Object value = values[i];
        if(isPropertyIncluded(value, subPath, subtypes[i])) {
          Type subtype = subtypes[i];
          if(subtype.isComponentType()) {
            appendComponentCondition(subPath, value, (CompositeType) subtype, criteria, criteriaQuery, buf);
          } else {
            appendPropertyCondition(subPath, value, criteria, criteriaQuery, buf);
          }
        }
      }
    }
  }

  public boolean isIncludeAssociations() {
    return includeAssociations;
  }

  public void setIncludeAssociations(boolean includeAssociations) {
    this.includeAssociations = includeAssociations;
  }
} 
