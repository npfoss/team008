package team008.finalBot;
import battlecode.common.*;

public class Gardener extends Bot {
    public boolean isExploring;
    public static Direction dirIAmMoving;
    public static boolean updatedLocs;
    public static int turnsIHaveBeenTrying;
    public static float myPatience;
    public static float dLastTurn;
    public static MapLocation targetLoc;
    public static boolean trapped = false;

    public Gardener(RobotController r) throws GameActionException {
        super(r);
        debug = true;
        isExploring = true;
        updatedLocs = false;
        //tankBuilder = false;
        turnsIHaveBeenTrying = 0;
       // turnsTank = 0;
        myPatience = 1;
        dLastTurn = 1;
        targetLoc = Message.GARDENER_BUILD_LOCS.getClosestLocation(here);
        // anything else gardener specific
    }

    private static Direction findOpenSpaces() throws GameActionException {
		// TODO: make this better
		Direction dir = new Direction(0);
		int thingsInTheWay = 0;
		int bestScore = 10000;
		Direction bestDir = new Direction(0);
		for (int i = 0; i < 16; i++) {
			if (!rc.onTheMap(here.add(dir, (float) (type.sensorRadius - .001)))) {
				thingsInTheWay += 10;
			}
			for (TreeInfo t : nearbyAlliedTrees)
				if (Math.abs(dir.radiansBetween(here.directionTo(t.location))) < Math.PI / 2) {
					thingsInTheWay += 2;
				}
			for (TreeInfo t : nearbyNeutralTrees){
				if(here.distanceTo(t.location) < 5 + t.radius){
					Message.CLEAR_TREES_PLEASE.addLocation(here);
				}
				if (Math.abs(dir.radiansBetween(here.directionTo(t.location))) < Math.PI / 2){
					thingsInTheWay += 2;
				}
			}
			for (RobotInfo t : nearbyRobots)
				if ((t.type == RobotType.ARCHON || t.type == RobotType.GARDENER)
						&& Math.abs(dir.radiansBetween(here.directionTo(t.location))) < Math.PI / 2) {
					thingsInTheWay += (t.type == RobotType.ARCHON ? 2 : 10);
				}
			if (thingsInTheWay < bestScore) {
				bestDir = dir;
				bestScore = thingsInTheWay;
			}
			// rc.setIndicatorDot(here.add(dir), thingsInTheWay*10,
			// thingsInTheWay*10, thingsInTheWay*10);
			// System.out.println("ThisScore: " + thingsInTheWay);
			// System.out.println(dir.toString());
			dir = dir.rotateLeftDegrees((float) 22.5);
			thingsInTheWay = 0;
		}
		// System.out.println("Best Score: " + bestScore);
		// System.out.println(bestDir.toString());

        return bestDir;

    }
    public boolean isBadLocation(MapLocation targetLoc) throws GameActionException{
        if(targetLoc == null || here.distanceTo(targetLoc) < type.bodyRadius){
            return false;
        }
        if(here.distanceTo(targetLoc) > 25){
        	return true;
        }
        if(turnsIHaveBeenTrying > 70){
            turnsIHaveBeenTrying = 0;
            Message.GARDENER_BUILD_LOCS.removeLocation(targetLoc);
            return true;
        }
        if(rc.canSenseLocation(targetLoc) && rc.senseRobotAtLocation(targetLoc) != null && rc.senseRobotAtLocation(targetLoc).type != RobotType.GARDENER || !rc.canSenseLocation(targetLoc)){
            turnsIHaveBeenTrying++;
            return false;
        }

        float dist = here.distanceTo(targetLoc);
        if( dist < type.sensorRadius && edgesOfSpotAreOffMap(targetLoc, RobotType.GARDENER.bodyRadius)){
            Message.GARDENER_BUILD_LOCS.removeLocation(targetLoc);
        }

        if(
        (dist < type.sensorRadius -.001 && (!rc.onTheMap(targetLoc))) || rc.senseRobotAtLocation(targetLoc) != null && rc.senseRobotAtLocation(targetLoc).type == RobotType.GARDENER 
        		|| isCircleOccupiedByTree(targetLoc, 2) || (!rc.onTheMap(here.add(here.directionTo(targetLoc), (float)(dist + (type.sensorRadius -.001 - dist < 2 ? type.sensorRadius -.001 - dist : 2))))
                && Message.GARDENER_BUILD_LOCS.getLength() > 1)){
            if(isCircleOccupiedByTree(targetLoc, 2)){
            	if(nearbyNeutralTrees.length > 0)
            		Message.CLEAR_TREES_PLEASE.addLocation(targetLoc);
            }
        	turnsIHaveBeenTrying = 0;
            Message.GARDENER_BUILD_LOCS.removeLocation(targetLoc);
            return true;
        }
        return false;

    }

