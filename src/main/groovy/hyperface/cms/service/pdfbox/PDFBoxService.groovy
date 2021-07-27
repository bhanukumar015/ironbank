package hyperface.cms.service.pdfbox

import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject

import java.awt.image.BufferedImage

interface PDFBoxService {

    float getTextWidth(PDFont fontType, float fontSize, String text) throws IOException

    void writeText(PDFont font, float[] rgb, float fontSize, float x, float y, float lineSpace, int wrapLength, String text) throws IOException

    void drawSolidLine(float[] rgb, float sX, float sY, float eX, float eY, float th) throws IOException

    void drawRect(float[] rgb, float x, float y, float w, float h) throws IOException

    void drawImage(PDImageXObject imageXObject, float x, float y, float w, float h) throws IOException

    void writeTableContents(float[] rgb, List<List<String>> content, float x, float y, float rowH, float[] colW, float cellPadding, float fontHeight, float fontSize, PDFont font, char[] colAlign) throws IOException

    void drawTableBorders(float[] rgb, int rows, int cols, float x, float y, float rowH, float rowW, float[] colW, boolean isHorizontalBorder, boolean isVerticalBorder) throws IOException

    void close()
}