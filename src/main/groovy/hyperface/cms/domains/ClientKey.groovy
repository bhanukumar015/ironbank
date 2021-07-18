package hyperface.cms.domains

import org.hibernate.annotations.GenericGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToOne
import javax.validation.constraints.NotNull

@Entity
@EntityListeners(AuditingEntityListener.class)
class ClientKey {
    @Id
    @GenericGenerator(name = "client_key_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "client_key_id")
    String id

    @NotNull
    @Column(nullable = false)
    String secretKey

    @OneToOne
    Client client

    @NotNull
    @CreatedDate
    @Column(nullable = false, updatable = false)
    Date createdAt

    @NotNull
    @LastModifiedDate
    @Column(nullable = false)
    Date modifiedAt
}
