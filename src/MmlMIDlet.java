import java.io.*;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.ToneControl;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;

public final class MmlMIDlet extends MIDlet implements CommandListener
{
    Player player = null;

    List mainDisp;
    TextBox titleBox, codingBox;
    Alert confirmDelete, confirmBack;
    Form helpViewer = null;
    Keyboard keyboard;

    Command exitCommand,
            newCommand,
            titleOkCommand,
            titleCancelCommand,
            codingBackCommand,
            codingSaveCommand,
            codingPlayCommand,
            codingStopCommand,
            codingDeleteCommand,
            cancelDeleteCommand,
            doDeleteCommand,
            cancelBackCommand,
            goBackCommand,
            helpCommand = null,
            closeHelpCommand = null,
            keyboardCommand,
            closePianoCommand;

    RecordStore currentRecord = null;

    public MmlMIDlet()
    {
        mainDisp = new List("MML on OAP", List.IMPLICIT);
        titleBox = new TextBox("INPUT TITLE", "", 28, TextField.ANY);
        codingBox = new TextBox("INPUT MML", "", 5000, TextField.ANY);
        confirmDelete = new Alert("CONFIRM", "", null, AlertType.WARNING);
        confirmBack = new Alert("CONFIRM", "GO BACK WITHOUT SAVING ??", null, AlertType.WARNING);
        keyboard = new Keyboard();

        confirmDelete.setTimeout(Alert.FOREVER);
        confirmBack.setTimeout(Alert.FOREVER);

        exitCommand = new Command("EXIT", Command.EXIT, 1);
        mainDisp.addCommand(exitCommand);
        newCommand = new Command("NEW", Command.SCREEN, 2);
        mainDisp.addCommand(newCommand);

        titleCancelCommand = new Command("CANCEL", Command.CANCEL, 1);
        titleBox.addCommand(titleCancelCommand);
        titleOkCommand = new Command("OK", Command.OK, 2);
        titleBox.addCommand(titleOkCommand);

        codingBackCommand = new Command("BACK", Command.BACK, 1);
        codingBox.addCommand(codingBackCommand);
        codingSaveCommand = new Command("SAVE", Command.SCREEN, 2);
        codingBox.addCommand(codingSaveCommand);
        codingPlayCommand = new Command("PLAY", Command.SCREEN, 3);
        codingBox.addCommand(codingPlayCommand);
        codingStopCommand = new Command("STOP", Command.SCREEN, 4);
        codingBox.addCommand(codingStopCommand);
        codingDeleteCommand = new Command("DELETE", Command.SCREEN, 5);
        codingBox.addCommand(codingDeleteCommand);
        keyboardCommand = new Command("PIANO", Command.SCREEN, 6);
        codingBox.addCommand(keyboardCommand);

        cancelDeleteCommand = new Command("CANCEL", Command.CANCEL, 1);
        confirmDelete.addCommand(cancelDeleteCommand);
        doDeleteCommand = new Command("DELETE", Command.SCREEN, 2);
        confirmDelete.addCommand(doDeleteCommand);

        cancelBackCommand = new Command("CANCEL", Command.CANCEL, 1);
        confirmBack.addCommand(cancelBackCommand);
        goBackCommand = new Command("GOBACK", Command.SCREEN, 2);
        confirmBack.addCommand(goBackCommand);

        closePianoCommand = new Command("BACK", Command.BACK, 1);
        keyboard.addCommand(closePianoCommand);

        mainDisp.setCommandListener(this);
        titleBox.setCommandListener(this);
        codingBox.setCommandListener(this);
        confirmDelete.setCommandListener(this);
        confirmBack.setCommandListener(this);
        keyboard.setCommandListener(this);

        String[] mmlList = RecordStore.listRecordStores();
        if (mmlList != null)
        {
            for (int i = 0; i < mmlList.length; i++)
            {
                String mml = mmlList[i];
                if (mml != null && mml.startsWith("mml."))
                {
                    mainDisp.append(mml.substring(4), null);
                }
            }
        }

        loadHelpText();

        Display.getDisplay(this).setCurrent(mainDisp);
    }

    // @Override MIDlet.startApp
    protected void startApp() throws MIDletStateChangeException {}

    // @Override MIDlet.pauseApp
    protected void pauseApp() {}

