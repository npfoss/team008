package team008.finalBot;

import battlecode.common.*;

public class Gardener extends Bot {
	public Gardener(RobotController r) throws GameActionException {
		super(r);
		// anything else gardener specific
	}

	public void takeTurn() throws GameActionException {
		waterLowestHealthTree();
		buildSomething();
	}

	public void waterLowestHealthTree() throws GameActionException {
		TreeInfo[] treesToWater = rc.senseNearbyTrees(-1, us);
		TreeInfo treeToHeal = Util.leastHealth(treesToWater, true);
		if (treeToHeal != null) {
			rc.water(treeToHeal.getID());
		}
	}

	public void buildSomething() throws GameActionException {
		int typeToBuild = rc.readBroadcast(14);
		int numToBuild = rc.readBroadcast(15);
		if (typeToBuild == 3 && numToBuild > 0) {
			System.out.println("I must build Unit Type:" + typeToBuild + ":" + numToBuild);
			if (buildRobot(RobotType.SCOUT)) {
				rc.broadcast(15, numToBuild - 1);
			}
			return;
		}
		if (nearbyEnemyRobots.length > 0) {
			if (numToBuild > 0) {
				System.out.println("I must build Unit Type:" + typeToBuild + ":" + numToBuild);
				switch (typeToBuild) {
				case 0:
					break;
				case 1:
					if (buildRobot(RobotType.SOLDIER)) {
						rc.broadcast(15, numToBuild - 1);
						return;
					}
					break;
				case 2:
					if (buildRobot(RobotType.TANK)) {
						rc.broadcast(15, numToBuild - 1);
						return;
					}
					break;
				case 3:
					if (buildRobot(RobotType.SCOUT)) {
						rc.broadcast(15, numToBuild - 1);
						return;
					}
					break;
				case 4:
					if (buildRobot(RobotType.LUMBERJACK)) {
						rc.broadcast(15, numToBuild - 1);
						return;
					}
					break;
				case 5:
					break;
				}
			}
		}
		if (plantATree()) {
			return;
		} else {
			if (numToBuild > 0) {
				System.out.println("I must build Unit Type:" + typeToBuild + ":" + numToBuild);
				switch (typeToBuild) {
				case 0:
					break;
				case 1:
					if (buildRobot(RobotType.SOLDIER)) {
						rc.broadcast(15, numToBuild - 1);
						return;
					}
					break;
				case 2:
					if (buildRobot(RobotType.TANK)) {
						rc.broadcast(15, numToBuild - 1);
						return;
					}
					break;
				case 3:
					if (buildRobot(RobotType.SCOUT)) {
						rc.broadcast(15, numToBuild - 1);
						return;
					}
					break;
				case 4:
					if (buildRobot(RobotType.LUMBERJACK)) {
						rc.broadcast(15, numToBuild - 1);
						return;
					}
					break;
				case 5:
					break;
				}
			}
		}
	}

	public boolean buildRobot(RobotType type) throws GameActionException {
		if(rc.getTeamBullets() < type.bulletCost)
			return false;
		Direction dir = here.directionTo(MapAnalysis.center);
		for (int i = 36; i-- > 0;) {
			if (rc.canBuildRobot(type, dir)) {
				rc.buildRobot(type, dir);
				return true;
			} else {
				dir = dir.rotateLeftDegrees(10);
			}
		}
		return false;
	}

	public boolean plantATree() throws GameActionException {

		Direction dir = here.directionTo(MapAnalysis.center);
		Boolean skipped = false;
		for (int i = 35; i-- > 0;) {
			if (rc.canPlantTree(dir)) {
				if (skipped) {
					rc.plantTree(dir);
					return true;
				} else {
					skipped = true;
				}
			}
			if (skipped) {
				dir = dir.rotateLeftDegrees(60);
				i -= 5;
			} else {
				dir = dir.rotateLeftDegrees(10);
			}
		}
		return false;
	}

}