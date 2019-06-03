package testpay.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import testpay.model.Notification;

@Repository
public interface NotificationsRepository extends CrudRepository<Notification, Long> {

  List<Notification> findByOrderByLastAttemptDesc();
}
