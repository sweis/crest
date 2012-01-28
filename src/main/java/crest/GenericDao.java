package crest;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class GenericDao <T, PK extends Serializable> {
    private final SessionFactory sessionFactory;
    private final Class<T> entityClass;
    
    static <U, V extends Serializable> GenericDao<U, V> getGenericDao(Class<U> entityClass, Class<V> keyClass) {
      return new GenericDao<U, V>(entityClass, HibernateUtil.getSessionFactory());
    }
    
    GenericDao(Class<T> entityClass, SessionFactory sessionFactory) {
      this.entityClass = entityClass;
      this.sessionFactory = sessionFactory;
    }
    
    /**
     * @param A new instance to persist in the database
     * @return The primary key of the given instance or null if the operation failed
     */
    @SuppressWarnings("unchecked")  // Casting the primary key to the PK class
    PK save(T toSave) {
      Session session = sessionFactory.getCurrentSession();
      try {
        session.beginTransaction();
        Serializable identifier = session.save(toSave);
        return (PK) identifier;
      } finally {
        session.getTransaction().commit();
      }
    }

    /** 
     * @return An entity with the given ID or null if not found
     */
    @SuppressWarnings("unchecked")  // Casing result to the T class
    T findById(PK id) {
      Session session = sessionFactory.getCurrentSession();
      try {
        session.beginTransaction();
        T value = (T) session.get(entityClass, id);
        return value;
      } finally {
        session.getTransaction().commit();
      }
    }
}
