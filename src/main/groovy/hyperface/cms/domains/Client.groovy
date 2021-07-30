package hyperface.cms.domains

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.GenericGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToOne
import javax.validation.constraints.NotNull

import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@EntityListeners(AuditingEntityListener.class)
class Client {
    @Id
    @GenericGenerator(name = "client_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "client_id")
    String id

    @NotNull
    @Column(nullable = false, unique = true)
    String name

    String emailAddress

    /** Uploaded image URL */
    String logoUrl

    /** Image data */
    @Column(columnDefinition = "MEDIUMTEXT")
    String logo

    @JsonIgnoreProperties("client")
    @OneToOne(mappedBy = "client", cascade = CascadeType.REMOVE)
    ClientKey clientKey

    @NotNull
    @CreatedDate
    @Column(nullable = false, updatable = false)
    Date createdAt


    @NotNull
    @LastModifiedDate
    @Column(nullable = false)
    Date modifiedAt
}
