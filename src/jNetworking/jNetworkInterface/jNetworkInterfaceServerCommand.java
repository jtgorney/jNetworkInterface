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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Execute a command sent to the server.
 */
public class jNetworkInterfaceServerCommand {
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
    * Execute a command.
    * @todo Accept an ArrayList for data.
    * @param command Command to execute
    * @return Data
    */
   public static String execute(String command) {
      switch (command) {
         case "version":
            return version();
         case "ping":
            return ping();
         case "check":
            return check();
         case "reddit":
            return reddit();
         default:
            return RESPONSE_INVALID;
      }
   }

   /**
    * Command: Version of server.
    * @return Server version
    */
   public static String version() {
      return "jNetworkInterfaceServer " + + jNetworkInterfaceServer.VERSION_MAJOR + "." +
              jNetworkInterfaceServer.VERSION_MINOR + "." +
              jNetworkInterfaceServer.VERSION_REVISION;
   }

   /**
    * Ping the server.
    * @return String value of current time in MS
    */
   public static String ping() {
      return String.valueOf(System.currentTimeMillis());
   }

   /**
    * Check server.
    * @return String status
    */
   public static String check() {
      return "jNetworkInterfaceServer " + jNetworkInterfaceServer.VERSION_MAJOR + "." +
              jNetworkInterfaceServer.VERSION_MINOR + "." +
              jNetworkInterfaceServer.VERSION_REVISION + ": OK";
   }

   public static String reddit() {
      URL url;
      BufferedReader reader;
      String response = "";
      String line = "";
      try {
         url = new URL("http://reddit.com");
         reader = new BufferedReader(new InputStreamReader(url.openStream()));
         while ((line = reader.readLine()) != null)
            response += line;
         reader.close();
         return response;
      } catch (IOException ex) {
         return "Error loading page";
      }
   }
}
