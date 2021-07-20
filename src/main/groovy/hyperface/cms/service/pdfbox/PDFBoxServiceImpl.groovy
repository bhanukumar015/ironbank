package hyperface.cms.service.pdfbox

import hyperface.cms.domains.PDFBox
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject

class PDFBoxServiceImpl implements PDFBoxService {

    private static final PDFBoxServiceImpl pdfBoxService = new PDFBoxServiceImpl()

    private PDPageContentStream contentStream

    static PDFBoxServiceImpl getInstance() {
        return pdfBoxService
    }

    PDPageContentStream addPage(PDDocument document, PDPage pdPage) {
        document.addPage(pdPage)
        contentStream = new PDPageContentStream(document, pdPage)
        return contentStream
    }

    @Override
    void updateX(int x) {

    }

    @Override
    void updateY(int y) {

    }

    @Override
    float getTextWidth(PDFont fontType, float fontSize, String text) {
        return 0
    }

    @Override
    void writeText(PDPageContentStream contentStream, PDFBox pdfBox) {
        contentStream.saveGraphicsState()

        contentStream.beginText()
        contentStream.setFont(pdfBox.pdFont, pdfBox.fontSize)
        //contentStream.newLineAtOffset(pdfBox.cursor.getX(), pdfBox.cursor.getY())
        contentStream.newLineAtOffset(10, -50)
        contentStream.showText(pdfBox.text)
        contentStream.endText()

        contentStream.beginText()
        contentStream.setFont(pdfBox.pdFont, pdfBox.fontSize)
        //contentStream.newLineAtOffset(pdfBox.cursor.getX(), pdfBox.cursor.getY())
        contentStream.newLineAtOffset(10, 150)
        contentStream.showText(pdfBox.text)
        contentStream.endText()

        contentStream.restoreGraphicsState()
    }

    @Override
    void drawSolidLine(PDPageContentStream contentStream, PDFBox.Cursor begin, PDFBox.Cursor end) {
        contentStream.saveGraphicsState()

        contentStream.setStrokingColor(1.0, 0.0, 0.0) //todo: take it from enums
        contentStream.moveTo(begin.getX(), begin.getY())
        contentStream.lineTo(end.getX(), end.getY())
        contentStream.stroke()

        contentStream.restoreGraphicsState()
    }

    @Override
    void drawRectangularBox(PDPageContentStream contentStream, PDFBox.Cursor cursor, float w, float h) {
        contentStream.saveGraphicsState()

        contentStream.addRect(cursor.getX(), cursor.getY(), w, h)
        contentStream.setNonStrokingColor(22/255 as float, 99/255f as float, 51/255f as float)
        contentStream.fill()

        contentStream.restoreGraphicsState()
    }

    @Override
    void drawImage(PDPageContentStream contentStream, PDImageXObject imageXObject, PDFBox.Image image) {
        contentStream.saveGraphicsState()

        contentStream.drawImage(imageXObject, image.cursor.getX(), image.cursor.getY(), image.getWidth(), image.getHeight())

        contentStream.restoreGraphicsState()
    }

    @Override
    void close(PDPageContentStream contentStream) {
        contentStream.close()
    }
}
