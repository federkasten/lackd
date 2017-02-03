package lackd;

import java.io.Serializable;
import java.util.Comparator;
import java.nio.ByteBuffer;

public class SimpleByteComparator implements Comparator<byte[]>, Serializable {

  private static Long bytesToLong(byte [] v) {
    ByteBuffer buf = ByteBuffer.wrap(v);
    return buf.getLong();
  }

  @Override
  public int compare(byte[] o1, byte[] o2) {
    Long l1 = bytesToLong(o1);
    Long l2 = bytesToLong(o2);

    return l1.compareTo(l2);
  }
}
