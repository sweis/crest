package crest.keys;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.codec.binary.Base64;

/**
 * Hibernate entity for encrypted session keys
 */
@Entity
@Table(name = "session_keys")
public class SessionKey extends Key implements Serializable {
  private static final long serialVersionUID = 1724559050420055018L;
  private PublicKey publicKey;
  
  public SessionKey() {
    // Empty constructor for Hibernate
  }
  
  public SessionKey(PublicKey publicKey, SecretKeySpec aesKey) throws GeneralSecurityException {
    setPublicKey(publicKey);
    byte[] wrappedSecretKey = publicKey.wrapSecretKey(aesKey);
    setKeyValue(wrappedSecretKey);
    setCreatedOn(new Date());
    generateKeyHash();
  }
  
  @OneToOne(cascade = CascadeType.ALL)
  public PublicKey getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(PublicKey publicKey) {
    this.publicKey = publicKey;
  }
 
  public String toString() {
    return Base64.encodeBase64String(getKeyValue());
  }
}
