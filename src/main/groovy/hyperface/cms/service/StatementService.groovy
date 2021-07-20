package hyperface.cms.service

import hyperface.cms.domains.Address
import hyperface.cms.domains.Bank
import hyperface.cms.domains.Card
import hyperface.cms.domains.CardStatement
import hyperface.cms.domains.Client
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import hyperface.cms.domains.PDFBox
import hyperface.cms.domains.ledger.TransactionLedger
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.TransactionLedgerRepository
import hyperface.cms.service.pdfbox.PDFBoxService
import hyperface.cms.service.pdfbox.PDFBoxServiceImpl
import org.apache.pdfbox.cos.COSArray
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils

import java.time.ZonedDateTime

@Service
class StatementService {

    @Autowired
    CreditAccountRepository creditAccountRepository

    @Autowired
    TransactionLedgerRepository transactionLedgerRepository

    PDFBoxService pdfBoxService = PDFBoxServiceImpl.getInstance()

    PDDocument document = new PDDocument()
    PDPage pdPage = new PDPage(PDRectangle.A4)
    PDFont font = PDType1Font.HELVETICA
    PDFBox pdfBox = new PDFBox(pdPage, 15.0, font)
    float x
    float y

    PDDocument generateStatement(CardStatement statement) {
        Date from = getStatementStartDate(statement.getDueDate())
        Date to = getStatementEndDate(statement.getDueDate())

        Optional<CreditAccount> creditAccount = creditAccountRepository.findById(statement.getId())
        Customer customer = creditAccountRepository.findByCustomerId(creditAccount.get().customer.getId())
        //Card card = how to fetch a single card??
        //CreditCardProgram cardProgram = we can fetch from Card
        //Client client = we can fetch from CreditCardProgram
        //List<TransactionLedger> ledgerList = transactionLedgerRepository.  ..not clear about this
        //what about Card first four digits??
        //From where to fetch Bank image

    }

    PDDocument generateDummyStatement() {
        Date from = new Date()
        Date to = new Date()

        Address address = new Address();
        address.setLine1("line1")
        address.setLine2("line2")
        address.setCity("Bangalore")
        address.setState("karnataka")
        address.setCountry("India")
        address.setLandmark("Near Hyperface")
        address.setCountryCodeIso("IND")

        Customer customer = new Customer()
        customer.setId("1")
        customer.setFirstName("Wilson")
        customer.setLastName("Fisk")
        customer.setPreferredName(customer.getFirstName())
        customer.setDateOfBirth("11 May 2021")
        customer.setEmail("wilson@mail.com")
        customer.setMobile("8998899889")
        customer.setCurrentAddress(address)
        customer.setPermanentAddress(address)

        CreditAccount creditAccount = new CreditAccount()
        creditAccount.setId("1")
        creditAccount.setApprovedCreditLimit(10000.00)
        creditAccount.setAvailableCreditLimit(10000.00)
        creditAccount.setCustomer(customer)

        Card card = new Card()
        card.setCreditAccount(creditAccount)
        card.setLastFourDigits("1234") //what about first four digits??

        Client client = new Client()
        client.setName("Chaipoint")

        CardStatement statement = new CardStatement()
        statement.setCreditAccount(creditAccount)
        statement.setTotalAmountDue(1200.33)
        statement.setMinAmountDue(200.00)
        statement.setDueDate(ZonedDateTime.now())
        statement.setTotalCredits(100.00)
        statement.setTotalDebits(100.00)
        statement.setOpeningBalance(0.00)
        statement.setClosingBalance(-100.00)
        statement.setDeferredInterest(0.00)
        statement.setResidualInterest(0.00)
        statement.setUnpaidResidualBalance(0.00)
        statement.setBilledInterest(0.00)
        statement.setWaivedInterest(0.00)
        statement.setNetTaxOnInterest(0.00)
        statement.setNetTaxOnFees(0.00)
        statement.setNetFinanceCharges(0.00)
        statement.setNetFeeCharges(0.00)
        statement.setNetRepayments(0.00)
        statement.setRefunds(20.00)
        statement.setNetCashback(20.00)
        statement.setNetPurchases(3200)
        statement.setBillingCycleNumber(2)
        statement.setGeneratedOn(ZonedDateTime.now())

        PDPageContentStream contentStream = pdfBoxService.addPage(document, pdPage)
        drawHeader(true, contentStream, document)
        println pdPage.getMediaBox().height
        pdfBoxService.close(contentStream)
        return document
    }

    private Date getStatementStartDate(ZonedDateTime dueDate) {
        //todo: impl
        //may require to use billingCycleNumber
        return dueDate as Date
    }

    private Date getStatementEndDate(ZonedDateTime dueDate) {
        //todo: impl
        return dueDate as Date
    }

    private String convertDateToDDMMMYYYY() {
        //todo: move to utils
        return "20 Jun 2021"
    }

    private void drawHeader(boolean isFirstPage, PDPageContentStream contentStream, PDDocument document) {

    }

    private void drawFooter() {

    }
}
