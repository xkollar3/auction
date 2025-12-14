package edu.fi.muni.cz.marketplace.user.query;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNicknameRepository extends JpaRepository<UserNicknameReadModel, String> {

    boolean existsByNicknameAndDiscriminator(String nickname, Integer discriminator);

    List<UserNicknameReadModel> findByNickname(String nickname);

    @Query("SELECT MAX(u.discriminator) FROM UserNicknameReadModel u WHERE u.nickname = :nickname")
    Optional<Integer> findMaxDiscriminatorByNickname(@Param("nickname") String nickname);
}
