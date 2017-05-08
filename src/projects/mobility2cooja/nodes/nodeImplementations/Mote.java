package projects.mobility2cooja.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import projects.mobility2cooja.LogL;
import projects.mobility2cooja.models.mobilityModels.OfficeMobility;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.messages.Inbox;
import sinalgo.tools.Tools;

public class Mote extends Node {
	
	private Position homePosition = new Position();
	
	@Override
	public void handleMessages(Inbox inbox) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preStep() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		
		
		if (this.ID == 1) {

			try {
				String place = Configuration.getStringParameter("OfficeMobility/SinkPlacement");
				setPlacement(place);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		homePosition.assign(this.getPosition());
		//Set initial position
		if(OfficeMobility.mobilityLogging != null){
			OfficeMobility.mobilityLogging.log(LogL.mobilityLog, (this.ID - 1) + "");
			OfficeMobility.mobilityLogging.log(LogL.mobilityLog, " 0 ");
			OfficeMobility.mobilityLogging.logln(LogL.mobilityLog, homePosition.xCoord + " " + homePosition.yCoord);
		}
		/*
		if(OfficeMobility.mobilityLogging != null){
			OfficeMobility.mobilityLogging.log(LogL.mobilityLog, (this.ID - 1) + " ");
			OfficeMobility.mobilityLogging.log(LogL.mobilityLog, (Tools.getGlobalTime() + OfficeMobility.skippedTimeFromStart) + " ");
			OfficeMobility.mobilityLogging.logln(LogL.mobilityLog, homePosition.xCoord + " " + homePosition.yCoord);
		}*/
		//Tools.appendToOutput("Node "+this.ID+" home "+homePosition.toString()+"\n");
	}

	@Override
	public void neighborhoodChange() {
		// TODO Auto-generated method stub

	}

	@Override
	public void postStep() {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub

	}

	public Position getHomePosition() {
		return homePosition;
	}

	public void setHomePosition(Position homePosition) {
		this.homePosition = homePosition;
	}
	
		
	private void setPlacement(String place) {
		switch (place) {
		case "CENTER":
			this.setPosition(Configuration.dimX / 2, Configuration.dimY / 2, Configuration.dimZ / 2);
			break;
		case "CORNER":
			this.setPosition(0, 0, 0);
			break;
		case "RANDOM":
			// Random r = new Random();
			// this.setPosition(r.getNextPosition());
			break;

		default:
			// this.setPosition(0, 0, 0);
			break;
		}
	}
	
	@NodePopupMethod(menuText = "Degree")
	public void sendEventPopupMethod() {
		Tools.appendToOutput("Node "+this.ID+" degree "+this.outgoingConnections.size()+"\n");
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		// TODO Auto-generated method stub
		String text = ""+this.ID;
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 16, Color.YELLOW);
	}
	
	

}
