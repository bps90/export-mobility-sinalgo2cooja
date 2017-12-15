package projects.checkNodePos.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.tools.Tools;

public class APs extends Node {

	public APs() {
		// TODO Auto-generated constructor stub
	}

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
		// TODO Auto-generated method stub

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
	
	private static int radius;
	{ try {
		radius = Configuration.getIntegerParameter("GeometricNodeCollection/rMax");
	} catch(CorruptConfigurationEntryException e) {
		Tools.fatalError(e.getMessage());
	}}

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		// TODO Auto-generated method stub
		Color bckup = g.getColor();
		g.setColor(Color.RED);
		this.drawingSizeInPixels = (int) (defaultDrawingSizeInPixels * pt.getZoomFactor());
		super.drawNodeAsSquareWithText(g, pt, highlight, "AP", 15, Color.YELLOW);;
		
		g.setColor(Color.RED);
		pt.translateToGUIPosition(this.getPosition());
		int r = (int) (radius * pt.getZoomFactor());
		g.drawOval(pt.guiX - r, pt.guiY - r, r*2, r*2);
		g.setColor(bckup);
	}
	
	
}
