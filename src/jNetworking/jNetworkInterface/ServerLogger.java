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

import java.io.*;
import java.util.Date;

/**
 * Logger system for the server.
 */
public class ServerLogger {
    /**
     * Log error messages.
     */
    public static final String LOG_ERROR = "ERROR";
    /**
     * Log warn messages.
     */
    public static final String LOG_WARN = "WARN";
    /**
     * Log notice messages.
     */
    public static final String LOG_NOTICE = "NOTICE";
    /**
     * Log all messages.
     */
    public static final String LOG_ALL = "ALL";
    /**
     * The log file object.
     */
    private String logFile;
    /**
     * The log level for messages.
     */
    private String logLevel;

    /**
     * Constructor that takes a log path and level.
     * @param logFile Path to log file
     * @param logLevel Log level
     */
    public ServerLogger(String logFile, String logLevel) {
        this.logFile = logFile;
        this.logLevel = logLevel;
    }

    /**
     * Constructor that takes a custom log file.
     * @param logFile Path to log file
     */
    public ServerLogger(String logFile) {
        this.logFile = logFile;
        logLevel = LOG_ALL;
    }

    /**
     * Default constructor.
     */
    public ServerLogger() {
        File[] roots = File.listRoots();
        logFile = roots[0].toString() + "jnetworkserver_log_" + System.currentTimeMillis();
        logLevel = LOG_ALL;
    }

    /**
     * Write a log entry to the file.
     * @param message Message to log
     * @param level Level of message
     */
    public void write(String message, String level) {
        // Get diagnostic data from the system.
        Runtime runtime = Runtime.getRuntime();
        Date currentTime = new Date();
        // Memory data
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        // Determine if we log this message.
        if (level.equals(logLevel) || logLevel.equals(LOG_ALL)) {
            // Build the log file
            try {
                String data = level + "\t" + currentTime + "\t" + message + "\t" +
                        maxMemory + "\t" + totalMemory + "\t" + freeMemory;
                System.out.println(data);
                PrintWriter pw = new PrintWriter(new FileWriter(logFile, true));
                // Log the data to the file
                pw.println(data);
                pw.flush();
                pw.close();
            } catch (UnsupportedEncodingException ex) {
                // Do nothing, we just cant log it.
                System.out.println("Could not write log to file. UTF-8 not supported by system.");
            } catch (IOException ex) {
                System.out.println("Could not write to log file.");
            }
        }
    }
}
