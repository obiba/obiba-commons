package org.obiba.wicket.markup.html;

import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.PackageResource;

public class ResourceGetter {
  public static Resource getImage(String imagePath) {
    return PackageResource.get(ResourceGetter.class, imagePath);
  }
}
