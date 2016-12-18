/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.wicket.markup.html;

import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.PackageResource;

public class ResourceGetter {
  public static Resource getImage(String imagePath) {
    return PackageResource.get(ResourceGetter.class, imagePath);
  }
}
