package team008.b1_28_2024;

import battlecode.common.*;

public class Tank extends Bot {

	public Tank(RobotController r) throws GameActionException {
		super(r);
	}

	public void takeTurn() throws Exception {
		// if(debug)System.out.println("In
		// instantiation:"+Clock.getBytecodeNum());
		if (nearbyEnemyRobots.length > 0
				&& !(nearbyEnemyRobots.length == 1 && nearbyEnemyRobots[0].type == RobotType.ARCHON)) {
			if ((rc.getRoundNum() + rc.getID()) % 25 == 0 || target == null) {
				notifyFriendsOfEnemies(nearbyEnemyRobots);
			}
			RangedCombat.execute();
		}
		if (target == null || (rc.getRoundNum() + rc.getID()) % 20 == 0) {
			assignNewTarget();
		}

		if (rc.getMoveCount() == 0) {
			if (target != null && rc.getLocation().distanceTo(target) < 6 && (nearbyEnemyRobots.length == 0
					|| (nearbyEnemyRobots.length == 1 && nearbyEnemyRobots[0].type == RobotType.ARCHON))) {
				Message.ENEMY_ARMIES.removeLocation(target);
				Message.ISOLATED_ENEMIES.removeLocation(target);
				Message.DISTRESS_SIGNALS.removeLocation(target);
				target = null;
				assignNewTarget();
			} else if (target != null) {
				if (debug) {
					rc.setIndicatorLine(here, target, 255, 0, 0);
				}
				goTo(target);
			} else {
				goTo(here.directionTo(Util.rc.getInitialArchonLocations(enemy)[0]));
			}
		}
	}
}