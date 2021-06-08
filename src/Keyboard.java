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

    StringBuffer tempCode = new StringBuffer(40);
    char[] tempBuffer = new char[40];
    String[] code = new String[64];
    int codeInsertPos = 0;
    int codeCount = 0;

    int lastOctave = 5;
    int lastVolume = 9;
    int lastTempCodeLength = -1;

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

        g.setColor(0x000000);
        int codeY = 0;
        for (int i = 0; i < 3; i++)
        {
            int pos = (codeInsertPos - 3 + i + code.length) & 63;
            if (code[pos] != null)
            {
                g.drawString(code[pos], 0, codeY, Graphics.LEFT | Graphics.TOP);
                codeY += 12;
            }
        }
        if (0 < tempCode.length())
        {
            g.drawChars(tempBuffer, 0, tempCode.length(), 0, codeY, Graphics.LEFT | Graphics.TOP);
        }

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

        if (0 < volume)
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
            case KEY_NUM0:
                addRest();
                repaint(0, 0, 240, 60);
                break;
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
            case -8: // KEY_CLR
                if (0 <= lastTempCodeLength)
                {
                    tempCode.setLength(lastTempCodeLength);
                    repaint(0, 0, 240, 60);
                    lastTempCodeLength = -1;
                }
                break;
        }
        if (note >= 0)
        {
            int note_value = note + octave * 12;
            if (note_value <= 127)
            {
                try { Manager.playTone(note_value, 200, 100); } catch (Exception ex) {}
                addNote(note);
                repaint();
            }
            else
            {
                repaint(0, 60, 240, 60);
            }
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

    void addRest()
    {
        if (40 < tempCode.length() + 4)
        {
            code[codeInsertPos] = tempCode.toString();
            codeInsertPos = (codeInsertPos + 1) & 63;
            if (codeCount < code.length) { codeCount++; }
            tempCode.setLength(0);
        }
        lastTempCodeLength = tempCode.length();
        tempCode.append('R')
            .append(LENGTH.charAt((length << 2) + 1))
            .append(LENGTH.charAt((length << 2) + 2))
            .append(LENGTH.charAt((length << 2) + 3));
        tempCode.getChars(0, tempCode.length(), tempBuffer, 0);
        lastOctave = octave + (note == 12 ? 1 : 0);
        lastVolume = volume;
    }

    void addNote(int note)
    {
        int len = 3;
        if (lastVolume != volume) { len += 4; }
        switch (Math.abs(octave - lastOctave + (note == 12 ? 1 : 0)))
        {
            case 0:
                break;
            case 1:
                len += 2;
                break;
            default:
                len += 3;
                break;
        }
        switch (note)
        {
            case 0:
            case 2:
            case 4:
            case 5:
            case 7:
            case 9:
            case 11:
            case 12:
                len++;
                break;
            case 1:
            case 3:
            case 6:
            case 8:
            case 10:
                len += 2;
                break;
        }
        if (40 < tempCode.length() + len)
        {
            code[codeInsertPos] = tempCode.toString();
            codeInsertPos = (codeInsertPos + 1) & 63;
            if (codeCount < code.length) { codeCount++; }
            tempCode.setLength(0);
        }
        lastTempCodeLength = tempCode.length();
        if (lastVolume != volume)
        {
            tempCode.append(VOLUME.charAt(volume << 2))
                .append(VOLUME.charAt((volume << 2) + 1))
                .append(VOLUME.charAt((volume << 2) + 2))
                .append(VOLUME.charAt((volume << 2) + 3));
        }
        switch (octave - lastOctave + (note == 12 ? 1 : 0))
        {
            case -2:
                tempCode.append(">> ");
                break;
            case -1:
                tempCode.append("> ");
                break;
            case 0:
                break;
            case 1:
                tempCode.append("< ");
                break;
            case 2:
                tempCode.append("<< ");
                break;
            default:
                tempCode.append(OCTAVE.charAt((octave << 1) + octave))
                    .append(OCTAVE.charAt(((octave << 1) + 1 + octave)))
                    .append(OCTAVE.charAt(((octave << 1) + 2 + octave)));
                break;
        }
        switch (note)
        {
            case 0:
                tempCode.append('C');
                break;
            case 1:
                tempCode.append("C#");
                break;
            case 2:
                tempCode.append('D');
                break;
            case 3:
                tempCode.append("D#");
                break;
            case 4:
                tempCode.append('E');
                break;
            case 5:
                tempCode.append('F');
                break;
            case 6:
                tempCode.append("F#");
                break;
            case 7:
                tempCode.append('G');
                break;
            case 8:
                tempCode.append("G#");
                break;
            case 9:
                tempCode.append('A');
                break;
            case 10:
                tempCode.append("A#");
                break;
            case 11:
                tempCode.append('B');
                break;
            case 12:
                tempCode.append('C');
                break;
        }
        tempCode.append(LENGTH.charAt((length << 2) + 1))
            .append(LENGTH.charAt((length << 2) + 2))
            .append(LENGTH.charAt((length << 2) + 3));
        tempCode.getChars(0, tempCode.length(), tempBuffer, 0);
        lastOctave = octave + (note == 12 ? 1 : 0);
        lastVolume = volume;
    }

    String getMmlString()
    {
        int len = tempCode.length() + 2;
        for (int i = 0; i < code.length; i++)
        {
            if (code[i] != null)
            {
                len += code[i].length() + 2;
            }
        }
        StringBuffer sb = new StringBuffer(len);
        int pos = codeInsertPos;
        for (int i = 0; i < code.length; i++)
        {
            if (code[pos] != null)
            {
                sb.append(code[pos]).append('\n');
            }
            pos = (pos + 1) & 63;
        }
        sb.append(tempBuffer, 0, tempCode.length()).append('\n');
        return sb.toString();
    }

    void clearMmlCode()
    {
        for (int i = 0; i < code.length; i++)
        {
            code[i] = null;
        }
        codeInsertPos = 0;
        codeCount = 0;
        lastTempCodeLength = -1;
        tempCode.setLength(0);
        repaint(0, 0, 240, 60);
    }

    Image makeBackgroundImage()
    {
        Image image = Image.createImage(240, 268);
        Graphics g = image.getGraphics();

        g.setColor(0xC0C0C0);
        g.fillRect(0, 0, getWidth(), 268);

        g.setColor(0xA0A0A0);
        for (int i = 1; i < 5; i++)
        {
            g.drawLine(0, i * 12, 240, i * 12);
        }

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