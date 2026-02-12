package org.obiba.oidc.shiro.realm;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.json.JSONArray;
import org.obiba.oidc.OIDCConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultOIDCGroupsExtractor implements OIDCGroupsExtractor {

  private static final Logger log = LoggerFactory.getLogger(DefaultOIDCGroupsExtractor.class);

  /**
   * Default claim to inspect for an array of group names to apply.
   */
  private static final String DEFAULT_GROUPS_CLAIM = "groups";

  private final ScriptEngineManager manager;

  public DefaultOIDCGroupsExtractor() {
    // Use the thread context ClassLoader so ScriptEngineManager can discover
    // ScriptEngineFactory implementations that are present on the application's classpath.
    this.manager = new ScriptEngineManager(Thread.currentThread().getContextClassLoader());
  }

  public Set<String> extractGroups(OIDCConfiguration configuration, Map<String, Object> userInfo) {
    Set<String> groups = Sets.newHashSet();
    String groupsJS = configuration.getCustomParam(OIDCRealm.GROUPS_JS_PARAM);

    // if groups JS script is not defined, use the groups claim
    // (either explicitly set or the default one).
    if (Strings.isNullOrEmpty(groupsJS)) {
      // expect a group name or an array of group names from the claim
      String groupsClaim = getGroupsClaim(configuration);
      log.debug("Extracting groups from claim: {}", groupsClaim);
      try {
        Object gps = userInfo.get(groupsClaim);
        if (gps != null) {
          groups.addAll(extractGroups(gps));
        }
      } catch (Exception e) {
        log.debug("UserInfo: {}", userInfo);
        log.warn("Failed at retrieving groups from UserInfo's claim: {}", groupsClaim, e);
      }
    } else {
      // process UserInfo with JS routine
      log.debug("Extracting groups using JS script: {}", groupsJS);
      try {
        ScriptEngine engine = manager.getEngineByName("nashorn");
        if (engine == null) {
          log.error("ScriptEngine is null!!!");
          log.error("EngineFactories=[{}]", manager.getEngineFactories().stream().map(ScriptEngineFactory::getEngineName).collect(Collectors.joining()));
          manager.getEngineFactories().forEach(f -> log.info("EngineFactory: {} '{}'", f.getClass().getSimpleName(), f.getEngineName()));
          return groups;
        }
        Bindings bindings = engine.createBindings();
        bindings.put("userInfo", userInfo);
        Object res = engine.eval(groupsJS, bindings);
        groups.addAll(extractGroups(res));
      } catch (Exception e) {
        log.warn("OIDC groups JS script evaluation failed: {}", groupsJS, e);
      }
    }
    log.debug("Groups found: '{}'", Joiner.on("', '").join(groups));
    return groups;
  }

  protected String getGroupsClaim(OIDCConfiguration configuration) {

    String groupsClaim = configuration.getCustomParam(OIDCRealm.GROUPS_CLAIM_PARAM);
    if (Strings.isNullOrEmpty(groupsClaim)) {
      groupsClaim = DEFAULT_GROUPS_CLAIM;
    }
    return groupsClaim;
  }

  protected Collection<String> extractGroups(Object gps) {
    if (gps instanceof Collection) {
      return Lists.newArrayList(((Collection<?>) gps).stream().filter(Objects::nonNull).map(Object::toString).iterator());
    }
    if (gps instanceof String gpsStr) {
      if (gpsStr.startsWith("[") && gpsStr.endsWith("]")) {
        // try parse as JSON array (e.g. provider returned a JSON array as string)
        try {
          JSONArray arr = new JSONArray(gpsStr);
          return Lists.newArrayList(arr.toList().stream().filter(Objects::nonNull).map(Object::toString).iterator());
        } catch (Exception ignore) {
          // fall through to split
        }
      }
      // split on commas or whitespace (supports "a,b,c", "a b c", "a, b, c", etc.)
      return Lists.newArrayList(Splitter.onPattern("[,\\s]+").trimResults().omitEmptyStrings().splitToList(gpsStr));
    }

    // handle Java arrays
    if (gps != null && gps.getClass().isArray()) {
      Object[] arr = (Object[]) gps;
      return Lists.newArrayList(Arrays.stream(arr).filter(Objects::nonNull).map(Object::toString).iterator());
    }

    // handle Map-like results (e.g. ScriptObjectMirror) by returning values
    if (gps instanceof Map) {
      return Lists.newArrayList(((Map<?, ?>) gps).values().stream().filter(Objects::nonNull).map(Object::toString).iterator());
    }

    // try to convert JavaScript array-like objects (e.g. Nashorn's ScriptObjectMirror)
    try {
      Method toList = gps.getClass().getMethod("toList");
      Object listObj = toList.invoke(gps);
      if (listObj instanceof Collection) {
        return Lists.newArrayList(((Collection<?>) listObj).stream().filter(Objects::nonNull).map(Object::toString).iterator());
      }
    } catch (Exception ignore) {
      // ignore and fall through
    }

    return Lists.newArrayList();
  }
}
