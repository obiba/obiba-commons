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
    this.manager = new ScriptEngineManager(null);
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
          extractGroups(gps).forEach(groups::add);
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
        extractGroups(res).forEach(groups::add);
      } catch (Exception e) {
        log.warn("OIDC groups JS script evaluation failed: {}", groupsJS, e);
      }
    }
    log.debug("Groups found: '{}'", Joiner.on("', '").join(groups));
    return groups;
  }

  protected String getGroupsClaim(OIDCConfiguration configuration) {
    String groupsClaim = configuration.getCustomParam(OIDCRealm.GROUPS_CLAIM_PARAM);
    return Strings.isNullOrEmpty(groupsClaim) ? DEFAULT_GROUPS_CLAIM : groupsClaim;
  }

  protected Iterable<String> extractGroups(Object groupsValue) {
    if (groupsValue == null) return Lists.newArrayList();

    if (groupsValue instanceof Bindings bindings) {
      Bindings jsRes = bindings;
      return jsRes.values().stream()
          .filter(g -> g instanceof String)
          .map(Object::toString)
          .collect(Collectors.toSet());
    } else if (groupsValue instanceof Collection collection) {
      return ((Collection<Object>) collection).stream()
          .filter(Objects::nonNull)
          .map(Object::toString)
          .collect(Collectors.toSet());
    } else if (groupsValue.getClass().isArray()) {
      JSONArray gps = new JSONArray(groupsValue);
      return gps.toList().stream()
          .filter(g -> g instanceof String)
          .map(Object::toString)
          .collect(Collectors.toSet());
    } else {
      String groupsValueStr = groupsValue.toString();
      if (groupsValueStr.startsWith("[") && groupsValueStr.endsWith("]")) {
        // expect a json array
        return new JSONArray(groupsValueStr).toList().stream()
            .filter(Objects::nonNull)
            .map(Object::toString)
            .collect(Collectors.toList());
      }
      if (groupsValueStr.contains(",")) {
        return Splitter.on(",").omitEmptyStrings().trimResults().split(groupsValueStr);
      } else {
        return Splitter.on(" ").omitEmptyStrings().trimResults().split(groupsValueStr);
      }
    }
  }
}
