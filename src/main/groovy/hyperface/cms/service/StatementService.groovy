package hyperface.cms.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageConfig
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import hyperface.cms.Constants
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.domains.Card
import hyperface.cms.domains.CardStatement
import hyperface.cms.domains.Client
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import hyperface.cms.domains.CustomerTransaction

import hyperface.cms.domains.ledger.TransactionLedger
import hyperface.cms.model.enums.LedgerTransactionType
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerTransactionRepository
import hyperface.cms.repository.TransactionLedgerRepository
import hyperface.cms.service.pdfbox.PDFBoxServiceImpl
import hyperface.cms.util.Utilities
import io.vavr.control.Either
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils

import java.awt.image.BufferedImage
import java.time.ZonedDateTime
import java.util.stream.Collectors

@Service
class StatementService {

    @Autowired
    CreditAccountRepository creditAccountRepository

    @Autowired
    TransactionLedgerRepository transactionLedgerRepository

    @Autowired
    CustomerTransactionRepository customerTransactionRepository

    @Autowired
    PDFBoxServiceImpl pdfBoxService


    File circleFile = ResourceUtils.getFile("classpath:images/statement/circle.png")
    File appStoreFile = ResourceUtils.getFile("classpath:images/statement/appstore.png")
    File playStoreFile = ResourceUtils.getFile("classpath:images/statement/playstore.png")
    File hdfcFile = ResourceUtils.getFile("classpath:images/statement/hdfcbank.png")
    File hyperfaceFile = ResourceUtils.getFile("classpath:images/statement/hyperface-logo.png")
    File visaFile = ResourceUtils.getFile("classpath:images/statement/visa.png")
    File cardBGFile = ResourceUtils.getFile("classpath:images/statement/black_bg.png")

    //1px = 0.75pts
    PDDocument document
    PDPage pdPage = new PDPage(PDRectangle.A4)
    PDFont helvetica_bold = PDType1Font.HELVETICA_BOLD
    PDFont helvetica = PDType1Font.HELVETICA

    float borderW = 18.75 //25px
    float tW = pdPage.getMediaBox().getWidth() //595
    float tH = pdPage.getMediaBox().getHeight() //842
    float w = tW - 2 * borderW as float
    float h = tH - 2 * borderW as float
    float x = 0.0
    float y = tH
    float th = 0.75 //thickness
    float footerH = 150
    float cardW = 150
    float cardH = 90
    float cellPadding = 10
    float[] black = [0 / 255f, 0 / 255f, 0 / 255f]
    float[] white = [255 / 255f, 255 / 255f, 255 / 255f]
    float[] lightGrey = [145 / 255f, 145 / 255f, 145 / 255f]
    float[] darkGrey = [89 / 255f, 89 / 255f, 89 / 255f]
    float[] blue = [35 / 255f, 96 / 255f, 232 / 255f]
    float[] green = [22 / 255f, 99 / 255f, 51 / 255f] //client logo dominant color

    float fontSize
    float rowH
    float textW
    float imgW
    float imgH
    float headerH
    float sx
    float sy
    float margin
    float blockW
    float blockH

    int maxRows
    int start
    int end
    int wrapLength

    float[] colW

    String text
    String addr = "124, Bond Street, Kolkata-310001" //todo: remove hardcoding

    List<List<String>> content = new ArrayList<>()
    List<List<String>> txnAmountList

    //todo: fetch values from ENUM
    String[] reversal_list = ['Auth Reversal', 'Refund', 'Refund Reversal', 'Purchase Reversal',
                              'Cashback Reversal', 'Fee Reversal', 'Repayment Reversal', 'Cash Withdrawal Reversal', 'Tax Reversal']
    String[] cash_list = ['Settle Debit Cash', 'Settle Credit Cash', 'Cash Withdrawal']

    Customer customer = new Customer()
    CardStatement statement = new CardStatement()
    CreditAccount creditAccount = new CreditAccount()
    Client client = new Client()
    Card card = new Card()
    CreditCardProgram cardProgram = new CreditCardProgram()
    List<TransactionLedger> ledgerList = new ArrayList<>()

    List<TransactionLedger> payments = new ArrayList<>()
    List<TransactionLedger> fees = new ArrayList<>()
    List<TransactionLedger> emis = new ArrayList<>()
    List<TransactionLedger> reversals = new ArrayList<>()
    List<TransactionLedger> txns = new ArrayList<>()

    List<TransactionLedger> domesticTxns = new ArrayList<>()
    List<TransactionLedger> domesticCashTxns = new ArrayList<>()
    List<TransactionLedger> internationalTxns = new ArrayList<>()
    List<TransactionLedger> internationCashTxns = new ArrayList<>()

    ZonedDateTime from
    ZonedDateTime to

