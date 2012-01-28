package crest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Hibernate entity for public keys
 */
@Entity
@Table(name = "public_keys")
public class PublicKey extends Key implements Serializable {
  private static final long serialVersionUID = -4175765959576175717L;

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
    byte[] x509Data = CryptoUtil.maybeConvertPemToDer(tempStream.toByteArray());
    // This will throw a GeneralSecurityException if the data is formatted incorrectly
    @SuppressWarnings("unused")
    RSAPublicKey jcePublicKey = CryptoUtil.getJcePublicKey(x509Data);
    setCreatedOn(new Date());
    setKeyValue(x509Data);
    generateKeyHash();
  }

  public String toString() {
    return CryptoUtil.derToPem(getKeyValue());
  }
}
