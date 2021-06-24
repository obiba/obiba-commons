package org.obiba.oidc.shiro.realm;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.json.JSONArray;
import org.obiba.oidc.OIDCConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
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

    // expect an array of group names from the claim
    String groupsClaim = getGroupsClaim(configuration);
    try {
      Object gps = userInfo.get(groupsClaim);
      if (gps != null) {
        extractGroups(gps).forEach(groups::add);
      }
    } catch (Exception e) {
      log.debug("UserInfo: {}", userInfo);
      log.warn("Failed at retrieving groups from UserInfo's claim: {}", groupsClaim, e);
    }

    // process UserInfo with JS routine
    String groupsJS = configuration.getCustomParam(OIDCRealm.GROUPS_JS_PARAM);
    if (!Strings.isNullOrEmpty(groupsJS)) {
      try {
        ScriptEngine engine = manager.getEngineByName("nashorn");
        if (engine == null) {
          log.error("ScriptEngine is null!!!");
          log.info("EngineFactories.size={}", manager.getEngineFactories().size());
          manager.getEngineFactories().forEach(f -> log.info("EngineFactory: {} '{}'", f.getClass().getSimpleName(), f.getEngineName()));
          return groups;
        }
        Bindings bindings = engine.createBindings();
        bindings.put("userInfo", userInfo);
        Object res = engine.eval(groupsJS, bindings);
        if (res != null) {
          if (res instanceof Bindings) {
            Bindings jsRes = ((Bindings) res);
            jsRes.values().stream()
                .filter(g -> g instanceof String)
                .map(Object::toString)
                .forEach(groups::add);
          } else if (res instanceof Collection) {
            ((Collection<Object>) res).stream()
                .filter(g -> g instanceof String)
                .map(Object::toString)
                .forEach(groups::add);
          } else if (res.getClass().isArray()) {
            JSONArray gps = new JSONArray(res);
            gps.toList().stream()
                .filter(g -> g instanceof String)
                .map(Object::toString)
                .forEach(groups::add);
          } else if (res instanceof String) {
            groups.add(res.toString());
          }
        }
      } catch (Exception e) {
        log.warn("OIDC groups JS script evaluation failed: {}", groupsJS, e);
      }
    }
    return groups;
  }

  protected String getGroupsClaim(OIDCConfiguration configuration) {
    String groupsClaim = configuration.getCustomParam(OIDCRealm.GROUPS_CLAIM_PARAM);
    return Strings.isNullOrEmpty(groupsClaim) ? DEFAULT_GROUPS_CLAIM : groupsClaim;
  }

  protected Iterable<String> extractGroups(Object groupsParam) {
    if (groupsParam instanceof Collection) {
      return ((Collection<Object>) groupsParam).stream()
          .filter(Objects::nonNull)
          .map(Object::toString)
          .collect(Collectors.toSet());
    } else {
      String groupsParamStr = groupsParam.toString();
      if (groupsParamStr.startsWith("[") && groupsParamStr.endsWith("]")) {
        // expect a json array
        return new JSONArray(groupsParamStr).toList().stream()
            .filter(Objects::nonNull)
            .map(Object::toString)
            .collect(Collectors.toList());
      }
      if (groupsParamStr.contains(",")) {
        return Splitter.on(",").omitEmptyStrings().trimResults().split(groupsParamStr);
      } else {
        return Splitter.on(" ").omitEmptyStrings().trimResults().split(groupsParamStr);
      }
    }
  }
}
