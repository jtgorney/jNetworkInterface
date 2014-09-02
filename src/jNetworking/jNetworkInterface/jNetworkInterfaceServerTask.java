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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Class responsible for processing the commands sent to the server.
 */
public class jNetworkInterfaceServerTask implements Runnable {
   /**
    * Socket to process.
    */
   Socket socket;
   /**
    * A reference back to the server.
    */
   jNetworkInterfaceServer serverRef;

   /**
    * Class constructor that takes an open socket connection.
    * @param s Socket
    */
   public jNetworkInterfaceServerTask(Socket s, jNetworkInterfaceServer server) {
      socket = s;
      serverRef = server;
   }

   @Override
   public void run() {
      // Run the command
      performCommand();
   }

   /**
    * Perform a server command.
    */
   private void performCommand() {
      try {
         ObjectInputStream socketIn = new ObjectInputStream(socket.getInputStream());
         String rawData = (String)socketIn.readObject();
         // @todo process the response
         // Write the response
         ObjectOutputStream socketOut = new ObjectOutputStream(socket.getOutputStream());
         // @todo Pass data as ArrayList
         String responseData = jNetworkInterfaceServerCommand.execute(rawData, serverRef);
         socketOut.writeObject(responseData);
         // Close the connections
         socketIn.close();
         socketOut.close();
         socket.close();
      } catch (IOException ex) {
         throw new RuntimeException("Could not execute command.");
      } catch (ClassNotFoundException ex) {
         throw new RuntimeException("Could not execute command.");
      }
   }
}