package org.obiba.wicket.markup.html.panel;

import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.wicket.markup.html.ResourceGetter;



/**
 * A convient panel for setting an image in a span (typically in a EntityList).
 * @author ymarcon
 *
 */
public class ImagePanel extends Panel {

  private static final long serialVersionUID = 234647687L;
  
  public static final String IMAGE_ID = "image";

  /**
   * @param id
   * @param model model of the image
   */
  public ImagePanel(String id, IModel model) {
    super(id, model);
    add(new Image(IMAGE_ID, model));
  }
  
  /**
   * @param id
   * @param resource resource of the image
   */
  public ImagePanel(String id, Resource resource) {
    super(id);
    add(new Image(IMAGE_ID, resource));
  }

  /**
   * Image is accessible to the ResourceGetter
   * @param id
   * @param imagePath path to the ResourceGetter image
   */
  public ImagePanel(String id, String imagePath) {
    super(id);
    add(new Image(IMAGE_ID, ResourceGetter.getImage(imagePath)));
  }

  public ImagePanel(String id, Image image) {
    super(id);
    if(image.getId().equals(IMAGE_ID) == false) {
      throw new IllegalArgumentException("Image ID should be '"+IMAGE_ID+"'");
    }
    add(image);
  }

  public ImagePanel(String id, ContextImage image) {
    super(id);
    if(image.getId().equals(IMAGE_ID) == false) {
      throw new IllegalArgumentException("Image ID should be '"+IMAGE_ID+"'");
    }
    add(image);
  }
}
