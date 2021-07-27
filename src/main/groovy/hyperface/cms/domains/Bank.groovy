package hyperface.cms.domains

import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Bank {
    @Id
    @GenericGenerator(name = "bank_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "bank_id")
    String id

    String name
    String emailAddress

    // Below two fields would be used to transfer amount for FD fund transfer
    String omnibusAccountNumber
    String ifsCode
}
