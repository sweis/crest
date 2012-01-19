package crest;

import org.keyczar.exceptions.KeyczarException;
import org.keyczar.interfaces.KeyczarReader;

public class KeyReader implements KeyczarReader {

  private static final String FIXED_KEY = 
    "{\"hmacKey\": {\"hmacKeyString\": \"9_4wqXs3fyx4VhUCGVN6DelPuuC1XYy-oY2oVxOJ8t0\", \"size\": 256}, \"aesKeyString\": \"lSWq_bw7UIpssD4AvwIEjw\", \"mode\": \"CBC\", \"size\": 128}";
  
  private static final String FIXED_METADATA = 
    "{\"encrypted\": false, \"versions\": [{\"status\": \"PRIMARY\", \"versionNumber\": 1, \"exportable\": false}], \"type\": \"AES\", \"name\": \"Test\", \"purpose\": \"DECRYPT_AND_ENCRYPT\"}";
  
  public KeyReader(String keyId) throws KeyczarException {
    if (!"foo".equals(keyId)) {
      throw new KeyczarException(String.format("Key %s not found", keyId));
    }
  }

  @Override
  public String getKey(int version) throws KeyczarException {
    return getKey();
  }

  @Override
  public String getKey() throws KeyczarException {
    return FIXED_KEY;
  }

  @Override
  public String getMetadata() throws KeyczarException {
    return FIXED_METADATA;
  }

}
