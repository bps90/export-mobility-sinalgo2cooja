package projects.mobility2cooja.models.mobilityModels;

import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import projects.mobility2cooja.CustomGlobal;
import projects.mobility2cooja.LogL;
import projects.mobility2cooja.nodes.nodeImplementations.Mote;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.models.MobilityModel;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.runtime.Main;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.Distribution;
import sinalgo.tools.statistics.UniformDistribution;


/**
 * Random Way Point Mobility Model
 * <p>
 * The node selects a random point in the simulation area and moves there with constant speed. 
 * When arrived at the position, the node waits for a predefined amount of time and then selects
 * the next point to move to.
 * <p>
 * Both, the speed and the waiting time can be described using a random-number generator that
 * returns values according to a given distribution probability.
 * The distribions must be specified in the XML configuration file, and may look as following: 
 * <pre>
		&lt;RandomWayPoint&gt;
		&nbsp; &lt;Speed distribution="Gaussian" mean="40" variance="10"/&gt;
		&nbsp; &lt;WaitingTime distribution="Poisson" lambda="10"/&gt;
		&lt;/RandomWayPoint&gt;
	</pre>
 * 
 * <p>
 * <b>Note:</b> The speed is measured in distance-units per round, the waiting time in rounds. 
 * For the speed, the absolute value of the sample produced by the speed-distribution is used.
 * <p>
 * Note that with the random way point mobility model, nodes tend to gather in the center of the
 * deployment area. I.e. the node densitity changes over time, even if you have an initial random
 * placement of the nodes. To circumvent this problem, consider using the random direction mobiltiy
 * model.
 * <p>
 * Further implementation notes:
 * <ul>
 *   <li>If the waiting time expires between two rounds, the node continues waiting until the next round starts.</li>
 *   <li>If a node arrives at its destination between two rounds, it starts waiting, but this time is not counted towards the waiting time.</li>
 *   <li>This implementation assumes that all nodes move according to the same speed and waiting time distributions (it stores the distribution generators in static fields used by all instances).</li>
 *   <li>If the node is moved (either through the gui or any other means) between two successive calls to getNextPos(), this mobility model asks for a new position to walk to.</li>
 *   <li>This mobility model works for 2D as well as for 3D.</li>
 * </ul>
 * 
 * @see projects.defaultProject.models.mobilityModels.PerfectRWP For a perfect random waypoint mobility model which ensures that the simulation only performs in the stationary regime, starting from the first round.
 */
public class OfficeMobility extends MobilityModel {
	// we assume that these distributions are the same for all nodes
		protected static Distribution speedDistribution;
		protected static Distribution waitingTimeDistribution;

		private static boolean initialized = false; // a flag set to true after initialization of the static vars of this class has been done.
		protected static Random random = Distribution.getRandom(); // a random generator of the framework 
		
		protected Position nextDestination = new Position(); // The point where this node is moving to
		protected Position moveVector = new Position(); // The vector that is added in each step to the current position of this node
		protected Position currentPosition = null; // the current position, to detect if the node has been moved by other means than this mobility model between successive calls to getNextPos()
		protected int remaining_hops = 0; // the remaining hops until a new path has to be determined
		protected int remaining_waitingTime = 0;
		
		
		protected static Distribution distStops;
		public static Set<Node> curr_mob_nodes = new HashSet<Node>();
		protected static Set<Node> stoppedMinusMobNodesSet = new HashSet<Node>();
		
		public static int perMobNodes = 10;
		
		
		private boolean init_vars_after_add_nodes = false;
		
		private int num_stops = -1;
		private int curr_stopsUpToReturn = 0;
		private boolean going_home = false;
		
