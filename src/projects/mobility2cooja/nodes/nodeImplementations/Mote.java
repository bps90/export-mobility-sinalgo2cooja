package projects.mobility2cooja.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

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
		
		homePosition.assign(this.getPosition());
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

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		// TODO Auto-generated method stub
		String text = ""+this.ID;
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 16, Color.YELLOW);
	}
	
	

}
