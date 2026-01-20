/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.shiro.tools.hasher;

import org.apache.shiro.authc.credential.DefaultPasswordService;

/**
 * Inspired from org.apache.shiro.tools.hasher.Hasher and used by Debian while installing Opal.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Hasher {

  private static final DefaultPasswordService PASSWORD_SERVICE = new DefaultPasswordService();

  private Hasher() {}

  public static void main(String... args) {
    if(args == null || args.length != 1) {
      printUsage();
      return;
    }
    try {
      System.out.println(hash(args[0]));
    } catch(Exception e) {
      printException(e);
      System.exit(-1);
    }
  }

  public static String hash(String value) {
     return PASSWORD_SERVICE.encryptPassword(value);
   }

  private static void printException(Exception e) {
    System.out.println();
    System.out.println("Error: ");
    e.printStackTrace(System.out);
    System.out.println(e.getMessage());
  }

  private static void printUsage() {
    System.out.println("Usage: java -jar password-hasher-<version>.jar <value>");
    System.out.println("\nPrint a cryptographic hash (aka message digest) of the specified <value>.");
  }

}
