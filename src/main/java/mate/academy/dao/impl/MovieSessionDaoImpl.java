package mate.academy.dao.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import mate.academy.dao.MovieSessionDao;
import mate.academy.exception.DataProcessingException;
import mate.academy.model.MovieSession;
import mate.academy.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class MovieSessionDaoImpl implements MovieSessionDao {
    @Override
    public MovieSession add(MovieSession movieSession) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.persist(movieSession);
            transaction.commit();
            return movieSession;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't save movie session", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Optional<MovieSession> get(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(MovieSession.class, id));
        } catch (Exception e) {
            throw new DataProcessingException("Can't get movie session by id: " + id, e);
        }
    }

    @Override
    public List<MovieSession> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM MovieSession", MovieSession.class).getResultList();
        } catch (Exception e) {
            throw new DataProcessingException("Can't get all movie session from DB", e);
        }
    }

    @Override
    public List<MovieSession> findAvailableSessions(Long id, LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23,59,59);

            return session.createQuery(
                    "FROM MovieSession ms "
                            +
                            "WHERE ms.movie.id = :movieId "
                            +
                            "AND ms.showTime BETWEEN :start AND :end", MovieSession.class)
                    .setParameter("movieId", id)
                    .setParameter("start", startOfDay)
                    .setParameter("end", endOfDay)
                    .getResultList();

        } catch (Exception e) {
            throw new DataProcessingException("Can't find available session for movie id", e);
        }
    }
}
