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

import jNetworking.jNetworkInterface.LogLocation;
import jNetworking.jNetworkInterface.jNetworkInterfaceServer;

/**
 * Simple test for jNetworkInterfaceServer.
 */
public class jNetworkInterfaceServerTest {
    /**
     * Main function
     * @param args Command arguments
     */
    public static void main(String[] args) {
        // Set the log location
        // Not setting the location of the log file will default to the root drive.
        // Ensure this program is executed with appropriate filesystem permissions.
        LogLocation.setLocation("/Users/jacob/Desktop/log.txt");
        // Spawn the server
        jNetworkInterfaceServer server = new jNetworkInterfaceServer(8080, 10, false);
        // Spawn the server
        new Thread(server).start();
    }
}
