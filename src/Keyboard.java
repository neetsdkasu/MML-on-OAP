import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Manager;

final class Keyboard extends Canvas
{
    static final String
        OCTAVE = "O-1O0 O1 O2 O3 O4 O5 O6 O7 O8 O9 ",
        LENGTH = "L1. L1  L2. L2  L4. L4  L8. L8  L16.L16 L32.L32 L3  L6  L12 L24 ",
        VOLUME = "V10 V20 V30 V40 V50 V60 V70 V80 V90 V100";

    final Font smallFont, mediumFont;
    final Image backgroundImage;

    int note = -1;
    int octave = 5;
    int length = 5;
    int volume = 9;
    int key_type = 0;

    Keyboard()
    {
        boolean wtk = String
            .valueOf(System.getProperty("microedition.platform"))
            .startsWith("Sun");

        // font height = 12
        smallFont = wtk
             ? Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM)
             : Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);

        mediumFont = wtk
             ? Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE)
             : Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);

        backgroundImage = makeBackgroundImage();
    }

    // @Override Canvas.paint
    public void paint(Graphics g)
    {
        g.setFont(smallFont);

        g.drawImage(backgroundImage, 0, 0, Graphics.LEFT | Graphics.TOP);

        if (key_type == 0)
        {
            g.setColor(0x800080);
            for (int i = 0; i < 8; i++)
            {
                if (octave == 10 && i >= 5) { break; }
                g.drawSubstring("12345678", i, 1, i * 30 + 12, 95, Graphics.LEFT | Graphics.TOP);
            }
        }
        else
        {
            g.setColor(0xF040F0);
            for (int i = 0; i < 2; i++)
            {
                g.drawSubstring("12", i, 1, i * 30 + 27, 65, Graphics.LEFT | Graphics.TOP);
            }
            for (int i = 0; i < 3; i++)
            {
                if (octave == 10 && i > 0) { break; }
                g.drawSubstring("456", i, 1, i * 30 + 117, 65, Graphics.LEFT | Graphics.TOP);
            }
        }

        g.setFont(mediumFont);

        g.setColor(0x000000);
        g.drawSubstring(OCTAVE, octave * 3, 3, 12, 130, Graphics.LEFT | Graphics.TOP);
        g.drawSubstring(LENGTH, length * 4, 4, 12, 150, Graphics.LEFT | Graphics.TOP);
        g.drawSubstring(VOLUME, volume * 4, 4, 12, 170, Graphics.LEFT | Graphics.TOP);

        if (0 < octave)
        {
            g.drawSubstring(OCTAVE, (octave - 1) * 3, 3, 62, 142, Graphics.LEFT | Graphics.TOP);
        }
        if (octave < 10)
        {
            g.drawSubstring(OCTAVE, (octave + 1) * 3, 3, 147, 142, Graphics.LEFT | Graphics.TOP);
        }

        if (0 < length)
        {
            g.drawSubstring(LENGTH, (length - 1) * 4, 4, 105, 128, Graphics.LEFT | Graphics.TOP);
        }
        if (length < 15)
        {
            g.drawSubstring(LENGTH, (length + 1) * 4, 4, 105, 154, Graphics.LEFT | Graphics.TOP);
        }

        if (key_type == 0)
        {
            g.drawString("C", 71, 178, Graphics.LEFT | Graphics.TOP);
            g.drawString("D", 111, 178, Graphics.LEFT | Graphics.TOP);
            g.drawString("E", 151, 178, Graphics.LEFT | Graphics.TOP);

            g.drawString("F", 71, 200, Graphics.LEFT | Graphics.TOP);
            g.drawString("G", 111, 200, Graphics.LEFT | Graphics.TOP);
            if (octave < 10)
            {
                g.drawString("A", 151, 200, Graphics.LEFT | Graphics.TOP);

                g.drawString("B", 71, 222, Graphics.LEFT | Graphics.TOP);
                g.drawString("<C>", 101, 222, Graphics.LEFT | Graphics.TOP);
            }
            g.drawString("#", 151, 222, Graphics.LEFT | Graphics.TOP);
        }
        else
        {
            g.drawString("C#", 71, 178, Graphics.LEFT | Graphics.TOP);
            g.drawString("D#", 111, 178, Graphics.LEFT | Graphics.TOP);
            // g.drawString("?", 151, 178, Graphics.LEFT | Graphics.TOP);

            g.drawString("F#", 71, 200, Graphics.LEFT | Graphics.TOP);
            if (octave < 10)
            {
                g.drawString("G#", 111, 200, Graphics.LEFT | Graphics.TOP);
                g.drawString("A#", 151, 200, Graphics.LEFT | Graphics.TOP);

                // g.drawString("?", 71, 222, Graphics.LEFT | Graphics.TOP);
                // g.drawString("?", 101, 222, Graphics.LEFT | Graphics.TOP);
            }
            g.setColor(0xF00070);
            g.drawString("#", 151, 222, Graphics.LEFT | Graphics.TOP);
            g.setColor(0x000000);
        }

        if (volume > 0)
        {
            g.drawSubstring(VOLUME, (volume - 1) * 4, 4, 63, 244, Graphics.LEFT | Graphics.TOP);
        }
        g.drawString("R", 111, 244, Graphics.LEFT | Graphics.TOP);
        if (volume < 9)
        {
            g.drawSubstring(VOLUME, (volume + 1) * 4, 4, 143, 244, Graphics.LEFT | Graphics.TOP);
        }

        switch (note)
        {
            case 0:
                g.setColor(0xF07000);
                g.fillRect(8, 110, 15, 5);
                break;
            case 1:
                g.setColor(0xF07000);
                g.fillRect(23, 80, 15, 5);
                break;
            case 2:
                g.setColor(0xF07000);
                g.fillRect(30 + 8, 110, 15, 5);
                break;
            case 3:
                g.setColor(0xF07000);
                g.fillRect(53, 80, 15, 5);
                break;
            case 4:
                g.setColor(0xF07000);
                g.fillRect(60 + 8, 110, 15, 5);
                break;
            case 5:
                g.setColor(0xF07000);
                g.fillRect(90 + 8, 110, 15, 5);
                break;
            case 6:
                g.setColor(0xF07000);
                g.fillRect(113, 80, 15, 5);
                break;
            case 7:
                g.setColor(0xF07000);
                g.fillRect(120 + 8, 110, 15, 5);
                break;
            case 8:
                g.setColor(0xF07000);
                g.fillRect(143, 80, 15, 5);
                break;
            case 9:
                g.setColor(0xF07000);
                g.fillRect(150 + 8, 110, 15, 5);
                break;
            case 10:
                g.setColor(0xF07000);
                g.fillRect(173, 80, 15, 5);
                break;
            case 11:
                g.setColor(0xF07000);
                g.fillRect(180 + 8, 110, 15, 5);
                break;
            case 12:
                g.setColor(0xF07000);
                g.fillRect(210 + 8, 110, 15, 5);
                break;
        }
    }

    // @Override Canvas.keyPressed
    protected void keyPressed(int keyCode)
    {
        note = -1;
        switch (keyCode)
        {
            case KEY_NUM1:
                note = 0 + key_type;
                break;
            case KEY_NUM2:
                note = 2 + key_type;
                break;
            case KEY_NUM3:
                if (key_type == 0) { note = 4; }
                break;
            case KEY_NUM4:
                note = 5 + key_type;
                break;
            case KEY_NUM5:
                note = 7 + key_type;
                break;
            case KEY_NUM6:
                note = 9 + key_type;
                break;
            case KEY_NUM7:
                if (key_type == 0) { note = 11; }
                break;
            case KEY_NUM8:
                if (key_type == 0) { note = 12; }
                break;
            case KEY_NUM9:
                key_type = 1 - key_type;
                repaint(0, 60, 240, 208);
                break;
            case -1: // KEY_UP
                if (0 < length)
                {
                    length -= 1;
                    repaint(0, 120, 160, 60);
                }
                break;
            case -2: // KEY_DOWN
                if (length < 15)
                {
                    length += 1;
                    repaint(0, 120, 160, 60);
                }
                break;
            case -3: // KEY_LEFT
                if (0 < octave)
                {
                    octave -= 1;
                    if (octave == 9)
                    {
                        repaint(0, 60, 240, 186);
                    }
                    else
                    {
                        repaint(0, 120, 180, 40);
                    }
                }
                break;
            case -4: // KEY_RIGHT
                if (octave < 10)
                {
                    octave += 1;
                    if (octave == 10)
                    {
                        repaint(0, 60, 240, 186);
                    }
                    else
                    {
                        repaint(0, 120, 180, 40);
                    }
                }
                break;
            case KEY_STAR: // *
                if (0 < volume)
                {
                    volume -= 1;
                    repaint(12, 170, 180, 98);
                }
                break;
            case KEY_POUND: // #
                if (volume < 9)
                {
                    volume += 1;
                    repaint(12, 170, 180, 98);
                }
                break;
        }
        if (note >= 0)
        {
            int note_value = note + octave * 12;
            if (note_value <= 127)
            {
                try { Manager.playTone(note_value, 200, 100); } catch (Exception ex) {}
            }
            repaint(0, 60, 240, 60);
        }
    }

    // @Override Canvas.keyReleased
    protected void keyReleased(int keyCode)
    {
        if (note >= 0)
        {
            note = -1;
            repaint(0, 60, 240, 60);
        }
    }

    Image makeBackgroundImage()
    {
        Image image = Image.createImage(240, 268);
        Graphics g = image.getGraphics();

        g.setColor(0xC0C0C0);
        g.fillRect(0, 0, getWidth(), 268);

        for (int i = 0; i < 8; i++)
        {
            g.setColor(0xF0F0F0);
            g.fillRect(i * 30, 60, 30, 60);
            g.setColor(0x000000);
            g.drawRect(i * 30, 60, 30, 60);
        }
        for (int i = 0; i < 2; i++)
        {
            g.fillRect(i * 30 + 20, 60, 20, 30);
        }
        for (int i = 0; i < 3; i++)
        {
            g.fillRect(i * 30 + 110, 60, 20, 30);
        }
        g.fillRect(230, 60, 20, 30);

        int w = 40, h = 22, offX, offY;

        offX = (getWidth() - w) / 2;
        offY = 120 + 5;
        g.setColor(0xD0D0D0);
        g.fillRoundRect(offX, offY, w, h, 10, 10);
        g.setColor(0x000000);
        g.drawRoundRect(offX, offY, w, h, 10, 10);

        offY += h + 3;
        g.setColor(0xD0D0D0);
        g.fillRoundRect(offX, offY, w, h, 10, 10);
        g.setColor(0x000000);
        g.drawRoundRect(offX, offY, w, h, 10, 10);

        offY += h + 3;
        offX = (getWidth() - 3 * w) / 2;
        g.setColor(0xD0D0D0);
        g.fillRoundRect(offX, offY, 3 * w, 4 * h, 10, 10);
        g.setColor(0x000000);
        g.drawRoundRect(offX, offY, 3 * w, 4 * h, 10, 10);
        for (int i = 1; i < 3; i++)
        {
            g.drawLine(i * w + offX, offY, i * w + offX, 4 * h + offY);
        }
        for (int i = 1; i < 4; i++)
        {
            g.drawLine(offX, i * h + offY, 3 * w + offX, i * h + offY);
        }

        offX = (getWidth() - 3 * w - 6) / 2;
        offY = 120 + 8 + h / 2;
        g.setColor(0xD0D0D0);
        g.fillRoundRect(offX, offY, w, h, 10, 10);
        g.setColor(0x000000);
        g.drawRoundRect(offX, offY, w, h, 10, 10);

        offX = getWidth() - offX - w;
        g.setColor(0xD0D0D0);
        g.fillRoundRect(offX, offY, w, h, 10, 10);
        g.setColor(0x000000);
        g.drawRoundRect(offX, offY, w, h, 10, 10);

        return Image.createImage(image);
    }
}