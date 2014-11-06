package jNetworking.jNetworkInterface;

import java.net.Socket;
import java.util.ArrayList;

/**
 * Interface class for commands.
 */
public interface Command {
    /**
     * Setup method for pre-execution code.
     * @param input Input parameters
     * @param client Input socket
     */
    public abstract void setup(ArrayList<String> input, Socket client);

    /**
     * Command execution. The 'Workhorse' of the command.
     * @return Result
     */
    public abstract String run();
}