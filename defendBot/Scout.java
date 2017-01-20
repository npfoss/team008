package defendBot;

import battlecode.common.*;

public class Scout extends Bot {

	public Scout(RobotController r) throws GameActionException {
		super(r);
	}

	public void takeTurn() throws Exception {
		rc.disintegrate();
		return;
	}

}