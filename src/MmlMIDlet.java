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
import javax.microedition.media.control.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;

public final class MmlMIDlet extends MIDlet implements CommandListener
{
    Player player = null;
    Thread downloading = null;

    final List mainDisp;
    final TextBox titleBox, codingBox, keyboardCodeViewer;
    final Alert confirmDelete, confirmBack;
    final Keyboard keyboard;
    final DownloadForm downloader;
    Form  helpViewer = null;
    Alert waitDownload = null;

    final Command
            exitCommand,
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
            keyboardCommand,
            closeKeyboardCommand,
            clearKeyboardCommand,
            viewKeyboardCommand,
            closeKeyboardCodeViewerCommand,
            playKeyboardCodeViewerCommand,
            stopKeyboardCodeViewerCommand,
            downloadCommand,
            closeDownloaderCommand,
            doDownloadCommand;

    Command helpCommand = null,
            closeHelpCommand = null;

    RecordStore currentRecord = null,
                keyboardCodeRecord = null;

    int availableSize = 0;

    public MmlMIDlet()
    {
        mainDisp = new List("MML on OAP", List.IMPLICIT);
        titleBox = new TextBox("INPUT TITLE", "", 28, TextField.ANY);
        codingBox = new TextBox("INPUT MML", "", 5000, TextField.ANY);
        confirmDelete = new Alert("CONFIRM", "", null, AlertType.WARNING);
        confirmBack = new Alert("CONFIRM", "GO BACK WITHOUT SAVING ??", null, AlertType.WARNING);
        keyboard = new Keyboard();
        keyboardCodeViewer = new TextBox("VIEW FOR COPY", "", 5000, TextField.ANY);
        downloader = new DownloadForm(mainDisp);

        confirmDelete.setTimeout(Alert.FOREVER);
        confirmBack.setTimeout(Alert.FOREVER);

        exitCommand = new Command("EXIT", Command.EXIT, 1);
        mainDisp.addCommand(exitCommand);
        newCommand = new Command("NEW", Command.SCREEN, 2);
        mainDisp.addCommand(newCommand);
        downloadCommand = new Command("HTTP", Command.SCREEN, 3);
        mainDisp.addCommand(downloadCommand);

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
        keyboardCommand = new Command("KEYBD", Command.SCREEN, 6);
        codingBox.addCommand(keyboardCommand);

        cancelDeleteCommand = new Command("CANCEL", Command.CANCEL, 1);
        confirmDelete.addCommand(cancelDeleteCommand);
        doDeleteCommand = new Command("DELETE", Command.SCREEN, 2);
        confirmDelete.addCommand(doDeleteCommand);

        cancelBackCommand = new Command("CANCEL", Command.CANCEL, 1);
        confirmBack.addCommand(cancelBackCommand);
        goBackCommand = new Command("GOBACK", Command.SCREEN, 2);
        confirmBack.addCommand(goBackCommand);

        closeKeyboardCommand = new Command("BACK", Command.BACK, 1);
        keyboard.addCommand(closeKeyboardCommand);
        clearKeyboardCommand = new Command("CLEAR", Command.SCREEN, 2);
        keyboard.addCommand(clearKeyboardCommand);
        viewKeyboardCommand = new Command("VIEW", Command.SCREEN, 3);
        keyboard.addCommand(viewKeyboardCommand);

        closeKeyboardCodeViewerCommand = new Command("BACK", Command.BACK, 1);
        keyboardCodeViewer.addCommand(closeKeyboardCodeViewerCommand);
        playKeyboardCodeViewerCommand = new Command("PLAY", Command.SCREEN, 2);
        keyboardCodeViewer.addCommand(playKeyboardCodeViewerCommand);
        stopKeyboardCodeViewerCommand = new Command("STOP", Command.SCREEN, 3);
        keyboardCodeViewer.addCommand(stopKeyboardCodeViewerCommand);

        closeDownloaderCommand = new Command("BACK", Command.BACK, 1);
        downloader.addCommand(closeDownloaderCommand);
        doDownloadCommand = new Command("GET", Command.OK, 2);
        downloader.addCommand(doDownloadCommand);

        mainDisp.setCommandListener(this);
        titleBox.setCommandListener(this);
        codingBox.setCommandListener(this);
        confirmDelete.setCommandListener(this);
        confirmBack.setCommandListener(this);
        keyboard.setCommandListener(this);
        keyboardCodeViewer.setCommandListener(this);
        downloader.setCommandListener(this);

        availableSize = -1;

        try
        {
            keyboardCodeRecord = RecordStore.openRecordStore("mml#keyboardCode", true);
            availableSize = keyboardCodeRecord.getSizeAvailable();
            if (keyboardCodeRecord.getNumRecords() > 0)
            {
                byte[] data = keyboardCodeRecord.getRecord(1);
                keyboard.loadKeyboardCode(data);
            }
        }
        catch (Exception ex)
        {
            if (keyboardCodeRecord != null)
            {
                try { keyboardCodeRecord.closeRecordStore(); }
                catch (Exception ex2) {}
                keyboardCodeRecord = null;
            }
        }

        String[] mmlList = RecordStore.listRecordStores();
        if (mmlList != null)
        {
            for (int i = 0; i < mmlList.length; i++)
            {
                String mml = mmlList[i];
                if (mml == null) { continue; }
                if (mml.startsWith("mml."))
                {
                    mainDisp.append(mml.substring(4), null);
                }
                if (availableSize >= 0) { continue; }
                RecordStore rs = null;
                try
                {
                    rs = RecordStore.openRecordStore(mml, false);
                    availableSize = rs.getSizeAvailable();
                }
                catch(Exception ex) {}
                finally
                {
                    if (rs != null)
                    {
                        try { rs.closeRecordStore(); } catch (Exception ex) {}
                        rs = null;
                    }
                }
            }
        }

        if (availableSize < 0) { availableSize = 32768; }
        mainDisp.setTitle("MML on OAP (" + availableSize + "/32768)");

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
        if (keyboardCodeRecord != null)
        {
            byte[] data = keyboard.getKeyboardCodeForSave();
            if (data == null) { data = new byte[0]; }
            try
            {
                if (keyboardCodeRecord.getNumRecords() > 0)
                {
                    keyboardCodeRecord.setRecord(1, data, 0, data.length);
                }
                else
                {
                    keyboardCodeRecord.addRecord(data, 0, data.length);
                }
            }
            catch (Exception ex) { ex.printStackTrace(); }
            try { keyboardCodeRecord.closeRecordStore(); } catch (Exception ex) {}
            keyboardCodeRecord = null;
        }
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
            else if (cmd == downloadCommand)
            {
                Display.getDisplay(this).setCurrent(downloader);
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
            if (cmd == closeKeyboardCommand)
            {
                Display.getDisplay(this).setCurrent(codingBox);
            }
            else if (cmd == clearKeyboardCommand)
            {
                keyboard.clearMmlCode();
            }
            else if (cmd == viewKeyboardCommand)
            {
                String mml = keyboard.getMmlString();
                keyboardCodeViewer.setString(mml);
                Display.getDisplay(this).setCurrent(keyboardCodeViewer);
            }
        }
        else if (disp == keyboardCodeViewer)
        {
            if (cmd == closeKeyboardCodeViewerCommand)
            {
                if (player != null)
                {
                    try { player.stop(); } catch (Exception ex) {}
                }
                Display.getDisplay(this).setCurrent(keyboard);
                keyboardCodeViewer.setString(null);
            }
            else if (cmd == playKeyboardCodeViewerCommand)
            {
                playKeyboardCode();
            }
            else if (cmd == stopKeyboardCodeViewerCommand)
            {
                if (player != null)
                {
                    try { player.stop(); } catch (Exception ex) {}
                }
            }
        }
        else if (disp == downloader)
        {
            if (cmd == closeDownloaderCommand)
            {
                downloader.setTicker(null);
                Display.getDisplay(this).setCurrent(mainDisp);
            }
            else if (cmd == doDownloadCommand)
            {
                doDownload();
            }
        }
        else if (disp == waitDownload)
        {
            if (cmd == Alert.DISMISS_COMMAND)
            {
                checkDownloaded();
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
            availableSize = currentRecord.getSizeAvailable();
            mainDisp.setTitle("MML on OAP (" + availableSize + "/32768)");
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
            availableSize = currentRecord.getSizeAvailable();
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
        mainDisp.setTitle("MML on OAP (" + availableSize + "/32768)");
        codingBox.setTitle(title);
        codingBox.setString(null);
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
            try { availableSize += currentRecord.getSize(); } catch (Exception ex) {}
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
        mainDisp.setTitle("MML on OAP (" + availableSize + "/32768)");
        String msg = "DELTED! " + title;
        Alert alert = new Alert("INFO", msg, null, AlertType.CONFIRMATION);
        Display.getDisplay(this).setCurrent(alert, mainDisp);
    }

    private void playMml()
    {
        String mml = codingBox.getString();
        if (mml == null)
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
            try { ((VolumeControl)player.getControl("VolumeControl")).setLevel(100); }
            catch (Exception ex) {}
            ((ToneControl)player.getControl("ToneControl")).setSequence(sequence);
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

    private void playKeyboardCode()
    {
        String mml = keyboardCodeViewer.getString();
        if (mml == null)
        {
            Alert alert = new Alert("ERROR", "CODE IS EMPTY", null, AlertType.ERROR);
            Display.getDisplay(this).setCurrent(alert, keyboardCodeViewer);
            return;
        }
        ByteArrayOutputStream baos = null;
        byte[] sequence = null;
        try
        {
            baos = new ByteArrayOutputStream();
            String error = Mml.parse(mml, baos);

            if (error != null)
            {
                Alert alert = new Alert("ERROR", error, null, AlertType.ERROR);
                Display.getDisplay(this).setCurrent(alert, keyboardCodeViewer);
                return;
            }

            sequence = baos.toByteArray();
        }
        catch (Exception ex)
        {
            Alert alert = new Alert("ERROR", ex.toString(), null, AlertType.ERROR);
            Display.getDisplay(this).setCurrent(alert, keyboardCodeViewer);
            return;
        }
        finally
        {
            if (baos != null)
            {
                try { baos.close(); } catch (Exception ex) {}
                baos = null;
            }
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
            try { ((VolumeControl)player.getControl("VolumeControl")).setLevel(100); }
            catch (Exception ex) {}
            ((ToneControl)player.getControl("ToneControl")).setSequence(sequence);
            player.start();
        }
        catch (Exception ex)
        {
            Alert alert = new Alert("ERROR", ex.toString(), null, AlertType.ERROR);
            Display.getDisplay(this).setCurrent(alert, keyboardCodeViewer);
            if (player != null)
            {
                try { player.stop(); } catch (Exception ex2) {}
                try { player.close(); } catch (Exception ex2) {}
                player = null;
            }
        }
    }

    void loadHelpText()
    {
        InputStream src = null;
        try
        {
            src = Class.class.getResourceAsStream("/help.txt");
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

    void doDownload()
    {
        if (!downloader.isValid()) { return; }
        downloading = new Thread(downloader);
        downloading.start();
        waitDownload = new Alert("download", "downloading...", null, null);
        waitDownload.setTimeout(Alert.FOREVER);
        Display.getDisplay(this).setCurrent(waitDownload);
        (new Thread(new Runnable() {
            public void run()
            {
                if (downloading != null)
                {
                    try { downloading.join(); }
                    catch (Exception ex) {}
                }
                commandAction(Alert.DISMISS_COMMAND, waitDownload);
            }
        })).start();
    }

    void checkDownloaded()
    {
        if (downloading != null)
        {
            if (downloading.isAlive())
            {
                return;
            }
            downloading = null;
        }
        waitDownload = null;
        if (downloader.error == null)
        {
            Alert alert = new Alert("INFO", "SUCCESS DOWNLOAD", null, AlertType.CONFIRMATION);
            Display.getDisplay(this).setCurrent(alert, mainDisp);
        }
        else
        {
            downloader.setTicker(new Ticker(downloader.error));
            Display.getDisplay(this).setCurrent(downloader);
        }
    }
}