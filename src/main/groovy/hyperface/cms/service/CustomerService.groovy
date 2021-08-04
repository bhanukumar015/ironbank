package hyperface.cms.service

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import hyperface.cms.commands.CreateCustomerRequest
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.SwitchProviders.Nium.CustomerManagement.NiumCustomerService
import hyperface.cms.util.Response
import io.vavr.control.Either
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class CustomerService {
    @Autowired
    CustomerRepository customerRepository

    @Autowired
    NiumCustomerService niumCustomerService

    Either<GenericErrorResponse,Customer> createCustomer(CreateCustomerRequest req){
        log.info(new ObjectMapper().writeValueAsString(req))
        List<Customer> existingCustomer = customerRepository.findByEmailOrMobile(req.emailAddress, req.mobileNumber)
        log.info(new ObjectMapper().writeValueAsString(existingCustomer))
        if(!existingCustomer.empty){
            String errorMessage = "Customer already exists with the given email and mobile number"
            log.error("Customer creation failed with error: ${errorMessage}")
            return Either.left(new GenericErrorResponse(reason: errorMessage))
        }
        Map<String,Object> switchMetadata = niumCustomerService.createCustomer(req)
        Customer customer = createCustomerObject(req, switchMetadata)
        customerRepository.save(customer)
        return Either.right(customer)
    }

    Customer createCustomerObject(CreateCustomerRequest req, Map<String, Object> switchData) {
        return new Customer().tap {
            firstName = req.firstname
            middleName = req.middlename
            lastName = req.lastname
            dateOfBirth = req.dateOfBirth
            email = req.emailAddress
            mobile = req.mobileNumber
            pancard = req.pancard
            countryCode = req.countryCode
            nationality = req.nationality
            currentAddress = req.currentAddress
            permanentAddress = req.permanentAddress
            switchMetadata = switchData
        }
    }
}
