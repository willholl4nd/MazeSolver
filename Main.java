import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.io.*;
import java.awt.image.*;
import java.awt.Color;
import javax.imageio.ImageIO;

public class Main {

    static BufferedImage bf;
    static int width, height, wall, nodeCount;	
    static int count1, count2, count3, count4;
    static Node startNode, endNode, currentNode;
    static Node nodes[][];

    public static void main(String args[]) throws IOException {
        Timer t = new Timer();
        t.start();
        bf = null;
        File f = null;	
        nodeCount = 0;
        count1 = 0;
        count2 = 0;
        count3 = 0;
        count4 = 0;
        startNode = null;
        endNode = null;
        currentNode = null;
        String filename = "m2.png";
        if(args.length > 0)
            filename = args[0];
        File copy = new File("solved/" + filename.split(".png")[0] + "solved.png");
        try {
            f = new File("unsolved/" + filename);
            copy.createNewFile();
            FileOutputStream copyOut = new FileOutputStream(copy);
            Files.copy(f.toPath(), copyOut); 
            bf = ImageIO.read(copy);

        } catch (IOException e) {
            e.printStackTrace();
        }
        width = bf.getWidth();
        height = bf.getHeight();
        boolean image[][] = new boolean[height][width]; //True = wall, false = path	
        System.out.printf("Running maze solver on " + filename + "\nWidth: %d, height: %d\n", width, height);

        wall = bf.getRGB(0,0);

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                image[i][j] = bf.getRGB(j,i) == wall;
            }
        }

        ArrayList<Pos> positions = findNodePositions();
        nodes = new Node[height][width];
        generateNodes(positions);
        System.out.println("The total number of nodes is " + nodeCount);
        System.out.printf("Number of nodes with: 1 path-%d, 2 paths-%d, 3 paths-%d, 4 paths-%d\n", count1, count2, count3, count4);

        for (int i = 0; i < 10; i++)
            System.out.printf("Node(%d,1) = %s\n", i, nodes[i][1]);
        //System.exit(0);

        //TODO Add starting and ending position detection
        Node currentNode = nodes[1][0];	
        System.out.println(currentNode);
        boolean hasEast = currentNode.east;

        for(int i = 1; i < height; i++) {
            for(int j = 1; j < width; j++){
                if(nodes[i][j] != null) {
                    if(hasEast && nodes[i][j].west) {
                        Node temp = nodes[i][j];

                        currentNode.neighbors.add(temp);
                        temp.neighbors.add(currentNode);

                        //If temp has a path to the east, we make that the next currentNode
                        if(nodes[i][j].east) {
                            currentNode = nodes[i][j];
                        } else {
                            currentNode = null;
                            hasEast = false;
                        }
                    } else {
                        //Handle if there is no current node or vertex	
                        currentNode = nodes[i][j];
                        hasEast = currentNode.east;
                    }
                }
            }
        }


        currentNode = nodes[1][1];
        boolean hasSouth = currentNode == null ? false : currentNode.south;

        for(int j = 1; j < width; j++){
            for(int i = 1; i < height; i++) {
                if(nodes[i][j] != null) {
                    if(hasSouth && nodes[i][j].north){
                        Node temp = nodes[i][j];

                        currentNode.neighbors.add(temp);
                        temp.neighbors.add(currentNode);

                        if(nodes[i][j].south) {
                            currentNode = nodes[i][j];
                        } else {
                            currentNode = null;
                            hasSouth = false;
                        }
                    } else {
                        currentNode = nodes[i][j];
                        hasSouth = currentNode.south;
                    }
                }
            }
        }

        System.out.println("Starting A* algorithm");
        ArrayList<Node> path = findPath();	
        System.out.println("Path length: " + path.size());

        System.out.println("Saving solution image back to png");
        bf.setRGB(startNode.pos.col, startNode.pos.row, Color.red.getRGB());
        double cyclesOfTheRainbow = 1;
        double Hchange = (cyclesOfTheRainbow * 360) / (double)path.size();
        double H = 0, S = 1, V = 1;
        for(int i = 0; i < path.size()-1; i++) {
            H += Hchange;
            Node n = path.get(i);
            Node toN = path.get(i+1);
            int colDif = toN.pos.col - n.pos.col;
            int rowDif = toN.pos.row - n.pos.row;
            if(H >= 360)
                H = 0;
            Color c = getRGB(H, S, V);
            if(colDif < 0) {
                for(int j = 0; j < Math.abs(colDif); j++) {
                    bf.setRGB(n.pos.col - j, n.pos.row, c.getRGB());
                }
            } else if(colDif > 0) {
                for(int j = 0; j < Math.abs(colDif); j++)
                    bf.setRGB(n.pos.col + j, n.pos.row, c.getRGB());

            } else if(rowDif < 0) {
                for(int j = 0; j < Math.abs(rowDif); j++)
                    bf.setRGB(n.pos.col, n.pos.row - j, c.getRGB());

            } else if(rowDif > 0) {
                for(int j = 0; j < Math.abs(rowDif); j++)
                    bf.setRGB(n.pos.col, n.pos.row + j, c.getRGB());

            }
        }

        ImageIO.write(bf, "png", copy);
        t.end();
        System.out.println("Took " + t.getTimeFromStart());

    }

    public static Color getRGB(double H, double S, double V) {
        double C = V * S;
        double X = C * (1 - Math.abs((H / 60) % 2 - 1));
        double m = V - C;
        double Rprime=0, Gprime=0, Bprime=0;
        if(H >= 0 && H < 60) {
            Rprime = C;
            Gprime = X;
            Bprime = 0;
        } else if(H >= 60 && H < 120) {
            Rprime = X;
            Gprime = C;
            Bprime = 0;
        } else if(H >= 120 && H < 180) {
            Rprime = 0;
            Gprime = C;
            Bprime = X;
        } else if(H >= 180 && H < 240) {
            Rprime = 0;
            Gprime = X;
            Bprime = C;
        } else if(H >= 240 && H < 300) {
            Rprime = X;
            Gprime = 0;
            Bprime = C;
        } else if(H >= 300 && H < 360) {
            Rprime = C;
            Gprime = 0;
            Bprime = X;
        }
        Color c = new Color((int)((Rprime+m)*255), (int)((Gprime+m)*255), (int)((Bprime+m)*255));
        //		System.out.println(c.getRGB());
        //		System.out.printf("R': %f, G': %f, B': %f, m: %f\n", Rprime, Gprime, Bprime, m);
        return c;
    }

    public static ArrayList<Node> findPath() {
        //Start generating the h values (the distance from the node to the last node)
        //We will use euclidean distance (sqrt[(x1 - x2)^2 + (y1 - y2)^2])

        System.out.println("EndNode: " + endNode);
        for(Node[] nt : nodes) {
            for(Node n : nt) {
                if(n != null) n.h = Math.sqrt(Math.pow(n.pos.row - endNode.pos.row, 2) +
                        Math.pow(n.pos.col - endNode.pos.col, 2));
            }
        }
        currentNode = startNode;

        //Create three arraylists: one for the open list, one for the closed list, and one for the path
        ArrayList<Node> openList = new ArrayList<>();
        ArrayList<Node> closedList = new ArrayList<>();
        ArrayList<Node> path = new ArrayList<>();
        Node lastNode = null;

        openList.add(currentNode);
        currentNode.g = 0; 
        currentNode.f = currentNode.h;

        while(openList.size() != 0) {
            currentNode = openList.stream().min(Comparator.comparingDouble(o -> o.f)).get();

            openList.remove(currentNode);
            closedList.add(currentNode);

            //Diagonals are not possible because it is a maze
            if(currentNode.h <= Math.sqrt(2)) {
                lastNode = currentNode;
                break;
            }

            for(Node n : currentNode.neighbors) {
                if(!closedList.contains(n)) {
                    double tentativeG = currentNode.g + 1;
                    if(tentativeG < n.g) {
                        n.cameFrom = currentNode;
                        n.g = tentativeG;
                        n.f = n.g + n.h;
                        if(!openList.contains(n))
                            openList.add(n);
                    }
                }
            }
        }

        path.add(endNode);
        Node n = lastNode;
        while(n.cameFrom != null) {
            path.add(n);
            n = n.cameFrom;
        }
        path.add(startNode);

        return path;
    }

    public static void printMap(boolean mapOfNodes[][]) {
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                System.out.print(mapOfNodes[i][j] ? "?" : ".");
            }
            System.out.println();
        }
    }

    public static void generateNodes(ArrayList<Pos> positions) {
        for(Pos p : positions) {
            int i = p.row;
            int j = p.col;

            //indices: 0 = east, 1 = west, 2 = south, 3 = north
            boolean walls[] = findWalls(j, i);
            nodes[i][j] = new Node(p, walls[0], walls[1], walls[2], walls[3]);
        }
    }

    public static boolean[] findWalls(int i, int j) {
        boolean[] walls = new boolean[4];
        if(inBounds(i+1, j) && bf.getRGB(i+1, j) != wall)
            walls[0] = true;
        if(inBounds(i-1, j) && bf.getRGB(i-1, j) != wall) 
            walls[1] = true;
        if(inBounds(i, j+1) && bf.getRGB(i, j+1) != wall) 
            walls[2] = true;
        if(inBounds(i, j-1) && bf.getRGB(i, j-1) != wall) 
            walls[3] = true;
        return walls;
    }

    /**
     * Create ArrayList of all nodes 
     */
    public static ArrayList<Pos> findNodePositions() {
        boolean nodes[][] = new boolean[height][width];
        ArrayList<Pos> nodesAL = new ArrayList<>();

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                boolean temp = isNode(j, i);	 
                nodes[i][j] = temp;
                if(temp)
                    nodesAL.add(new Pos(i, j));
            }
        }
        return nodesAL;
    }

    /**
     * Determines whether the current position of the board we are looking at 
     * is a node by analyzing the surrounding positions
     */
    public static boolean isNode(int i, int j) {
        //No need to check further since position is a wall
        if(bf.getRGB(i,j) == wall)
            return false;

        int count = 0; //Number of positions connecting to one we are checking
        boolean lineEW = false, lineNS = false;

        int north = 0, east = 0, south = 0, west = 0;
        boolean nb, eb, sb, wb;

        //Check node to east 
        if((eb = inBounds(i+1, j)) && (east = bf.getRGB(i+1, j)) != wall)
            count++;
        //Check node to west 
        if((wb = inBounds(i-1, j)) && (west = bf.getRGB(i-1, j)) != wall) 
            count++;	
        //Check node to south 
        if((sb = inBounds(i, j+1)) && (south = bf.getRGB(i, j+1)) != wall) 
            count++;
        //Check node to north 
        if((nb = inBounds(i, j-1)) && (north = bf.getRGB(i, j-1)) != wall) 
            count++;

        //Determine if node is part of lines in either direction
        lineEW = eb && east != wall && wb && west != wall;
        lineNS = sb && south != wall && nb && north != wall;

        boolean isNode = count == 1 || (count == 2 && !(lineEW || lineNS)) || count == 3 || count == 4; 
        if(isNode) {
            nodeCount++;
            switch(count) {
                case 1: count1++; break;
                case 2: count2++; break;
                case 3: count3++; break;
                case 4: count4++; break;
            }
        }

        return isNode;
    }

    /**
     * Checks whether the coordinates are appropriate
     */
    public static boolean inBounds(int j, int i) {
        return i >= 0 && i < height && j >= 0 && j < width; 
    }
}

