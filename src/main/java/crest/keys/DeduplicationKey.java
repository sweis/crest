package crest.keys;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.apache.commons.codec.binary.Hex;

/**
 * Hibernate entity for internal HMAC keys used for deduplication
 */
@Entity
@Table(name = "deduplication_keys")
public class DeduplicationKey extends Key implements Serializable {
  private static final String MAC_ALGORITHM = "HMACSHA1";
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

  public String computeDedupeHmac(byte[] plaintext) throws GeneralSecurityException {
    SecretKeySpec hmacDedupeKey = CryptoUtil.getHmacKeySpec(getKeyValue());
    Mac hmac = Mac.getInstance(MAC_ALGORITHM);
    hmac.init(hmacDedupeKey);
    byte[] dedupeHmac = hmac.doFinal(plaintext);
    return Hex.encodeHexString(dedupeHmac);
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
