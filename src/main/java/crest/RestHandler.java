package crest;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/v1")
public class RestHandler {
  private final GenericDao<PublicKey, String> publicKeyDao =
      GenericDao.getGenericDao(PublicKey.class, String.class);
  private final GenericDao<SessionKey, String> sessionKeyDao =
      GenericDao.getGenericDao(SessionKey.class, String.class);

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
    String keyHash = pubKey.getKeyHash();
    PublicKey existingKey = publicKeyDao.findById(keyHash);
    if (existingKey == null) {
      // If the key does not already exist, then store it
      keyHash = publicKeyDao.save(pubKey);
    }
    return Response.status(Status.OK).entity(keyHash).build();      
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
  
  @GET
  @Path("/encrypt/{keyHash}")
  @Produces("text/plain")
  public Response encrypt(@PathParam("keyHash") String keyHash, @Context HttpServletRequest request) {
    PublicKey existingKey = publicKeyDao.findById(keyHash);
    if (existingKey == null) {
      return Response.status(Status.NOT_FOUND).entity(
          String.format("Key %s not found\n", keyHash)).build();      
    } else {
      try {
        // TODO: This doesn't encrypt anything yet. It's just generating an AES session key and wrapping it.
        SecretKeySpec aesKey = CryptoUtil.generateAesKey();
        SessionKey sessionKey = new SessionKey(existingKey, aesKey);
        String sessionKeyHash = sessionKeyDao.save(sessionKey);
        return Response.status(Status.OK).entity(sessionKeyHash).build();
      } catch (GeneralSecurityException e) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      }
    }
  }
}
