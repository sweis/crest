package crest;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class GenericDao <T, PK extends Serializable> {
    private final SessionFactory sessionFactory;
    private final Class<T> type;
    
    GenericDao(Class<T> type, SessionFactory sessionFactory) {
      this.type = type;
      this.sessionFactory = sessionFactory;
    }
    
    /**
     * @param A new instance to persist in the database
     * @return The primary key of the given instance or null if the operation failed
     */
    @SuppressWarnings("unchecked")  // Casting the primary key to the PK class
    PK save(T toSave) {
      Session session = sessionFactory.getCurrentSession();
      session.beginTransaction();
      Serializable identifier = session.save(toSave);
      session.getTransaction().commit();
      return (PK) identifier;
    }

    /** 
     * @return An entity with the given ID or null if not found
     */
    @SuppressWarnings("unchecked")  // Casing result to the T class
    T findById(PK id) {
      Session session = sessionFactory.getCurrentSession();
      session.beginTransaction();
      T value = (T) session.get(type, id);
      session.getTransaction().commit();
      return value;
    }

    /**
     * @param criteria A search criteria
     * @return A list of any entities matching the given criteria
     */
//    List<T> findByCriteria(DetachedCriteria criteria) {
      
  //  }
}
