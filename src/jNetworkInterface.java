/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.*;
import java.net.*;

/**
 * @author Jacob Gorney
 * @version 1.0.0
 *
 * jNetworkInterface is a multi-use object that
 * can be used to send data and objects over network
 * connections, or establish network connections
 * for speed and reliability testing.
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
    * Class constructor to establish the connection.
    * @param hostname Hostname of the connection
    * @param port Port number of the connection
    */
   public jNetworkInterface(String hostname, int port, boolean ssl) {
      this.hostname = hostname;
      this.port = port;
      this.ssl = ssl;
      this.isConnected = false;
   }

   /**
    * Establish and create the socket connection.
    */
   public void connect() {
      try {
         socket = new Socket(hostname, port);
         isConnected = true;
      } catch (IOException ex) {
         isConnected = false;
      }
   }

   /**
    * Get the raw socket.
    * @return Socket connection. Null if not available.
    */
   public Socket getConnection() {
      if (socket != null && isConnected())
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
         long responseTimeStart = System.currentTimeMillis();
         socketOut.writeLong(responseTimeStart);
         // Get the response message and parse it
         long responseTime;
         // Check if the ping command is accepted. If it is not
         // we cannot determine the connection quality. No soup for you.
         try {
            responseTime = Long.parseLong(socketIn.readLine());
         } catch (NumberFormatException ex) {
            quality = 0;
            return;
         }
         long difference = responseTime - responseTimeStart;
         // Determine the connection quality based on the difference
         // of the two timestamps.
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
            quality = 0;
      } catch (IOException ex) {
         quality = 0;
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
}
