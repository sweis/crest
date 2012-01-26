package crest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

final class Util {
  private static final int READ_BUF_SIZE = 8192;
  private final static char[] HEX_CHARS = {
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
  
  private Util() { /* Don't new me */ }
  
  static String byteArrayToHex(byte[] data) {
    char[] chars = new char[data.length * 2];
    for (int i = 0; i < data.length; i++) {
      chars[2 * i] = HEX_CHARS[(data[i] >> 4) & 0x0F ];
      chars[2 * i + 1] = HEX_CHARS[data[i] & 0x0F]; 
    }
    
    return new String(chars);
  }
  
  static byte[] readStreamFully(InputStream inStream) throws IOException {
    ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
    byte[] buf = new byte[Util.READ_BUF_SIZE];
    int bytesRead = 0;
    while ((bytesRead = inStream.read(buf)) != -1) {
      tempStream.write(buf, 0, bytesRead);
    }
    return tempStream.toByteArray();
  }
}
