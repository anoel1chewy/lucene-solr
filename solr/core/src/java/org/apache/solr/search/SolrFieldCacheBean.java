/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.search;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.codahale.metrics.MetricRegistry;
import org.apache.solr.core.SolrInfoBean;
import org.apache.solr.metrics.MetricsMap;
import org.apache.solr.metrics.SolrMetricManager;
import org.apache.solr.metrics.SolrMetricProducer;
import org.apache.solr.uninverting.UninvertingReader;
import org.apache.solr.util.stats.MetricUtils;

/**
 * A SolrInfoBean that provides introspection of the Solr FieldCache
 *
 */
public class SolrFieldCacheBean implements SolrInfoBean, SolrMetricProducer {

  private boolean disableEntryList = Boolean.getBoolean("disableSolrFieldCacheMBeanEntryList");
  private boolean disableJmxEntryList = Boolean.getBoolean("disableSolrFieldCacheMBeanEntryListJmx");

  private MetricRegistry registry;
  private Set<String> metricNames = new HashSet<>();

  @Override
  public String getName() { return this.getClass().getName(); }
  @Override
  public String getDescription() {
    return "Provides introspection of the Solr FieldCache ";
  }
  @Override
  public Category getCategory() { return Category.CACHE; }
  @Override
  public Set<String> getMetricNames() {
    return metricNames;
  }
  @Override
  public MetricRegistry getMetricRegistry() {
    return registry;
  }

  @Override
  public void initializeMetrics(SolrMetricManager manager, String registryName, String scope) {
    registry = manager.registry(registryName);
    MetricsMap metricsMap = new MetricsMap((detailed, map) -> {
      if (detailed && !disableEntryList && !disableJmxEntryList) {
        UninvertingReader.FieldCacheStats fieldCacheStats = UninvertingReader.getUninvertedStats();
        String[] entries = fieldCacheStats.info;
        map.put("entries_count", entries.length);
        map.put("total_size", fieldCacheStats.totalSize);
        for (int i = 0; i < entries.length; i++) {
          final String entry = entries[i];
          map.put("entry#" + i, entry);
        }
      } else {
        map.put("entries_count", UninvertingReader.getUninvertedStatsSize());
      }
    });
    manager.register(this, registryName, metricsMap, true, "fieldCache", Category.CACHE.toString(), scope);
  }
}