		protected static double skippedTimeFromStart = 0.0;
		public static Logging mobilityLogging = Logging.getLogger("mobilityLog.txt");
		
		
		/* (non-Javadoc)
		 * @see mobilityModels.MobilityModelInterface#getNextPos(nodes.Node)
		 */
		public Position getNextPos(Node n) {
			// restart a new move to a new destination if the node was moved by another means than this mobility model
			if(currentPosition != null) {
				if(!currentPosition.equals(n.getPosition())) {
					remaining_waitingTime = 0;
					remaining_hops = 0;
				}
			} else {
				currentPosition = new Position(0, 0, 0);
			}
			
			if(!init_vars_after_add_nodes){
				updateSetOfNodes();
				
				init_vars_after_add_nodes = true;
			}
			
			if(!canImove(n)){
				return n.getPosition();				
			}
						
			Position nextPosition = new Position();
			
			// execute the waiting loop
			if(remaining_waitingTime > 0) {
				remaining_waitingTime --;
				return n.getPosition();
			}
			
			//set the next position to be the home_position of the Mote
			if(curr_stopsUpToReturn == num_stops){
				System.out.println("Node "+n.ID+" returning to home position.");
				
				curr_stopsUpToReturn = 0;
				going_home = true;
				
				// determine the speed at which this node moves
				double speed = Math.abs(speedDistribution.nextSample()); // units per round

				// determine the next point where this node moves to
				nextDestination = getHomePosition(n);
				//Tools.appendToOutput("Node "+n.ID+" go to "+nextDestination.toString()+"\n");
				
				// determine the number of rounds needed to reach the target
				double dist = nextDestination.distanceTo(n.getPosition());
				double rounds = dist / speed;
				remaining_hops = (int) Math.ceil(rounds);
				// determine the moveVector which is added in each round to the position of this node
				double dx = nextDestination.xCoord - n.getPosition().xCoord;
				double dy = nextDestination.yCoord - n.getPosition().yCoord;
				double dz = nextDestination.zCoord - n.getPosition().zCoord;
				moveVector.xCoord = dx / rounds;
				moveVector.yCoord = dy / rounds;
				moveVector.zCoord = dz / rounds;
			}

			if(remaining_hops == 0) {
				// determine the speed at which this node moves
				double speed = Math.abs(speedDistribution.nextSample()); // units per round

				// determine the next point where this node moves to
				nextDestination = getNextWayPoint();
				//Tools.appendToOutput("Node "+n.ID+" go to "+nextDestination.toString()+"\n");
				
				// determine the number of rounds needed to reach the target
				double dist = nextDestination.distanceTo(n.getPosition());
				double rounds = dist / speed;
				remaining_hops = (int) Math.ceil(rounds);
				// determine the moveVector which is added in each round to the position of this node
				double dx = nextDestination.xCoord - n.getPosition().xCoord;
				double dy = nextDestination.yCoord - n.getPosition().yCoord;
				double dz = nextDestination.zCoord - n.getPosition().zCoord;
				moveVector.xCoord = dx / rounds;
				moveVector.yCoord = dy / rounds;
				moveVector.zCoord = dz / rounds;

				num_stops = (int) distStops.nextSample();
			}
			if(remaining_hops <= 1) { // don't add the moveVector, as this may move over the destination.
				nextPosition.xCoord = nextDestination.xCoord;
				nextPosition.yCoord = nextDestination.yCoord;
				nextPosition.zCoord = nextDestination.zCoord;
				// set the next waiting time that executes after this mobility phase
				remaining_waitingTime = (int) Math.ceil(waitingTimeDistribution.nextSample());
				remaining_hops = 0;
				
				System.out.println("Node "+n.ID+" stop number " + curr_stopsUpToReturn);
				curr_stopsUpToReturn++;
			} else {
				double newx = n.getPosition().xCoord + moveVector.xCoord; 
				double newy = n.getPosition().yCoord + moveVector.yCoord; 
				double newz = n.getPosition().zCoord + moveVector.zCoord; 
				nextPosition.xCoord = newx;
				nextPosition.yCoord = newy;
				nextPosition.zCoord = newz;
				remaining_hops --;
			}
			currentPosition.assign(nextPosition);
			
			mobilityLogging.log(LogL.mobilityLog, (n.ID - 1) + " ");
			mobilityLogging.log(LogL.mobilityLog, (Tools.getGlobalTime() + skippedTimeFromStart) + " ");
			mobilityLogging.logln(LogL.mobilityLog, nextPosition.xCoord + " " + nextPosition.yCoord);
			
			return nextPosition;
		}
		
		private Position getHomePosition(Node n) {
			if(n instanceof Mote){
				System.out.println("Node "+n.ID+" Go back to "+((Mote) n).getHomePosition());
				return ((Mote) n).getHomePosition();
			}
			// If the Mote does not have home position, then we return to the origin.
			return new Position(0, 0, 0); 
		}

