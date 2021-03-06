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

import jNetworking.jNetworkInterface.jNetworkInterface;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Test jNetworkInterface client.
 */
public class jNetworkInterfaceTest {
    /**
     * Main function
     * @param args Command arguments
     */
    public static void main(String[] args) {
        jNetworkInterface client = new jNetworkInterface("127.0.0.1", 8080, false);
        System.out.println();
        System.out.println("****************************************");
        System.out.println("*        jNetworkInterface Test        *");
        System.out.println("****************************************");
        System.out.println();
        // Check the connection quality
        client.pollQuality();
        System.out.println("Connection Quality Rating (To Server): " + client.getConnectionQuality());
        System.out.println();
        while (true) {
            Scanner keyboard = new Scanner(System.in);
            System.out.print("Send command (type 'exit' to stop): ");
            String cliText = keyboard.nextLine();
            // Parse
            String[] cliSplit = cliText.split(" ");
            // Check for cancel
            if (cliSplit[0].equals("exit"))
                break;
            // Send the command
            String response = "";
            try {
                // Get the params
                ArrayList<String> params = new ArrayList<>();
                String command = cliSplit[0];
                for (int i = 1; i < cliSplit.length; i++)
                    params.add(cliSplit[i]);
                response = client.sendCommand(command, params);
            } catch (RuntimeException ex) {
                System.out.println("The server is not accepting connections or has not been started.");
            }
            if (!response.isEmpty()) {
                System.out.println();
                System.out.println(response);
            }
            System.out.println();
        }
    }
}