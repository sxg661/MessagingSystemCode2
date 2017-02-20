import java.io.*;
import java.net.*;

// Gets messages from other clients via the server (by the
// ServerSender thread).

public class ClientReceiver extends Thread {

  private BufferedReader server;
  private boolean quit;

  ClientReceiver(BufferedReader server) {
    this.server = server;
    quit = false;
  }

  public void run() {
    // Print to the user whatever we get from the server:
    try {
      while (!quit) {
        String s = server.readLine(); // Matches FFFFF in ServerSender.java
        if (s != null)
          System.out.println(s);
        else
          Report.errorAndGiveUp("Server seems to have died"); 
      }
    }
    catch (IOException e) {
      //Report.errorAndGiveUp("Server seems to have died " + e.getMessage());
      //e.printStackTrace();
      System.out.println("Disconnecting from server");
    }
  }
}
