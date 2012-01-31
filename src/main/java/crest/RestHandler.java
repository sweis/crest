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

import org.apache.commons.io.IOUtils;

import crest.files.EncryptedFile;
import crest.keys.DeduplicationKey;
import crest.keys.PublicKey;

@Path("/v1")
public class RestHandler {
  private final GenericDao<PublicKey, String> publicKeyDao =
      GenericDao.getGenericDao(PublicKey.class, String.class);
  private final GenericDao<DeduplicationKey, String> deduplicationKeyDao =
      GenericDao.getGenericDao(DeduplicationKey.class, String.class);
  private final GenericDao<EncryptedFile, String> encryptedFileDao =
      GenericDao.getGenericDao(EncryptedFile.class, String.class);

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
      publicKeyDao.save(pubKey);
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
    byte[] plaintext = null;
    try {
      plaintext = IOUtils.toByteArray(request.getInputStream());
    } catch (IOException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }

    // HMAC the plaintext contents using an internal HMAC key
    String dedupeMac = null;
    try {
      dedupeMac = dedupeKey.computeDedupeHmac(plaintext);
    } catch (GeneralSecurityException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }

    // See if we already have an existing file with the given dedupe MAC.
    EncryptedFile existingEncryptedFile = encryptedFileDao.findById(dedupeMac);
    if (existingEncryptedFile != null) {
      // We've already encrypted this blob. Return the existing session key hash.
      return Response.status(Status.OK)
          .entity(existingEncryptedFile.getSessionKey().getKeyHash()).build();
    } else {
      // We need to generate a new session key, encrypt the data with it, and wrap the key
      try {
        // Encrypt and Mac the plaintext using a wrapped AES & HMAC session key
        EncryptedFile encryptedFile =
            new EncryptedFile(dedupeMac, existingPublicKey, plaintext);
        //String sessionKeyHash = sessionKeyDao.save(encryptedFile.getSessionKey());
        encryptedFileDao.save(encryptedFile);
        return Response.status(Status.OK).entity(encryptedFile.getSessionKey().getKeyHash()).build();
      } catch (GeneralSecurityException e) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      }
    }
  }
}