    Either<GenericErrorResponse, PDDocument> generateStatement(CardStatement cardStatement) {
        from = Utilities.getStatementStartDate(statement.getDueDate())
        to = Utilities.getStatementEndDate(statement.getDueDate())

        statement = cardStatement
        Optional<CreditAccount> creditAccountOptional = creditAccountRepository.findById(statement.getId())
        if (!creditAccountOptional.isPresent()) {
            return Either.left(new GenericErrorResponse(reason: "Credit Account is not found"))
        }

        creditAccount = creditAccountOptional.get()
        customer = creditAccountRepository.findByCustomerId(creditAccount.getCustomer().getId())
        List<Card> cards = creditAccount.cards
                .stream()
                .filter(card -> card.isPrimaryCard)
                .collect(Collectors.toList())
        if (cards.size() != 1) {
            return Either.left(new GenericErrorResponse(reason: "Primary Card associated with account: ${creditAccount.getId()} is not found"))
        }

        card = cards.get(0)
        cardProgram = card.getCardProgram()
        client = cardProgram.getClient()
        ledgerList = transactionLedgerRepository.findAllByCreditAccountInRange(creditAccount, from, to)

        for (TransactionLedger ledgerTxn : ledgerList) {
            if (ledgerTxn.getTransactionType() == LedgerTransactionType.REPAYMENT) {
                payments.add(ledgerTxn)
            } else if (ledgerTxn.getTransactionType() == LedgerTransactionType.FEE) { //todo: Auth Type
                fees.add(ledgerTxn)
            } else if (ledgerTxn.getTransactionType() == LedgerTransactionType.EMI) {
                emis.add(ledgerTxn)
            } else if (reversal_list.contains(ledgerTxn.getTransactionType())) {
                reversals.add(ledgerTxn)
            } else {
                txns.add(ledgerTxn)
            }
        }

        for (TransactionLedger txn : txns) {
            Optional<CustomerTransaction> customerTransaction = customerTransactionRepository.findById(txn.transaction.id)
            if (customerTransaction.isPresent()) {
                if (customerTransaction.get().getTransactionCurrency() == "INR") { //todo: refer from enum
                    if (cash_list.contains(txn.getTransactionType())) {
                        domesticCashTxns.add(txn)
                    } else {
                        domesticTxns.add(txn)
                    }
                } else {
                    if (cash_list.contains(txn.getTransactionType())) {
                        internationCashTxns.add(txn)
                    } else {
                        internationalTxns.add(txn)
                    }
                }
            } else {
                if (cash_list.contains(txn.getTransactionType())) {
                    domesticCashTxns.add(txn)
                } else {
                    domesticTxns.add(txn)
                }
            }
        }

        document = new PDDocument()
        PDPageContentStream contentStream = pdfBoxService.addA4Page(document)
        drawHeader(contentStream, true)
        drawStatementSummary(contentStream)
        drawStatementIllustration(contentStream)

        content = [["Opening Balance", "Earned", "Redeemed/Expired", "Available for Redemption"],
                   ["2000.00", "2000.00", "200.00", "23.50"]]

        //todo: Reward Program logic to be implemented later
        drawRewardsProgramSummary(contentStream, false, content)
        //content = [["Cashback Credited", "Cashback reversals/adjustment", "Net cashback earned this month", "Total cashback earned till date"],
        //                   ["2000.00", "2000.00", "200.00", "23.50"]]
        //drawCashbackSummary() // part of if-else
        drawPaymentOptions(contentStream)
        drawNotice(contentStream, borderW, borderW)
        contentStream = addNewPage(contentStream)
        drawTransactions(contentStream)
        contentStream = addNewPage(contentStream)
        drawLastPage(contentStream)
        pdfBoxService.close(contentStream)

        return Either.right(document)
    }

    private PDPageContentStream addNewPage(PDPageContentStream contentStream) {
        pdfBoxService.close(contentStream)
        contentStream = pdfBoxService.addA4Page(document)
        x = 0.0
        y = tH
        drawHeader(contentStream, false)
        return contentStream
    }

    private float updateX(float dx) {
        x = x + dx as float
    }

    private float updateY(float dy) {
        y = y + dy as float
    }

    private PDImageXObject getImageXObjectFromFile(File file) {
        return PDImageXObject.createFromFileByContent(file, document)
    }

