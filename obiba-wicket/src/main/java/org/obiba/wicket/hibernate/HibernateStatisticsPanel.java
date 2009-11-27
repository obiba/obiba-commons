package org.obiba.wicket.hibernate;

import java.util.Arrays;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;

/**
 * Initial code from http://rockingcode.blogspot.com/2009/04/hibernate-statistics-wicket-page.html copied on 2009-11-26.
 */
public class HibernateStatisticsPanel extends Panel {

  private static final long serialVersionUID = 1813824904948229024L;

  public HibernateStatisticsPanel(String id, IModel<Statistics> statsModel) {
    super(id, statsModel);

    final WebMarkupContainer st = new WebMarkupContainer("stats", new CompoundPropertyModel<Statistics>(statsModel));
    st.add(new Label("isStatisticsEnabled"));
    st.add(new Label("startTime"));
    st.add(new Label("sessionOpenCount"));
    st.add(new Label("sessionCloseCount"));
    st.add(new Label("flushCount"));
    st.add(new Label("connectCount"));
    st.add(new Label("prepareStatementCount"));
    st.add(new Label("closeStatementCount"));
    st.add(new Label("entityLoadCount"));
    st.add(new Label("entityUpdateCount"));
    st.add(new Label("entityInsertCount"));
    st.add(new Label("entityDeleteCount"));
    st.add(new Label("entityFetchCount"));
    st.add(new Label("collectionLoadCount"));
    st.add(new Label("collectionUpdateCount"));
    st.add(new Label("collectionRemoveCount"));
    st.add(new Label("collectionRecreateCount"));
    st.add(new Label("collectionFetchCount"));
    st.add(new Label("secondLevelCacheHitCount"));
    st.add(new Label("secondLevelCacheMissCount"));
    st.add(new Label("secondLevelCachePutCount"));
    st.add(new Label("queryExecutionCount"));
    st.add(new Label("queryExecutionMaxTime"));
    st.add(new Label("queryExecutionMaxTimeQueryString"));
    st.add(new Label("queryCacheHitCount"));
    st.add(new Label("queryCacheMissCount"));
    st.add(new Label("queryCachePutCount"));
    st.add(new Label("commitedTransactionCount"));
    st.add(new Label("transactionCount"));
    st.add(new Label("optimisticFailureCount"));
    add(st);

    ListView<String> entityStats = new ListView<String>("entities", Arrays.asList(getModelObject().getEntityNames())) {

      private static final long serialVersionUID = -2021620693275613902L;

      protected void populateItem(ListItem<String> item) {
        String entityName = item.getModelObject();
        final EntityStatistics entityStat = HibernateStatisticsPanel.this.getModelObject().getEntityStatistics(entityName);
        item.add(new Label("deleteCount", new Model(entityStat.getDeleteCount())));
        item.add(new Label("updateCount", new Model(entityStat.getUpdateCount())));
        item.add(new Label("fetchCount", new Model(entityStat.getFetchCount())));
        item.add(new Label("insertCount", new Model(entityStat.getInsertCount())));
        item.add(new Label("loadCount", new Model(entityStat.getLoadCount())));
        item.add(new Label("optimisticFailureCount", new Model(entityStat.getOptimisticFailureCount())));
        item.add(new Label("entityName", new Model(entityName)));
      }
    };
    add(entityStats);

    ListView<String> collectionStats = new ListView<String>("collections", Arrays.asList(getModelObject().getCollectionRoleNames())) {

      private static final long serialVersionUID = 90593970649625929L;

      protected void populateItem(ListItem<String> item) {
        String collName = item.getModelObject();
        CollectionStatistics collectionStatistics = HibernateStatisticsPanel.this.getModelObject().getCollectionStatistics(collName);
        item.add(new Label("recreateCount", new Model(collectionStatistics.getRecreateCount())));
        item.add(new Label("updateCount", new Model(collectionStatistics.getUpdateCount())));
        item.add(new Label("fetchCount", new Model(collectionStatistics.getFetchCount())));
        item.add(new Label("removeCount", new Model(collectionStatistics.getRemoveCount())));
        item.add(new Label("loadCount", new Model(collectionStatistics.getLoadCount())));
        item.add(new Label("collName", new Model(collName)));
      }
    };
    add(collectionStats);

    ListView<String> queryStats = new ListView<String>("queries", Arrays.asList(getModelObject().getQueries())) {

      private static final long serialVersionUID = 1L;

      protected void populateItem(ListItem<String> item) {
        String queryName = item.getModelObject();
        QueryStatistics queryStatistics = HibernateStatisticsPanel.this.getModelObject().getQueryStatistics(queryName);
        item.setModel(new CompoundPropertyModel(queryStatistics));
        item.add(new Label("cacheHitCount"));
        item.add(new Label("cacheMissCount"));
        item.add(new Label("cachePutCount"));
        item.add(new Label("executionCount"));
        item.add(new Label("executionRowCount"));
        item.add(new Label("executionAvgTime"));
        item.add(new Label("executionMaxTime"));
        item.add(new Label("executionMinTime"));
        item.add(new Label("categoryName"));
      }
    };
    add(queryStats);

    ListView<String> cacheStats = new ListView<String>("caches", Arrays.asList(getModelObject().getSecondLevelCacheRegionNames())) {

      private static final long serialVersionUID = 1L;

      protected void populateItem(ListItem<String> item) {
        String cacheName = (String) item.getModelObject();
        SecondLevelCacheStatistics cacheStatistics = HibernateStatisticsPanel.this.getModelObject().getSecondLevelCacheStatistics(cacheName);
        item.setModel(new CompoundPropertyModel(cacheStatistics));
        item.add(new Label("hitCount"));
        item.add(new Label("missCount"));
        item.add(new Label("putCount"));
        item.add(new Label("elementCountInMemory"));
        item.add(new Label("elementCountOnDisk"));
        item.add(new Label("sizeInMemory"));
        item.add(new Label("categoryName"));
      }
    };
    add(cacheStats);

  }

  public Statistics getModelObject() {
    return (Statistics) getDefaultModel().getObject();
  }

}