    public void updateLocs() throws GameActionException{
        for(int i = 0; i < 6 ; i++){
        	Direction dir = new Direction((float) (Math.PI/3 * i));
        	MapLocation loc = here.add(dir, (float) (type.sensorRadius - .1));
        	if(rc.onTheMap(loc) && !rc.isLocationOccupiedByTree(loc)){
        		Message.GARDENER_BUILD_LOCS.addLocation(here.add(dir, (float) 8.5));
        	}
        	else if(rc.isLocationOccupiedByTree(loc) && nearbyNeutralTrees.length > 0){
        		Message.CLEAR_TREES_PLEASE.addLocation(loc);
        	}
        }
    }

    public void takeTurn() throws GameActionException {
    	/*
        if(debug){
            if(tankBuilder)
                System.out.println("tank builder");
            System.out.println("dtc = " + here.distanceTo(MapAnalysis.center));
            System.out.println("rb = " + Message.DIST_TO_CENTER.getFloatValue());
        }
        if(tankBuilder && Math.abs(here.distanceTo(MapAnalysis.center) - Message.DIST_TO_CENTER.getFloatValue()) > 0.1){
            tankBuilder = false;
            if(debug)System.out.println("not tank builder");
        }
        if(tankBuilder && rc.getHealth() < 9){//announce my death
            Message.DIST_TO_CENTER.setValue((float)(999));
        }
        if(tankBuilder && turnsTank > 25){
            tankBuilder = false;
            Message.DIST_TO_CENTER.setValue((float)(999));
        }*/
        if(debug){System.out.println("Started with " +Clock.getBytecodeNum());}
        waterLowestHealthTree();
        if (nearbyEnemyRobots.length > 0) {
            //System.out.println("sent target d");
        	if(nearbyEnemyRobots[0].type != RobotType.ARCHON && nearbyEnemyRobots[0].type != RobotType.GARDENER && nearbyEnemyRobots[0].type != RobotType.SCOUT)
        		Message.DISTRESS_SIGNALS.addLocation(nearbyEnemyRobots[0].location);
        }
        if (isExploring) {
            if(debug){System.out.println("Am exploring" +Clock.getBytecodeNum());}

            myPatience++;

            if(targetLoc == null || (here.distanceTo(targetLoc) - dLastTurn < -.5)) {
                targetLoc = Message.GARDENER_BUILD_LOCS.getClosestLocation(here);
            }

            if(myPatienceIsUp(targetLoc)){
                if(debug){System.out.println("Patience Up " +Clock.getBytecodeNum());}
                if(notTerribleSpot()){
                    //just sit down
                    isExploring = false;

                } else {
                    grabAnOpenSpot();
                }
                if(debug){System.out.println("End of Patience up " +Clock.getBytecodeNum());}

            }

            if(debug){System.out.println("Finding new loc" +Clock.getBytecodeNum());}

            while (isBadLocation(targetLoc)) {

                System.out.println(isBadLocation(targetLoc));
                targetLoc = Message.GARDENER_BUILD_LOCS.getClosestLocation(here);
            }

            if(debug){System.out.println("end of finding new loc" +Clock.getBytecodeNum());}


            if (targetLoc == null) {
                if(debug){System.out.println("No good loc " +Clock.getBytecodeNum());}


                if (dirIAmMoving == null || myRand.nextDouble() < .5
                        + (double) (-rc.getRoundNum()) / (double) (2 * rc.getRoundLimit())) {
                    dirIAmMoving = findOpenSpaces();
                }
                if(!rc.hasMoved()) {
                    goTo(dirIAmMoving);
                }
                boolean farAway = true;
                for (RobotInfo r : nearbyAlliedRobots) {
                    if (r.type == RobotType.GARDENER || r.type == RobotType.ARCHON) { //shouldnt be hard set
                        farAway = false;
                        break;
                    }
                }
                isExploring = !farAway;

            } else {
                if(debug)rc.setIndicatorLine(here, targetLoc, (us == Team.A ? 255: 0), (us == Team.A ? 0: 255), 0);
                if(debug){System.out.println("going to target");}
                if(debug){System.out.println(here.distanceTo(targetLoc));}

                goToDangerous(targetLoc);
                if (here.distanceTo(targetLoc) < .5) {
                    if(debug){System.out.println("done exploring");}
                    isExploring = false;
                    Message.GARDENER_BUILD_LOCS.removeLocation(here);
                }
                if(here.distanceTo(targetLoc) > 20){
                    boolean farAway = true;
                    for (RobotInfo r : nearbyAlliedRobots) {
                        if (r.type == RobotType.GARDENER || r.type == RobotType.ARCHON) {
                            farAway = false;
                            break;
                        }
                    }
                    isExploring = !farAway;
                }

                dLastTurn = here.distanceTo(targetLoc);

            }
            if (Message.NUM_GARDENERS.getValue() == 1) {
                isExploring = false;
            }
        }
        if(debug){System.out.println("almost done " +Clock.getBytecodeNum());}

        if (!isExploring
                || nearbyEnemyRobots.length > 0) {
            buildSomething();
        }
        if(!isExploring && (!updatedLocs || (rc.getRoundNum() + rc.getID()) % 100 == 0)){
            //this should check if we're in a decent spot
        	if(debug)System.out.println("here");
            updateLocs();
            updatedLocs = true;
        }
    }


