package hyperface.cms.service.pdfbox

import hyperface.cms.Constants
import org.apache.commons.text.WordUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI
import org.apache.pdfbox.pdmodel.interactive.annotation.AnnotationFilter
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary
import org.springframework.stereotype.Service

import java.awt.image.BufferedImage
import java.util.stream.Collectors

@Service
class PDFBoxServiceImpl implements PDFBoxService {

    private PDDocument document
    PDPage pdPage
    private PDPageContentStream contentStream

    PDFBoxServiceImpl() {
        document = new PDDocument()
    }

    PDDocument getDocument() {
        return document
    }

    void addA4Page() {
        pdPage = new PDPage(PDRectangle.A4)
        document.addPage(pdPage)
        contentStream = new PDPageContentStream(document, pdPage)
    }

    @Override
    float getTextWidth(PDFont fontType, float fontSize, String text) {
        return ((fontType.getStringWidth(text) / 1000.0f) * fontSize) as float
    }

    @Override
    void writeText(PDFont font, float[] rgb, float fontSize, float x, float y, float lineSpace, int wrapLength, String text) throws IOException {
        contentStream.saveGraphicsState()

        String[] paragraph = text.split(Constants.DELIMITER)
        for (int i = 0; i < paragraph.length; i++) {
            String[] wrappedText = WordUtils.wrap(paragraph[i], wrapLength as int).split("\\r?\\n")
            if(wrappedText.length > 1 && lineSpace == 0) {
                lineSpace = -fontSize
            }
            for (int j=0; j < wrappedText.length; j++) {
                y += lineSpace

                contentStream.beginText()
                contentStream.setFont(font, fontSize)
                contentStream.setNonStrokingColor(rgb[0], rgb[1], rgb[2])
                contentStream.newLineAtOffset(x, y)
                contentStream.showText(wrappedText[j])
                contentStream.endText()
            }
        }


        contentStream.restoreGraphicsState()
    }

    @Override
    void drawSolidLine(float[] rgb, float sX, float sY, float eX, float eY, float th) throws IOException {
        contentStream.saveGraphicsState()

        contentStream.setLineWidth(th)
        contentStream.setStrokingColor(rgb[0], rgb[1], rgb[2])
        contentStream.moveTo(sX, sY)
        contentStream.lineTo(eX, eY)
        contentStream.stroke()

        contentStream.restoreGraphicsState()
    }

    @Override
    void drawRect(float[] rgb, float x, float y, float w, float h) throws IOException {
        contentStream.saveGraphicsState()

        contentStream.addRect(x, y, w, h)
        contentStream.setNonStrokingColor(rgb[0], rgb[1], rgb[2])
        contentStream.fill()

        contentStream.restoreGraphicsState()
    }

    @Override
    void drawImage(PDImageXObject imageXObject, float x, float y, float w, float h) throws IOException {
        contentStream.saveGraphicsState()

        contentStream.drawImage(imageXObject, x, y, w, h)

        contentStream.restoreGraphicsState()
    }

    @Override
    void writeTableContents(float[] rgb, List<List<String>> content, float x, float y, float rowH, float[] colW, float cellPadding, float fontHeight, float fontSize, PDFont font, char[] colAlign) throws IOException {
        contentStream.saveGraphicsState()

        float tableW = 0.0
        for (int i=0; i< colW.length; i++) {
            tableW += colW[i]
        }

        float sx = x
        float sy = y

        for (int i = 0; i < content.size(); i++) {
            for (int j = 0; j < content[i].size(); j++) {
                if (colAlign[j] == Constants.ALIGN_RIGHT) {
                    sx += colW[j] - 2 * cellPadding - getTextWidth(font, fontSize, content[i][j]) as float
                }
                writeText(font, rgb, fontSize, sx + cellPadding as float, sy - (rowH + fontSize)/2 as float, 0, 100, content[i][j]) //todo: estimate by width

                sx += colW[j]
            }
            sx = x
            sy += (-rowH)
        }

        contentStream.restoreGraphicsState()
    }

    @Override
    void drawTableBorders(float[] rgb, int rows, int cols, float x, float y, float rowH, float rowW, float[] colW, boolean isHorizontalBorder, boolean isVerticalBorder) throws IOException {
        contentStream.saveGraphicsState()

        float sx = x
        float sy = y
        if(isHorizontalBorder) {
            for(int i=0; i <= rows; i++) {
                drawSolidLine(rgb, sx, sy, sx + rowW as float, sy, 1)
                sy += (-rowH)
            }
        }

        if(isVerticalBorder) {
            sx = x
            sy = y
            for (int i=0; i < cols; i++) {
                drawSolidLine(rgb, sx, sy, sx, sy - (rows* rowH) as float, 1)
                sx += colW[i]
            }
            drawSolidLine(rgb, sx, sy, sx, sy - (rows * rowH) as float, 1)
        }

        contentStream.restoreGraphicsState()
    }

    @Override
    void close() {
        contentStream.close()
    }
}
