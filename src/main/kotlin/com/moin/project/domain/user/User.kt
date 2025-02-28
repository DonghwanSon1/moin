package com.moin.project.domain.user

import com.fasterxml.jackson.annotation.JsonIgnore
import com.moin.project.domain.user.enums.Role
import com.moin.project.domain.user.rqrs.UserRq
import jakarta.persistence.*

@Entity
@Table(name = "user")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn")
    val sn: Long? = null,

    @Column(name = "user_id", unique = true, nullable = false)
    val userId: String,

    @JsonIgnore
    @Column(name = "password", nullable = false)
    val password: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "id_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val idType: Role,

    @Column(name = "id_value", nullable = false)
    val idValue: String,

) {
    companion object {
        fun createUser(userRq: UserRq, encryptedPassword: String, encryptIdValue: String): User {
            return User(
                userId = userRq.userId,
                password = encryptedPassword,
                name = userRq.name,
                idType = userRq.idType,
                idValue = encryptIdValue
            )
        }
    }
}