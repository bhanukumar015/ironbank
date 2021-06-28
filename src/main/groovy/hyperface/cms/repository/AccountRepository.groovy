package hyperface.cms.repository

import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.Repository

@NoRepositoryBean
interface AccountRepository <T> extends Repository<T, String> {

}