    // @Override MIDlet.destroyApp
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException
    {
        if (currentRecord != null)
        {
            saveMml(null);
            try { currentRecord.closeRecordStore(); } catch (Exception ex) {}
            currentRecord = null;
        }
        if (player != null)
        {
            try { player.stop(); } catch (Exception ex) {}
            try { player.close(); } catch (Exception ex) {}
            player = null;
        }
    }

    // implement CommandListener.commandAction
    public void commandAction(Command cmd, Displayable disp)
    {
        if (cmd == null || disp == null)
        {
            return;
        }
        if (disp == mainDisp)
        {
            if (cmd == exitCommand)
            {
                notifyDestroyed();
            }
            else if (cmd == newCommand)
            {
                mainDisp.setTicker(null);
                titleBox.setString(null);
                titleBox.setTicker(null);
                Display.getDisplay(this).setCurrent(titleBox);
            }
            else if (cmd == List.SELECT_COMMAND)
            {
                loadMml();
            }
        }
        else if (disp == titleBox)
        {
            if (cmd == titleCancelCommand)
            {
                titleBox.setTicker(null);
                Display.getDisplay(this).setCurrent(mainDisp);
            }
            else if (cmd == titleOkCommand)
            {
                makeNewMml();
            }
        }
        else if (disp == codingBox)
        {
            if (cmd == codingBackCommand)
            {
                Display.getDisplay(this).setCurrent(confirmBack);
            }
            else if (cmd == codingSaveCommand)
            {
                if (saveMml(codingBox))
                {
                    Alert alert = new Alert("INFO", "SAVED!", null, AlertType.CONFIRMATION);
                    Display.getDisplay(this).setCurrent(alert, codingBox);
                    codingBox.setTicker(null);
                }
            }
            else if (cmd == codingPlayCommand)
            {
                playMml();
            }
            else if (cmd == codingStopCommand)
            {
                if (player != null)
                {
                    try { player.stop(); } catch (Exception ex) {}
                }
            }
            else if (cmd == codingDeleteCommand)
            {
                String msg = "DELETE ?? " + codingBox.getTitle();
                confirmDelete.setString(msg);
                Display.getDisplay(this).setCurrent(confirmDelete);
            }
            else if (cmd == keyboardCommand)
            {
                if (player != null)
                {
                    try { player.stop(); } catch (Exception ex) {}
                }
                Display.getDisplay(this).setCurrent(keyboard);
            }
            else if (cmd == helpCommand)
            {
                if (helpViewer != null)
                {
                    Display.getDisplay(this).setCurrent(helpViewer);
                }
            }
        }
        else if (disp == confirmDelete)
        {
            if (cmd == cancelDeleteCommand)
            {
                Display.getDisplay(this).setCurrent(codingBox);
            }
            else if (cmd == doDeleteCommand)
            {
                deleteMml();
            }
        }
        else if (disp == confirmBack)
        {
            if (cmd == cancelBackCommand)
            {
                Display.getDisplay(this).setCurrent(codingBox);
            }
            else if (cmd == goBackCommand)
            {
                if (currentRecord != null)
                {
                    try { currentRecord.closeRecordStore(); } catch (Exception ex) {}
                    currentRecord = null;
                }
                if (player != null)
                {
                    try { player.stop(); } catch (Exception ex) {}
                }
                Alert alert = new Alert("INFO", "GO BACK!", null, AlertType.CONFIRMATION);
                Display.getDisplay(this).setCurrent(alert, mainDisp);
                mainDisp.setTicker(null);
                codingBox.setTicker(null);
            }
        }
        else if (disp == keyboard)
        {
            if (cmd == closePianoCommand)
            {
                Display.getDisplay(this).setCurrent(codingBox);
            }
        }
        else if (disp == helpViewer)
        {
            if (cmd == closeHelpCommand)
            {
                Display.getDisplay(this).setCurrent(codingBox);
            }
        }
    }

