package crest;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

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
      Criteria criteria = session.createCriteria(PublicKey.class)
          .add(Restrictions.eq("keyHash", pubKey.getKeyHash()))
          .setProjection(Projections.id());
      String message = String.format("Public key already exists: %s", pubKey.getKeyHash());
      if (criteria.uniqueResult() == null) {
        // If the key does not already exist, then store it
        session.save(pubKey);
        message = String.format("Stored public key: %s", pubKey.getKeyHash());
      }
      session.getTransaction().commit();
      return Response.status(Status.OK).entity(message).build();          
    } catch (IOException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    } catch (GeneralSecurityException e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();  
    }
  }
}
