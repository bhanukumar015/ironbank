package hyperface.cms.service.pdfbox

import hyperface.cms.domains.PDFBox
import hyperface.cms.domains.PDFBox.Cursor
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject

interface PDFBoxService {

    void updateX(int x)

    void updateY(int y)

    float getTextWidth(PDFont fontType, float fontSize, String text)

    void writeText(PDPageContentStream contentStream, PDFBox pdfBox)

    void drawSolidLine(PDPageContentStream contentStream, Cursor begin, Cursor end)

    void drawRectangularBox(PDPageContentStream contentStream, Cursor cursor, float w, float h)

    void drawImage(PDPageContentStream contentStream, PDImageXObject imageXObject, PDFBox . Image image)

    void close(PDPageContentStream contentStream)
}