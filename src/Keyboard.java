import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.media.Manager;

class Keyboard extends Canvas
{
    int note = -1;

    // @Override Canvas.paint
    public void paint(Graphics g)
    {
        g.setColor(0xF0F0F0);
        g.fillRect(0, 0, getWidth(), getHeight());

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

        switch (note)
        {
            case 0:
                g.setColor(0xF07000);
                g.fillRect(4, 110, 23, 5);
                break;
            case 2:
                g.setColor(0xF07000);
                g.fillRect(30 + 4, 110, 23, 5);
                break;
            case 4:
                g.setColor(0xF07000);
                g.fillRect(60 + 4, 110, 23, 5);
                break;
            case 5:
                g.setColor(0xF07000);
                g.fillRect(90 + 4, 110, 23, 5);
                break;
            case 7:
                g.setColor(0xF07000);
                g.fillRect(120 + 4, 110, 23, 5);
                break;
            case 9:
                g.setColor(0xF07000);
                g.fillRect(150 + 4, 110, 23, 5);
                break;
            case 11:
                g.setColor(0xF07000);
                g.fillRect(180 + 4, 110, 23, 5);
                break;
            case 12:
                g.setColor(0xF07000);
                g.fillRect(210 + 4, 110, 23, 5);
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