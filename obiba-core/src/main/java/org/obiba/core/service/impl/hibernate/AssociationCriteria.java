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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.core.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps a Hibernate {@link Criteria} to allow adding restrictions on association paths.
 */
public class AssociationCriteria {

  static private final Logger log = LoggerFactory.getLogger(AssociationCriteria.class);

  /**
   * Used to add {@link Restrictions} to an {@link AssociationCriteria}
   */
  public enum Operation {

    /**
     * Used to add an {@link AssociationExample} criterion to the Criteria.
     * 
     * Required values:
     * <ol>
     * <li>the template object</li>
     * </ol>
     */
    match {
      @Override
      protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
          String propertyName, Object... values) {
        // Use the whole path (eg: a.b.c) and not the qualifier (eg: a.b)
        Criteria c = criteria.getAssociationCriteria(path, false);
        c.add(AssociationExample.create(values[0]));
        return criteria;
      }
    },

    /**
     * Tests that a property is equal to the specified value.
     * 
     * Required values:
     * <ol>
     * <li>the value to compare to</li>
     * </ol>
     */
    eq {
      @Override
      protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
          String propertyName, Object... values) {
        Criteria c = criteria.getAssociationCriteria(qualifier, false);
        c.add(Restrictions.eq(propertyName, values[0]));
        return criteria;
      }
    },

    /**
     * Tests that a property is not equal to the specified value.
     * 
     * Required values:
     * <ol>
     * <li>the value to compare to</li>
     * </ol>
     */
    ne {
      @Override
      protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
          String propertyName, Object... values) {
        Criteria c = criteria.getAssociationCriteria(qualifier, false);
        c.add(Restrictions.ne(propertyName, values[0]));
        return criteria;
      }
    },

    /**
     * Tests that a property is less or equal to the specified value.
     * 
     * Required values:
     * <ol>
     * <li>the value to compare to</li>
     * </ol>
     */
    le {
      @Override
      protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
          String propertyName, Object... values) {
        Criteria c = criteria.getAssociationCriteria(qualifier, false);
        c.add(Restrictions.le(propertyName, values[0]));
        return criteria;
      }
    },

    /**
     * Tests that a property is less than the specified value.
     * 
     * Required values:
     * <ol>
     * <li>the value to compare to</li>
     * </ol>
     */
    lt {
      @Override
      protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
          String propertyName, Object... values) {
        Criteria c = criteria.getAssociationCriteria(qualifier, false);
        c.add(Restrictions.lt(propertyName, values[0]));
        return criteria;
      }
    },

    /**
     * Tests that a property is greater or equal to the specified value.
     * 
     * Required values:
     * <ol>
     * <li>the value to compare to</li>
     * </ol>
     */
    ge {
      @Override
      protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
          String propertyName, Object... values) {
        Criteria c = criteria.getAssociationCriteria(qualifier, false);
        c.add(Restrictions.ge(propertyName, values[0]));
        return criteria;
      }
    },

    /**
     * Tests that a property is greater than the specified value.
     * 
     * Required values:
     * <ol>
     * <li>the value to compare to</li>
     * </ol>
     */
    gt {
      @Override
      protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
          String propertyName, Object... values) {
        Criteria c = criteria.getAssociationCriteria(qualifier, false);
        c.add(Restrictions.gt(propertyName, values[0]));
        return criteria;
      }
    },

    /**
     * Tests that a property is like (SQL semantic) the specified value.
     * 
     * Required values:
     * <ol>
     * <li>the value to compare to</li>
     * </ol>
     */
    like {
      @Override
      protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
          String propertyName, Object... values) {
        Criteria c = criteria.getAssociationCriteria(qualifier, false);
        c.add(Restrictions.like(propertyName, values[0]));
        return criteria;
      }
    },

    /**
     * Tests that a property is case insensitive like (SQL semantic) the specified value.
     * 
     * Required values:
     * <ol>
     * <li>the value to compare to</li>
     * </ol>
     */
    ilike {
      @Override
      protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
          String propertyName, Object... values) {
        Criteria c = criteria.getAssociationCriteria(qualifier, false);
        c.add(Restrictions.ilike(propertyName, values[0]));
        return criteria;
      }
    },

    /**
     * Tests that a property is empty
     * 
     * Required values: none.
     */
    isEmpty {
      @Override
      protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
          String propertyName, Object... values) {
        Criteria c = criteria.getAssociationCriteria(qualifier, false);
        c.add(Restrictions.isEmpty(propertyName));
        return criteria;
      }
    },

    /**
     * Tests that a property is not empty
     * 
     * Required values: none.
     */
    isNotEmpty {
      @Override
      protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
          String propertyName, Object... values) {
        Criteria c = criteria.getAssociationCriteria(qualifier, false);
        c.add(Restrictions.isNotEmpty(propertyName));
        return criteria;
      }
    },

    /**
     * Tests that a property is NULL
     * 
     * Required values: none.
     */
    isNull {
      @Override
      protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
          String propertyName, Object... values) {
        Criteria c = criteria.getAssociationCriteria(qualifier, false);
        c.add(Restrictions.isNull(propertyName));
        return criteria;
      }
    },

    /**
     * Tests that a property is not NULL
     * 
     * Required values: none.
     */
    isNotNull {
      @Override
      protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
          String propertyName, Object... values) {
        Criteria c = criteria.getAssociationCriteria(qualifier, false);
        c.add(Restrictions.isNotNull(propertyName));
        return criteria;
      }
    },

    /**
     * Union of two {@link Criterion} rooted at the specified path
     * 
     * Required values:
     * <ol>
     * <li>first {@link Criterion}</li>
     * <li>second {@link Criterion}</li>
     * </ol>
     */
    or {
      @Override
      protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
          String propertyName, Object... values) {
        Criteria c = criteria.getAssociationCriteria(path, false);
        if(values.length != 2 || values[0] instanceof Criterion == false || values[1] instanceof Criterion == false) {
          throw new IllegalArgumentException(
              "Operation.or requires exactly two parameters or type org.hibernate.criterion.Criterion");
        }
        c.add(Restrictions.or((Criterion) values[0], (Criterion) values[1]));
        return criteria;
      }
    },

    /**
     * Test whether a property is equal to one of ('in') the specified values
     * 
     * Required values:
     * <ol>
     * <li>Object[]</li>
     * </ol>
     */
    in {
      @Override
      protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
          String propertyName, Object... values) {
        Criteria c = criteria.getAssociationCriteria(qualifier, false);
        c.add(Restrictions.in(propertyName, values));
        return criteria;
      }
    };

    /**
     * Add this operation to the specified AssociationCriteria.
     *
     * @param criteria the criteria
     * @param path the association path (eg: a.b.c.name)
     * @param values the required operation values
     * @return the criteria for method call chaining
     */
    protected AssociationCriteria add(AssociationCriteria criteria, String path, Object... values) {
      String propertyName = getProperty(path);
      String qualifier = getQualifier(path);
      log.debug("Adding operation {}({}) at path {}",
          new Object[] { toString(), StringUtil.deferToString(values), path });
      return add(criteria, path, qualifier, propertyName, values);
    }

    /**
     * Implemented by each operation where the actual {@link Criterion} is added to the {@link Criteria}
     *
     * @param criteria the criteria
     * @param path the association path (eg: a.b.c.name)
     * @param qualifier the entity qualifier (eg: a.b.c)
     * @param propertyName the entity property name (eg: name)
     * @param values the required operation values
     * @return the criteria for method call chaining
     */
    abstract protected AssociationCriteria add(AssociationCriteria criteria, String path, String qualifier,
        String propertyName, Object... values);

  }

  /**
   * The resulting Criteria "rooted" on the initial entity
   */
  private final Criteria baseCriteria;

  /**
   * A Map of association path to the Criteria instance
   */
  private final Map<String, Criteria> associationCriteria = new HashMap<String, Criteria>();

  /**
   * Builds a new instance of an AssociationCriteria for the specified entity type and the specified session.
   *
   * @param entityType the entity type returned by this criteria.
   * @param session the session used to create the Criteria.
   */
  public AssociationCriteria(Class<?> entityType, Session session) {
    baseCriteria = session.createCriteria(entityType);
    associationCriteria.put("", baseCriteria);

    // This is not always required.
    // When required and not present, the result of the criteria is invalid.
    // When not required and present, the result is valid, but performance may suffer.
    // The sensible decision is to always use it...
    baseCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
  }

  /**
   * Constructs a new AssociationCriteria using the builder pattern to allow method chaining.
   *
   * @param entityType the type of entity this Criteria will return
   * @param session the hibernate session used to create the Criteria
   * @return the new instance
   */
  static public AssociationCriteria create(Class<?> entityType, Session session) {
    return new AssociationCriteria(entityType, session);
  }

  /**
   * Adds an {@link Operation} to the {@link Criteria}.
   * 
   * This allows to easily create restrictions on association paths (ie: a.b.c), something
   * that the native Hibernate {@link Criteria} does not allow.
   * 
   * For example, one can create a "Not Null" restriction on a.b.c.name like so:
   * 
   * <pre>
   * AssociationCriteria.create(A.class, session).add(Operation.isNotNull, "a.b.c.name");
   * </pre>
   * Chaining other {@link AssociationCriteria#add} calls, complex queries can be created easily.
   *
   * @param op the operation
   * @param path the association path (eg: a.b.c.name)
   * @param values the parameters required by the {@link Operation} (see the operation javadoc)
   * @return this for method call chaining
   */
  public AssociationCriteria add(String path, Operation op, Object... values) {
    return op.add(this, path, values);
  }

  /**
   * Adds {@link SortingClause}s to the resulting {@link Criteria}.
   *
   * @param clauses an array of {@link SortingClause}
   * @return this for method call chaining
   */
  public AssociationCriteria addSortingClauses(SortingClause... clauses) {
    if(clauses != null && clauses.length > 0) {
      for(SortingClause clause : clauses) {
        if(clause != null) {
          String path = clause.getField();
          String qualifier = getQualifier(path);
          String property = getProperty(path);
          Criteria c = getAssociationCriteria(qualifier, true);
          c.addOrder(clause.isAscending() ? Order.asc(property) : Order.desc(property));
        }
      }
    }
    return this;
  }

  /**
   * Adds a {@link PagingClause} to the resulting {@link Criteria}
   *
   * @param clause the paging clause
   * @return this for method call chaining
   */
  public AssociationCriteria addPagingClause(PagingClause clause) {
    if(clause != null) {
      if(clause.getOffset() > 0) {
        baseCriteria.setFirstResult(clause.getOffset());
      }
      if(clause.getLimit() > 0) {
        baseCriteria.setMaxResults(clause.getLimit());
      }
    }
    return this;
  }

  /**
   * Returns the resulting {@link Criteria}
   *
   * @return the resulting {@link Criteria}
   */
  public Criteria getCriteria() {
    return baseCriteria;
  }

  public int count() {
    Object res = baseCriteria.setProjection(Projections.rowCount()).uniqueResult();
    return res != null ? Long.valueOf(res.toString()).intValue() : 0;
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> list() {
    return baseCriteria.list();
  }

  /**
   * Returns the {@link Criteria} "rooted" at the specified association path. This method will create
   * all the required {@link Criteria} instances along the path.
   *
   * @param path the association path from the root (eg: a.b.c)
   * @param leftJoin whether to use a left join when creating new {@link Criteria} instances (useful when creating paths for sorting)
   * @return the {@link Criteria} instance "rooted" at the specified path.
   */
  protected Criteria getAssociationCriteria(String path, boolean leftJoin) {
    Criteria c = associationCriteria.get(path);
    if(c == null) {
      c = createAssociationCriteria(path, leftJoin);
    }
    return c;
  }

  /**
   * Creates a {@link Criteria} instance for the specified path. This method walks the
   * association path and creates any required {@link Criteria} along the way. After returning,
   * each node in the path will have its own {@link Criteria} instance.
   *
   * @param path the path (eg: a.b.c)
   * @param leftJoin true to create left joins for new {@link Criteria} instances
   * @return an instance of {@link Criteria} "rooted" at the specified path.
   */
  protected Criteria createAssociationCriteria(String path, boolean leftJoin) {
    String[] elements = path.split("\\.");
    Criteria parentCriteria = baseCriteria;
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < elements.length; i++) {
      if(i > 0) sb.append(".");
      String subPath = sb.append(elements[i]).toString();
      Criteria criteria = associationCriteria.get(subPath);
      if(criteria == null) {
        criteria = createSubCriteria(parentCriteria, elements[i], leftJoin);
        associationCriteria.put(subPath, criteria);
      }
      parentCriteria = criteria;
    }
    return parentCriteria;
  }

  /**
   * Creates a sub criteria with the specified Criteria as its parent.
   *
   * @param parentCriteria the parent {@link Criteria}
   * @param property the property name
   * @param leftJoin true to create a left join association
   * @return the new {@link Criteria} instance for the specified property of the parent {@link Criteria}
   */
  protected Criteria createSubCriteria(Criteria parentCriteria, String property, boolean leftJoin) {
    return leftJoin
        ? parentCriteria.createCriteria(property, JoinType.LEFT_OUTER_JOIN)
        : parentCriteria.createCriteria(property);
  }

  /**
   * Given a.b.name returns name
   *
   * @param path the association path to an entity's property
   * @return the property expression
   */
  static private String getProperty(String path) {
    int dotIndex = path.lastIndexOf('.');
    if(dotIndex != -1) {
      return path.substring(dotIndex + 1);
    }
    return path;
  }

  /**
   * Given a.b.name returns a.b
   *
   * @param path the association path to an entity's property
   * @return the qualifier (path from root) to the entity
   */
  static private String getQualifier(String path) {
    int dotIndex = path.lastIndexOf('.');
    if(dotIndex != -1) {
      return path.substring(0, dotIndex);
    }
    // It's the root entity
    return "";
  }

}