class Node {

    Pos pos;
    boolean east, west, south, north;
    int routes;
    double f, g, h;
    boolean isStartNode, isEndNode;
    ArrayList<Node> neighbors;
    Node cameFrom;

    public Node(Pos pos, boolean east, boolean west, boolean south, boolean north) {
        this.pos = pos;
        neighbors = new ArrayList<Node>();
        this.east = east;
        if(east) 
            routes++;
        this.west = west;
        if(west) 
            routes++;
        this.south = south;
        if(south) 
            routes++;
        this.north = north;
        if(north)
            routes++;

        isStartNode = pos.row == 1 && pos.col == 0;
        isEndNode = pos.row == Main.height - 2 && pos.col == Main.width - 1;
        if(isStartNode) {
            Main.startNode = this;
            System.out.println("Found the start node");
        }
        if(isEndNode) {
            Main.endNode = this;
            System.out.println("Found the end node");
        }
        g = Double.MAX_VALUE;
        f = Double.MAX_VALUE;
        cameFrom = null;	
    }

    @Override
    public String toString() {
        return String.format("Row: %d, col: %d, routes: %d, paths: north-%b, east-%b, south-%b, west-%b, f: %.5f, g: %.5f, h: %.5f", pos.row, pos.col, routes, north, east, south, west, f, g, h);
    }
}

class Pos {
    int row, col;

    public Pos(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public String toString() {
        return String.format("Row: %d, col: %d", row, col);
    }
}
