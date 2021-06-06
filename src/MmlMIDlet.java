import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.*;

public final class MmlMIDlet extends MIDlet implements CommandListener
{
	List mainDisp;
	TextBox titleBox, codingBox;
	
	Command exitCommand, 
			newCommand,
			titleOkCommand,
			titleCancelCommand,
			codingBackCommand,
			codingSaveCommand,
			codingPlayCommand,
			codingStopCommand,
			codingDeleteCommand;
	
	public MmlMIDlet()
	{
		mainDisp = new List("MML on OAP", List.IMPLICIT);
		titleBox = new TextBox("INPUT TITLE", "", 32, TextField.ANY);
		codingBox = new TextBox("INPUT MML", "", 5000, TextField.ANY);
		
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
		
		mainDisp.setCommandListener(this);
		titleBox.setCommandListener(this);
		codingBox.setCommandListener(this);
		
		Display.getDisplay(this).setCurrent(mainDisp);
	}

	// @Override
	protected void startApp() throws MIDletStateChangeException
	{
		
	}

	// @Override
	protected void pauseApp() {}

	// @Override
	protected void destroyApp(boolean unconditional) throws MIDletStateChangeException
	{
		
	}

	// CommandListener.commandAction
	public void commandAction(Command cmd, Displayable disp)
	{
		if (disp == mainDisp)
		{
			if (cmd == exitCommand)
			{
				notifyDestroyed();
			}
			else if (cmd == newCommand)
			{
				titleBox.setString(null);
				titleBox.setTicker(null);
				Display.getDisplay(this).setCurrent(titleBox);
			}
			else if (cmd == List.SELECT_COMMAND)
			{
				Display.getDisplay(this).setCurrent(codingBox);
			}
		}
		else if (disp == titleBox)
		{
			if (cmd == titleCancelCommand)
			{
				Display.getDisplay(this).setCurrent(mainDisp);
			}
			else if (cmd == titleOkCommand)
			{
				mainDisp.append(titleBox.getString(), null);
				Display.getDisplay(this).setCurrent(mainDisp);				
			}
		}
		else if (disp == codingBox)
		{
			if (cmd == codingBackCommand)
			{
				Display.getDisplay(this).setCurrent(mainDisp);
			}
			else if (cmd == codingSaveCommand)
			{
				
			}
			else if (cmd == codingPlayCommand)
			{
				
			}
			else if (cmd == codingStopCommand)
			{
				
			}
			else if (cmd == codingDeleteCommand)
			{
				
			}
		}
	}

}