package crest.keys;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * Hibernate entity for public keys
 */
@Entity
@Table(name = "public_keys")
public class PublicKey extends Key implements Serializable {
  private static final long serialVersionUID = -4175765959576175717L;
  private static final String ASYMMETRIC_ALGORITHM = "RSA";
  private static final String ASYMMETRIC_MODE = "RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING";
  private static final String PEM_HEADER = "-----BEGIN PUBLIC KEY-----";
  private static final String PEM_FOOTER = "-----END PUBLIC KEY-----";
  private static final Pattern PEM_HEADER_PATTERN = Pattern.compile("-----BEGIN ([A-Z ]+)-----");
  private static final Pattern PEM_FOOTER_PATTERN = Pattern.compile("-----END ([A-Z ]+)-----");
  
  public PublicKey() {
    // Empty constructor for Hibernate
  }

  public PublicKey(InputStream x509Stream) throws GeneralSecurityException, IOException {
    byte[] x509Data = maybeConvertPemToDer(IOUtils.toByteArray(x509Stream));
    setKeyValue(x509Data);
    // This will throw a GeneralSecurityException if the data is formatted incorrectly
    convertToJcePublicKey();
    setCreatedOn(new Date());
    generateKeyHash();
  }
  
  private RSAPublicKey convertToJcePublicKey() throws GeneralSecurityException {
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(getKeyValue());
    // This will throw an exception if the key spec is not a valid form 
    RSAPublicKey javaPublicKey = (RSAPublicKey) 
        KeyFactory.getInstance(ASYMMETRIC_ALGORITHM).generatePublic(keySpec);
    return javaPublicKey;
  }
  
  private Cipher asRsaKeyWrapCipher() throws GeneralSecurityException {
    Cipher rsaCipher = Cipher.getInstance(ASYMMETRIC_MODE);
    rsaCipher.init(Cipher.WRAP_MODE, convertToJcePublicKey());
    return rsaCipher;
  }
  
  byte[] wrapSecretKey(SecretKeySpec secretKey) throws GeneralSecurityException{
    return asRsaKeyWrapCipher().wrap(secretKey);
  }

  public String toString() {
    return derToPem(getKeyValue());
  }
  
  private static String derToPem(byte[] derBytes) {
    String encoded = new String(Base64.encodeBase64Chunked(derBytes));
    return String.format("%s\n%s%s\n", PEM_HEADER, encoded, PEM_FOOTER);
  }

  private static byte[] maybeConvertPemToDer(byte[] data) throws IOException {
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

  private static byte[] decodePemBase64(BufferedReader inputStream, String expectedFooter)
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