    private void drawHeader(PDPageContentStream contentStream, boolean isFirstPage) {
        if (isFirstPage) {
            headerH = 180.00
            margin = 15
            pdfBoxService.drawRect(contentStream, green, updateX(0), updateY(-headerH), tW, headerH)

            //first layer
            imgW = 68.0
            imgH = 21.0
            pdfBoxService.drawImage(contentStream, getImageXObjectFromFile(hdfcFile), updateX(borderW), updateY((((2 / 3) * headerH) + margin) as float), imgW, imgH)

            imgW = 85.0
            imgH = 21.0
            //todo: fetch image from client object. Design flow for in case of nullable client logo is required.
            pdfBoxService.drawImage(contentStream, getImageXObjectFromFile(hdfcFile), updateX((w - imgW) as float), y, imgW, imgH)

            updateX(-x + borderW as float)
            updateY(-y + tH - ((1 / 3) * headerH) as float)
            pdfBoxService.drawSolidLine(contentStream, white, x, y, x + w as float, y, 0.75f)

            //second layer
            fontSize = 7.5f
            text = Utilities.convertDateToCustomFormat(from, Constants.MMM_SPACE_DD_COMMA_SPACE_YYYY) <<
                    " - " << Utilities.convertDateToCustomFormat(to, Constants.MMM_SPACE_DD_COMMA_SPACE_YYYY)
            pdfBoxService.writeText(contentStream, helvetica, white, fontSize, x, updateY(-(margin + fontSize) as float), 0, text.length(), text)

            text = "Card Number"
            pdfBoxService.writeText(contentStream, helvetica, white, fontSize, x + 300 as float, y, 0, text.length(), text)
            //max width for static strings

            fontSize = 13.5
            text = client.getName() << " Card Statement"
            pdfBoxService.writeText(contentStream, helvetica_bold, white, fontSize, x, updateY(-(3 + fontSize) as float), 0, 30, text)

            th = 0.35f
            fontSize = 9
            text = "XXXX XXXX XXXX " << card.getLastFourDigits().toString()
            pdfBoxService.writeText(contentStream, helvetica_bold, white, fontSize, x + 300 as float, y, 0, text.length(), text)

            pdfBoxService.drawSolidLine(contentStream, white, x + 250 as float, y, x + 250 as float, y + 20 as float, th)

            //third layer
            updateX(-x + borderW as float)
            updateY(-y + tH - ((2 / 3) * headerH) as float)
            pdfBoxService.drawSolidLine(contentStream, white, x, y, x + w - cardW as float, y, 0.75f)

            float cellMargin = 25
            text = "Statement Date"
            pdfBoxService.writeText(contentStream, helvetica, white, 8, x, updateY(-(margin + 8) as float), 0, text.length(), text)
            //todo: FIX date value
            pdfBoxService.writeText(contentStream, helvetica_bold, white, 10, x, y - 15 as float, 0, 11, Utilities.convertDateToCustomFormat(ZonedDateTime.now(), Constants.DD_SPACE_MMM_SPACE_YYYY))

            updateX(pdfBoxService.getTextWidth(helvetica, 8, text) + cellMargin as float)
            pdfBoxService.drawSolidLine(contentStream, white, x, y + 2 as float, x, y - 12 as float, th)

            updateX(cellMargin)
            text = "Credit Limit"
            pdfBoxService.writeText(contentStream, helvetica, white, 8, x, y, 0, text.length(), text)
            pdfBoxService.writeText(contentStream, helvetica_bold, white, 10, x, y - 15 as float, 0, 12, creditAccount.getApprovedCreditLimit().toString())
            //todo: Add rupee symbol

            updateX(pdfBoxService.getTextWidth(helvetica, 8, text) + cellMargin as float)
            pdfBoxService.drawSolidLine(contentStream, white, x, y + 2 as float, x, y - 12 as float, th)

            updateX(cellMargin + 2.5 as float)
            text = "Available Credit Limit"
            pdfBoxService.writeText(contentStream, helvetica, white, 8, x, y, 0, text.length(), text)
            pdfBoxService.writeText(contentStream, helvetica_bold, white, 10, x, y - 15 as float, 0, 12, creditAccount.getAvailableCreditLimit().toString())

            updateX(pdfBoxService.getTextWidth(helvetica, 8, text) + cellMargin as float)
            pdfBoxService.drawSolidLine(contentStream, white, x, y + 2 as float, x, y - 12 as float, th)

            updateX(cellMargin)
            text = "Available Cash Limit"
            pdfBoxService.writeText(contentStream, helvetica, white, 8, x, y, 0, text.length(), text)
            pdfBoxService.writeText(contentStream, helvetica_bold, white, 10, x, y - 15 as float, 0, 12, creditAccount.getAvailableCashWithdrawalLimit().toString())

            drawCardDisplay(contentStream)
        } else {
            fontSize = 15
            x = borderW
            updateY(-borderW - fontSize as float)
            text = client.getName() << " Card Statement"
            pdfBoxService.writeText(contentStream, helvetica_bold, green, fontSize, x, y, 0, 50, text)

            imgW = 84.75
            imgH = 21.0
            updateX(w - imgW as float)
            pdfBoxService.drawImage(contentStream, getImageXObjectFromFile(hdfcFile), x, y, imgW, imgH)
            //todo: read from appropriate bank image

            updateX(-5)
            pdfBoxService.drawSolidLine(contentStream, lightGrey, x, y + 5 as float, x, y + 5 as float, th)

            imgW = 67.5 //90px
            imgH = 21.0 //28px
            updateX(-20 - imgW as float)
            pdfBoxService.drawImage(contentStream, getImageXObjectFromFile(hdfcFile), x, y, imgW, imgH)
            //todo: need to change client imageXObject

            x = borderW
            y = (tH - 50) as float //move to a variable later
            pdfBoxService.drawSolidLine(contentStream, black, x, y, x + w as float, y, th)
        }
    }

    private drawCardDisplay(PDPageContentStream contentStream) {
        x = (tW - borderW - cardW) as float
        y = (tH - ((1 / 3) * headerH) - margin - cardH) as float
        pdfBoxService.drawImage(contentStream, getImageXObjectFromFile(cardBGFile), x, y, cardW, cardH)

        float padding = 11.25
        float lx = x + padding as float //
        float rx = (x + cardW - padding) as float //
        float by = y + padding as float //
        float ty = y + cardH - padding as float //
        pdfBoxService.drawImage(contentStream, getImageXObjectFromFile(hdfcFile), lx, ty - 10 as float, 42, 10)
        pdfBoxService.drawImage(contentStream, getImageXObjectFromFile(hdfcFile), rx - 32 as float, ty - 8 as float, 32, 10)
        //todo: FIX name on card: from where to fetch
        pdfBoxService.writeText(contentStream, helvetica_bold, white, 10, lx, by, 0, 20, "RAMANATHAN R")
        pdfBoxService.drawImage(contentStream, getImageXObjectFromFile(visaFile), rx - 25 as float, by, 25, 10)
        //visa image ??
        pdfBoxService.drawImage(contentStream, getImageXObjectFromFile(hdfcFile), lx, y + (cardH / 2) as float, 17, 15)
        // simcard image
    }

