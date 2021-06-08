import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
import javax.microedition.media.Manager;

final class Keyboard extends Canvas
{
    final Font font;

    int note = -1;

    Keyboard()
    {
        boolean wtk = String
            .valueOf(System.getProperty("microedition.platform"))
            .startsWith("Sun");

        // font height = 12
        font = wtk
             ? Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM)
             : Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    }

    // @Override Canvas.paint
    public void paint(Graphics g)
    {
        g.setColor(0xD0D0D0);
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

        int w = 40, h = 22;
        int offX = (getWidth() - w) / 2;
        int offY = 120 + 5;
        g.drawRoundRect(offX, offY, w, h, 10, 10);
        offY += h + 3;
        g.drawRoundRect(offX, offY, w, h, 10, 10);
        offY += h + 3;
        offX = (getWidth() - 3 * w) / 2;
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
        g.drawRoundRect(offX, offY, w, h, 10, 10);
        offX = getWidth() - offX - w;
        g.drawRoundRect(offX, offY, w, h, 10, 10);

        switch (note)
        {
            case 0:
                g.setColor(0xF07000);
                g.fillRect(8, 110, 15, 5);
                break;
            case 2:
                g.setColor(0xF07000);
                g.fillRect(30 + 8, 110, 15, 5);
                break;
            case 4:
                g.setColor(0xF07000);
                g.fillRect(60 + 8, 110, 15, 5);
                break;
            case 5:
                g.setColor(0xF07000);
                g.fillRect(90 + 8, 110, 15, 5);
                break;
            case 7:
                g.setColor(0xF07000);
                g.fillRect(120 + 8, 110, 15, 5);
                break;
            case 9:
                g.setColor(0xF07000);
                g.fillRect(150 + 8, 110, 15, 5);
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
                note = 0;
                break;
            case KEY_NUM2:
                note = 2;
                break;
            case KEY_NUM3:
                note = 4;
                break;
            case KEY_NUM4:
                note = 5;
                break;
            case KEY_NUM5:
                note = 7;
                break;
            case KEY_NUM6:
                note = 9;
                break;
            case KEY_NUM7:
                note = 11;
                break;
            case KEY_NUM8:
                note = 12;
                break;
        }
        if (note >= 0)
        {
            try { Manager.playTone(60 + note, 200, 100); } catch (Exception ex) {}
            repaint(3, 110, 234, 5);
        }
    }

    // @Override Canvas.keyReleased
    protected void keyReleased(int keyCode)
    {
        if (note >= 0)
        {
            note = -1;
            repaint(3, 110, 234, 5);
        }
    }
}