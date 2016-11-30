package com.rexcel.jedit.tools;

/* This class encapsulates the strings send via UDP to jEdit or its ilk from the Eclipse UDPMarkerListener.
 * The message itself is limited to about 1400 bytes (a single UDP buffer);
 * A simple compression scheme is used to reduce the payload. Since many strings repeat they are sent as a header and
 * other messages contextualy reference these strings by an index (first is 0).
 * A "M" (kMax) flag is used to indicate there are more unsent problems.
 *
 * Each line beging with a type flag (k....) and ends with an EOL marker (\r\n).
 * 
 *
 * Legend for line components:
 * text     - the field text. Any \r or \n characters in the text are changed to ' ' (space).
 * pref     - the filename path as reported by IMarker. pref -> the string array.
 * fref     - the filename as reported by IMarker. fref -> the string array. 
 * prob     - the problem reference. prob -> the string array.
 * line     - the line number as reported by IMarker.
*/


import java.util.HashMap;

public class UDPMarkerMessage {

  //                       Name        Code     ..... Format ......
  public static final char kLOG      = 'L';     // Ltext
  public static final char kSTR      = 'S';     // Stext
  public static final char kERROR    = 'E';     // Epref,fref,line,prob
  public static final char kWARN     = 'W';     // Wpref,fref,line,prob
  public static final char kMAX      = 'M';     // M
  
  public static final char nEOL      = 2;       // Size of EOL marker
  
  private StringBuilder oStrs  = new StringBuilder();
  private StringBuilder oBody  = new StringBuilder();
  private HashMap<String,String> oStrMap = new HashMap<>();
  private int nMax;
  private boolean bMaxxed = false;              // True if message buffer maxxed out
  
  public UDPMarkerMessage(int nMax) {
    this.nMax = nMax;
    if (this.nMax == 0) this.nMax = 1400;
  }
  
  private int getCurSize() {
    return oStrs.length()+oBody.length();
  }

  public boolean addError(int nLine,String sPath,String sFile,String sProb) {
    return addProblem(kERROR,nLine,sPath,sFile,sProb);
  }
  
  public boolean addWarning(int nLine,String sPath,String sFile,String sProb) {
    return addProblem(kWARN,nLine,sPath,sFile,sProb);
  }

  // We approximate the ptr to a string as 3 (2 digits + ,).
  // We return true if problem added, false if not
  private boolean addProblem(char cType,int nLine,String sPath,String sFile,String sProb) {
    if (bMaxxed) return false;
    int nSize = (""+nLine).length()+ (1 + 3 + 3 + 2 + nEOL);
    if (!oStrMap.containsKey(sPath)) nSize += sPath.length() + 1 + nEOL;
    if (!oStrMap.containsKey(sFile)) nSize += sFile.length() + 1 + nEOL;
    if (!oStrMap.containsKey(sProb)) nSize += sProb.length() + 1 + nEOL;
    if (nSize +getCurSize() >= nMax) {
      oBody.append(""+kMAX+"\r\n");
      bMaxxed = true;
      return false;
    }
    // fits so add
    String sMsg = (""+cType)+nLine+","+asStrRef(sPath)+","+asStrRef(sFile)+","+asStrRef(sProb);
    oBody.append(sMsg+"\r\n");
    return true;
  }

  // Assume no CRLF in message
  public boolean addLog(String sMsg) {
    if (sMsg.length() + 3 > nMax) return false;
    oBody.append(""+kLOG+sMsg+"\r\n");
    return true;
  }
  
  private String asStrRef(String sStr) {
    sStr = sStr.trim();
    String sRef = oStrMap.get(sStr);
    if (sRef!= null) return sRef;
    int nRef = oStrMap.size();
    oStrMap.put(sStr,""+nRef);
    oStrs.append(""+kSTR+sStr.replaceAll("[\\r\\n]"," ")+"\r\n");
    return ""+nRef;
  }
  
  public String toString() {
    return ""+oStrs+oBody;
  }
  

}