    private void drawStatementSummary(PDPageContentStream contentStream) {
        x = borderW
        fontSize = 10
        y = (tH - headerH - borderW - fontSize) as float
        pdfBoxService.writeText(contentStream, helvetica_bold, black, fontSize, x, y, 0, 25, "Statement Summary")

        margin = 16
        blockW = (w / 4 - margin) as float
        blockH = (w / 4 - 2 * margin) as float

        updateY(-margin)

        //todo: addr value to be calculated
        String address = "Personal Info: " << Constants.DELIMITER <<
                addr << Constants.DELIMITER <<
                customer.getEmail() << Constants.DELIMITER <<
                customer.getMobile()
        String msg1 = "If you pay your monthly balance" << Constants.DELIMITER <<
                "in full every month, you will avoid" << Constants.DELIMITER <<
                "being charged interest"
        String msg2 = "Minimum amount you need to" << Constants.DELIMITER <<
                "pay before the due date to avoid" << Constants.DELIMITER <<
                "late payment fees"
        String msg3 = "The date by which the payment must" << Constants.DELIMITER <<
                "be received by us to your credit card" << Constants.DELIMITER <<
                "account in good standing"

        String[][] content = [["Name of the Card Holder", "Ramanathan R", address],
                              ["Total Amount Due", "Rs. " + statement.getTotalAmountDue(), msg1],
                              ["Minimum Amount Due", "Rs. " + statement.getMinAmountDue(), msg2],
                              ["Payment Due By", "10 Jun 2021", msg3]]

        float[][] co_ords = [[x, y], [x + blockW + margin, y], [x, y - blockH - margin], [x + blockW + margin, y - blockH - margin]]

        th = 0.5f
        for (int i = 0; i < 4; i++) {
            pdfBoxService.drawSolidLine(contentStream, lightGrey, co_ords[i][0], co_ords[i][1], co_ords[i][0] + blockW as float, co_ords[i][1], th)
            pdfBoxService.drawSolidLine(contentStream, lightGrey, co_ords[i][0], co_ords[i][1], co_ords[i][0], co_ords[i][1] - blockH as float, th)

            x = co_ords[i][0] + margin as float
            y = co_ords[i][1] - margin - 7.5 as float
            pdfBoxService.writeText(contentStream, helvetica_bold, darkGrey, 7.5, x, y, 0, 36, content[i][0])

            y += (-18)
            pdfBoxService.writeText(contentStream, helvetica_bold, green as float[], 12, x, y, 0, 36, content[i][1])

            y += (-20)
            pdfBoxService.writeText(contentStream, helvetica_bold, darkGrey, 5.75, x, y, -9f, 38, content[i][2])
        }
    }

    private void drawStatementIllustration(PDPageContentStream contentStream) {
        x = (w / 2) + (2 * margin) as float
        fontSize = 10
        y = (tH - headerH - borderW - fontSize) as float

        pdfBoxService.writeText(contentStream, helvetica_bold, black, fontSize, x, y, 0, 30, "Statement Illustration")

        updateY(-margin)

        pdfBoxService.drawSolidLine(contentStream, lightGrey, x, y, (tW - borderW) as float, y, th)
        pdfBoxService.drawSolidLine(contentStream, lightGrey, x, y, x, y - (2 * blockH) - margin as float, th)

        sx = x + margin as float
        sy = y - margin + 5 as float

        float tmp = sy - 16.5 as float
        for (int i = 0; i < 7; i++) {
            tmp += (-20)
            pdfBoxService.drawImage(contentStream, getImageXObjectFromFile(circleFile), sx + (w / 4) - 2.5 as float, tmp, 10, 10)
        }

        //todo: need to re-iterate regarding + and -
        content = [["Opening Balance", ""], ["Payments", "-"], ["Refund/ Credits", "+"], ["Purchase/ Debits", "+"], ["Fees & Charges", "+"], ["Finance Charges", "+"], ["Tax", "+"], ["Cashback", "-"]]
        pdfBoxService.writeTableContents(contentStream, black, content, sx, sy, 20, [(w / 4), 5] as float[], 0, 10, 9, helvetica, [Constants.ALIGN_LEFT, Constants.ALIGN_LEFT] as char[])

        content = [["Rs. " + String.valueOf(statement.openingBalance)],
                   ["Rs. " + String.valueOf(statement.netRepayments)],
                   ["Rs. " + String.valueOf(statement.refunds + statement.totalCredits)],
                   ["Rs. " + String.valueOf(statement.netPurchases + statement.totalAmountDue)],
                   ["Rs. " + String.valueOf(statement.netFeeCharges)],
                   ["Rs. " + String.valueOf(statement.netFinanceCharges)],
                   ["Rs. " + String.valueOf(statement.netTaxOnFees + statement.netTaxOnInterest)],
                   ["Rs. " + String.valueOf(statement.netCashback)]]
        pdfBoxService.writeTableContents(contentStream, black, content, sx + (w / 4) + 5 as float, sy, 20, [(w / 4) - 5 - (2 * margin)] as float[], 0, 10, 9, helvetica_bold, [Constants.ALIGN_RIGHT] as char[])

        sy = (y - (2 * blockH) - margin) as float
        pdfBoxService.drawSolidLine(contentStream, lightGrey, sx, sy, sx + w / 2 - (2 * margin) as float, sy, th)
        pdfBoxService.drawSolidLine(contentStream, lightGrey, sx, sy + 30 as float, sx + w / 2 - (2 * margin) as float, sy + 30 as float, th)

        sy += (26)
        content = [["Total Amount Due", "=", "Rs. " + statement.getTotalAmountDue().toString()]]
        pdfBoxService.drawImage(contentStream, getImageXObjectFromFile(circleFile), sx + (w / 4) - 2.5 as float, sy - 16 as float, 10, 10)
        pdfBoxService.writeTableContents(contentStream, black, content, sx, sy, 20, [(w / 4), 5, (w / 4) - 5 - (2 * margin)] as float[], 0, 10, 9, helvetica_bold, [Constants.ALIGN_LEFT, Constants.ALIGN_LEFT, Constants.ALIGN_RIGHT] as char[])
    }

