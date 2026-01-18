package edu.fi.muni.cz.marketplace.order.query;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_user_id_lookup_table")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderUserLookup {

  @Id
  private UUID userId;

  @Column(unique = true, nullable = false)
  private String keycloakUserId;
}
