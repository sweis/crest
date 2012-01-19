
package crest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;

@Path("/v1")
public class RestHandler {
    @GET 
    @Path("/file/{id}")
    @Produces("text/plain")
    public String getBlob() {
        return "Hi there!";
    }

    @GET
    @Path("/encrypt/{keyId}/{value}")
    @Consumes("text/plain")    
    public Response encryptData(@PathParam("keyId") String keyId, @PathParam("value") String value) {
      try {
        Crypter crypter = new Crypter(new KeyReader(keyId));
        return Response.ok(crypter.encrypt(value)).build();
      } catch (KeyczarException e) {
        return Response.status(Status.NOT_FOUND).entity(String.format("Key ID \"%s\" not found", keyId)).build(); 
      }
    }
}
