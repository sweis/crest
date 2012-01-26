package crest;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.hibernate.Session;
import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;

@Path("/v1")
public class RestHandler {
  @PUT
  @Path("/publickey")
  @Produces("text/plain")
  public Response testDatabaseWrite(@Context HttpServletRequest request) {
    if (request.getContentLength() == 0) {
      return Response.status(Status.BAD_REQUEST).entity("No data").build();
    }

    try {
      PublicKey pubKey = new PublicKey(request.getInputStream());
      Session session = HibernateUtil.getSessionFactory().getCurrentSession();
      session.beginTransaction();
      session.save(pubKey);
      session.getTransaction().commit();
      return Response.status(Status.OK).entity(pubKey.getKeyHash()).build();
    } catch (IOException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    } catch (GeneralSecurityException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();  
    } catch (KeyczarException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
  }

  @GET
  @Path("/encrypt/{keyId}/{value}")
  @Consumes("text/plain")
  public Response encryptData(@PathParam("keyId") String keyId,
      @PathParam("value") String value) {
    try {
      Crypter crypter = new Crypter(new KeyReader(keyId));
      return Response.ok(crypter.encrypt(value)).build();
    } catch (KeyczarException e) {
      return Response.status(Status.NOT_FOUND)
          .entity(String.format("Key ID \"%s\" not found", keyId)).build();
    }
  }
}
