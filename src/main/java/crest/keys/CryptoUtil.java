package crest.keys;

import java.security.SecureRandom;

import javax.crypto.spec.SecretKeySpec;

public final class CryptoUtil {
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final String MAC_ALGORITHM = "HMACSHA1";
  private static final String SYMMETRIC_ALGORITHM = "AES";
  private static final int SYMMETRIC_KEY_LENGTH_BYTES = 16;
  private CryptoUtil() { /* Don't new me */ }
  
  public static SecureRandom getSecureRandom() {
    return SECURE_RANDOM;
  }

  public static SecretKeySpec getAesKeySpec(byte[] aesBytes) {
    return new SecretKeySpec(aesBytes, SYMMETRIC_ALGORITHM);
  }
  
  public static SecretKeySpec getHmacKeySpec(byte[] hmacBytes) {
    return new SecretKeySpec(hmacBytes, MAC_ALGORITHM);
  }

  public static SecretKeySpec generateAesKey() {
    byte[] randBytes = new byte[SYMMETRIC_KEY_LENGTH_BYTES];
    getSecureRandom().nextBytes(randBytes);
    return getAesKeySpec(randBytes);
  }
  
}
