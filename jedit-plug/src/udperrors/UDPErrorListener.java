package udperrors;

/* This tool is written as the workhorse of the UDPErrors plugin.
 *
 * It listens to messages from UDPMarkerListener or its ilk.  Since there may be more than 1
 * Eclipse running to compile various projects and since there may be various jEdit 'windows'
 * open, this directs the messages to the most recently focused jEdit window.
 *
 * It is initiated by installing the UDPErrors.jar in the jEdit jars folder.
 *
 */

import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.msg.EditPaneUpdate;

import errorlist.DefaultErrorSource;
import errorlist.ErrorSource;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class UDPErrorListener implements EBComponent {
  int nPort;
  int nMaxSize = 1400;   // Note added 50 bytes from sender size.
  View oErrs = null;

  UdpListen oUL;
  View oCurView = null;
  EditPlugin oPlug;
  DefaultErrorSource oDES = null;

  boolean bHaveErrWin = false;

  public static void log(Object oWho,String sMsg) {
    Log.log(Log.DEBUG,oWho,sMsg);
  }

  public static UDPErrorListener create(int nPort) {
    UDPErrorListener oUEL = new UDPErrorListener();
    oUEL.nPort = nPort;
    return oUEL;
  }

  public String toString() {return "UDPErrorListener V1.1";}

  /* Note: we start the listener after the GUIis alive so that we can report bad binds. */

  public void start() {
    Object oWho = this;
    log(oWho,"UDPErrorListener V1.1 starting listener on port="+nPort);
    EditBus.addToBus(this);
  }

  public void stop() {
    Object oWho = this;
    log(oWho,"UDPErrorListener V1.1 stopping on port="+nPort);
  }


  public void handleMessage(EBMessage oMsg) {
    if (oMsg instanceof EditPaneUpdate) {
      EditPaneUpdate oEPU = (EditPaneUpdate)oMsg;
      log(this,"EditPane "+oEPU.getWhat()+" "+oEPU.getSource());
      switch(oEPU.getWhat().toString()) {
        case "CREATED":
          log(this,"ActiveView "+oEPU.getEditPane().getView());
          oCurView = oEPU.getEditPane().getView();
          if (oUL == null) startListener();
          activatePlugin();
          break;
        case "DESTROYED":
          oCurView = null;
          if (oDES != null) {
            log(this,"Unregister "+oDES);
            ErrorSource.unregisterErrorSource(oDES);
          }
          oDES = null;
          log(this,"DestroyView "+oEPU.getEditPane().getView());
          break;
      }
    }
    //if (oMsg instanceof EditorExiting) {
      //stopListener();
    //}
      /*jEdit.getPlugin("errorlist.ErrorListPlugin",true);
      View oV = oEPU.getEditPane().getView();
      if (!bHaveWin) {
        for(EditPlugin oEP:jEdit.getPlugins()) {
          log(this,"PLUGIN "+oEP.getClassName());
        }
        DockableWindowManager dwm = oV.getDockableWindowManager();
        if (dwm != null) {
          bHaveWin = true;
          dwm.addDockableWindow("error-list");
          log(this,"making view");
        }
      } */
  }

  private void activatePlugin() {
    if (oPlug == null) oPlug = jEdit.getPlugin("errorlist.ErrorListPlugin",true);
    if (oPlug == null) {
      if (oCurView != null) {
        GUIUtilities.error(oCurView,"UDPListener.plug",new String[0]);
      }
    } else {
      log(this,"errorlist.ErrorListPlugin plugin loaded");
    }
  }
  public void startListener() {
    log(this,"starting listener on port="+nPort);
    oUL = new UdpListen(this,nPort,nMaxSize);
    oUL.setName("UDPThread");
    oUL.setDaemon(true);
    oUL.start();
  }

  public void stopListener() {
    try {
      log(this,"stopping listener on port="+nPort);
      oUL.stopListener();
      oUL.join(5000);
      if(oUL.getState() == Thread.State.TERMINATED) {
        log(this,"Thread terminated OK");
      } else {
        log(this,"Thread terminated state "+oUL.getState());
      }
    } catch (Exception e) {
      log(this,"stopListener exception "+e);
    }
  }

  public static class UdpListen extends Thread {
    private int nSize = 0;
    private DatagramSocket oSock;
    private DatagramPacket oPkg;
    private int nPort;
    UDPErrorListener oSelf;

    public UdpListen(UDPErrorListener oSelf,int nPort,int nSize) {
      this.oSelf = oSelf;
      this.nPort = nPort;
      this.nSize = nSize;
    }

    public void stopListener() throws Exception {
      if (oSock != null) {
        oSock.close();
        oSock = null;
        log(oSelf,"close issued");
      }
    }

    public void run() {
      log(oSelf,"UDPErrorListener listening on "+nPort);
      int nTimes = 0;
      try {
        oSock = new DatagramSocket(nPort,InetAddress.getByName("loopback"));
      } catch (Exception e) {
        oSock = null;
        if (oSelf.oCurView != null) {
          GUIUtilities.error(oSelf.oCurView,"UDPListener.bind",new String[]{""+nPort});
        }
        return;
      }

      try {
        byte[] oBuf = new byte[nSize];
        oPkg = new DatagramPacket(oBuf, nSize);
        while (true) {
          nTimes += 1;
          log(oSelf,"wait."+nTimes+" for UDP packet: "+oSock+" "+oSock.getLocalAddress()+" "+InetAddress.getByName("loopback"));
          oSock.receive(oPkg);
          if ((oBuf == null) || (oBuf.length == 0)) {
            log(oSelf,"zero len buffer. assumed shutdown");
            break;
          }
          String sText = new String(oBuf, 0, oPkg.getLength());
          String sPkg = sText;
          log(oSelf,"------ got UDP packet:\r\n"+sPkg);
          processPackage(sPkg);
        }
      } catch (Exception e) {
        log(oSelf,"UDPErrorListener exception: " + e);
        oSock = null;
      }
      log(oSelf,"UDPErrorListener listener thread terminating");
    }

    private void processPackage(String sPkg) throws Exception {
      if (oSelf.oCurView == null) return;
      DockableWindowManager dwm = oSelf.oCurView.getDockableWindowManager();
      if (dwm != null) {
        Object o = dwm.getDockableWindow("error-list"); //test window there.  If there do not activate to prevent Windows Focus notification
        if (o == null) dwm.addDockableWindow("error-list");// create. Will cause window notification msg in windwos taskbar
        o = dwm.getDockableWindow("error-list");
        log(oSelf,"Have err-list(1) window "+o.getClass().getName()+" "+oSelf.oCurView);
        if (oSelf.oDES == null) {
          oSelf.oDES = new DefaultErrorSource("eclipse errors",oSelf.oCurView);
          ErrorSource.registerErrorSource(oSelf.oDES);
        } else {
          oSelf.oDES.clear();
        }
        log(oSelf,"DES Title "+dwm.getDockableTitle("error-list"));
        dwm.setDockableTitle("error-list","Error List: "+getNow());
        createErrors(sPkg,oSelf.oDES);
      }
    }
    
    public String getNow() {
      String sPattern = "MMM-dd HH:mm:ss";
      SimpleDateFormat oDF = new SimpleDateFormat(sPattern);
      Calendar oCal = Calendar.getInstance();
      return oDF.format(oCal.getTime());
    }
  
    private void createErrors(String sPkg,DefaultErrorSource oDES) throws Exception {
      ArrayList<String> oLits = new ArrayList<>();
      String[] sLines = sPkg.split("\\r\\n");
      for(String sLine:sLines) {
        log(oSelf,"process line "+sLine);
        switch(sLine.charAt(0)) {
          case 'S':
            oLits.add(sLine.substring(1));
            break;
          case 'L':
            log(oSelf,"log "+sLine.substring(1));
            break;
          case 'E':
            addError(oDES,oLits,ErrorSource.ERROR,sLine);
            break;
          case 'W':
            addError(oDES,oLits,ErrorSource.WARNING,sLine);
            break;
        }
      }
    }

    //                                     1        2        3        4
    static Matcher oM = Pattern.compile("^.([0-9]+),([0-9]+),([0-9]+),([0-9]+)$").matcher("");
    private void addError(DefaultErrorSource oDES,ArrayList<String> oLits,int nType,String sLine) throws Exception {
      if(!oM.reset(sLine).find()) {
        log(oSelf,"bad msg "+sLine);
        return;
      }
      int nLine = Integer.parseInt(oM.group(1))-1; // jEdit needs bias of -1;
      int nPath = Integer.parseInt(oM.group(2));
      int nFile = Integer.parseInt(oM.group(3));
      int nMsg  = Integer.parseInt(oM.group(4));
      String sMsg = oLits.get(nMsg);
      String sFileName = oLits.get(nPath)+"/"+oLits.get(nFile);
      sFileName = sFileName.replace('/',File.separatorChar);
      //log(oSelf,"filename "+sFileName+" "+sMsg);
      DefaultErrorSource.DefaultError oErr = new DefaultErrorSource.DefaultError(oDES,nType,sFileName,nLine,0,0,sMsg);
      oDES.addError(oErr);
    }
  }
}