	public void buildSomething() throws GameActionException {
		int typeToBuild = Message.GARDENER_BUILD_ORDERS.getValue();
		int myGenetics = Message.GENETICS.getValue();
		int myAdaptation = Message.ADAPTATION.getValue();
		/*
		if(rc.getTeamBullets() > 500 && tankBuilder && (MapAnalysis.numTank + 1) * 4 < MapAnalysis.numSoldier){
			if(buildRobot(RobotType.TANK, true)){
				return;
			}
		}*/
		if (nearbyEnemyRobots.length == 0  && roundNum > 5 && (rc.readBroadcast(15) == 0 || roundNum < 40 && MapAnalysis.conflictDist > 10 * rc.getTreeCount()) && plantATree())
			return;
		else if (rc.getBuildCooldownTurns() == 0 && (rc.readBroadcast(15) > 0)) {
			if(myAdaptation != MapAnalysis.DEFEND_SOMETHING && ((!canPlantTree() && rc.senseNearbyTrees(2, us).length < 3 && roundNum < 50) || (calcTrappedInHeuristic() > 7 + 2 * numLumberjacksInSightRadius() && myGenetics != MapAnalysis.RUSH_VP))){
				System.out.println("trying to build lumberjack");
				if (buildRobot(RobotType.LUMBERJACK, false)) {
					return;
				}
			}
			else{
				switch (typeToBuild) {
				case 0:
					break;
				case 1:
					if (buildRobot(RobotType.SOLDIER, true)) {
						return;
					}
					break;
				case 2:
					/*
					if(tankBuilder && debug)
						System.out.println("trying to build tank");
					if (tankBuilder && buildRobot(RobotType.TANK, true)) {
						return;
					}
					else if(tankBuilder && rc.getTeamBullets() > 300){
						turnsTank++;
					}*/
					break;
				case 3:
					if (buildRobot(RobotType.SCOUT, true)) {
						return;
					}
					break;
				case 4:
					if (buildRobot(RobotType.LUMBERJACK, true)) {
						return;
					}
					break;
				case 5:
					break;
				}
			}
		}
		else if(rc.getBuildCooldownTurns() == 0 && nearbyEnemyRobots.length > 0 && (nearbyEnemyRobots.length != 1 || nearbyEnemyRobots[0].type != RobotType.ARCHON)){
			buildRobot(RobotType.SOLDIER, false);
		}
	}
	
	private float calcTrappedInHeuristic() {
		float ret = 0;
		for(TreeInfo t: nearbyNeutralTrees){
			float dist = here.distanceTo(t.location);
			ret += (float)(t.radius * (type.sensorRadius - dist));
		}
		if(debug)System.out.println("trapped heuristic = " + ret);
		return ret;
	}

	private int numLumberjacksInSightRadius() {
		int ret = 0;
		for(RobotInfo a: nearbyAlliedRobots){
			ret += (a.type == RobotType.LUMBERJACK ? 1 : 0);
		}
		return ret;
	}
	
	/**
     * Dont use broadcasted locations.
     */
    private void grabAnOpenSpot() throws GameActionException {
        targetLoc = Message.GARDENER_BUILD_LOCS.getClosestLocation(here);

        if(nearbyAlliedTrees.length >= 1) {
            goTo(here.directionTo(nearbyAlliedTrees[0].location).opposite());
        } else if(targetLoc != null){
            goTo(targetLoc);
        }
//        if(nearbyAlliedRobots.length >= 1) {
//            goTo(here.directionTo(nearbyAlliedRobots[0].location).opposite());
//        }
    }

    /**
     * rough metric for a passable spot
     * @return Boolean true if the spot is decent
     */
    private boolean notTerribleSpot() {
        return (nearbyAlliedTrees.length == 0 || here.distanceTo(nearbyAlliedTrees[0].location) > 3);
    }
    
