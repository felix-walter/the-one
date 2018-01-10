package movement.map;

import core.Coord;
import core.Settings;
import core.SettingsError;
import core.SimError;
import input.WKTMapReader;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by duschi on 05.01.2017.
 */
public class SimMapCreator {

    /** map based movement model's settings namespace ({@value})*/
    public static final String MAP_BASE_MOVEMENT_NS = "MapBasedMovement";
    /** number of map files -setting id ({@value})*/
    public static final String NROF_FILES_S = "nrofMapFiles";
    /** map file -setting id ({@value})*/
    public static final String FILE_S = "mapFile";

    private static SimMapCreator instance;
    private SimMap cachedMap;
    private List<String> cachedMapFiles;
    private int maxX;
    private int maxY;
    private int nrOfMapFilesRead;

    private SimMapCreator() {
        cachedMapFiles      = new LinkedList<>();
        nrOfMapFilesRead    = 0;
        maxX                = 0;
        maxY                = 0;
    }

    public static synchronized SimMapCreator getInstance() {
        if (instance == null) {
            instance = new SimMapCreator();
        }
        return instance;
    }

    public synchronized SimMap getSimMap(int maxX, int maxY) {
        this.maxX = maxX;
        this.maxY = maxY;
        SimMap simMap;
        Settings settings = new Settings(MAP_BASE_MOVEMENT_NS);
        WKTMapReader r = new WKTMapReader(true);

        if (cachedMap == null) {
            cachedMapFiles = new ArrayList<String>(); // no cache present
        } else { // something in cache
            // check out if previously asked map was asked again
            SimMap cached = checkCache(settings);
            if (cached != null) {
                nrOfMapFilesRead = cachedMapFiles.size();
                return cached; // we had right map cached -> return it
            }
            else { // no hit -> reset cache
                cachedMapFiles = new ArrayList<String>();
                cachedMap = null;
            }
        }

        try {
            int nrofMapFiles = settings.getInt(NROF_FILES_S);
            for (int i = 1; i <= nrofMapFiles; i++ ) {
                String pathFile = settings.getSetting(FILE_S + i);
                cachedMapFiles.add(pathFile);
                r.addPaths(new File(pathFile), i);
            }
            this.nrOfMapFilesRead = nrofMapFiles;
        } catch (IOException e) {
            throw new SimError(e.toString(),e);
        }

        simMap = r.getMap();
        checkMapConnectedness(simMap.getNodes());
        // mirrors the map (y' = -y) and moves its upper left corner to origo
        /*simMap.mirror();
        Coord offset = simMap.getMinBound().clone();
        simMap.translate(-offset.getX(), -offset.getY());
        System.out.println("offsetX: " + offset.getX() + " offsetY: " + offset.getY());*/
        checkCoordValidity(simMap.getNodes());
        cachedMap = simMap;
        return simMap;
    }

    /**
     * Checks map cache if the requested map file(s) match to the cached
     * sim map
     * @param settings The Settings where map file names are found
     * @return A cached map or null if the cached map didn't match
     */
    private SimMap checkCache(Settings settings) {
        int nrofMapFiles = settings.getInt(NROF_FILES_S);

        if (nrofMapFiles != cachedMapFiles.size() || cachedMap == null) {
            return null; // wrong number of files
        }

        for (int i = 1; i <= nrofMapFiles; i++ ) {
            String pathFile = settings.getSetting(FILE_S + i);
            if (!pathFile.equals(cachedMapFiles.get(i-1))) {
                return null;	// found wrong file name
            }
        }

        // all files matched -> return cached map
        return cachedMap;
    }

    /**
     * Checks that all map nodes can be reached from all other map nodes
     * @param nodes The list of nodes to check
     * @throws SettingsError if all map nodes are not connected
     */
    private void checkMapConnectedness(List<MapNode> nodes) {
        Set<MapNode> visited = new HashSet<MapNode>();
        Queue<MapNode> unvisited = new LinkedList<MapNode>();
        MapNode firstNode;
        MapNode next = null;

        if (nodes.size() == 0) {
            throw new SimError("No map nodes in the given map");
        }

        firstNode = nodes.get(0);

        visited.add(firstNode);
        unvisited.addAll(firstNode.getNeighbors());

        while ((next = unvisited.poll()) != null) {
            visited.add(next);
            for (MapNode n: next.getNeighbors()) {
                if (!visited.contains(n) && ! unvisited.contains(n)) {
                    unvisited.add(n);
                }
            }
        }

        if (visited.size() != nodes.size()) { // some node couldn't be reached
            MapNode disconnected = null;
            for (MapNode n : nodes) { // find an example node
                if (!visited.contains(n)) {
                    disconnected = n;
                    break;
                }
            }
            throw new SettingsError("SimMap is not fully connected. Only " +
                    visited.size() + " out of " + nodes.size() + " map nodes " +
                    "can be reached from " + firstNode + ". E.g. " +
                    disconnected + " can't be reached");
        }
    }

    /**
     * Checks that all coordinates of map nodes are within the min&max limits
     * of the movement model
     * @param nodes The list of nodes to check
     * @throws SettingsError if some map node is out of bounds
     */
    private void checkCoordValidity(List<MapNode> nodes) {
        // Check that all map nodes are within world limits
        for (MapNode n : nodes) {
            double x = n.getLocation().getX();
            double y = n.getLocation().getY();
            if (x < 0 || x > maxX || y < 0 || y > maxY) {
                throw new SettingsError("Map node " + n.getLocation() +
                        " is out of world  bounds "+
                        "(x: 0..." + maxX + " y: 0..." + maxY + ")");
            }
        }
    }

    public int getNrOfMapFilesRead() {
        return nrOfMapFilesRead;
    }
}
