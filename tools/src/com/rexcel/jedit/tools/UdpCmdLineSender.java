package com.rexcel.jedit.tools;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;

public class UdpCmdLineSender {

  public static void log(String sMsg) {
    System.out.println(sMsg);
  }

  //private int nMaxSize = 1400;
  private int nPort = -1;

  String sMsg = "Lheader\r\n"
              + "Ffilename\r\n"
              + "E100,thsis the error\r\n"
              + "W200,the warning\r\n";

  public void start(int nPort) throws Exception {
    this.nPort = nPort;
    /*UDPMarkerMessage oUMM = new UDPMarkerMessage(nMaxSize);
    oUMM.addLog("this is a test log message");
    udpSendMsg(oUMM);
    oUMM = new UDPMarkerMessage(nMaxSize);
    oUMM.addError  (10,"path1","filename1","this is the problem message 1");
    oUMM.addError  (11,"path1","filename1","this is the problem message 1");
    oUMM.addError  (12,"path1","filename2","this is the problem message 2");
    oUMM.addWarning(13,"path2","filename1","this is the warning message 3");
    oUMM.addError  (14,"path2","filename1","this is 2 line .\r\n. problem message 4");
    udpSendMsg(oUMM);
    oUMM = new UDPMarkerMessage(200);
    oUMM.addError  (10,"path1","filename1","this is the problem message 1");
    oUMM.addError  (11,"path1","filename1","this is the problem message 1");
    oUMM.addError  (12,"path1","filename2","this is the problem message 2");
    oUMM.addWarning(13,"path2","filename1","this is the warning message 3");
    oUMM.addError  (14,"path2","filename1","this is 2 line .\r\n. problem message 4");
    udpSendMsg(oUMM);*/
   String sUniq = ""+(new Date()).getTime();
   String sText = "SH:/data/jedit/tools/src/com/rexcel/jedit/tools\r\n"
     +"SUdpCmdLineListener.java\r\n"
     +"SnMaxSize cannot be resolved to a variable u="+sUniq+"\r\n"
     +"SThe value of the field UdpCmdLineListener.nMaxSizex is not used\r\n"
     +"SThe value of the local variable sNever is not used\r\n"
     +"E16,0,1,2\r\n"
     +"W13,0,1,3\r\n"
     +"W37,0,1,4\r\n"
     +"LHave 1 error(s), 2 warming(s)\r\n";
    udpSendMsg(sText);
  }

  public static void main(String[] sArgs) {
    try {
      if (sArgs.length < 1)  throw new Exception("Require port parm");
      UdpCmdLineSender oPgm = new UdpCmdLineSender();
      log("UdpCmdLineSender invoked port="+sArgs[0]);
      oPgm.start(Integer.parseInt(sArgs[0]));
    } catch (Exception e) {
      log("TRAP"+e);
    }
  }

  public void udpSendMsg(UDPMarkerMessage oUMM) {
    String sText = ""+oUMM;
    udpSendMsg(sText);
  }

  public void udpSendMsg(String sText) {
    log("Sending message len="+sText.length()+"\r\n"+sText);
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


}
