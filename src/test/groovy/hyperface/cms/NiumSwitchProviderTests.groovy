package hyperface.cms

import hyperface.cms.domains.Customer
import hyperface.cms.service.SwitchProviders.NiumSwitchProvider
import org.junit.jupiter.api.Test
import org.assertj.core.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class NiumSwitchProviderTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    NiumSwitchProvider niumSwitchProvider

    private static Random random = new Random(System.currentTimeMillis())

    String responseCode = 200

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
        customer.deliveryCity = "Bengaluru"
        customer.deliveryZipCode = "560001"
        customer.deliveryAddress1 = "1, MG Road"
        customer.billingCity = customer.deliveryCity
        customer.billingZipCode = customer.deliveryZipCode
        customer.billingAddress1 = customer.deliveryAddress1

        return customer
    }

    private String getRandomEmailAddress(){
        def length = 16
        def pool = ['a'..'z','A'..'Z',0..9,'.'].flatten()
        return ((0..length-1).collect{pool[random.nextInt(pool.size())]}).join().concat('@gmail.com')
    }

    private String getRandomMobileNumber(){
        return (Math.abs(random.nextInt() % 9000000000) + 1000000).toString()
    }

    @Test
    void testNiumSwitchCreateCustomer(){
        Customer customer = this.getTestCustomer()
        Customer updatedCustomer = niumSwitchProvider.createCustomer(customer)
        println(customer.switchMetadata)
        assert updatedCustomer.switchMetadata != ""
    }
}
