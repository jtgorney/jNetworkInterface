package jNetworking.jNetworkInterface;

import java.util.ArrayList;

/**
 * Created by Jacob on 9/2/2014.
 */
public interface Command {
   public abstract void setup(ArrayList<Object> input);
   public abstract String run();
}