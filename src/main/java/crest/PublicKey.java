package crest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * Hibernate entity for public keys
 */
@Entity
@Table(name = "public_keys")
public class PublicKey implements Serializable {
  private static final long serialVersionUID = -4175765959576175717L;
  private static final String PEM_HEADER = "-----BEGIN PUBLIC KEY-----";
  private static final String PEM_FOOTER = "-----END PUBLIC KEY-----";
  private byte[] keyValue;
  private String keyHash;
  private Date createdOn;

  public PublicKey() {
    // Empty constructor for Hibernate
  }

  public PublicKey(InputStream x509Stream) throws GeneralSecurityException, IOException {
    ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int bytesRead = 0;
    while ((bytesRead = x509Stream.read(buf)) != -1) {
      tempStream.write(buf, 0, bytesRead);
    }
    byte[] x509Data = tempStream.toByteArray();
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(x509Data);

    // This will throw an exception if the key spec is not a valid form 
    RSAPublicKey javaPublicKey = (RSAPublicKey) 
        KeyFactory.getInstance("RSA").generatePublic(keySpec);    
    if (javaPublicKey != null) {
      setCreatedOn(new Date());
      setKeyValue(x509Data);
      MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
      setKeyHash(new String(Hex.encodeHex(sha1.digest(x509Data), true)));
    }
  }

  @Column(updatable = false, name = "key_value", nullable = false, length = 600)
  public byte[] getKeyValue() {
    return keyValue;
  }

  public void setKeyValue(byte[] keyValue) {
    this.keyValue = keyValue;
  }

  @Id
  @Column(updatable = false, name = "key_hash", nullable = false, unique = true)
  public String getKeyHash() {
    return keyHash;
  }

  public void setKeyHash(String keyHash) {
    this.keyHash = keyHash;
  }

  @Column(updatable = false, name = "created_on", nullable = false)
  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }
  
  public String toString() {
    String encoded = new String(Base64.encodeBase64Chunked(getKeyValue()));
    return String.format("%s\n%s%s\n", PEM_HEADER, encoded, PEM_FOOTER);
  }
}
