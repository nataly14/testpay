package testpay.persistence;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import testpay.model.Payment;

@Repository
public interface PaymentsRepository extends CrudRepository<Payment, Long> {
  Optional<Payment> findByExternalId(String externalId);
}
