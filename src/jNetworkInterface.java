/**
 The MIT License (MIT)

 Copyright (c) 2014 Jacob Gorney

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

import java.io.*;
import java.net.*;
import javax.net.ssl.*;

/**
 * @author Jacob Gorney
 * @version 1.0.0
 *
 * jNetworkInterface is a multi-use object that
 * can be used to send data and objects over network
 * connections, or establish network connections
 * for speed and reliability testing.
 *
 * see jNetworkInterfaceServer for a server object that
 * can communicate properly with this class.
 */
public class jNetworkInterface {
   /**
    * Contains the string hostname value. May contain
    * an ip address.
    */
   private String hostname;

   /**
    * Contains the port number for the socket connection.
    */
   private int port;

   /**
    * Flag to determine the connection status.
    */
   private boolean connectionStatus;

   /**
    * Flag to use SSL.
    */
   private boolean ssl;

   /**
    * Flag to determine connection status.
    */
   private boolean isConnected;

   /**
    * An integer value between 1 and 100 that determines the
    * quality of the established connection. Must call
    * checkQuality() to update.
    */
   private int quality;

   /**
    * The socket connection to the server.
    */
   private Socket socket;

   /**
    * Test address for internet connection test. This is the IP Address
    * for google.com. Using IP address doesn't require DNS lookup.
    */
   private static final String TEST_ADDR = "74.125.225.36";

   /**
    * Class constructor to establish the connection.
    * @param hostname Hostname of the connection
    * @param port Port number of the connection
    */
   public jNetworkInterface(String hostname, int port, boolean ssl) {
      this.hostname = hostname;
      this.port = port;
      this.ssl = ssl;
      this.isConnected = false;
      this.quality = -1;
   }

   /**
    * Establish and create the socket connection.
    */
   public void connect() {
      try {
         if (ssl) {
            // Build an SSL connection instead of a normal socket connection
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = (SSLSocket) sslSocketFactory.createSocket(hostname, port);
         } else
            socket = new Socket(hostname, port);
         isConnected = true;
      } catch (IOException ex) {
         // We couldn't create a connection.
         isConnected = false;
      }
   }

   /**
    * Checks if an internet connection exists. May want to call this in a thread.
    * @return Connection status
    */
   public boolean isOnline() {
      try {
         URL testURL = new URL(this.TEST_ADDR);
         HttpURLConnection conn = (HttpURLConnection) testURL.openConnection();
         conn.getContent();
      } catch (MalformedURLException ex) {
         System.out.println("ERROR: Test address failed.");
         return false;
      } catch (IOException ex) {
         System.out.println("Error: Test address failed.");
         return false;
      }
      return true;
   }

   /**
    * Get the raw socket.
    * @return Socket connection. Null if not available.
    */
   public Socket getConnection() {
      // Also determines if the socket is SSL and returns the appropriate object.
      if (socket != null && isConnected())
         if (ssl)
            return (SSLSocket)socket;
         else
            return socket;
      else
         return null;
   }

   /**
    * Check the connection status of the network connection.
    * @return Connection status
    */
   public boolean isConnected() {
      if (socket == null)
         return false;
      else
         return (!socket.isClosed() && isConnected);
   }

   /**
    * Send a command with data to the server and return the response.
    * @param command Command to send
    * @param data Data to send
    * @return Server response
    */
   public String sendUTF8Command(String command, String data) {
      String response = "";
      try {
         DataOutputStream socketOut = new DataOutputStream(
                 socket.getOutputStream());
         BufferedReader socketIn = new BufferedReader(
                 new InputStreamReader(socket.getInputStream()));
         socketOut.writeUTF(command);
         socketOut.writeUTF(data);
         socketOut.flush();
         socketOut.close();
         // Get the response from the server
         String socketResponse;
         while ((socketResponse = socketIn.readLine()) != null)
            response += socketResponse;
         // Return the received data from the server
         return response;
      } catch (IOException ex) {
         System.out.println("UTF8 Command failed to send.");
         return null;
      }
   }

   /**
    * Send an object to the server using a command.
    * @param command Command
    * @param obj Object to send
    * @return Response from server
    */
   public String sendObjectCommand(String command, Object obj) {
      String response = "";
      try {
         ObjectOutputStream socketOut = new ObjectOutputStream(
                 socket.getOutputStream());
         BufferedReader socketIn = new BufferedReader(
                 new InputStreamReader(socket.getInputStream()));
         socketOut.writeUTF(command);
         socketOut.writeObject(obj);
         socketOut.flush();
         socketOut.close();
         // Get the response from the server
         String socketResponse;
         while ((socketResponse = socketIn.readLine()) != null)
            response += socketResponse;
         // Return the received data from the server
         return response;
      } catch (IOException ex) {
         System.out.println("Object Command failed to send.");
         return null;
      }
   }

   /**
    * Determine the connection quality.
    */
   public void pollQuality() {
      // First the server must support the operation
      // we are going to send to it. The "poll" command.
      try {
         DataOutputStream socketOut = new DataOutputStream(
                 socket.getOutputStream());
         BufferedReader socketIn = new BufferedReader(
                 new InputStreamReader(socket.getInputStream()));
         // Send the ping message with the current time in miliseconds.
         // The response will then be compared.
         socketOut.writeUTF("ping");
         socketOut.flush();
         socketOut.close();
         // Get the current system time for comparison with the server
         long responseTimeStart = System.currentTimeMillis();
         // Get the response message and parse it
         long responseTime;
         // Check if the ping command is accepted. If it is not
         // we cannot determine the connection quality. No soup for you.
         try {
            responseTime = Long.parseLong(socketIn.readLine());
         } catch (NumberFormatException ex) {
            quality = -1;
            return;
         }
         // Calculate the difference between t
         long difference = responseTime - responseTimeStart;
         // Determine the connection quality based on the difference
         // of the two timestamps.
         // @todo this can be improved to become more precise.
         if (difference < 1)
            quality = 100;
         else if (difference > 1 && difference < 5)
            quality = 90;
         else if (difference > 5 && difference < 20)
            quality = 80;
         else if (difference > 20 && difference < 100)
            quality = 70;
         else if (difference > 100 && difference < 200)
            quality = 60;
         else if (difference > 200 && difference < 500)
            quality = 50;
         else if (difference > 500 && difference < 1000)
            quality = 40;
         else if (difference > 1000 && difference < 2000)
            quality = 30;
         else if (difference > 2000 && difference < 5000)
            quality = 20;
         else if (difference > 5000 && difference < 10000)
            quality = 10;
         else
            quality = 1;
         // @todo check other factors that affect quality such as the local connection
      } catch (IOException ex) {
         quality = -1;
      }
   }

   /**
    * Return the connection quality. 1 - 100 where
    * 1 is the worst possible connection.
    * @return 1 - 100 integer value representing quality
    */
   public int getConnectionQuality() {
      return quality;
   }

   /**
    * Close the socket connection.
    */
   public void closeConnection() {
      try {
         socket.close();
      } catch (IOException ex) {
         // Do nothing. We were already closed.
         System.out.println("Connection already closed.");
      }
      isConnected = false;
      quality = -1;
   }

   /**
    * Check if an object is serializable.
    * @param obj Object to check
    * @return Result
    */
   private boolean isSerializable(Object obj) {
      try {
         new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(obj);
         return true;
      } catch (Exception ex) {
         return false;
      }
   }
}