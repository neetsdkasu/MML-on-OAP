import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.microedition.media.control.ToneControl;

class Mml
{
    public static String parse(String src, OutputStream dst) throws IOException
    {
        if (src == null)
        {
            throw new NullPointerException("src");
        }
        if (dst == null)
        {
            throw new NullPointerException("dst");
        }
        Mml mml = new Mml(src);
        if (!mml.parseTempo())
        {
            if (mml.hasError()) { return mml.getError(); }
        }
        if (!mml.parseResolution())
        {
            if (mml.hasError()) { return mml.getError(); }
        }
        dst.write(ToneControl.VERSION);
        dst.write(1);
        dst.write(ToneControl.TEMPO);
        dst.write(mml.tempo >> 2);
        dst.write(ToneControl.RESOLUTION);
        dst.write(mml.resolution);
        while (mml.parseBlock(dst)) {}
        if (mml.hasError()) { return  mml.getError(); }
        mml.setDefault();
        int event = mml.parseSequence(dst);
        if (mml.hasError()) { return mml.getError(); }
        if (event == 0)
        {
            return "EMPTY SEQUENCE";
        }
        if (!mml.isValid()) { return mml.getError(); }
        return null;
    }

    String src;
    String error = null;
    int pos = 0;

    int nextBlockId = 0;

    int tempo = 120;
    int resolution = 64;

    // current octave
    // octave 4 = ToneControl.C4 (octave 3 = C4 - 12, octave 5 = C4 + 12)
    int octave = ToneControl.C4;

    // default duration (quarter note = resolution / 4)
    int duration = 16;

    Mml(String src)
    {
        this.src = src;
    }
    boolean hasError()
    {
        return error != null;
    }

    String getError()
    {
        if (!hasError())
        {
            return "NO ERROR";
        }
        int begin = Math.max(0, pos - 3);
        int end = Math.min(pos + 3, src.length());
        String arround = src.substring(begin, end);
        if (hasChar())
        {
            return error + " (pos: " + pos + ", char: '" + getChar() + "') \"" + arround + "\"";
        }
        else
        {
            return error + " (pos: " + pos + ") \"" + arround + "\"";
        }
    }

    boolean hasChar()
    {
        return pos < src.length();
    }

    char getChar()
    {
        return src.charAt(pos);
    }

    boolean nextChar()
    {
        pos++;
        return hasChar();
    }

    boolean isDigit()
    {
        if (!hasChar()) { return false; }
        return Character.isDigit(getChar());
    }

    boolean isValid()
    {
        skipWhitespace();
        if (hasChar())
        {
            error = "INVALID SYNTAX";
            return false;
        }
        return true;
    }

    void skipWhitespace()
    {
        boolean has = hasChar();
        while (has && " \t\r\n".indexOf(getChar()) >= 0)
        {
            has = nextChar();
        }
    }

    void setDefault()
    {
        octave = ToneControl.C4;
        duration = Math.max(1, resolution >> 2);
    }

    int parseNumber()
    {
        int t = 0;
        while (hasChar())
        {
            char ch = getChar();
            if ('0' <= ch && ch <= '9')
            {
                t = 10 * t + (int)ch - '0';
                if (t > 1000)
                {
                    return 0x10000;
                }
                nextChar();
            }
            else
            {
                break;
            }
        }
        return t;
    }

    boolean parseTempo()
    {
        skipWhitespace();
        if (!hasChar()) { return false; }
        char ch = getChar();
        if (ch != 'T' && ch != 't') { return false; }
        nextChar();
        int t = parseNumber();
        if (t < 20 || 508 < t)
        {
            error = "INVALID TEMPO";
            return false;
        }
        tempo = t;
        return true;
    }

    boolean parseResolution()
    {
        skipWhitespace();
        if (!hasChar()) { return false; }
        if (getChar() != '%') { return false; }
        nextChar();
        int r = parseNumber();
        if (r < 1 || 127 < r)
        {
            error = "INVALID RESOLUTION";
            return false;
        }
        resolution = r;
        return true;
    }

    boolean parseBlock(OutputStream dst) throws IOException
    {
        skipWhitespace();
        if (!hasChar()) { return false; }
        if (getChar() != '{') { return false; }
        if (!nextChar())
        {
            error = "INVALID BLOCK START";
            return false;
        }

        if (!isDigit())
        {
            error = "INVALID BLOCK ID";
            return false;
        }
        int id = parseNumber();
        if (id != nextBlockId || 127 < id)
        {
            error = "INVALID BLOCK ID";
            return false;
        }

        dst.write(ToneControl.BLOCK_START);
        dst.write(id);

        setDefault();

        int event = parseSequence(dst);
        if (hasError()) { return false; }
        if (event == 0)
        {
            error = "INVALID BLOCK";
            return false;
        }

        skipWhitespace();
        if (!hasChar() || getChar() != '}')
        {
            error = "INVALID BLOCK END";
            return false;
        }
        nextChar();

        dst.write(ToneControl.BLOCK_END);
        dst.write(id);

        nextBlockId++;

        return true;
    }