    private void drawRewardsProgramSummary(PDPageContentStream contentStream, boolean isCashbackApplicable, List<List<String>> content) {
        text = isCashbackApplicable ? "Cashback Summary" : "Rewards Points Summary"
        x = borderW
        fontSize = 10
        y = (tH / 2 - 80) as float
        pdfBoxService.writeText(contentStream, helvetica_bold, black, fontSize, x, y, 0, text.length(), text)

        if (!isCashbackApplicable) {
            text = "Rewards Points expiring in 30 days: 200"
            textW = pdfBoxService.getTextWidth(helvetica_bold, 8, text) as float
            pdfBoxService.writeText(contentStream, helvetica_bold, black, 8, x + w - textW as float, y, 0, text.length(), text)
        }

        updateY(-20)
        rowH = 30
        colW = [w / 4, w / 4, w / 4, w / 4]
        pdfBoxService.drawRect(contentStream, [242 / 255f, 242 / 255f, 242 / 255f] as float[], x, y - rowH as float, w, rowH)
        pdfBoxService.writeTableContents(contentStream, black, content, x, y, rowH, colW, 15, 14, 8, helvetica_bold, [Constants.ALIGN_LEFT, Constants.ALIGN_LEFT, Constants.ALIGN_LEFT, Constants.ALIGN_LEFT] as char[])
        pdfBoxService.drawTableBorders(contentStream, lightGrey, content.size(), content[0].size(), x, y, rowH, w, colW, true, true)
    }

    private void drawPaymentOptions(PDPageContentStream contentStream) {
        x = borderW
        updateY(-2 * rowH - 50 as float)
        fontSize = 10
        pdfBoxService.writeText(contentStream, helvetica_bold, black, fontSize, x, y, 0, 30, "Payment Options")

        updateY(-margin)
        pdfBoxService.drawSolidLine(contentStream, lightGrey, x, y, x + w as float, y, th)
        pdfBoxService.drawSolidLine(contentStream, lightGrey, x, y, x, borderW + margin as float, th)

        updateY(-margin)
        drawInAppPayment(contentStream, x + margin as float, y, w / 4 - 2 * margin as float)
        drawBankTransfer(contentStream, x + w / 4 + margin as float, y, w / 2 - 2 * margin as float)
        drawOnlinePayment(contentStream, x + (3 / 4 * w) + margin as float, y, w / 4 - margin as float)

        pdfBoxService.drawSolidLine(contentStream, lightGrey, x + w / 4 as float, y - 25 as float, x + w / 4 as float, y - 125 as float, 0.5)
        pdfBoxService.drawSolidLine(contentStream, lightGrey, x + (3 / 4 * w) as float, y - 25 as float, x + (3 / 4 * w) as float, y - 125 as float, 0.5)
    }

    private void drawInAppPayment(PDPageContentStream contentStream, float sx, float sy, float w) {
        fontSize = 9
        writePaymentOptionsHeading(contentStream, sx, sy, w, "In-app Payment")

        sy += (-30 + 7.5) as float
        text = "Open " << client.getName() << " app > Menu > Credit Card > Pay Now"
        // todo: this will be very specific to client ??
        pdfBoxService.writeText(contentStream, helvetica_bold, black, 7.5, sx, sy, -10, 25, text)

        sy += (-40) as float
        text = "Don't have " << client.getName() << " app yet?" << Constants.DELIMITER <<
                "Download now from:"
        pdfBoxService.writeText(contentStream, helvetica_bold, lightGrey, 6.5, sx, sy, -15, 30, text)

        sy += (-30)
        pdfBoxService.drawImage(contentStream, getImageXObjectFromFile(playStoreFile), sx, sy - 15 as float, 25, 9)
        pdfBoxService.drawImage(contentStream, getImageXObjectFromFile(appStoreFile), sx, sy - 30 as float, 25, 9)

        sx += (30)
        pdfBoxService.writeText(contentStream, helvetica_bold, blue, 6.5, sx, sy - 12.5 as float, 0, 22, "www.chai.pt/PlayStore")
        // todo: need to make it hyperlink
        pdfBoxService.writeText(contentStream, helvetica_bold, blue, 6.5, sx, sy - 27.5 as float, 0, 22, "www.chai.pt/AppStore")
    }

    private void drawBankTransfer(PDPageContentStream contentStream, float sx, float sy, float w) {
        fontSize = 9
        writePaymentOptionsHeading(contentStream, sx, sy, w, "Bank Transfer")

        margin = 10
        sy += (-30)
        pdfBoxService.writeText(contentStream, helvetica, lightGrey, 7.5, sx, sy, 0, 100, "Via NEFT/IMPS")
        pdfBoxService.writeText(contentStream, helvetica, lightGrey, 7.5, sx + w / 2 + margin as float, sy, 0, 100, "Via UPI")

        sy += (-30)
        content = [["Account no:", "10001239999"], ["IFSC:", "HDFC0000011"], ["Bank Name:", "Hdfc Bank"], ["Branch:", "WodeHouse Road,"], ["", "Colaba, Mumbai"]]
        for (int i = 0; i < content.size(); i++) {
            float textW = pdfBoxService.getTextWidth(helvetica, 8, content[i][0])
            pdfBoxService.writeText(contentStream, helvetica, black, 8, sx, sy, 0, 100, content[i][0])
            pdfBoxService.writeText(contentStream, helvetica_bold, black, 8, sx + textW + 2.5 as float, sy, 0, 100, content[i][1])
            sy += (-12)
        }

        //todo: remove hardcoding
        sx += (w / 2 + margin)
        sy += (content.size() * 12)
        text = "10001239999@HDFC"
        pdfBoxService.writeText(contentStream, helvetica_bold, black, 7.5, sx, sy, 0, 100, text)

        sy += (-5)
        text = "Open any UPI app > New Payment" << Constants.DELIMITER <<
                "/Transfer Money/Pay > Enter the" << Constants.DELIMITER <<
                "above the Virtual Payment Address" << Constants.DELIMITER <<
                "(VPA) or UPI id > Enter the amount" << Constants.DELIMITER <<
                "> Pay"
        pdfBoxService.writeText(contentStream, helvetica, lightGrey, 6.5, sx, sy, -10, 35, text)
    }

