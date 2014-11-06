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

package jNetworking.jNetworkInterface.Commands;

import jNetworking.jNetworkInterface.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

/**
 * jNetworkInterfaceServer Command object template.
 */
public class Website implements Command {

   private String url;

   @Override
   public void setup(ArrayList<String> input, Socket client) {
      // Do nothing for this command
       // Store the URL
       url = input.get(0);
       // Check for http/s
       if (!(url.startsWith("http://") || url.startsWith("https://")))
           url = "http://" + url;
   }

   @Override
   public String run() {
       URL url;
       BufferedReader reader;
       String response = "";
       String line = "";
       try {
           url = new URL(this.url);
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
