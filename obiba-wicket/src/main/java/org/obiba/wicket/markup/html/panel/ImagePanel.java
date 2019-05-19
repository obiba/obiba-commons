/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.wicket.markup.html.panel;

import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.wicket.markup.html.ResourceGetter;

/**
 * A convient panel for setting an image in a span (typically in a EntityList).
 *
 * @author ymarcon
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
   *
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
      throw new IllegalArgumentException("Image ID should be '" + IMAGE_ID + "'");
    }
    add(image);
  }

  public ImagePanel(String id, ContextImage image) {
    super(id);
    if(image.getId().equals(IMAGE_ID) == false) {
      throw new IllegalArgumentException("Image ID should be '" + IMAGE_ID + "'");
    }
    add(image);
  }
}
