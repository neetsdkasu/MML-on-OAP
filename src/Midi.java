import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.media.control.ToneControl;

final class Midi
{
    static int instPos = 32;

    public static void changeInst(byte[] midi, int inst)
    {
        midi[instPos] = (byte)(inst & 0x7F);
    }

    public static byte[] compile(byte[] tseq, int inst) throws IOException
    {
        int tempo = 120;
        int resolution = 64;

        int[] blockPos = new int[128];

        ByteArrayOutputStream baos = new ByteArrayOutputStream(tseq.length * 2);
        DataOutputStream dos = new DataOutputStream(baos);

        int pos = 2;

        // tempo
        if (pos < tseq.length && tseq[pos] == ToneControl.TEMPO)
        {
            tempo = ((int)tseq[pos + 1] & 0x7F) << 2;
            pos += 2;
        }

        // resolution
        if (pos < tseq.length && tseq[pos] == ToneControl.RESOLUTION)
        {
            resolution = (int)tseq[pos + 1] & 0x7F;
            pos += 2;
        }

        // define block
        while (pos < tseq.length && tseq[pos] == ToneControl.BLOCK_START)
        {
            blockPos[(int)tseq[pos + 1] & 0xFF] = pos + 2;
            pos += 2;
            while (pos < tseq.length)
            {
                byte cmd = tseq[pos];
                if (cmd == ToneControl.BLOCK_END)
                {
                    pos += 2;
                    break;
                }
                pos += cmd == ToneControl.REPEAT ? 4 : 2;
            }
        }

        // header signature "MThd"
        dos.writeByte('M');
        dos.writeByte('T');
        dos.writeByte('h');
        dos.writeByte('d');
        // header length (= 6) (32bit BE)
        dos.writeInt(6);
        // header midi-format (= 0) (16bit BE)
        dos.writeShort(0);
        // header n-tracks (= 1) (16bit BE)
        dos.writeShort(1);
        // header division (= resolution / 4) (1bit(=0) + 15bit BE)
        dos.writeShort(Math.max(1, resolution >> 2));

        // track header "MTrk"
        dos.writeByte('M');
        dos.writeByte('T');
        dos.writeByte('r');
        dos.writeByte('k');
        // track length (dummy value) (32bit BE)
        int sizePos = baos.size();
        dos.writeInt(0);

        int trackStartPos = baos.size();

        // set tempo (FF 51 03 tttttt)
        dos.writeByte(0); // delta time (= 0)
        dos.writeByte(0xFF);
        dos.writeByte(0x51);
        dos.writeByte(0x03);
        int usecTempo = 60000000 / tempo; // (120 bpm = 500,000 usec/beat) (bpm = beats/minute)
        dos.writeByte(usecTempo >> 16);
        dos.writeByte(usecTempo >> 8);
        dos.writeByte(usecTempo);

        // program change (Cn xx) (n = channel, xx = inst id)
        dos.writeByte(0); // delta time (= 0)
        dos.writeByte(0xC0);
        instPos = baos.size();
        dos.writeByte(inst & 0x7F); // inst ID (0..127)

        boolean lastNoteOn = false;
        int deltaTime = 0;
        int volume = 127;

        int[] posStack = new int[256];
        int sp = 0;

        while (pos < tseq.length)
        {
            byte cmd = tseq[pos];
            if (cmd == ToneControl.PLAY_BLOCK)
            {
                posStack[sp] = pos + 2;
                sp++;
                pos = blockPos[(int)tseq[pos + 1] & 0x7F];
            }
            else if (cmd == ToneControl.SET_VOLUME)
            {
                volume = (127 * ((int)tseq[pos + 1] & 0x7F) / 100) & 0x7F;
                pos += 2;
            }
            else if (cmd == ToneControl.BLOCK_END)
            {
                sp--;
                pos = posStack[sp];
            }
            else if (cmd == ToneControl.REPEAT)
            {
                int multiplier = (int)tseq[pos + 1] & 0xFF;
                cmd = tseq[pos + 2];
                int len = (int)tseq[pos + 3] & 0x7F;
                if (cmd == ToneControl.SILENCE)
                {
                    deltaTime += multiplier * len;
                }
                else
                {
                    int note = (int)cmd & 0x7F;
                    // first note on
                    if (deltaTime <= 127)
                    {
                        dos.writeByte(deltaTime);
                    }
                    else
                    {
                        writeDeltaTime(dos, deltaTime);
                    }
                    if (!lastNoteOn)
                    {
                        // note on status (9n kk vv)
                        dos.writeByte(0x90);
                    }
                    dos.writeByte(note);
                    dos.writeByte(volume);
                    // note off (note on status (vel=0)) (running status kk 00)
                    dos.writeByte(len); // delta time (len <= 127)
                    dos.writeByte(note);
                    dos.writeByte(0);
                    // repeat
                    for (int i = 1; i < multiplier; i++)
                    {
                        // note on  (running status kk vv)
                        dos.writeByte(0); // delta time
                        dos.writeByte(note);
                        dos.writeByte(volume);
                        // note off (running status kk 00)
                        dos.writeByte(len); // delta time (len <= 127)
                        dos.writeByte(note);
                        dos.writeByte(0);
                    }
                    lastNoteOn = true;
                    deltaTime = 0;
                }
                pos += 4;
            }
            else if (cmd == ToneControl.SILENCE)
            {
                deltaTime += (int)tseq[pos + 1] & 0x7F;
                pos += 2;
            }
            else
            {
                int note = (int)cmd & 0x7F;
                int len = (int)tseq[pos + 1] & 0x7F;
                // note on
                if (deltaTime <= 127)
                {
                    dos.writeByte(deltaTime);
                }
                else
                {
                    writeDeltaTime(dos, deltaTime);
                }
                if (!lastNoteOn)
                {
                    // note on status (9n kk vv)
                    dos.writeByte(0x90);
                }
                dos.writeByte(note);
                dos.writeByte(volume);
                // note off (note on status (running status kk 00))
                dos.writeByte(len); // delta time (len <= 127)
                dos.writeByte(note);
                dos.writeByte(0);
                lastNoteOn = true;
                deltaTime = 0;
                pos += 2;
            }
        }

        // end of track (FF 2F 00)
        dos.writeByte(0xFF);
        dos.writeByte(0x2F);
        dos.writeByte(0x00);

        int trackSize = baos.size() - trackStartPos;

        byte[] data = baos.toByteArray();

        data[sizePos] = (byte)((trackSize >> 24) & 0xFF);
        data[sizePos + 1] = (byte)((trackSize >> 16) & 0xFF);
        data[sizePos + 2] = (byte)((trackSize >> 8) & 0xFF);
        data[sizePos + 3] = (byte)(trackSize & 0xFF);

        return data;
    }

    static void writeDeltaTime(DataOutputStream dos, int deltaTime) throws IOException
    {
        int temp = deltaTime & 0x7F;
        while ((deltaTime >>= 7) > 0)
        {
            temp = (temp << 8) | 0x80 | (deltaTime & 0x7F);
        }
        for (;;)
        {
            dos.writeByte(temp);
            if ((temp & 0x80) == 0)
            {
                break;
            }
            temp >>= 8;
        }
    }
}