    private void drawOnlinePayment(PDPageContentStream contentStream, float sx, float sy, float w) {
        fontSize = 9
        writePaymentOptionsHeading(contentStream, sx, sy, w, "Online Payment")

        margin = 10
        sy += (-30)
        text = "Scan & Pay"
        textW = pdfBoxService.getTextWidth(helvetica_bold, fontSize, text)
        pdfBoxService.writeText(contentStream, helvetica, lightGrey, 7.5, (sx + (w - textW) / 2) as float, sy, 0, 100, text)

        sy += (-5)
        pdfBoxService.drawTableBorders(contentStream, lightGrey, 1, 1, (sx + (w - 65) / 2) as float, sy, 65, 65, [65] as float[], true, true)
        addQRCode(contentStream, sx, sy - 2.5 as float, "www.google.com", w, 60, -60) //todo: FIX

        sy += (-80)
        text = "Click & Pay"
        textW = pdfBoxService.getTextWidth(helvetica, 7.5, text)
        pdfBoxService.writeText(contentStream, helvetica, lightGrey, 7.5, (sx + (w - textW) / 2) as float, sy, 0, 100, text)

        text = "hf.co/1223828828"
        textW = pdfBoxService.getTextWidth(helvetica, 7.5, text)
        pdfBoxService.writeText(contentStream, helvetica, blue, 7.5, (sx + (w - textW) / 2) as float, sy - 7.5 as float, 0, 100, text)
    }

    private void writePaymentOptionsHeading(PDPageContentStream contentStream, float sx, float sy, float w, String text) {
        sy += (-fontSize)
        textW = pdfBoxService.getTextWidth(helvetica_bold, fontSize, text)
        pdfBoxService.writeText(contentStream, helvetica_bold, black, fontSize, (sx + (w - textW) / 2) as float, sy, 0, 100, text)
        textW = pdfBoxService.getTextWidth(helvetica_bold, fontSize, text)
        pdfBoxService.drawSolidLine(contentStream, lightGrey, (sx + (w - textW) / 2) as float, sy - 4 as float, (sx + (w + textW) / 2) as float, sy - 4 as float, th)
    }

    private void addQRCode(PDPageContentStream contentStream, float sx, float sy, String text, float bW, float w, float h) {
        def hintMap = [:]
        hintMap.put(EncodeHintType.MARGIN, 0)
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)

        BitMatrix matrix = new MultiFormatWriter().encode(
                new String(text.getBytes("UTF-8"), "UTF-8"),
                BarcodeFormat.QR_CODE, 100, 100, hintMap)

