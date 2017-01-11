package team008.finalBot;

import battlecode.common.*;

public class Util extends Bot {
	/**
	 * Returns a random Direction
	 * 
	 * @return a random Direction
	 */
	public static Direction randomDirection() {
		return new Direction((float) Math.random() * 2 * (float) Math.PI);
	}
	
	public static void notifyFriendsOfEnemies(RobotInfo[] enemies) throws GameActionException{
		if(enemies.length == 1){
			Messaging.updateEnemyUnitLocation(enemies[0].location);
		}
		else if (enemies.length > 1){
			Messaging.updateEnemyArmyLocation(Util.centroidOfUnits(enemies));
		}
	}

    public static RobotInfo closestRobot(RobotInfo[] robots, MapLocation toHere) {
	        RobotInfo closest = null;
	        float bestDist = 999999;
	        float dist;
	        for (int i = robots.length; i-- > 0;) {
	            dist = toHere.distanceTo(robots[i].location);
	            if (dist < bestDist) {
	                bestDist = dist;
	                closest = robots[i];
	            }
	        }
	        return closest;
	    }
	public static TreeInfo closestTree(TreeInfo[] trees, MapLocation toHere) {
		TreeInfo closest = null;
		float bestDist = 999999;
		float dist;
		for (int i = trees.length; i-- > 0;) {
			dist = toHere.distanceTo(trees[i].location);
			if (dist < bestDist) {
				bestDist = dist;
				closest = trees[i];
			}
		}
		return closest;
	}
	   
	   public static MapLocation closestLocation(MapLocation[] locs, MapLocation toHere) {
	        float bestDist = 999999;
	        float dist;
            MapLocation bestLoc = null;
            for (MapLocation loc : locs) {
	        	if(loc == null){
	        		continue;
	        	}
	            dist = toHere.distanceTo(loc);
	            if (dist < bestDist) {
	                bestDist = dist;
                    bestLoc = loc;
	            }
	        }
	        return bestLoc;
	    }
	   
	   public static RobotInfo leastHealth(RobotInfo[] robots, boolean excludeArchons) {
			RobotInfo ret = null;
			double minHealth = 99999;
			for (int i = 0; i < robots.length; i++) {
				if (robots[i].health < minHealth && ( !excludeArchons || robots[i].type != RobotType.ARCHON)) {
					minHealth = robots[i].health;
					ret = robots[i];
				}
			}
			return ret;
		}
	   public static TreeInfo leastHealth(TreeInfo[] trees, boolean canWater) {
			TreeInfo ret = null;
			double minHealth = 1e99;
			for (int i = 0; i < trees.length; i++) {
                if ((!canWater || rc.canWater(trees[i].ID)) && trees[i].health < minHealth) {
                    minHealth = trees[i].health;
                    ret = trees[i];
                }
            }
        return ret;
	}

	public static TreeInfo closestTree(TreeInfo[] robots, MapLocation toHere, int size) {
		TreeInfo closest = null;
		float bestDist = 999999;
		float dist;
		for (int i = 0; i < size; i++) {
			dist = toHere.distanceTo(robots[i].location);
			if (dist < bestDist) {
				bestDist = dist;
				closest = robots[i];
			}
		}
		return closest;
	}

	public static RobotInfo[] combineTwoRIArrays(RobotInfo[] array1, RobotInfo[] array2) {
		RobotInfo[] combo = new RobotInfo[array1.length + array2.length];
		for (int i = 0; i < array1.length; i++) {
			combo[i] = array1[i];
		}
		for (int i = 0; i < array2.length; i++) {
			combo[i + array1.length] = array2[i];
		}
		return combo;
	}

	public static MapLocation centroidOfUnits(RobotInfo[] robots) {
		float xavg = 0, yavg = 0;
		MapLocation loc;
		for (RobotInfo bot : robots) {
			loc = bot.location;
			xavg += loc.x;
			yavg += loc.y;
		}
		return new MapLocation(xavg / robots.length, yavg / robots.length);
	}

	public static boolean containsMapLocation(MapLocation[] locs, MapLocation location, int size) {
		for (int i = 0; i < size; i++) {
			if (locs[i] == null) {
				continue;
			}
			if (locs[i].equals(location)) {
				return true;
			}
		}
		return false;
	}

	public static RobotInfo closestSpecificType(RobotInfo[] robots, MapLocation toHere, RobotType type) {
		RobotInfo closest = null;
		float bestDist = 99999;
		float dist;
		for (int i = robots.length; i-- > 0;) {
			if (robots[i].type == type) {
				dist = toHere.distanceTo(robots[i].location);
				if (dist < bestDist) {
					bestDist = dist;
					closest = robots[i];
				}
			}
		}
		return closest;
	}

	public static void removeIndexFromArray(Object[] array, int index, int size) {
		for (int i = index; i < size - 1; i++) {
			array[i] = array[i + 1];
		}
	}

	public static MapLocation getLocationOfType(RobotInfo[] array, RobotType t) {
		for (RobotInfo ri : array)
			if (ri.type == t)
				return ri.location;
		return null;
	}

	public static RobotInfo[] getUnitsOfType(RobotInfo[] array, RobotType t) {
		int[] inds = new int[array.length];
		int s = 0;
		for (int i = array.length; i-- > 0;) {
			if (array[i].type == t) {
				inds[s++] = i;
			}
		}
		RobotInfo[] units = new RobotInfo[s];
		for (int i = s; i-- > 0;) {
			units[i] = array[inds[i]];
		}
		return units;
	}

	public static RobotInfo[] combineThreeRIArrays(RobotInfo[] array1, RobotInfo[] array2, RobotInfo[] array3) {
		RobotInfo[] combo = new RobotInfo[array1.length + array2.length + array3.length];
		for (int i = 0; i < array1.length; i++) {
			combo[i] = array1[i];
		}
		for (int i = 0; i < array2.length; i++) {
			combo[i + array1.length] = array2[i];
		}
		for (int i = 0; i < array3.length; i++) {
			combo[i + array1.length + array2.length] = array3[i];
		}
		return combo;
	}

	public static int indexOfLocation(MapLocation[] array, MapLocation loc) {
		for (int i = array.length; i-- > 0;)
			if (array[i] != null && array[i].equals(loc))
				return i;
		return -1;
	}
}