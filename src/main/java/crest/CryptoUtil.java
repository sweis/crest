package crest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class CryptoUtil {
  private static final String PEM_HEADER = "-----BEGIN PUBLIC KEY-----";
  private static final String PEM_FOOTER = "-----END PUBLIC KEY-----";
  private static final Pattern PEM_HEADER_PATTERN = Pattern.compile("-----BEGIN ([A-Z ]+)-----");
  private static final Pattern PEM_FOOTER_PATTERN = Pattern.compile("-----END ([A-Z ]+)-----");
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final int AES_KEY_LENGTH_BYTES = 16;
  private CryptoUtil() { /* Don't new me */ }
  
  static SecretKeySpec generateAesKey() {
    byte[] aesBytes = new byte[AES_KEY_LENGTH_BYTES];
    SECURE_RANDOM.nextBytes(aesBytes);
    return new SecretKeySpec(aesBytes, "AES");
  }
  
  static RSAPublicKey getJcePublicKey(byte[] keyBytes) throws GeneralSecurityException {
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
    // This will throw an exception if the key spec is not a valid form 
    RSAPublicKey javaPublicKey = (RSAPublicKey) 
        KeyFactory.getInstance("RSA").generatePublic(keySpec);
    return javaPublicKey;
  }
  
  static String derToPem(byte[] derBytes) {
    String encoded = new String(Base64.encodeBase64Chunked(derBytes));
    return String.format("%s\n%s%s\n", PEM_HEADER, encoded, PEM_FOOTER);
  }

  static byte[] maybeConvertPemToDer(byte[] data) throws IOException {
    BufferedReader bis = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)));
    String firstLine = bis.readLine();
    Matcher headerMatcher = PEM_HEADER_PATTERN.matcher(firstLine);
    if (!headerMatcher.matches()) {
      // No properly-formatted header? Assume it's DER format.
      return data;
    } else {
      String expectedFooter = headerMatcher.group(1);
      return decodePemBase64(bis, expectedFooter);
    }
  }

  static byte[] decodePemBase64(BufferedReader inputStream, String expectedFooter)
      throws IOException {
    String line;
    ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
    while ((line = inputStream.readLine()) != null) {
      Matcher footerMatcher = PEM_FOOTER_PATTERN.matcher(line);
      if (!footerMatcher.matches()) {
        tempStream.write(Base64.decodeBase64(line));
      } else if (footerMatcher.group(1).equals(expectedFooter)) {
        return tempStream.toByteArray();
      } else {
        break;
      }
    }
    throw new IOException("Invalid PEM input");
  }
  
}