    private boolean saveMml(Displayable disp)
    {
        if (currentRecord == null)
        {
            if (disp != null)
            {
                disp.setTicker(new Ticker("NO OPEN STORAGE"));
            }
            return false;
        }
        String mml = codingBox.getString();
        byte[] data = null;
        if (mml == null)
        {
            data = new byte[0];
        }
        else
        {
            data = mml.getBytes();
        }
        try
        {
            int num = currentRecord.getNumRecords();
            if (num == 0)
            {
                currentRecord.addRecord(data, 0, data.length);
            }
            else
            {
                currentRecord.setRecord(1, data, 0, data.length);
            }
            return true;
        }
        catch (RecordStoreFullException ex)
        {
            if (disp != null)
            {
                disp.setTicker(new Ticker("STORAGE IS FULL!"));
            }
            return false;
        }
        catch (Exception ex)
        {
            if (disp != null)
            {
                Alert alert = new Alert("ERROR", ex.toString(), null, AlertType.ERROR);
                Display.getDisplay(this).setCurrent(alert, disp);
                disp.setTicker(new Ticker("UNKNOWN ERROR"));
            }
            return false;
        }
    }

    private void loadMml()
    {
        int index = mainDisp.getSelectedIndex();
        String title = mainDisp.getString(index);
        if (title == null)
        {
            mainDisp.setTicker(new Ticker("WRONG NAME"));
            return;
        }
        String rsName = "mml." + title;

        String mml = "";
        try
        {
            currentRecord = RecordStore.openRecordStore(rsName, false);
            if (currentRecord.getNumRecords() > 0)
            {
                byte[] data = currentRecord.getRecord(1);
                if (data != null && data.length > 0)
                {
                    mml = new String(data);
                }
            }
        }
        catch (Exception ex)
        {
            Alert alert = new Alert("ERROR", ex.toString(), null, AlertType.ERROR);
            Display.getDisplay(this).setCurrent(alert, mainDisp);
            mainDisp.setTicker(new Ticker("UNKNOWN ERROR"));
            if (currentRecord != null)
            {
                try { currentRecord.closeRecordStore(); } catch (Exception ex2) {}
                currentRecord = null;
            }
            return;
        }

        codingBox.setTitle(title);
        codingBox.setString(mml);

        mainDisp.setTicker(null);
        codingBox.setTicker(null);
        Display.getDisplay(this).setCurrent(codingBox);
    }

    private void makeNewMml()
    {
        String title = titleBox.getString();
        if (title == null || (title = title.trim()).length() == 0)
        {
            titleBox.setTicker(new Ticker("TITLE IS EMPTY!"));
            return;
        }

        String rsName = "mml." + title;

        RecordStore rs = null;

        // check RecordStore Name
        try
        {
            rs = RecordStore.openRecordStore(rsName, false);
            titleBox.setTicker(new Ticker("ALREADY EXISTS!"));
            return;
        }
        catch (IllegalArgumentException ex)
        {
            // createIfNecessary == false
            // unreachable here ?
            titleBox.setTicker(new Ticker("INVALID NAME!"));
            return;
        }
        catch (RecordStoreNotFoundException ex)
        {
            // Ok
        }
        catch (RecordStoreFullException ex)
        {
            // createIfNecessary == false
            // unreachable here ?
            titleBox.setTicker(new Ticker("STORAGE IS FULL!"));
            return;
        }
        catch (RecordStoreException ex)
        {
            Alert alert = new Alert("ERROR", ex.toString(), null, AlertType.ERROR);
            Display.getDisplay(this).setCurrent(alert, titleBox);
            titleBox.setTicker(new Ticker("UNKNOWN ERROR"));
            return;
        }
        finally
        {
            if (rs != null)
            {
                try { rs.closeRecordStore(); } catch (Exception ex) {}
                rs = null;
            }
        }

        // create New RecordStore
        try
        {
            rs = RecordStore.openRecordStore(rsName, true);
            currentRecord = rs;
            rs = null;
        }
        catch (IllegalArgumentException ex)
        {
            titleBox.setTicker(new Ticker("INVALID NAME!"));
            return;
        }
        catch (RecordStoreFullException ex)
        {
            titleBox.setTicker(new Ticker("STORAGE IS FULL!"));
            return;
        }
        catch (RecordStoreException ex)
        {
            Alert alert = new Alert("ERROR", ex.toString(), null, AlertType.ERROR);
            Display.getDisplay(this).setCurrent(alert, titleBox);
            titleBox.setTicker(new Ticker("UNKNOWN ERROR"));
            return;
        }
        finally
        {
            if (rs != null)
            {
                try { rs.closeRecordStore(); } catch (Exception ex) {}
                rs = null;
            }
        }

        mainDisp.append(title, null);
        codingBox.setTitle(title);

        mainDisp.setTicker(null);
        titleBox.setTicker(null);
        codingBox.setTicker(null);
        Display.getDisplay(this).setCurrent(codingBox);
    }

