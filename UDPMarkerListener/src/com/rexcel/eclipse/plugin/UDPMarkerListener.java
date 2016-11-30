package com.rexcel.eclipse.plugin;

import org.eclipse.ui.IStartup;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
//import org.eclipse.core.runtime.CoreException;
import com.rexcel.jedit.tools.UDPMarkerMessage;
//import java.util.Map;
//import java.util.Iterator;

public class UDPMarkerListener implements IStartup  {

  public static void log(String sMsg) {
    System.out.println(sMsg);
  }
  
  int nPort    = 7771;  // TODO get this from properties
  int nMaxSize = 1400;  // TODO get this from properties
	
  public void earlyStartup() {
   	log("Starting MarkerListener Ver 1.02 using udpPort "+nPort);
   	udpSendMsg("Learly startup called MarkerListener started",nPort);
   	IWorkspace oWS = ResourcesPlugin.getWorkspace();
   	IResourceChangeListener oLis = new IResourceChangeListener() {
      public void resourceChanged(IResourceChangeEvent oEvt) {
        String    sType = "?"+oEvt.getType()+"?";
        IResource oRes  = oEvt.getResource();
        if (oRes != null) log("Resource "+oRes.getFullPath());
        IResourceDelta oDelta = oEvt.getDelta();
        if (oDelta != null) log("ResourceDelta "+oDelta.getResource().getFullPath());
        switch(oEvt.getType()) {
          case IResourceChangeEvent.PRE_BUILD:   sType = "PRE_BUILD";   break;
          case IResourceChangeEvent.POST_BUILD:  sType = "POST_BUILD";  break;
          case IResourceChangeEvent.PRE_CLOSE:   sType = "PRE_CLOSE";   break;
          case IResourceChangeEvent.PRE_DELETE:  sType = "PRE_DELETE";  break;
          case IResourceChangeEvent.PRE_REFRESH: sType = "PRE_REFRESH"; break;
          case IResourceChangeEvent.POST_CHANGE: sType = "POST_CHANGE"; break;
        }
        log("Something changed "+sType);
        if ((oEvt.getType() == IResourceChangeEvent.POST_BUILD) && (oDelta != null) && (oDelta.getResource() != null)) {
          sendMarkers(oDelta.getResource());
        }
      }
    	};
    oWS.addResourceChangeListener(oLis,IResourceChangeEvent.PRE_BUILD|IResourceChangeEvent.POST_BUILD|IResourceChangeEvent.POST_CHANGE);
  }
	
  // The plug-in ID
	//public static final String PLUGIN_ID = "ResourceListener"; //$NON-NLS-1$
	
  public void udpSendMsg(String sText,int nPort) {
    try {
      DatagramSocket oSock = new DatagramSocket();
      byte oBuf[] = (sText).getBytes();
      String sHost = "localhost";
      DatagramPacket oPkg = new DatagramPacket(oBuf, oBuf.length, InetAddress.getByName(sHost), nPort);
      log("Transfer bytes "+sText.length()+" to " + sHost+" "+InetAddress.getByName(sHost)+" port="+nPort);
      oSock.send(oPkg);
      oSock.close();
    } catch (Exception e) {
      System.err.println("UdpSend.exception: " + e);
    }
  }
  
  private void sendMarkers(IResource oRes) {
    IMarker[]  oProbs = null;
    int nDepth = IResource.DEPTH_INFINITE;
    try {
      oProbs = oRes.findMarkers(IMarker.PROBLEM, true, nDepth);
      if (oProbs == null) {
        log("Returned null problems "+oRes.getFullPath());
      } else {
        log("Returned "+oProbs.length+" problem(s) from "+oRes.getFullPath());
        String sUdpMsg = makeUdpMsg(oProbs);
        udpSendMsg(sUdpMsg,nPort);
      }
    } catch (Exception e) {
      log("it broke "+e);
    }
  }
  
  /* Ship error and warning messages.
  *  Note that we do all errors first in case there is a buffer overflow
  *  Finally we ship counters (if they fit).
  */
  private String makeUdpMsg(IMarker[]  oProbs) throws Exception {
    UDPMarkerMessage oUMM = new UDPMarkerMessage(nMaxSize);
    int nErrs  = processMarkerTypes(oUMM,oProbs,'E');
    int nWarns = processMarkerTypes(oUMM,oProbs,'W');
    oUMM.addLog("Have "+nErrs+" error(s), "+nWarns+" warming(s)");
    log(""+oUMM.toString());
    return oUMM.toString();
  }
  
  private int processMarkerTypes(UDPMarkerMessage oUMM,IMarker[]  oProbs,char cWhat) throws Exception {
    int nSeverity = (cWhat == 'E') ? 2 : 1;
    int nUsed = 0;
    for(IMarker oM:oProbs) {
      if (!"org.eclipse.jdt.core.problem".equals(oM.getType())) continue;
      int nLev = ((Integer)oM.getAttribute("severity")).intValue();
      if (nSeverity != nLev) continue;
      IResource oR = oM.getResource();
      String sLocn = ""+oR.getLocation();
      String sFile = ""+oR.getName();
      String sPath = sLocn.substring(0,sLocn.length()-1-sFile.length());
      String sMsg = ""+oM.getAttribute("message");
      int    nLine = ((Integer)oM.getAttribute("lineNumber")).intValue();
      nUsed += 1;
      if (cWhat == 'E') {
        oUMM.addError(nLine,sPath,sFile,sMsg);
      } else {
        oUMM.addWarning(nLine,sPath,sFile,sMsg);
      }
    }
    return nUsed;
  }

}