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

import com.esotericsoftware.yamlbeans.YamlReader;
import jNetworking.jNetworkInterface.HTTP.HTTPRequestUtil;

import java.io.*;
import java.lang.reflect.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

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
    private Socket socket;
    /**
     * A reference back to the server.
     */
    private jNetworkInterfaceServer serverRef;
    /**
     * Max thread indicator.
     */
    private boolean isMaxThreads;
    /**
     * Server logger object.
     */
    private ServerLogger logger;

    /**
     * Class constructor that takes an open socket connection.
     *
     * @param s Socket
     */
    public jNetworkInterfaceServerTask(Socket s, jNetworkInterfaceServer server) {
        socket = s;
        serverRef = server;
        isMaxThreads = false;
        if (LogLocation.getLocation() != null)
            logger = new ServerLogger(LogLocation.getLocation(), ServerLogger.LOG_ALL);
        else
            logger = new ServerLogger();
    }

    public jNetworkInterfaceServerTask(Socket s, jNetworkInterfaceServer server, boolean isMaxThreads) {
        this.isMaxThreads = isMaxThreads;
        socket = s;
        serverRef = server;
        if (LogLocation.getLocation() != null)
            logger = new ServerLogger(LogLocation.getLocation(), ServerLogger.LOG_ALL);
        else
            logger = new ServerLogger();
    }

    @Override
    public void run() {
        performCommand();
    }

    /**
     * Perform a server command.
     */
    private void performCommand() {
        if (!isMaxThreads && !serverRef.isPaused())
            serverRef.incrementResources();
        try {
            BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                    StandardCharsets.UTF_8));
            String command = "";
            ArrayList<String> data = new ArrayList<>();
            // Read the data
            String line;
            boolean isGET, isPOST, httpChecked;
            // Set checked to false
            httpChecked = false;
            isGET = false;
            isPOST = false;
            // httpRequest holds the buffer for the request.
            StringBuilder httpRequest = new StringBuilder();;
            while ((line = socketIn.readLine()) != null) {
                // Check for HTTP request
                if (!httpChecked) {
                    isGET = HTTPRequestUtil.isHTTPGet(line);
                    isPOST = HTTPRequestUtil.isHTTPPost(line);
                    // Build the buffer for the HTTP request
                    if (isGET || isPOST)
                        httpRequest.append(line);
                    else
                        // This is the first check so it must be a command
                        command = line.toLowerCase().trim();
                    httpChecked = true;
                } else {
                    if (isGET || isPOST) {
                        // Process the HTTP request from socket
                        // The request has been completely read.
                        if (line.equals("\r\n")) {
                            // HTTP 1.1 says the request must end with \r\n
                            httpRequest.append("\r\n");
                            break;
                        }
                        // Add to the buffer.
                        httpRequest.append(line);
                    } else {
                        // Break the reader loop and process the response
                        if (line.equals("END COMMAND"))
                            break;
                        // Append data
                        data.add(line);
                    }
                }
            }
            // Check for HTTP
            if (isGET || isPOST) {
                // @todo process the HTTP request
            } else
                // Send a normal server command.
                sendCommand(command, data);
            // This is commented out because the sendCommand closes the
            // socket anyways and the GC will take care of the rest.
        } catch (IOException ex) {
            // ex.printStackTrace();
            if (!isMaxThreads && !serverRef.isPaused())
                serverRef.decrementResources();
            logger.write("Could not execute command.", ServerLogger.LOG_ERROR);
            throw new RuntimeException("Could not execute command.");
        }
    }

    /**
     * Process a normal server command.
     * @param command Command to execute
     * @param data Data to process
     * @throws IOException
     */
    private void sendCommand(String command, ArrayList<String> data) throws IOException {
        // Build the response
        String responseData;
        // Check for server stats, version, and name commands. These are defaults
        if (serverRef.isPaused() && !command.equals("unpause")) {
            logger.write("Server is paused.", ServerLogger.LOG_WARN);
            responseData = "Error: Server is paused.";
        } else if (isMaxThreads) {
            // Handle max thread error
            logger.write("Server has reached maximum capacity..", ServerLogger.LOG_WARN);
            responseData = "Error: Server has reached maximum capacity.";
        } else if (command.equals("")) {
            logger.write("Server did not receive a command.", ServerLogger.LOG_WARN);
            responseData = "Error: No command.";
        } else if (command.equals("stats")) {
            logger.write("Executing command '" + command.toLowerCase() + "'", ServerLogger.LOG_NOTICE);
            responseData = serverRef.getStartTime().toString() + "," + serverRef.getRequests();
        } else if (command.equals("version")) {
            logger.write("Executing command '" + command.toLowerCase() + "'.", ServerLogger.LOG_NOTICE);
            responseData = "jNetworkInterfaceServer " + jNetworkInterfaceServer.VERSION_MAJOR + "." +
                    jNetworkInterfaceServer.VERSION_MINOR + "." +
                    jNetworkInterfaceServer.VERSION_REVISION;
        } else if (command.equals("pause")) {
            serverRef.pause();
            logger.write("Server paused.", ServerLogger.LOG_NOTICE);
            responseData = "Server paused.";
        } else if (command.equals("unpause")) {
            serverRef.unpause();
            logger.write("Server unpaused.", ServerLogger.LOG_NOTICE);
            responseData = "Server Unpaused.";
        } else {
            try {
                // Create the command
                // Get the command
                YamlReader reader = new YamlReader(
                        new InputStreamReader(getClass().getResourceAsStream("commands.yaml"), StandardCharsets.UTF_8));
                Map map = (Map)reader.read();
                // Load the command
                String classCommand = command.toLowerCase().trim();
                String className = (String)map.get(classCommand);
                Class<?> commandObj = Class.forName("jNetworking.jNetworkInterface.Commands." + className);
                Constructor<?> cs = commandObj.getConstructor();
                Command cmd = (Command) cs.newInstance();
                // Execute the command
                logger.write("Executing command '" + command.toLowerCase() + "'.", ServerLogger.LOG_NOTICE);
                cmd.setup(data, socket);
                responseData = cmd.run();
            } catch (Exception ex) {
                // ex.printStackTrace();
                logger.write("Error executing command '" + command.toLowerCase() + "'", ServerLogger.LOG_ERROR);
                responseData = RESPONSE_INVALID;
            }
        }
        // Write the response
        PrintWriter socketOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),
                StandardCharsets.UTF_8), true);
        socketOut.println(responseData);
        // Close the connections
        socketOut.close();
        socket.close();
        if (!isMaxThreads && !serverRef.isPaused())
            serverRef.decrementResources();
    }

    /**
     * Capitalize the first character of a string.
     *
     * @param s String
     * @return Capitalized string
     */
    private String capitalize(String s) {
        char[] arr = s.toCharArray();
        arr[0] = Character.toUpperCase(arr[0]);
        return new String(arr);
    }
}
