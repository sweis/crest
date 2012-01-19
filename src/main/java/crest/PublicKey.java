package crest;

import java.util.Date;

/**
 * Hibernate entity for public keys
 */
public class PublicKey {
  private Long id;
  private String keyValue;
  private Date timestamp;
  
  public PublicKey() {  }

  private Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getKeyValue() {
    return keyValue;
  }

  public void setKeyValue(String keyValue) {
    this.keyValue = keyValue;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }
}