    /**
     * rough method for checking how long weve been waiting
     * @param targetLoc
     * @return
     */
    private boolean myPatienceIsUp(MapLocation targetLoc) {
        if(targetLoc == null) {return myPatience > 150;}
        return myPatience > 200 && here.distanceTo(targetLoc) > 5;
    }
	
	public boolean buildRobot(RobotType type, boolean dec) throws GameActionException {
		if (rc.getTeamBullets() < type.bulletCost)
			return false;
		Direction dir = here.directionTo(MapAnalysis.center);
		if(type == RobotType.LUMBERJACK){
			MapLocation closestTree = Message.CLEAR_TREES_PLEASE.getClosestLocation(here);
			if(closestTree != null){
				dir = here.directionTo(closestTree);
			}
			else if(nearbyNeutralTrees.length > 0){
				dir = here.directionTo(nearbyNeutralTrees[0].location);
			}
		}
		if (rc.canBuildRobot(type, dir)) {
			rc.buildRobot(type, dir);
			if(dec)
				rc.broadcast(15, rc.readBroadcast(15) - 1);
			switch (type) {
			case SOLDIER:
				Message.NUM_SOLDIERS.setValue(Message.NUM_SOLDIERS.getValue() + 1);
				break;
			case TANK:
				Message.NUM_TANKS.setValue(Message.NUM_TANKS.getValue() + 1);
				break;
			case SCOUT:
				Message.NUM_SCOUTS.setValue(Message.NUM_SCOUTS.getValue() + 1);
				break;
			case LUMBERJACK:
				Message.NUM_LUMBERJACKS.setValue(Message.NUM_LUMBERJACKS.getValue() + 1);
				break;
			default:
				break;
			}
			return true;
		}
		int dirsToCheck = 72;
		Direction left = dir.rotateLeftDegrees(360/dirsToCheck);
		Direction right = dir.rotateRightDegrees(360/dirsToCheck);
		for (int i = dirsToCheck; i-- > 0;) {
			if (rc.canBuildRobot(type, left)) {
				rc.buildRobot(type, left);
				if(dec)
					rc.broadcast(15, rc.readBroadcast(15) - 1);
				switch (type) {
				case SOLDIER:
					Message.NUM_SOLDIERS.setValue(Message.NUM_SOLDIERS.getValue() + 1);
					break;
				case TANK:
					Message.NUM_TANKS.setValue(Message.NUM_TANKS.getValue() + 1);
					break;
				case SCOUT:
					Message.NUM_SCOUTS.setValue(Message.NUM_SCOUTS.getValue() + 1);
					break;
				case LUMBERJACK:
					Message.NUM_LUMBERJACKS.setValue(Message.NUM_LUMBERJACKS.getValue() + 1);
					break;
				default:
					break;
				}
				return true;
			}
			left = left.rotateLeftDegrees(360/dirsToCheck);
			right = right.rotateRightDegrees(360/dirsToCheck);
		}
		return false;
	}

	public boolean plantATree() throws GameActionException {
		Direction dir = here.directionTo(MapAnalysis.center);
		Boolean skipped = false;
		for (int i = 36; i-- > 0;) {
			if (rc.canPlantTree(dir)) {
				if (skipped) {
					rc.plantTree(dir);
					return true;
				} else {
					skipped = true;
					dir = dir.rotateLeftDegrees(60);
					i -= 6;
				}
			}
			dir = dir.rotateLeftDegrees(10);
		}
		return false;
	}
	
	public boolean canPlantTree() throws GameActionException {
		if(rc.getTeamBullets() < GameConstants.BULLET_TREE_COST){
			return false;
		}
		Direction dir = here.directionTo(MapAnalysis.center);
		Boolean skipped = false;
		for (int i = 36; i-- > 0;) {
			if (rc.canPlantTree(dir)) {
				if (skipped) {
					return true;
				} else {
					skipped = true;
					dir = dir.rotateLeftDegrees(60);
					i -= 6;
				}
			}
			dir = dir.rotateLeftDegrees(10);
		}
		if (!trapped) {
			trapped = true;
			Message.GARDENER_TRAPPED_NUM.setValue(Message.GARDENER_TRAPPED_NUM.getValue() + 1);
		}
		return false;
	}

    public void waterLowestHealthTree() throws GameActionException {
        TreeInfo[] treesToWater = nearbyAlliedTrees;
        TreeInfo treeToHeal = Util.leastHealth(treesToWater, true);
        if (treeToHeal != null) {
            rc.water(treeToHeal.getID());
        }
    }
}