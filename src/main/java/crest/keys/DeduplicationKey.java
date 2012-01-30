package crest.keys;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Hibernate entity for internal HMAC keys used for deduplication
 */
@Entity
@Table(name = "deduplication_keys")
public class DeduplicationKey extends Key implements Serializable { 
  private static final int MAC_KEY_LENGTH_BYTES = 32;
  private static final long serialVersionUID = 1724559050420055018L;
  private PublicKey publicKey;
  
  public DeduplicationKey() {
    // Empty constructor for Hibernate
  }
  
  public DeduplicationKey(PublicKey publicKey) throws GeneralSecurityException {
    setPublicKey(publicKey);
    // Generate an internal HMAC key for message plaintext
    byte[] keyValue = new byte[MAC_KEY_LENGTH_BYTES];
    setKeyValue(keyValue);
    setKeyHash(publicKey.getKeyHash());
    setCreatedOn(new Date());
  }
  
  @OneToOne(optional = false, cascade = CascadeType.ALL)
  @PrimaryKeyJoinColumn
  public PublicKey getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(PublicKey publicKey) {
    this.publicKey = publicKey;
  }
}