    int parseSequence(OutputStream dst) throws IOException
    {
        int event = 0;
        skipWhitespace();
        while (hasChar())
        {
            if (parseChangeOctave())
            {
                skipWhitespace();
                continue;
            }
            else if (hasError()) { return -1; }
            else if (parseChangeDuration())
            {
                skipWhitespace();
                continue;
            }
            else if (hasError()) { return -1; }
            else if (parseNote(dst))
            {
                event += 2;
            }
            else if (hasError()) { return -1; }
            else if (parseRest(dst))
            {
                event += 2;
            }
            else if (hasError()) { return -1; }
            else if (parsePlayBlock(dst)) {}
            else if (hasError()) { return -1; }
            else if (parseRepeat(dst)) {}
            else if (hasError()) { return -1; }
            else if (parseVolume(dst)) {}
            else if (hasError()) { return -1; }
            else
            {
                char ch = getChar();
                if (ch == ']' || ch == '}')
                {
                    break;
                }
                error = "INVALID CHARACTER";
                return -1;
            }
            skipWhitespace();
            event |= 1;
        }
        return event;
    }

    int parseDuration()
    {
        if (!hasChar())
        {
            return duration;
        }

        if (getChar() == '(')
        {
            if (!nextChar())
            {
                error = "INVALID DURATION";
                return -1;
            }
            if (!isDigit())
            {
                error = "INVALID DURATION";
                return -1;
            }
            int dur = parseNumber();
            if (dur < 1 || 127 < dur)
            {
                error = "INVALID DURATION";
                return -1;
            }
            if (!hasChar() || getChar() != ')')
            {
                error = "INVALID DURATION END";
                return -1;
            }
            nextChar();
            return dur;
        }

        if (!isDigit())
        {
            return duration;
        }

        int num = parseNumber();
        if (num < 1 || resolution < num)
        {
            error = "INVALID LENGTH";
            return -1;
        }
        int dur = Math.max(1, resolution / num);
        while (hasChar() && getChar() == '.')
        {
            num <<= 1;
            if (num > resolution)
            {
                error = "INVALID LENGTH";
                return -1;
            }
            dur += Math.max(1, resolution / num);
            nextChar();
        }

        if (0 < dur && dur <= 127)
        {
            return dur;
        }
        else
        {
            error = "INVALID LENGTH";
            return -1;
        }
    }

    boolean parseNote(OutputStream dst) throws IOException
    {
        // REQUIRE skipWhitespace(), hasChar() == true
        int note = octave;
        switch (getChar())
        {
            case 'C':
            case 'c':
                note += 0;
                break;
            case 'D':
            case 'd':
                note += 2;
                break;
            case 'E':
            case 'e':
                note += 4;
                break;
            case 'F':
            case 'f':
                note += 5;
                break;
            case 'G':
            case 'g':
                note += 7;
                break;
            case 'A':
            case 'a':
                note += 9;
                break;
            case 'B':
            case 'b':
                note += 11;
                break;
            default:
                return false;
        }
        if (note < 0 || 127 < note)
        {
            error = "INVALID NOTE";
            return false;
        }

        if (!nextChar())
        {
            dst.write(note);
            dst.write(duration);
            return true;
        }

        switch (getChar())
        {
            case '+':
            case '#':
                note++;
                nextChar();
                break;
            case '-':
                note--;
                nextChar();
                break;
        }
        if (note < 0 || 127 < note)
        {
            error = "INVALID NOTE";
            return false;
        }

        int dur = parseDuration();
        if (hasError()) { return false; }

        dst.write(note);
        dst.write(dur);
        return true;
    }

    boolean parseRest(OutputStream dst) throws IOException
    {
        // REQUIRE skipWhitespace(), hasChar() == true
        char ch = getChar();
        if (ch != 'R' && ch != 'r') { return false; }

        if (!nextChar())
        {
            dst.write(ToneControl.SILENCE);
            dst.write(duration);
            return true;
        }

        int dur = parseDuration();
        if (hasError()) { return false; }

        dst.write(ToneControl.SILENCE);
        dst.write(dur);
        return true;
    }

