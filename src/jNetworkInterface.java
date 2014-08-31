import java.io.IOException;
import java.net.Socket;

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