		/**
		 * Determines the next waypoint where this node moves after having waited.
		 * The position is expected to be within the deployment area.
		 * @return the next waypoint where this node moves after having waited. 
		 */
		protected Position getNextWayPoint() {
			double randx = random.nextDouble() * Configuration.dimX;
			double randy = random.nextDouble() * Configuration.dimY;
			double randz = 0;
			if(Main.getRuntime().getTransformator().getNumberOfDimensions() == 3) {
				randz = random.nextDouble() * Configuration.dimZ;
			}
			return new Position(randx, randy, randz);
		}
		
		/**
		 * 
		 */
		protected boolean canImove(Node n){
			int num_nodes = Tools.getNodeList().size();
			
			if(n.ID == 1){return false;} //sink always remains fixed
			
			if(n.getPosition().equals(((Mote)n).getHomePosition()) && going_home){
				System.out.println("Node "+n.ID+" back to home.");
				curr_mob_nodes.remove(n);
				stoppedMinusMobNodesSet.add(n);
				going_home = false;
			}
			
			if((num_nodes > 0) && ((curr_mob_nodes.size()*100) / num_nodes <= perMobNodes) ){
				chooseSomeoneOrNoOne();
			}
			
			if(curr_mob_nodes.contains(n)){return true;}
			
			return false;
		}
		
		private void chooseSomeoneOrNoOne(){
			UniformDistribution ud = new UniformDistribution(0, 1);
			int numNodes = Tools.getNodeList().size();
			int r = 0;
			Node n;
			if(numNodes > 1){
				if(ud.nextSample() > 0.5){
					r = random.nextInt(stoppedMinusMobNodesSet.size());
					System.out.println("stoppedMinusMobNodesSet Size = " + stoppedMinusMobNodesSet.size() + " choose r "+r);
					n = (Node) stoppedMinusMobNodesSet.toArray()[r];
					stoppedMinusMobNodesSet.remove(n);
					curr_mob_nodes.add(n);
					System.out.println("stoppedMinusMobNodesSet after Size = " + stoppedMinusMobNodesSet.size());
					System.out.println("curr_mob_nodes after Size = " + curr_mob_nodes.size());
				}
			}
		}
		
		private void updateSetOfNodes(){
			stoppedMinusMobNodesSet.clear();
			Iterator<Node> it = Tools.getNodeList().iterator();
			Node cur_node = null;
			while(it.hasNext()){
				cur_node = it.next();
				stoppedMinusMobNodesSet.add(cur_node);
			}
		}
		
		/*protected boolean canImove(Node n){
			int num_nodes = Tools.getNodeList().size();
			UniformDistribution ud = new UniformDistribution(0, 1);
			
			if(n.ID == 1){return false;} //sink always remains fixed
			
			if(n.getPosition().equals(((Mote)n).getHomePosition()) && going_home){
				System.out.println("Node "+n.ID+" back to home.");
				curr_mob_nodes.remove(n);
				going_home = false;
				return false;
			}
			
			if(curr_mob_nodes.contains(n)){return true;}
			
			
			if(num_nodes > 0 && ud.nextSample() > 0.5){				
				if ((curr_mob_nodes.size()*100) / num_nodes <= perMobNodes) {
					curr_mob_nodes.add(n);
					return true;
				}
			}
			return false;
		}*/
		
		/**
		 * Creates a new random way point object, and reads the speed distribution and 
		 * waiting time distribution configuration from the XML config file.
		 * @throws CorruptConfigurationEntryException When a needed configuration entry is missing.
		 */
		public OfficeMobility() throws CorruptConfigurationEntryException {
			if(!initialized) {
				speedDistribution = Distribution.getDistributionFromConfigFile("OfficeMobility/Speed");
				perMobNodes = Configuration.getIntegerParameter("OfficeMobility/PerMobNodes");
				if(perMobNodes < 0 || perMobNodes > 100){
					perMobNodes = 10;
					System.out.println("Using 10% as default value of perMobNodes");
				}
				waitingTimeDistribution = Distribution.getDistributionFromConfigFile("OfficeMobility/WaitingTime");
				distStops = Distribution.getDistributionFromConfigFile("OfficeMobility/DistStops");
				skippedTimeFromStart = Configuration.getDoubleParameter("OfficeMobility/SkippedTimeFromStart");
				initialized = true;
			}
		}
}
