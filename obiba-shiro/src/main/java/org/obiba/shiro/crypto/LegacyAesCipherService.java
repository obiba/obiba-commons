package org.obiba.shiro.crypto;

import org.apache.shiro.crypto.cipher.DefaultBlockCipherService;

public class LegacyAesCipherService extends DefaultBlockCipherService {

  public LegacyAesCipherService() {
    super("AES");
  }

}
