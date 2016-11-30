package com.rexcel.jedit.tools;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpCmdLineListener implements Runnable {

  public static void log(String sMsg) {
    System.out.println(sMsg);
  }

  private int nMaxSize = 1400;
  
  public void start(int nPort) throws Exception {
    UdpListen oUL = new UdpListen(this,nPort,nMaxSize);
    oUL.setDaemon(true);
    oUL.start();
    Thread oWait = new Thread(this);
    oWait.start();
    oWait.join();
  }

  public void run() {
    log("UdpCmdLineListener waiting for events");
    try {
      while(true) {
        Thread.sleep(10000);
        //log("looping");
      }
    } catch (Exception e) {
      log("Caught "+e);
    }
  }

  public static void main(String[] sArgs) {
    //String sNever = null;
    try {
      if (sArgs.length < 1)  throw new Exception("Require port parm");
      UdpCmdLineListener oPgm = new UdpCmdLineListener();
      log("UdpCmdLineListener invoked port="+sArgs[0]);
      oPgm.start(Integer.parseInt(sArgs[0]));
    } catch (Exception e) {
      log("TRAP"+e);
    }
  }

  public static class UdpListen extends Thread {
    private int nSize = 0;
    private DatagramSocket oSock;
    private DatagramPacket oPkg;
    private int nPort;
    UdpCmdLineListener oSelf;

    public UdpListen(UdpCmdLineListener oSelf,int nPort,int nSize) {
      this.oSelf = oSelf;
      this.nPort = nPort;
      this.nSize = nSize;
    }

    public void run() {
      log("UdpCmdLineListener listening on "+nPort);
      int nTimes = 0;
      try {
        oSock = new DatagramSocket(nPort,InetAddress.getByName("loopback"));
        byte[] oBuf = new byte[nSize];
        oPkg = new DatagramPacket(oBuf, nSize);
        while (true) {
          nTimes += 1;
          log("wait."+nTimes+" for UDP packet: "+oSock+" "+oSock.getLocalAddress()+" "+InetAddress.getByName("loopback"));
          oSock.receive(oPkg);
          String sText = new String(oBuf, 0, oPkg.getLength());
          String sPkg = sText;
          log("------ got UDP packet:\r\n"+sPkg);
        }
      } catch (Exception e) {
        log("UdpCmdLineListener exception: " + e);
      }
    }
  }

  public static void mainSingle(String[] args) throws Exception {
    int nPort = Integer.parseInt(args[0]);
    log("oneshot version "+nPort);
    DatagramSocket ds = new DatagramSocket(nPort);
    byte[] buf = new byte[1024];
    DatagramPacket dp = new DatagramPacket(buf, 1024);
    ds.receive(dp);
    String str = new String(dp.getData(), 0, dp.getLength());
    System.out.println(str);
    ds.close();
  }
}
