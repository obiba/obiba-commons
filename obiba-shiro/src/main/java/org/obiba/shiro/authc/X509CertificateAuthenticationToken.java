/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.shiro.authc;

import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import org.apache.shiro.authc.AuthenticationToken;

public class X509CertificateAuthenticationToken implements AuthenticationToken {

  private static final long serialVersionUID = 1L;

  private final X509Certificate certificate;

  public X509CertificateAuthenticationToken(X509Certificate certificate) {
    if(certificate == null) throw new IllegalArgumentException("certificate cannot be null");
    this.certificate = certificate;
  }

  @Override
  public X509Certificate getCredentials() {
    return certificate;
  }

  @Override
  public X500Principal getPrincipal() {
    return certificate.getSubjectX500Principal();
  }

}