    private void deleteMml()
    {
        String title = codingBox.getTitle();

        if (currentRecord != null)
        {
            try { currentRecord.closeRecordStore(); } catch (Exception ex) {}
            currentRecord = null;
        }

        String rsName = "mml." + title;

        try
        {
            RecordStore.deleteRecordStore(rsName);
        }
        catch (Exception ex)
        {
            Alert alert = new Alert("ERROR", ex.toString(), null, AlertType.ERROR);
            Display.getDisplay(this).setCurrent(alert, mainDisp);
            mainDisp.setTicker(new Ticker("UNKNOWN ERROR"));
            codingBox.setTicker(null);
            return;
        }

        for (int i = 0; i < mainDisp.size(); i++)
        {
            if (title.equals(mainDisp.getString(i)))
            {
                mainDisp.delete(i);
                break;
            }
        }
        codingBox.setTicker(null);
        mainDisp.setTicker(null);
        String msg = "DELTED! " + title;
        Alert alert = new Alert("INFO", msg, null, AlertType.CONFIRMATION);
        Display.getDisplay(this).setCurrent(alert, mainDisp);
    }

    private void playMml()
    {
        String mml = codingBox.getString();
        if (mml == null || (mml = mml.trim()).length() == 0)
        {
            Alert alert = new Alert("ERROR", "CODE IS EMPTY", null, AlertType.ERROR);
            Display.getDisplay(this).setCurrent(alert, codingBox);
            return;
        }
        byte[] sequence = parseMml(mml);
        if (sequence == null)
        {
            return;
        }

        try
        {
            if (player == null)
            {
                player = Manager.createPlayer(Manager.TONE_DEVICE_LOCATOR);
            }
            if (player.getState() == Player.STARTED)
            {
                player.stop();
            }
            player.deallocate();
            player.realize();
            ToneControl tc = (ToneControl)player.getControl("ToneControl");
            tc.setSequence(sequence);
            player.start();
        }
        catch (Exception ex)
        {
            Alert alert = new Alert("ERROR", ex.toString(), null, AlertType.ERROR);
            Display.getDisplay(this).setCurrent(alert, codingBox);
            codingBox.setTicker(new Ticker("UNKNOWN ERROR"));
            if (player != null)
            {
                try { player.stop(); } catch (Exception ex2) {}
                try { player.close(); } catch (Exception ex2) {}
                player = null;
            }
        }
    }

    private byte[] parseMml(String mml)
    {
        ByteArrayOutputStream buf = null;

        try
        {
            buf = new ByteArrayOutputStream();

            String error = Mml.parse(mml, buf);

            if (error != null)
            {
                Alert alert = new Alert("ERROR", error, null, AlertType.ERROR);
                Display.getDisplay(this).setCurrent(alert, codingBox);
                return null;
            }

            byte[] data = buf.toByteArray();
            return data;
        }
        catch (Exception ex)
        {
            Alert alert = new Alert("ERROR", ex.toString(), null, AlertType.ERROR);
            Display.getDisplay(this).setCurrent(alert, codingBox);
            codingBox.setTicker(new Ticker("UNKNOWN ERROR"));
            return null;
        }
        finally
        {
            if (buf != null)
            {
                try { buf.close(); } catch (Exception ex) {}
                buf = null;
            }
        }
    }

    void loadHelpText()
    {
        InputStream src = null;
        try
        {
            src = getClass().getResourceAsStream("/help.txt");
            if (src == null) { return; }
            byte[] buf = new byte[16];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (;;)
            {
                int len = src.read(buf);
                if (len < 0) { break; }
                baos.write(buf, 0, len);
            }
            String text = new String(baos.toByteArray(), "UTF-8");
            helpViewer = new Form("HELP");
            helpViewer.append(text);

            closeHelpCommand = new Command("BACK", Command.BACK, 1);
            helpViewer.addCommand(closeHelpCommand);

            helpCommand = new Command("HELP", Command.SCREEN, 7);
            codingBox.addCommand(helpCommand);

            helpViewer.setCommandListener(this);
        }
        catch (Exception ex) {}
        finally
        {
            if (src != null)
            {
                try { src.close(); } catch (Exception ex) {}
            }
        }
    }
}