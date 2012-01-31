package crest.files;

import java.security.GeneralSecurityException;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import crest.keys.CryptoUtil;
import crest.keys.PublicKey;
import crest.keys.SessionKey;

@Entity
@Table(name = "encrypted_files")
public class EncryptedFile {
  private String dedupeMac;
  private Date createdOn;
  // TODO: Back this with a storage service rather than the local DB
  private byte[] iv;
  private byte[] ciphertext;
  private byte[] hmac;
  private SessionKey sessionKey;

  public EncryptedFile() {
    // Empty constructor for Hibernate
  }
  
  public EncryptedFile(String dedupeMac, PublicKey publicKey, byte[] plaintext)
        throws GeneralSecurityException {
    SecretKeySpec aesKey = CryptoUtil.generateAesKey();
    SecretKeySpec hmacKey = CryptoUtil.generateHmacKey();
    SessionKey sessionKey = new SessionKey(publicKey, aesKey, hmacKey);
    
    Cipher symmetricCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    symmetricCipher.init(Cipher.ENCRYPT_MODE, aesKey);
    Mac hmac = Mac.getInstance("HMACSHA1");
    hmac.init(hmacKey);
    byte[] iv = symmetricCipher.getIV();
    byte[] ciphertext = symmetricCipher.doFinal(plaintext);
    byte[] ciphertextMac = hmac.doFinal(ciphertext);

    setDedupeMac(dedupeMac);
    setSessionKey(sessionKey);
    setIv(iv);
    setCiphertext(ciphertext);
    setHmac(ciphertextMac);
    setCreatedOn(new Date());
  }
  
  @Id
  @Column(updatable = false, name = "dedupe_mac", nullable = false)
  public String getDedupeMac() {
    return dedupeMac;
  }

  public void setDedupeMac(String dedupeMac) {
    this.dedupeMac = dedupeMac;
  }

  @Column(updatable = false, name = "init_vector", nullable = false)
  public byte[] getIv() {
    return iv;
  }
  
  public void setIv(byte[] iv) {
    this.iv = iv;
  }

  // TODO: This is just for testing now. Artificial 2k limit.
  @Column(updatable = false, name = "ciphertext", nullable = false, length = 2000)
  public byte[] getCiphertext() {
    return ciphertext;
  }

  public void setCiphertext(byte[] ciphertext) {
    this.ciphertext = ciphertext;
  }
  
  @Column(updatable = false, name = "hmac", nullable = false, length = 30)
  public byte[] getHmac() {
    return hmac;
  }

  public void setHmac(byte[] hmac) {
    this.hmac = hmac;
  }
  
  @Column(updatable = false, name = "created_on", nullable = false)
  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }
  
  @OneToOne(cascade = CascadeType.ALL)
  public SessionKey getSessionKey() {
    return sessionKey;
  }

  public void setSessionKey(SessionKey sessionKey) {
    this.sessionKey = sessionKey;
  }
}
