import java.io.*;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;
import javax.microedition.rms.*;

final class DownloadForm extends Form implements Runnable
{
    final List fileList;
    final TextField nameField, urlField;
    volatile String error = null;

    DownloadForm(List list)
    {
        super("DOWNLOAD");
        fileList = list;

        nameField = new TextField("name:", "", 28, TextField.ANY);
        append(nameField);
        urlField = new TextField("url:", "", 500, TextField.URL);
        append(urlField);
    }

    boolean isValid()
    {
        String name = nameField.getString();
        if (name == null || (name = name.trim()).length() == 0)
        {
            setTicker(new Ticker("NAME IS EMPTY"));
            return false;
        }
        for (int i = 0; i < fileList.size(); i++)
        {
            if (name.equals(fileList.getString(i)))
            {
                setTicker(new Ticker("NAME ALREADY EXISTS"));
                return false;
            }
        }
        String url = urlField.getString();
        if (url == null || (url = url.trim()).length() == 0)
        {
            setTicker(new Ticker("URL IS EMPTY"));
            return false;
        }
        if (!url.startsWith("http://") && !url.startsWith("https://"))
        {
            setTicker(new Ticker("URL HAS INVALID PROTOCOL"));
            return false;
        }
        nameField.setString(name);
        urlField.setString(url);
        setTicker(null);
        return true;
    }

    // implement Runnable.run
    public void run()
    {
        error = null;
        String name = nameField.getString();
        String url = urlField.getString();
        HttpConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        byte[] data = null;
        try
        {
            conn = (HttpConnection)Connector.open(url);

            int rc = conn.getResponseCode();
            if (rc != HttpConnection.HTTP_OK)
            {
                String msg = conn.getResponseMessage();
                error = "failed: " + String.valueOf(msg);
                return;
            }

            is = conn.openInputStream();

            long lenL = conn.getLength();
            if (lenL > 5000L)
            {
                error = "too big file!";
                return;
            }
            int len = (int)lenL;

            if (len > 0) {
                int actual = 0;
                int bytesread = 0 ;
                data = new byte[len];
                while ((bytesread != len) && (actual != -1)) {
                    actual = is.read(data, bytesread, len - bytesread);
                    bytesread += actual;
                }
                if (actual < 0)
                {
                    error = "unknown error";
                    return;
                }
            } else {
                baos = new ByteArrayOutputStream(5001);
                int ch;
                while ((ch = is.read()) != -1) {
                    baos.write(ch);
                    if (baos.size() > 5000)
                    {
                        error = "too big file!";
                        return;
                    }
                }
                data = baos.toByteArray();
            }
        }
        catch (Exception ex)
        {
            error = ex.toString();
            return;
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); }
                catch (Exception ex) {}
                is = null;
            }
            if (conn != null)
            {
                try { conn.close(); }
                catch (Exception ex) {}
                conn = null;
            }
            if (baos != null)
            {
                try { baos.close(); }
                catch (Exception ex) {}
                baos = null;
            }
        }

        RecordStore rs = null;
        String rsName = "mml." + name;
        try
        {
            rs = RecordStore.openRecordStore(rsName, true);
            if (rs.getNumRecords() == 0)
            {
                rs.addRecord(data, 0, data.length);
            }
            else
            {
                rs.setRecord(1, data, 0, data.length);
            }
            fileList.append(name, null);
        }
        catch (Exception ex)
        {
            error = ex.toString();
        }
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