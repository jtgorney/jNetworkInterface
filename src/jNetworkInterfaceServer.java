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

/**
 * @author Jacob Gorney
 * @version 1.0.0
 *
 * jNetworkInterfaceServer object can be used to connect to jNetworkInterface.
 */
public class jNetworkInterfaceServer implements Runnable {
   /**
    * Empty response code.
    */
   public static final String RESPONSE_EMPTY = "EMPTY";
   /**
    * Error Response code.
    */
   public static final String RESPONSE_ERROR = "ERROR";
   /**
    * Server stopped flag.
    */
   private boolean isStopped;
   /**
    * Server paused flag.
    */
   private boolean isPaused;
   /**
    * Server port.
    */
   private int port;
   /**
    * Main server listening socket.
    */
   private ServerSocket server;
   /**
    * Socket of received request.
    */
   private Socket receivedSocket;
   /**
    * jNetworkInterfaceServer name.
    */
   private String serverName;
   /**
    * Class constructor to create a threaded server object.
    * @param port Port to run the server on
    * @param ssl SSL
    */
   public jNetworkInterfaceServer(int port, boolean ssl) {
      this.isStopped = true;
      this.isPaused = false;
      this.port = port;
      this.serverName = "jNetworkInterfaceServer 1.0.0";
   }

   /**
    * Perform a command sent to the server.
    * @param s Socket connection
    */
   private jNetworkInterfaceServer(Socket s) {
      receivedSocket = s;
   }

   @Override
   public void run() {
      // Only accept a command if we are not paused.
      if (!isPaused())
         performCommand(receivedSocket);
   }

   /**
    * Start the server.
    */
   public synchronized void start() {
      // Determine if we are already running first.
      if (!isStopped()) {
         System.out.println("jNetworkInterfaceServer already running.");
         return;
      }
      try {
         // Initialize server execution
         server = new ServerSocket(port);
         isStopped = false;
         beginThreading();
      } catch (IOException ex) {
         isStopped = true;
      }
   }

   /**
    * Stop the server.
    */
   public synchronized void stop() {
      isStopped = true;
   }

   /**
    * Stop accepting command requests from the server.
    */
   public synchronized void pause() {
      isPaused = true;
   }

   /**
    * Set the server name.
    * @param name Name of server
    */
   public synchronized void setServerName(String name) {
      serverName = name;
   }

   /**
    * Is the server stopped.
    * @return Stopped or not
    */
   public synchronized boolean isStopped() {
      return isStopped;
   }

   /**
    * Determine if server is paused.
    * @return
    */
   public synchronized boolean isPaused() {
      return isPaused;
   }

   /**
    * Ping the server and return the current server MS system time.
    * @return System time in MS
    */
   protected long ping() {
      return System.currentTimeMillis();
   }

   /**
    * Begin the class threading process.
    */
   private void beginThreading() {
      while (!isStopped) {
         try {
            Socket socket = server.accept();
            new Thread(new jNetworkInterfaceServer(socket)).start();
         } catch (IOException ex) {
            System.out.println("Could not start jNetworkInterfaceServer.");
         }
      }
   }

   /**
    * Perform a server command.
    * @param s Socket
    * @return Data
    */
   private String performCommand(Socket s) {
      try {
         // Get the data input stream, parse the command, and send the output back.
         DataInputStream socketIn = new DataInputStream(s.getInputStream());
         String command = socketIn.readUTF();
         if (command.isEmpty())
            return RESPONSE_EMPTY;
         // Parse the command
         // There are two parts to every command. The command itself and the data.
         String data = socketIn.readUTF();
         // @todo parse the command
         return null;
      } catch (IOException ex) {
         return RESPONSE_ERROR;
      }
   }
}