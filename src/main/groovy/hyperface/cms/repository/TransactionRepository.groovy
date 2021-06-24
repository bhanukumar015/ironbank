package hyperface.cms.repository


import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.Repository

@NoRepositoryBean
interface TransactionRepository<T> extends Repository<T, String> {

}