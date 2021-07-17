package hyperface.cms.domains

import org.hibernate.annotations.GenericGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToOne

@Entity
class ClientKey {
    @Id
    @GenericGenerator(name = "client_key_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "client_key_id")
    String id

    @Column(nullable = false)
    String secretKey

    @OneToOne
    Client client

    @Column(nullable = false, updatable = false)
    @CreatedDate
    Date createdAt = new Date()

    @Column(nullable = false)
    @LastModifiedDate
    Date modifiedAt = new Date()
}
