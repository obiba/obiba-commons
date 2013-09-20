package org.obiba.wicket.markup.html.panel;

import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.wicket.JavascriptEventConfirmation;

/**
 * Same as ConfirmImageLinkPanel, but much cooler. The image location is configurable, and the label is also part of the link.
 */
public abstract class ImageLabelLinkPanel extends Panel {
  private static final long serialVersionUID = 6840916983499844571L;

  public enum ImageLocation {left, right}

  ImageLocation imgLoc = ImageLocation.left;

  /**
   * Image with no label, no confirm message.
   *
   * @param id
   * @param image
   */
  public ImageLabelLinkPanel(String id, Resource image, ImageLocation imgLoc) {
    this(id, image, new Model<String>(""), imgLoc);
  }

  /**
   * Image without confirm message.
   *
   * @param id
   * @param image
   * @param model
   */
  public ImageLabelLinkPanel(String id, Resource image, IModel<?> model, ImageLocation imgLoc) {
    this(id, image, model, null, imgLoc);
  }

  /**
   * Image with a label and confirm message.
   *
   * @param id
   * @param image
   * @param model
   * @param messageModel
   */
  public ImageLabelLinkPanel(String id, Resource image, IModel<?> model, IModel<?> messageModel, ImageLocation imgLoc) {
    super(id);
    Link link = new Link("link") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick() {
        ImageLabelLinkPanel.this.onClick();
      }

    };

    Image leftImg;
    Image rightImg;
    if(imgLoc == ImageLocation.left) {
      leftImg = new Image("leftImage", image);
      rightImg = new Image("rightImage");
      rightImg.setVisible(false);
    } else {
      leftImg = new Image("leftImage");
      rightImg = new Image("rightImage", image);
      leftImg.setVisible(false);
    }
    link.add(leftImg);
    link.add(rightImg);
    link.add(new Label("label", model));

    if(messageModel != null) link.add(new JavascriptEventConfirmation("onclick", messageModel));
    add(link);
  }

  abstract public void onClick();
}
