package crest;

import java.io.IOException;
import java.security.GeneralSecurityException;

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
      new GenericDao<PublicKey, String>(PublicKey.class, HibernateUtil.getSessionFactory());
  
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
    PublicKey existingKey = publicKeyDao.findById(pubKey.getKeyHash());
 
    String message = String.format("Public key already exists: %s\n", pubKey.getKeyHash());
    if (existingKey == null) {
      // If the key does not already exist, then store it
      String keyHash = publicKeyDao.save(pubKey);
      message = String.format("Stored public key: %s\n", keyHash);
    }
    return Response.status(Status.OK).entity(message).build();      
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
}
