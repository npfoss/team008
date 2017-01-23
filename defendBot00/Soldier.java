package team008.defendBot00;

import battlecode.common.*;

public class Soldier extends Bot {

	public Soldier(RobotController r) throws GameActionException {
		super(r);
		// anything else soldier specific
	}

	public void takeTurn() throws Exception {
		rc.disintegrate();
		return;
	}

}