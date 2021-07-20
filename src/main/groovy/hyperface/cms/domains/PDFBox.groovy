package hyperface.cms.domains

import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.font.PDFont

class PDFBox {

    PDFBox(PDPage pdPage, float padding, PDFont font) {
        this.pw = pdPage.getMediaBox().getWidth()
        this.ph = pdPage.getMediaBox().getHeight()

        this.padding = new Padding()
        this.padding.top = padding
        this.padding.bottom = padding
        this.padding.left = padding
        this.padding.right = padding

        this.cursor = new Cursor(padding, (float) (this.ph - padding))

        this.pdFont = font
    }

    static class Cursor {
        float x
        float y

        Cursor(float x, float y) {
            this.x = x
            this.y = y
        }
    }

    static class Image {
        Cursor cursor
        float width
        float height

        Image(Cursor cursor, float width, float height) {
            this.cursor = cursor
            this.width = width
            this.height = height
        }
    }

    static class Padding {
        float top
        float bottom
        float left
        float right
    }

    Cursor cursor
    Image image
    Padding padding
    PDFont pdFont

    float fontSize
    float pw
    float ph

    String text

}
