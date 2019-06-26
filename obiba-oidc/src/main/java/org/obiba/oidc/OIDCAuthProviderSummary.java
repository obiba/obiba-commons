package org.obiba.oidc;

public class OIDCAuthProviderSummary {
  private String name;
  private String providerUrl;
  private String title;

  public OIDCAuthProviderSummary() {
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getProviderUrl() {
    return providerUrl;
  }

  public void setProviderUrl(String providerUrl) {
    this.providerUrl = providerUrl;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

}
