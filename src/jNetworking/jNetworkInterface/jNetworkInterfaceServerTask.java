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
import java.lang.reflect.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Class responsible for processing the commands sent to the server.
 */
public class jNetworkInterfaceServerTask implements Runnable {
   /**
    * Error response code.
    */
   public static final String RESPONSE_ERROR = "ERROR";
   /**
    * Empty response code.
    */
   public static final String RESPONSE_EMPTY = "EMPTY";
   /**
    * Invalid command response code.
    */
   public static final String RESPONSE_INVALID = "INVALID";
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
         String[] data = rawData.split(System.getProperty("line.separator"));
         String responseData = "";
         // Get the data we need
         String command = capitalize(data[0].toLowerCase().trim());
         // Check for server stats, version, and name commands. These are defaults
         if (command.equals("Stats")) {
            responseData = serverRef.getStartTime().toString() + "," + serverRef.getRequests();
         } else if (command.equals("Version")) {
            responseData = "jNetworkInterfaceServer " + jNetworkInterfaceServer.VERSION_MAJOR + "." +
                    jNetworkInterfaceServer.VERSION_MINOR + "." +
                    jNetworkInterfaceServer.VERSION_REVISION;
         } else {
            // Build params
            ArrayList<Object> params = new ArrayList<>();
            for (int i = 1; i < data.length; i++)
               params.add(data[i]);
            try {
               // Create the command
               Class<?> commandObj = Class.forName("jNetworking.jNetworkInterface.Commands." + command);
               Constructor<?> cs = commandObj.getConstructor();
               Command cmd = (Command) cs.newInstance();
               // Execute the command
               System.out.println("Executing command '" + command.toLowerCase() + "'");
               cmd.setup(params);
               responseData = cmd.run();
            } catch (Exception ex) {
               System.out.println("Error executing command '" + command.toLowerCase() + "'");
               responseData = RESPONSE_INVALID;
            }
         }
         // Write the response
         ObjectOutputStream socketOut = new ObjectOutputStream(socket.getOutputStream());
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

   /**
    * Capitalize the first character of a string.
    * @param s String
    * @return Capitalized string
    */
   private String capitalize(String s) {
         char[] arr = s.toCharArray();
         arr[0] = Character.toUpperCase(arr[0]);
         return new String(arr);
   }
}