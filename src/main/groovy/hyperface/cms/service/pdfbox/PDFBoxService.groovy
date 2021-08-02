package hyperface.cms.service.pdfbox

import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject

import java.awt.image.BufferedImage

interface PDFBoxService {

    float getTextWidth(PDFont fontType, float fontSize, String text) throws IOException

    void writeText(PDPageContentStream contentStream, PDFont font, float[] rgb, float fontSize, float x, float y, float lineSpace, int wrapLength, String text) throws IOException

    void drawSolidLine(PDPageContentStream contentStream, float[] rgb, float sX, float sY, float eX, float eY, float th) throws IOException

    void drawRect(PDPageContentStream contentStream, float[] rgb, float x, float y, float w, float h) throws IOException

    void drawImage(PDPageContentStream contentStream, PDImageXObject imageXObject, float x, float y, float w, float h) throws IOException

    void writeTableContents(PDPageContentStream contentStream, float[] rgb, List<List<String>> content, float x, float y, float rowH, float[] colW, float cellPadding, float fontHeight, float fontSize, PDFont font, char[] colAlign) throws IOException

    void drawTableBorders(PDPageContentStream contentStream, float[] rgb, int rows, int cols, float x, float y, float rowH, float rowW, float[] colW, boolean isHorizontalBorder, boolean isVerticalBorder) throws IOException

    void close(PDPageContentStream contentStream)
}