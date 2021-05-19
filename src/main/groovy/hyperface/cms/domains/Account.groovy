package hyperface.cms.domains

import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
class Account {
    @Id
    @GenericGenerator(name = "account_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "account_id")
    String id

    Double currentBalance
}
