package hyperface.cms.domains.fees


import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
class FeeStrategy  {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    public Double getFee(Double inputValue) {
        return 0
    }
}
