import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.io.IOException;
import java.awt.image.*;
import java.awt.Color;
import javax.imageio.ImageIO;

public class Main {

	static BufferedImage bf;
	static int width, height, wall, nodeCount;	
	static int count1, count2, count3, count4;
	static Nuts startNode, endNode, currentNode;
	static Nuts nodes[][];
	
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
		String filename = "new1.png";

		try {
			f = new File(filename);
			bf = ImageIO.read(f);
			
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
		nodes = new Nuts[height][width];
		generateNodes(positions);
		System.out.println("The total number of nodes is " + nodeCount);
		System.out.printf("Number of nodes with: 1 path-%d, 2 paths-%d, 3 paths-%d, 4 paths-%d\n", count1, count2, count3, count4);
		//To make the graph and connect all of the vertices correctly, I will need to create
		//an 2d array of all of the nodes. I will perform two passes over the entire array.
		//The first pass will be for connecting the nodes side-to-side by storing a currentNode
		//and checking if that node has a node it is connected to on the right. If so, we will
		//move along that row until we hit the node. If not, we move to the next node and check
		//if it has a node to the right, and so on. The same process will be applied to the 
		//vertical pass of this algorithm.

		//TODO Exploit the horizontal and vertical pass to find the neighboring nodes
		//and store them in the nuts neighbor arraylist

//		System.out.println("Now working on the horizontal pass");

		Nuts currentNode = nodes[1][0];	
		boolean hasEast = currentNode.east;


		for(int i = 1; i < height; i++) {
			for(int j = 1; j < width; j++){
				if(nodes[i][j] != null) {
//					System.out.println();
//					System.out.println("NODE FOUND " + nodes[i][j]);
					if(hasEast && nodes[i][j].west) {
//						System.out.println("Found the node next to " + currentNode.pos);
						Nuts temp = nodes[i][j];

						currentNode.neighbors.add(temp);
						temp.neighbors.add(currentNode);
//						System.out.println("Creating edge between " + currentNode.pos + " and " +  nodes[i][j].pos);

						//If temp has a path to the east, we make that the next currentNode
						if(nodes[i][j].east) {
//							System.out.println("Found node has a node east of it");
							currentNode = nodes[i][j];
						} else {
//							System.out.println("Found node doesn't have a node east of it");
							currentNode = null;
							hasEast = false;
						}
					} else {
						//Handle if there is no current node or vertex	
//						System.out.println("Found the next current node");
						currentNode = nodes[i][j];
						hasEast = currentNode.east;
					}
				}
			}
		}

//		System.out.println("\n\nNow working on the vertical passes");

		currentNode = nodes[1][1];
		boolean hasSouth = currentNode == null ? false : currentNode.south;

		for(int j = 1; j < width; j++){
			for(int i = 1; i < height; i++) {
				if(nodes[i][j] != null) {
//					System.out.println();
//					System.out.println("NODE FOUND " + nodes[i][j]);
					if(hasSouth && nodes[i][j].north){
//						System.out.println("Found the node below the " + currentNode.pos);
						Nuts temp = nodes[i][j];

						currentNode.neighbors.add(temp);
						temp.neighbors.add(currentNode);
//						System.out.println("Creating edge between " + currentNode.pos + " and " +  nodes[i][j].pos);
						
						if(nodes[i][j].south) {
//							System.out.println("Found node has a node south of it");
							currentNode = nodes[i][j];
						} else {
//							System.out.println("Found node doesn't have a node south of it");
							currentNode = null;
							hasSouth = false;
						}
					} else {
//						System.out.println("Found the next current node");
						currentNode = nodes[i][j];
						hasSouth = currentNode.south;
					}
				}
			}
		}

		ArrayList<Nuts> path = findPath();	

		//TODO Rewrite to a new file with the solved maze
		for(Nuts n : path) 
			System.out.println(n);
		t.end();
		System.out.println("Took " + t.getTimeFromStart());
		System.out.println("There are " + path.size() + " nodes in the final path");
	}

	public static ArrayList<Nuts> findPath() {
		//Start generating the h values (the distance from the node to the last node)
		//We will use euclidean distance (sqrt[(x1 - x2)^2 + (y1 - y2)^2])

		for(Nuts[] nt : nodes) {
			for(Nuts n : nt) {
				if(n != null) n.h = Math.sqrt(Math.pow(n.pos.row - endNode.pos.row, 2) + Math.pow(n.pos.col - endNode.pos.col, 2));
			}
		}
		currentNode = startNode;

		//Create three arraylists: one for the open list, one for the closed list, and one for the path
		ArrayList<Nuts> openList = new ArrayList<>();
		ArrayList<Nuts> closedList = new ArrayList<>();
		ArrayList<Nuts> path = new ArrayList<>();
		Nuts lastNode = null;
		
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

			for(Nuts n : currentNode.neighbors) {
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
		Nuts n = lastNode;
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
			nodes[i][j] = new Nuts(p, walls[0], walls[1], walls[2], walls[3]);
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

	public static boolean isNode(int i, int j) {
		int count = 0;
		boolean inLine1 = false, inLine2 = false;
		if(bf.getRGB(i,j) == wall)
			return false;
		
		if(inBounds(i+1, j) && bf.getRGB(i+1, j) != wall)
			count++;
		if(inBounds(i-1, j) && bf.getRGB(i-1, j) != wall) 
			count++;	
		if(inBounds(i, j+1) && bf.getRGB(i, j+1) != wall) 
			count++;
		if(inBounds(i, j-1) && bf.getRGB(i, j-1) != wall) 
			count++;
		inLine1 = inBounds(i+1, j) && bf.getRGB(i+1, j) != wall && inBounds(i-1, j) && bf.getRGB(i-1, j) != wall;
		inLine2 = inBounds(i, j+1) && bf.getRGB(i, j+1) != wall && inBounds(i, j-1) && bf.getRGB(i, j-1) != wall;

		boolean temp = count == 1 || (count == 2 && !(inLine1 || inLine2)) || count == 3 || count == 4; 
		if(temp) {
			nodeCount++;
			switch(count) {
				case 1: count1++; break;
				case 2: count2++; break;
				case 3: count3++; break;
				case 4: count4++; break;
			}
		}
		
		return temp;
	}

	public static boolean inBounds(int j, int i) {
		return i >= 0 && i < height && j >= 0 && j < width; 
	}
}

class Nuts {

	Pos pos;
	boolean east, west, south, north;
	int routes;
	double f, g, h;
	boolean isStartNode, isEndNode;
	ArrayList<Nuts> neighbors;
	Nuts cameFrom;

	public Nuts(Pos pos, boolean east, boolean west, boolean south, boolean north) {
		this.pos = pos;
		neighbors = new ArrayList<Nuts>();
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
		if(isStartNode)
			Main.startNode = this;
		if(isEndNode)
			Main.endNode = this;
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







