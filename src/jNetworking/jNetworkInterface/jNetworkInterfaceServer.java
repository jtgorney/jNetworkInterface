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

package jNetworking.jNetworkInterface;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Jacob Gorney
 * @version 1.0.0
 *
 * jNetworkInterfaceServer object can be used to connect to jNetworkInterface.
 */
public class jNetworkInterfaceServer implements Runnable {
   /**
    * Major version number.
    */
   public static final int VERSION_MAJOR = 1;
   /**
    * Minor version number.
    */
   public static final int VERSION_MINOR = 0;
   /**
    * Revision version number.
    */
   public static final int VERSION_REVISION = 0;
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
    * jNetworkInterfaceServer name.
    */
   private String serverName;
   /**
    * Start time of the server.
    */
   private Date serverStarted;
   /**
    * Count of the total requests sent to the server.
    */
   private int requests;
   /**
    * Processing queue for the server.
    */
   private Queue<jNetworkInterfaceServerTask> taskQueue;
   /**
    * The current task thread.
    */
   private Thread taskThread;
   /**
    * Maximum number of concurrent threads.
    */
   private int maxThreads;
   /**
    * Current number of threads running.
    */
   private int currentThreadCount;

   /**
    * Class constructor to create a threaded server object.
    * @param port Port to run the server on
    * @param maxThreads number of available threads
    * @param ssl SSL
    */
   public jNetworkInterfaceServer(int port, int maxThreads, boolean ssl) {
      this.currentThreadCount = 0;
      this.isStopped = true;
      this.isPaused = false;
      this.port = port;
      this.maxThreads = maxThreads;
      this.serverName = "jNetworkInterfaceServer 1.0.0";
      this.taskQueue = new LinkedList<>();
   }

   @Override
   public void run() {
      buildSocket();
      // Set some stat tracking
      synchronized (this) {
         isStopped = false;
         serverStarted = new Date();
         requests = 0;
      }
      // The main loop to listen for connections
      while (!isStopped()) {
         // Check for stop
         if (isStopped()) {
            System.out.println("Server stopped.");
            return;
         }
         try {
            Socket client = server.accept();
            System.out.println("Received request from client. Attempting to process.");
            synchronized (this) {
               requests++;
            }
            // Add the request to the queue.
            if (currentThreadCount == maxThreads || taskQueue.size() == maxThreads) {
               // Print an error response.
               System.out.println("The maximum number of tasks has been exceeded. Max tasks: " + maxThreads);
               // Throw max connection error
               new Thread(new jNetworkInterfaceServerTask(client, this, true)).start();
            } else {
               synchronized (this) {
                  taskQueue.add(new jNetworkInterfaceServerTask(client, this));
               }
               // Start the task thread if needed.
               if (taskThread == null || !taskThread.isAlive()) {
                  // Build a new thread
                  taskThread = new Thread(new Runnable() {
                     @Override
                     public void run() {
                        // Loop through the queue
                        while (!taskQueue.isEmpty()) {
                           new Thread(taskQueue.poll()).start();
                        }
                     }
                  });
                  taskThread.start();
               }
            }
         } catch (IOException ex) {
            throw new RuntimeException("Could not process request sent from client connection.");
         }
      }
   }

   /**
    * Get the total amount of requests.
    * @return Request count
    */
   public synchronized int getRequests() {
      return requests;
   }

   /**
    * Get the start time of the server.
    * @return Date of start
    */
   public synchronized Date getStartTime() {
      return serverStarted;
   }

   /**
    * Get the server's max thread count.
    * @return Max thread count
    */
   public synchronized int getMaxThreads() {
      return maxThreads;
   }

   /**
    * Set the server's max thread count.
    * @param threads Thread count
    */
   public synchronized void setMaxThreads(int threads) {
      if (threads < 0)
         return;
      maxThreads = threads;
   }

   /**
    * Increment resource usage.
    */
   public synchronized void incrementResources() {
      currentThreadCount++;
      System.out.println("Resources Incremented: " + currentThreadCount);
   }

   /**
    * Decrement resource usage.
    */
   public synchronized void decrementResources() {
      currentThreadCount--;
      System.out.println("Resources Decremented: " + currentThreadCount);
   }

   /**
    * Stop the server.
    */
   public synchronized void stop() {
      isStopped = true;
      serverStarted = null;
      requests = 0;
      System.out.println("jNetworkInterfaceServer stopped.");
   }

   /**
    * Stop accepting command requests from the server.
    */
   public synchronized void pause() {
      isPaused = true;
      System.out.println("jNetworkInterfaceServer paused.");
   }

   /**
    * Start accepting requests.
    */
   public synchronized void unpause() {
      isPaused = false;
      System.out.println("jNetworkInterfaceServer Unpaused.");
   }

   /**
    * Set the server name.
    * @param name Name of server
    */
   public synchronized void setServerName(String name) {
      serverName = name;
   }

   /**
    * Get the name of the server.
    * @return Server name
    */
   public synchronized String getServerName() {
      return serverName;
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
    * @return Pause status
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
    * Build the socket connection.
    */
   private synchronized void buildSocket() {
      try {
         server = new ServerSocket(port);
         System.out.println("jNetworkInterfaceServer " + jNetworkInterfaceServer.VERSION_MAJOR + "." +
                 jNetworkInterfaceServer.VERSION_MINOR + "." +
                 jNetworkInterfaceServer.VERSION_REVISION);
         System.out.println("======================================");
         System.out.println("Running on port: " + port);
         System.out.println("Maximum Threads: " + maxThreads);
         System.out.println();
      } catch (IOException ex) {
         throw new RuntimeException("Server socket could not be initialized.");
      }
   }
}