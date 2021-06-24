package org.obiba.oidc.shiro.realm;

import org.obiba.oidc.OIDCConfiguration;

import java.util.Map;
import java.util.Set;

public interface OIDCGroupsExtractor {

  Set<String> extractGroups(OIDCConfiguration configuration, Map<String, Object> userInfo);

}
