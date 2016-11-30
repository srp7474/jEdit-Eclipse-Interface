/*
 * UDPErrorsPlugin.java - UDP Errors plugin
 * :tabSize=2:indentSize=2:noTabs=true;
 *
 * Copyright (C) 2016 Steve Pritchard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package udperrors;

//{{{ Imports
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.jEdit;
//}}}

public class UDPErrorsPlugin extends EditPlugin
{
  public static void log(Object oWho,String sMsg) {
    Log.log(Log.DEBUG,oWho,sMsg);
  }
  private UDPErrorListener oUEL;

  //{{{ start() method
  public void start() {
    String sPort = jEdit.getProperty("udperrors.port");
    log(this,"UDPErrorsPlugin being started using port"+sPort);
    oUEL = UDPErrorListener.create(Integer.parseInt(sPort));
    oUEL.start();
	} //}}}

	//{{{ stop() method
  public void stop() {
    log(this,"UDPErrorsPlugin being stopped");
    oUEL.stop();
	} //}}}

}