    boolean parseVolume(OutputStream dst) throws IOException
    {
        // REQUIRE skipWhitespace(), hasChar() == true
        char ch = getChar();
        if (ch != 'V' && ch != 'v') { return false; }
        if (!nextChar())
        {
            error = "INVALID VOLUME";
            return false;
        }
        if (!isDigit())
        {
            error = "INVALID VOLUME";
            return false;
        }
        int vol = parseNumber();
        if (vol < 0 || 100 < vol)
        {
            error = "INVALID VOLUME";
            return false;
        }
        dst.write(ToneControl.SET_VOLUME);
        dst.write(vol);
        return true;
    }

    boolean parsePlayBlock(OutputStream dst) throws IOException
    {
        // REQUIRE skipWhitespace(), hasChar() == true
        if (getChar() != '$') { return false; }
        if (!nextChar())
        {
            error = "INVALID PLAY BLOCK ID";
            return false;
        }
        if (!isDigit())
        {
            error = "INVALID PLAY BLOCK ID";
            return false;
        }
        int id = parseNumber();
        if (id < 0 || nextBlockId <= id)
        {
            error = "INVALID PLAY BLOCK ID";
            return false;
        }
        dst.write(ToneControl.PLAY_BLOCK);
        dst.write(id);
        return true;
    }

    boolean parseRepeat(OutputStream dst) throws IOException
    {
        // REQUIRE skipWhitespace(), hasChar() == true
        if (getChar() != '[') { return false; }
        if (!nextChar())
        {
            error = "INVALID REPEAT";
            return false;
        }
        if (!isDigit())
        {
            error = "INVALID REPEAT";
            return false;
        }
        int multiplier = parseNumber();
        if (multiplier < 2 || 127 < multiplier)
        {
            error = "INVALID REPEAT NUMBER";
            return false;
        }

        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        try
        {
            int event = parseSequence(tmp);
            if (hasError()) { return false; }
            if (event == 0 || tmp.size() == 0)
            {
                error = "INVALID REPEAT";
                return false;
            }
            skipWhitespace();
            if (!hasChar() || getChar() != ']')
            {
                error = "INVALID REPEAT END";
                return false;
            }
            nextChar();
            byte[] inner = tmp.toByteArray();
            if (event == 3 && inner.length == 2)
            {
                dst.write(ToneControl.REPEAT);
                dst.write(multiplier);
                dst.write(inner);
                return true;
            }
            for (int i = 0; i < multiplier; i++)
            {
                dst.write(inner);
            }
            return true;
        }
        finally
        {
            tmp.close();
            tmp = null;
        }
    }

    boolean parseChangeOctave()
    {
        // REQUIRE skipWhitespace(), hasChar() == true
        switch (getChar())
        {
            case '>':
                nextChar();
                octave -= 12;
                if (octave < 0)
                {
                    error = "INVALID DECREASE OCTAVE";
                    return false;
                }
                return true;
            case '<':
                nextChar();
                octave += 12;
                if (127 < octave)
                {
                    error = "INVALID INCREASE OCTAVE";
                    return false;
                }
                return true;
            case 'o':
            case 'O':
                break;
            default:
                return false;
        }

        if (!nextChar())
        {
            error = "INVALID CHANGE OCTAVE";
            return false;
        }

        if (isDigit())
        {
            int oct = parseNumber();
            octave = ToneControl.C4 + (oct - 4) * 12;
            if (octave < 0 || 127 < octave)
            {
                error = "INVALID OCTAVE VALUE";
                return false;
            }
            return true;
        }

        if (getChar() != '-')
        {
            error = "INVALID OCTAVE VALUE";
            return false;
        }

        if (!nextChar())
        {
            error = "INVALID OCTAVE VALUE";
            return false;
        }

        if (!isDigit())
        {
            error = "INVALID OCTAVE VALUE";
            return false;
        }

        int oct = parseNumber();
        octave = ToneControl.C4 - (oct + 4) * 12;
        if (octave < 0 || 127 < octave)
        {
            error = "INVALID OCTAVE VALUE";
            return false;
        }
        return true;
    }

    boolean parseChangeDuration()
    {
        char ch = getChar();
        if (ch != 'L' && ch != 'l') { return false; }
        if (!nextChar())
        {
            error = "INVALID DEFAULT DURATION VALUE";
            return false;
        }

        if (!isDigit() && getChar() != '(')
        {
            error = "INVALID DEFAULT DURATION VALUE";
            return false;
        }

        int dur = parseDuration();
        if (hasError()) { return false; }

        duration = dur;
        return true;
    }
}