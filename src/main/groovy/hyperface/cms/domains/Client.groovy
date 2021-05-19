package hyperface.cms.domains

import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Client {
    @Id
    @GenericGenerator(name = "client_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "client_id")
    String id

    String name
    String emailAddress

}
