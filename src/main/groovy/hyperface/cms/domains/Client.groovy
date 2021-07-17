package hyperface.cms.domains

import org.hibernate.annotations.GenericGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Client {
    @Id
    @GenericGenerator(name = "client_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "client_id")
    String id

    @Column(nullable = false, unique = true)
    String name

    String emailAddress

    /** Uploaded image URL */
    String logoUrl

    /** Image data */
    @Column(columnDefinition = "MEDIUMTEXT")
    String logo

    @Column(nullable = false, updatable = false)
    @CreatedDate
    Date createdAt = new Date()

    @Column(nullable = false)
    @LastModifiedDate
    Date modifiedAt = new Date()
}
