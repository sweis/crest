package crest.keys;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * Hibernate entity for encrypted session keys, which consist of a pair of RSA-wrapped
 * AES and HMAC keys.
 */
@Entity
@Table(name = "session_keys")
public class SessionKey extends Key implements Serializable {
  private static final long serialVersionUID = 1724559050420055018L;
  private PublicKey publicKey;
  private byte[] macKeyValue;
  
  public SessionKey() {
    // Empty constructor for Hibernate
  }
  
  public SessionKey(PublicKey publicKey, SecretKeySpec symmetricKey, SecretKeySpec macKey)
        throws GeneralSecurityException {
    setPublicKey(publicKey);
    byte[] wrappedSecretKey = publicKey.wrapSecretKey(symmetricKey);
    setKeyValue(wrappedSecretKey);
    byte[] wrappedMacKey = publicKey.wrapSecretKey(macKey);
    setMacKeyValue(wrappedMacKey);
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

  @Column(updatable = false, name = "mac_key_value", nullable = false, length = 600)
  public byte[] getMacKeyValue() {
    return macKeyValue;
  }

  public void setMacKeyValue(byte[] wrappedMacKeyValue) {
    this.macKeyValue = wrappedMacKeyValue;
  }

  @Override
  protected void generateKeyHash() throws GeneralSecurityException {
    MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
    sha1.update(getKeyValue());
    sha1.update(getMacKeyValue());
    setKeyHash(new String(Hex.encodeHex(sha1.digest(), true)));
  }

  public String toString() {
    return Base64.encodeBase64String(getKeyValue());
  }
}
