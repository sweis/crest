package crest;

import java.util.Date;

import org.hibernate.Session;


/** 
 * A quick class to test that we can write to the DB
 */
public class HibTest {
  public static void main(String[] args) {
    if (args.length > 0) {
      Session session = HibernateUtil.getSessionFactory().getCurrentSession();
      session.beginTransaction();
      PublicKey pubKey = new PublicKey();
      pubKey.setTimestamp(new Date());
      pubKey.setKeyValue(args[0]);
      session.save(pubKey);
      session.getTransaction().commit();
    }

    HibernateUtil.getSessionFactory().close();
  }
}