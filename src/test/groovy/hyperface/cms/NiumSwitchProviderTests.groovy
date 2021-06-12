package hyperface.cms

import hyperface.cms.domains.Address
import hyperface.cms.domains.Customer
import hyperface.cms.service.SwitchProviders.Nium.NiumSwitchProvider
import org.springframework.http.HttpStatus
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class NiumSwitchProviderTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    NiumSwitchProvider niumSwitchProvider = new NiumSwitchProvider()

    private static Random random = new Random(System.currentTimeMillis())

    private Customer getTestCustomer(){
        Customer customer = new Customer()
        customer.firstName = "John"
        customer.middleName = ""
        customer.lastName = "Smith"
        customer.preferredName = customer.firstName
        customer.dateOfBirth = "2000-01-01"
        customer.email = getRandomEmailAddress()
        customer.mobile = getRandomMobileNumber()
        customer.countryCode = "IN"
        customer.nationality = "IN"
        customer.currentAddress = new Address()
        customer.currentAddress.city = "Bengaluru"
        customer.currentAddress.pincode = "560001"
        customer.currentAddress.line1 = "1, MG Road"

        return customer
    }

    // TODO: move to utils? can be used by other tests
    private String getRandomEmailAddress(){
        def length = 16
        def pool = ['a'..'z','A'..'Z',0..9,'.'].flatten()
        return ((0..length-1).collect{pool[random.nextInt(pool.size())]}).join().concat('1@gmail.com')
    }

    private String getRandomMobileNumber(){
        return (Math.abs(random.nextInt() % 9000000000) + 1000000).toString()
    }

    @Test
    void testNiumSwitchCreateCustomer(){
        Customer customer = this.getTestCustomer()
        HttpStatus response = niumSwitchProvider.createCustomer(customer)
        println("Test executed with response code: ${response.value()}")
        sleep(180000)
        // TODO: add better assert statements based on final response
        assert response == HttpStatus.OK
    }
}
