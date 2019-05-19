/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.wicket.markup.html.border;

import org.apache.wicket.markup.html.border.MarkupComponentBorder;

/**
 * An implementation of {@code IComponentBorder} that adds a HTML {@code &nbsp;} entity
 * after the component to which it is attached. This allows separating multiple components that
 * would otherwise be stuck together.
 * <p>
 * A common use case is when dealing with items of a {@code RepeatingView}. Consider the following markup:
 * </p>
 * <pre>
 *   &lt;wicket:panel&gt;
 *     &lt;a wicket:id="link"&gt;Click me&lt;/a&gt;
 *   &lt;/wicket:panel&gt;
 * </pre>
 * <p>
 * Attaching a repeating view to {@code link} would produce {@code a} tags one after the other without
 * any separation in between (not even a whitespace). To add a non breaking space, attach an instance of
 * this class to each item in the {@code RepeatingView}:
 * </p>
 * <pre>
 *   ...
 *   RepeatingView view = new RepeatingView("link");
 *   SeparatorMarkupComponentBorder border = new SeparatorMarkupComponentBorder();
 *   for(Link link : links) {
 *     link.add(border);
 *     view.add(link);
 *   }
 *   ...
 * </pre>
 * will produce
 * <pre>
 *   &lt;a wicket:id="link"&gt;Click me&lt;/a&gt;&amp;nbsp;&lt;a wicket:id="link"&gt;Click me&lt;/a&gt;
 * </pre>
 */
public class SeparatorMarkupComponentBorder extends MarkupComponentBorder {

  private static final long serialVersionUID = 8704787818551045807L;

  public SeparatorMarkupComponentBorder() {
    // There are no components to add. The border is defined in the associated markup file.
  }

}
