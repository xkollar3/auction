package edu.fi.muni.cz.marketplace.order.query;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderReadModelRepository extends JpaRepository<OrderReadModel, UUID> {

  List<OrderReadModel> findBySellerIdOrderByReservedAtDesc(UUID sellerId);

  List<OrderReadModel> findByBuyerIdOrderByReservedAtDesc(UUID buyerId);
}
