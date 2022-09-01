package org.obiba.shiro.crypto;

import org.apache.shiro.crypto.DefaultBlockCipherService;

public class LegacyAesCipherService extends DefaultBlockCipherService {

  public LegacyAesCipherService() {
    super("AES");
  }

}
