package edu.fi.muni.cz.marketplace.user.query;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_nicknames")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNicknameReadModel {

    @Id
    private String id;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private Integer discriminator;
}
