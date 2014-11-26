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

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

/**
 * @author Jacob Gorney
 * @version 1.0.0
 *          <p>
 *          jNetworking.jNetworkInterface.jNetworkInterface is a multi-use object that
 *          can be used to send data and objects over network
 *          connections, or establish network connections
 *          for speed and reliability testing.
 *          <p>
 *          see jNetworking.jNetworkInterface.jNetworkInterfaceServer for a server object that
 *          can communicate properly with this class.
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
     * Test address for internet connection test. This is the IP Address
     * for google.com. Using IP address doesn't require DNS lookup.
     */
    private static final String TEST_ADDR = "74.125.225.36";

    /**
     * Class constructor to establish the connection.
     *
     * @param hostname Hostname of the connection
     * @param port     Port number of the connection
     */
    public jNetworkInterface(String hostname, int port, boolean ssl) {
        this.hostname = hostname;
        this.port = port;
        this.ssl = ssl;
        this.isConnected = false;
        this.quality = -1;
    }

    /**
     * Checks if an internet connection exists. May want to call this in a thread.
     *
     * @return Connection status
     */
    public boolean isOnline() {
        try {
            URL testURL = new URL("http://" + this.TEST_ADDR);
            HttpURLConnection conn = (HttpURLConnection) testURL.openConnection();
            conn.getContent();
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Malformed host for online test.");
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    /**
     * Check the connection status of the network connection.
     *
     * @return Connection status
     */
    public boolean isConnected() {
        if (socket == null)
            return false;
        else
            return (!socket.isClosed() && isConnected);
    }

    /**
     * Send a command with data to the server and return the response.
     *
     * @param command Command to send
     * @param data    Data to send
     * @return Server response
     */
    public String sendCommand(String command, ArrayList<String> data) {
        connect();
        if (isConnected) {
            try {
                // Send the command
                PrintWriter socketOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),
                        StandardCharsets.UTF_8), true);
                BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                        StandardCharsets.UTF_8));
                // @todo change to accept data
                socketOut.println(command);
                // Print the command data
                if (data != null)
                    for (String s : data)
                        socketOut.println(s);
                socketOut.println("END COMMAND");
                socketOut.flush();
                // Get the response from the server
                String line;
                String response = "";
                while ((line = socketIn.readLine()) != null)
                    response += line;
                // Close the connections
                socketOut.close();
                socketIn.close();
                closeConnection();
                // Return the response
                return response;
            } catch (IOException ex) {
                throw new RuntimeException("Failed to send command.");
            }
        } else
            return null;
    }

    /**
     * Determine the connection quality.
     */
    public void pollQuality() {
        // First the server must support the operation
        // we are going to send to it. The "poll" command.
        try {
            long responseStart = System.currentTimeMillis();
            // If this parse fails, the server doesn't support ping. No soup for you.
            long responseEnd;
            try {
                responseEnd = Long.parseLong(sendCommand("ping", null));
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                quality = -1;
                return;
            }
            // Calculate the difference between t
            long difference = responseEnd - responseStart;
            // Determine the connection quality based on the difference
            // of the two timestamps.
            // @todo this can be improved to become more precise.
            if (difference < 10)
                quality = 100;
            else if (difference > 10 && difference < 50)
                quality = 90;
            else if (difference > 50 && difference < 100)
                quality = 80;
            else if (difference > 100 && difference < 200)
                quality = 70;
            else if (difference > 200 && difference < 500)
                quality = 60;
            else if (difference > 500 && difference < 1000)
                quality = 50;
            else if (difference > 1000 && difference < 2000)
                quality = 40;
            else if (difference > 2000 && difference < 5000)
                quality = 30;
            else if (difference > 5000 && difference < 10000)
                quality = 20;
            else if (difference > 10000 && difference < 20000)
                quality = 10;
            else
                quality = 1;
            // @todo check other factors that affect quality such as the local connection
        } catch (RuntimeException ex) {
            quality = -1;
        }
    }

    /**
     * Return the connection quality. 1 - 100 where
     * 1 is the worst possible connection.
     *
     * @return 1 - 100 integer value representing quality
     */
    public int getConnectionQuality() {
        return quality;
    }

    /**
     * Close the socket connection.
     */
    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException ex) {
            throw new RuntimeException("Connection already closed.");
        }
        isConnected = false;
        quality = -1;
    }

    /**
     * Encode a string to base64. requires JDK 1.8.
     *
     * @param s String to encode
     * @return Encoded string
     * @throws UnsupportedEncodingException
     */
    public static String base64Encode(String s) throws UnsupportedEncodingException {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode a base64 encoded string.
     *
     * @param s Encoded string
     * @return The decoded string
     */
    public static String base64Decode(String s) {
        return new String(Base64.getDecoder().decode(s));
    }

    /**
     * Establish and create the socket connection.
     */
    private void connect() {
        try {
            if (ssl) {
                // Build an SSL connection instead of a normal socket connection
                SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                socket = (SSLSocket) sslSocketFactory.createSocket(hostname, port);
            } else
                socket = new Socket(hostname, port);
            // Set the default timeout.
            socket.setSoTimeout(jNetworkInterfaceServer.TIMEOUT);
            isConnected = true;
        } catch (IOException ex) {
            isConnected = false;
            throw new RuntimeException("Connection could not be created.");
        }
    }

    /**
     * Check if an object is serializable.
     *
     * @param obj Object to check
     * @return Result
     */
    private boolean isSerializable(Object obj) {
        try {
            new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(obj);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}