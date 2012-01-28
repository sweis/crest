package crest;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.apache.commons.codec.binary.Hex;

/**
 * Abstract Hibernate entity for keys
 */
@MappedSuperclass
public abstract class Key {
  private byte[] keyValue;
  private String keyHash;
  private Date createdOn;
  
  @Column(updatable = false, name = "key_value", nullable = false, length = 600)
  public byte[] getKeyValue() {
    return keyValue;
  }

  public void setKeyValue(byte[] encryptedKeyValue) {
    this.keyValue = encryptedKeyValue;
  }
  
  @Id
  @Column(updatable = false, name = "key_hash", nullable = false, unique = true)
  public String getKeyHash() {
    return keyHash;
  }

  public void setKeyHash(String keyHash) {
    this.keyHash = keyHash;
  }

  protected void generateKeyHash() throws GeneralSecurityException {
    MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
    setKeyHash(new String(Hex.encodeHex(sha1.digest(getKeyValue()), true)));
  }

  @Column(updatable = false, name = "created_on", nullable = false)
  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }
}
