package hyperface.cms.domains

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    Double currentBalance
}
