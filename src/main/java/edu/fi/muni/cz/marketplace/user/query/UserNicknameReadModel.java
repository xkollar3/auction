package edu.fi.muni.cz.marketplace.user.query;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_nicknames")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserNicknameReadModel {

  @Id
  private UUID id;

  @Column(nullable = false)
  private String nickname;

  @Column(nullable = false)
  private Integer discriminator;
}
