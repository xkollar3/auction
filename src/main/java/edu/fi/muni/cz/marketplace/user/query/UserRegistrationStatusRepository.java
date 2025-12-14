package edu.fi.muni.cz.marketplace.user.query;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRegistrationStatusRepository extends JpaRepository<UserRegistrationStatusReadModel, UUID> {
}
