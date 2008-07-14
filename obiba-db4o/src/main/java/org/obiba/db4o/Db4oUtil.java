package org.obiba.db4o;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.obiba.core.util.HexUtil;


import com.db4o.ext.Db4oUUID;

public final class Db4oUtil {

  public static String uuidToString(Db4oUUID uuid) {
    if(uuid == null) return null;

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(baos);
      dos.writeLong(uuid.getLongPart());
      dos.write(uuid.getSignaturePart());
      dos.flush();
      return HexUtil.bytesToHex(baos.toByteArray());
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Db4oUUID stringToUuid(String uuid) {
    if(uuid == null || uuid.length() == 0) return null;

    byte[] bytes = HexUtil.hexToBytes(uuid);
    try {
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      DataInputStream dis = new DataInputStream(bais);
      long longPart = dis.readLong();
      byte[] signaturePart = new byte[dis.available()];
      dis.read(signaturePart);
      return new Db4oUUID(longPart, signaturePart);
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
    
  }
}