        MatrixToImageConfig config = new MatrixToImageConfig(0xFF000001 as int, 0xFFFFFFFF as int) //Black and White
        BufferedImage bImage = MatrixToImageWriter.toBufferedImage(matrix, config)
        PDImageXObject imageXObject = JPEGFactory.createFromImage(document, bImage)
        pdfBoxService.drawImage(contentStream, imageXObject, (sx + (bW - w) / 2) as float, sy, w, h)
    }

    private void drawNotice(PDPageContentStream contentStream, float sx, float sy) {
        text = "Making only the minimum payment every month would result in the repayment stretching over years with consequent " +
                "interest payment on your outstanding balance."
        fontSize = 6.8
        sy += (fontSize)
        pdfBoxService.writeText(contentStream, helvetica, black, fontSize, sx, sy, 0, 100, "Notice: ")
        pdfBoxService.writeText(contentStream, helvetica_bold, black, 6.8, sx + 22 as float, sy, 0, 1000, text)
    }

    private PDPageContentStream drawTransactions(PDPageContentStream contentStream) {
        rowH = 30
        maxRows = ((tH - 50) / rowH) - 1 as int

        x = borderW
        y = tH - 50 as float

        contentStream = drawPayments(contentStream)
        contentStream = drawOtherFeesAndCharges(contentStream)
        contentStream = drawEMITxns(contentStream)
        contentStream = drawReversalsAndRefunds(contentStream)
        contentStream = drawDomesticTxns(contentStream)
        contentStream = drawDomesticCashTxns(contentStream)
        contentStream = drawInternationalTxns(contentStream)
        contentStream = drawInternationCashTxns(contentStream)
        return contentStream
    }

    //think of a proper name
    private PDPageContentStream drawTransactionalRows(PDPageContentStream contentStream, List<TransactionLedger> ledgerList) {
        content = new ArrayList<>()
        txnAmountList = new ArrayList<>()
        for (TransactionLedger transactionLedger : ledgerList) {
            List<String> transaction = new ArrayList<>()
            transaction.add(Utilities.convertDateToCustomFormat(transactionLedger.postingDate, Constants.DD_SLASH_MM_SLASH_YYYY))
            transaction.add(transactionLedger.txnDescription)
            content.add(transaction)
            txnAmountList.add(Collections.singletonList(String.valueOf(transactionLedger.transactionAmount)))
        }

        if (y < 2 * rowH) {
            contentStream = addNewPage(contentStream)
        }

        int currRows = (y / rowH) - 1 as int
        start = 0
        end = Math.min(currRows, content.size()) - 1

        while (end != content.size() - 1) {
            drawTransactionContent(contentStream, content[start..end])
            drawTransactionAmounts(contentStream, txnAmountList[start..end])
            contentStream = addNewPage(contentStream)
            start = end + 1
            end = Math.min(start + maxRows, content.size()) - 1
        }

        if ((end - start + 1) > 0) {
            drawTransactionContent(contentStream, content[start..end])
            drawTransactionAmounts(contentStream, txnAmountList[start..end])
        }
        updateY(-(rowH * ((end - start + 1) + 1) - 20) as float)

        if (y < 2 * rowH) {
            contentStream = addNewPage(contentStream)
        }
        return contentStream
    }

    private PDPageContentStream drawPayments(PDPageContentStream contentStream) {
        if (payments.size() == 0) {
            return
        }

        pdfBoxService.writeText(contentStream, helvetica_bold, black, 12, x, updateY(-25), 0, 100, "Transactions for your account")
        pdfBoxService.writeText(contentStream, helvetica_bold, black, 10, x, updateY(-25), 0, 100, "Payments")

        updateY(-15)
        pdfBoxService.drawSolidLine(contentStream, black, x, y, x + w as float, y, th)
        content = [["Date", "Merchant", "Amount"]]
        drawTransactionHeading(contentStream, content)
        return drawTransactionalRows(contentStream, payments)
    }

    private PDPageContentStream drawOtherFeesAndCharges(PDPageContentStream contentStream) {
        if (fees.size() == 0) {
            return
        }
        pdfBoxService.writeText(contentStream, helvetica_bold, black, 10, x, updateY(-25), 0, 100, "Other Fees & Charges")
        updateY(-15)
        pdfBoxService.drawSolidLine(contentStream, black, x, y, x + w as float, y, th)
        content = [["Date", "Merchant", "Amount"]]
        drawTransactionHeading(contentStream, content)
        return drawTransactionalRows(contentStream, fees)
    }

    private PDPageContentStream drawEMITxns(PDPageContentStream contentStream) {
        if (emis.size() == 0) {
            return
        }
        pdfBoxService.writeText(contentStream, helvetica_bold, black, 10, x, updateY(-25), 0, 100, "Transactions for your Card ending: XXXX XXXX XXXX 1234 - Ramnathan R")
        updateY(-15)
        pdfBoxService.writeText(contentStream, helvetica_bold, black, 10, x, updateY(-25), 0, 100, "EMIs")
        updateY(-15)
        pdfBoxService.drawSolidLine(contentStream, black, x, y, x + w as float, y, th)
        content = [["Date", "Merchant", "Amount"]]
        drawTransactionHeading(contentStream, content)
        return drawTransactionalRows(contentStream, emis)
    }

    private PDPageContentStream drawReversalsAndRefunds(PDPageContentStream contentStream) {
        if (reversals.size() == 0) {
            return
        }
        pdfBoxService.writeText(contentStream, helvetica_bold, black, 10, x, updateY(-25), 0, 100, "Reversal & Refund")
        updateY(-15)
        pdfBoxService.drawSolidLine(contentStream, black, x, y, x + w as float, y, th)
        content = [["Date", "Merchant", "Amount"]]
        drawTransactionHeading(contentStream, content)
        return drawTransactionalRows(contentStream, reversals)
    }

    private PDPageContentStream drawDomesticTxns(PDPageContentStream contentStream) {
        if (domesticTxns.size() == 0) {
            return
        }
        pdfBoxService.writeText(contentStream, helvetica_bold, black, 10, x, updateY(-25), 0, 100, "Domestic Transactions")
        updateY(-15)
        pdfBoxService.drawSolidLine(contentStream, black, x, y, x + w as float, y, th)
        content = [["Date", "Merchant", "Amount"]]
        drawTransactionHeading(contentStream, content)
        return drawTransactionalRows(contentStream, domesticTxns)
    }

    private PDPageContentStream drawDomesticCashTxns(PDPageContentStream contentStream) {
        if (domesticCashTxns.size() == 0) {
            return
        }
        updateY(10)
        content = [["Cash Transactions"]]
        drawTransactionHeading(contentStream, content)
        return drawTransactionalRows(contentStream, domesticCashTxns)
    }

    private PDPageContentStream drawInternationalTxns(PDPageContentStream contentStream) {
        if (internationalTxns.size() == 0) {
            return
        }
        pdfBoxService.writeText(contentStream, helvetica_bold, black, 10, x, updateY(-25), 0, 100, "International Transactions")
        updateY(-15)
        pdfBoxService.drawSolidLine(contentStream, black, x, y, x + w as float, y, th)
        content = [["Date", "Merchant", "Amount"]]
        drawTransactionHeading(contentStream, content)
        return drawTransactionalRows(contentStream, internationalTxns)
    }

    private PDPageContentStream drawInternationCashTxns(PDPageContentStream contentStream) {
        if (internationCashTxns.size() == 0) {
            return
        }

        updateY(10)
        content = [["Cash Transactions"]]
        drawTransactionHeading(contentStream, content)
        return drawTransactionalRows(contentStream, internationCashTxns)
    }

    private void drawTransactionHeading(PDPageContentStream contentStream, List<List<String>> content) {
        pdfBoxService.writeTableContents(contentStream, black, content, x, y, rowH, [(w / 8), (3 / 4 * w), (w / 8)] as float[], 10, 10, 10, helvetica_bold, [Constants.ALIGN_LEFT, Constants.ALIGN_LEFT, Constants.ALIGN_RIGHT] as char[])
    }

    private void drawTransactionContent(PDPageContentStream contentStream, List<List<String>> content) {
        updateY(-rowH)
        pdfBoxService.writeTableContents(contentStream, black, content, x, y, rowH, [(w / 8), (3 / 4 * w)] as float[], cellPadding, 10, 9, helvetica, [Constants.ALIGN_LEFT, Constants.ALIGN_LEFT] as char[])
    }

    private void drawTransactionAmounts(PDPageContentStream contentStream, List<List<String>> content) {
        pdfBoxService.writeTableContents(contentStream, black, content, x + (w / 8 + (3 / 4 * w)) as float, y, rowH, [(w / 8)] as float[], cellPadding, 10, 8, helvetica_bold, [Constants.ALIGN_RIGHT] as char[])
        pdfBoxService.drawTableBorders(contentStream, lightGrey, content.size(), content.size(), x, y, rowH, w, [(w / 8), (3 / 4 * w), (w / 8)] as float[], true, false)

    }

    private void drawLastPage(PDPageContentStream contentStream) {
        content = [["In case you pay only the Minimum Amount Due or any amount lesser than Total Amount Due, interest will be charged on a daily basis on the unpaid amount. Any fresh purchases made by you will be added to the previous outstanding balance, on which interest will be applicable. Always try to pay the total amount due to avoid interest and late payment fees. Making only the minimum payment every month would result in the repayment stretching over years with consequent interest payment on your outstanding balance"],
                   ["To improve the functionality and stability of the Indian financial system, the Government of India and Reserve Bank of India (RBI) asked all banks and financial institutions to share consumer data with Credit Information Companies (CICs). Based on this data, these companies, or credit bureaus, generate a credit score and a credit report for each borrower, which is shared with banks and financial institutions as well as the borrower."],
                   ["Click here to check updated Most Important Terms and Conditions(MITC) and Card Member Terms and Conditions related to chaipoint card"]]

        x = borderW
        fontSize = 9
        y = (tH - 80) as float
        pdfBoxService.writeText(contentStream, helvetica_bold, black, fontSize, x, y, 0, 100, "Important Notice")
        updateY(-10)
        pdfBoxService.drawSolidLine(contentStream, [145 / 255f, 145 / 255f, 145 / 255f] as float[], x, y, x + w as float, y, th)

        updateY(-20)
        for (int i = 0; i < content.size(); i++) {
            pdfBoxService.writeText(contentStream, helvetica, black, 7.5, x, y, -12, 160, content[i][0])
            updateY(-((content[i][0].length() / 160) * 12) - 35)
        }
        drawFooter(contentStream)
    }

    private void drawFooter(PDPageContentStream contentStream) {
        x = borderW
        y = footerH
        fontSize = 9
        pdfBoxService.drawRect(contentStream, [22 / 255f, 99 / 255f, 51 / 255f] as float[], 0, 0, tW, footerH)

        updateY(-30)
        text = "HDFC Bank Limited"
        pdfBoxService.writeText(contentStream, helvetica_bold, white, 7.5, x, y, 0, 45, text)

        text = "GSTN: 27AADCI6523Q3Z0" << Constants.DELIMITER <<
                "10th Floor, Tower 2A, Unit No. 1001-1002" << Constants.DELIMITER <<
                "OneIndiabulls Centre, Senapati Bapat Marg" << Constants.DELIMITER <<
                "Lower Parel, Mumbai, Maharashtra, 400013"
        pdfBoxService.writeText(contentStream, helvetica, white, 7.5, x, y, -12.5, 45, text)

        updateX(170)
        y = (footerH - 30) as float
        text = "Customer Support"
        pdfBoxService.writeText(contentStream, helvetica_bold, white, 7.5, x, y, 0, 20, text)

        text = "+ 123 4545 4545" << Constants.DELIMITER <<
                "8484848484"
        pdfBoxService.writeText(contentStream, helvetica, white, 7.5, x, y, -10, 20, text)

        updateX(90)
        y = (footerH - 30 + 10) as float //to compensate line space
        text = "Place of Service - Maharastra" << Constants.DELIMITER <<
                "State code - 27" << Constants.DELIMITER <<
                "HSN - 997113 - Financial & related services" << Constants.DELIMITER <<
                "HSN - 997157 - Foreign exchange services" << Constants.DELIMITER
        pdfBoxService.writeText(contentStream, helvetica, white, 7.5, x, y, -10, 45, text)

        updateX(170)
        y = (footerH - 30 + 10) as float
        text = "Invoice No - May 21/419666" << Constants.DELIMITER <<
                "Account No. - 2065309022860346492"
        pdfBoxService.writeText(contentStream, helvetica, white, 7.5, x, y, -10, 35, text)

        updateY(-40)
        text = "Issued By:"
        pdfBoxService.writeText(contentStream, helvetica, white, 7.5, x, y, 0, 35, text)

        imgW = 30
        imgH = 10
        updateY(-15)
        pdfBoxService.drawImage(contentStream, getImageXObjectFromFile(hdfcFile), x, y, imgW, imgH)

        x = borderW
        updateY(-25)
        pdfBoxService.drawSolidLine(contentStream, white, x, y, x + w as float, y, th)

        updateY(-10)
        text = "This is an authenticated intimation/statement." << Constants.DELIMITER <<
                "You are requested to immediately notify us on help@gethypercard.app in case of any discrepancy in the statement."
        pdfBoxService.writeText(contentStream, helvetica, white, 6.5, x, y, -10, 125, text)

        imgW = 82.5
        imgH = 15
        text = "Powered by"
        updateY(-18)
        pdfBoxService.writeText(contentStream, helvetica_bold, white, 7.5, x + 430 as float, y, 0, text.length(), text)

        pdfBoxService.drawImage(contentStream, getImageXObjectFromFile(hyperfaceFile), x + w - imgW as float, y - 2 as float, imgW, imgH)
    }
}