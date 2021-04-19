package hyperface.cms.domains

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity(name="testitem")
public class TestItem {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    Long id

    String title
    String link
}