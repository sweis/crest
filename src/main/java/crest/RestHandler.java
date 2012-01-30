package crest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Hex;

import crest.keys.CryptoUtil;
import crest.keys.DeduplicationKey;
import crest.keys.PublicKey;
import crest.keys.SessionKey;

@Path("/v1")
public class RestHandler {
  private final GenericDao<PublicKey, String> publicKeyDao =
      GenericDao.getGenericDao(PublicKey.class, String.class);
  private final GenericDao<SessionKey, String> sessionKeyDao =
      GenericDao.getGenericDao(SessionKey.class, String.class);
  private final GenericDao<DeduplicationKey, String> deduplicationKeyDao =
      GenericDao.getGenericDao(DeduplicationKey.class, String.class);
  
  @PUT
  @Path("/publickey")
  @Produces("text/plain")
  public Response putPublicKey(@Context HttpServletRequest request) {
    if (request.getContentLength() == 0) {
      return Response.status(Status.BAD_REQUEST).entity("No data").build();
    }    
    PublicKey pubKey = null;
    try {
      pubKey = new PublicKey(request.getInputStream());
    } catch (GeneralSecurityException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    } catch (IOException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
    // The public key was properly initialized from the request input stream
    String publicKeyHash = pubKey.getKeyHash();
    PublicKey existingKey = publicKeyDao.findById(publicKeyHash);
    if (existingKey == null) {
      // If the key does not already exist, generate an internal deduplication key, 
      // then store both the public key and dedupe key
      DeduplicationKey dedupeKey;
      try {
        dedupeKey = new DeduplicationKey(pubKey);
      } catch (GeneralSecurityException e) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      }
      publicKeyHash = publicKeyDao.save(pubKey);
      deduplicationKeyDao.save(dedupeKey);
    }
    return Response.status(Status.OK).entity(publicKeyHash).build();      
  }
  
  @GET
  @Path("/publickey/{keyHash}")
  @Produces("text/plain")
  public Response getPublicKey(@PathParam("keyHash") String keyHash) {
    PublicKey existingKey = publicKeyDao.findById(keyHash);
    if (existingKey == null) {
      return Response.status(Status.NOT_FOUND).entity(
          String.format("Key %s not found\n", keyHash)).build();      
    } else {
      return Response.status(Status.OK).entity(existingKey.toString()).build();
    }
  }
  
  @PUT
  @Path("/encrypt/{keyHash}")
  @Produces("text/plain")
  public Response encrypt(@PathParam("keyHash") String keyHash, @Context HttpServletRequest request) {
    if (request.getContentLength() == 0) {
      return Response.status(Status.BAD_REQUEST).entity("No data").build();
    }
    
    // Find the public key by the given key hash
    PublicKey existingPublicKey = publicKeyDao.findById(keyHash);
    if (existingPublicKey == null) {
      return Response.status(Status.NOT_FOUND).entity(
          String.format("Key %s not found\n", keyHash)).build();      
    }
    
    DeduplicationKey dedupeKey = deduplicationKeyDao.findById(existingPublicKey.getKeyHash());
    if (dedupeKey == null) {
      // If for some reason there isn't an existing deduplication key, generate a fresh one
      try {
        dedupeKey = new DeduplicationKey(existingPublicKey);
        deduplicationKeyDao.save(dedupeKey);
      } catch (GeneralSecurityException e) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      }
    }
    
    // See if we have already encrypted this blob of plaintext
    byte[] plaintextBuffer = null;
    try {
      ServletInputStream inputStream = request.getInputStream();
      ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
      byte[] buf = new byte[8192];
      int bytesRead = 0;
      while ((bytesRead = inputStream.read(buf)) != -1) {
        tempStream.write(buf, 0, bytesRead);
      }
      plaintextBuffer = tempStream.toByteArray();
    } catch (IOException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }

    // HMAC the plaintext contents using an internal HMAC key
    SecretKeySpec hmacDedupeKey = CryptoUtil.getHmacKeySpec(dedupeKey.getKeyValue());
    try {
      Mac hmac = Mac.getInstance("HMACSHA1");
      hmac.init(hmacDedupeKey);
      byte[] dedupeHmac = hmac.doFinal(plaintextBuffer);
      String hmacHexString = Hex.encodeHexString(dedupeHmac);
      System.out.println("Hmac: " + hmacHexString);
    } catch (GeneralSecurityException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }

    try {
        // Create an AES session key
        SecretKeySpec aesKey = CryptoUtil.generateAesKey();
        SessionKey sessionKey = new SessionKey(existingPublicKey, aesKey);
        String sessionKeyHash = sessionKeyDao.save(sessionKey);
        return Response.status(Status.OK).entity(sessionKeyHash).build();
    } catch (GeneralSecurityException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }
}
