package team008.aaron_shit_poop;

import battlecode.common.*;

public class Util extends Bot {
	   public static RobotInfo closest(RobotInfo[] robots, MapLocation toHere) {
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

	public static int closestLocation(MapLocation[] locs, MapLocation toHere, int size) {
	        float bestDist = 999999;
	        float dist;
	        int bestIndex = -1;
	        for (int i = 0; i < size; i++) {
	        	if(locs[i] == null){
	        		continue;
	        	}
	            dist = toHere.distanceTo(locs[i]);
	            if (dist < bestDist) {
	                bestDist = dist;
	                bestIndex = i;
	            }
	        }
	        return bestIndex;
	    }
	   
	   public static RobotInfo leastHealth(RobotInfo[] robots, int excludeArchons) {
			RobotInfo ret = null;
			double minHealth = 1e99;
			for (int i = 0; i < robots.length; i++) {
				if (robots[i].health < minHealth && (excludeArchons == 0 || robots[i].type != RobotType.ARCHON)) {
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
	   public static RobotInfo[] combineTwoRIArrays( RobotInfo[] array1, RobotInfo[] array2){
	    	RobotInfo[] combo = new RobotInfo[array1.length + array2.length];
	    	for (int i = 0; i < array1.length; i++){
				combo[i] = array1[i];
			}
	    	for (int i = 0; i < array2.length; i++){
				combo[i + array1.length] = array2[i];
			}
	    	return combo;
	    }
	    
	    public static RobotInfo[] combineTwoRIArrays( RobotInfo[] array1, int a1size, RobotInfo[] array2){
	    	RobotInfo[] combo = new RobotInfo[a1size + array2.length];
	    	for (int i = 0; i < a1size; i++){
				combo[i] = array1[i];
			}
	    	for (int i = 0; i < array2.length; i++){
				combo[i + a1size] = array2[i];
			}
	    	return combo;
	    }
	    
	    public static RobotInfo[] combineTwoRIArrays( RobotInfo[] array1, RobotInfo[] array2, int a2size){
	    	RobotInfo[] combo = new RobotInfo[array1.length + a2size];
	    	for (int i = 0; i < array1.length; i++){
				combo[i] = array1[i];
			}
	    	for (int i = 0; i < a2size; i++){
				combo[i + array1.length] = array2[i];
			}
	    	return combo;
	    }
	    
	    public static MapLocation centroidOfUnits(RobotInfo[] robots){
			int xavg = 0, yavg = 0;
			MapLocation loc;
			for(int i = 0; i < robots.length; i++){
				loc = robots[i].location;
				xavg += loc.x;
				yavg += loc.y;
			}
			return new MapLocation(Math.round(xavg/robots.length), Math.round(yavg/robots.length));
	    }
	    
	    public static boolean containsMapLocation(MapLocation[] locs, MapLocation location, int size) {
	    	for(int i = 0; i < size; i++){
				MapLocation loc = locs[i];
				if(locs[i] == null){
					continue;
				}
				if(loc.equals(location)){
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
	            dist = toHere.distanceTo(robots[i].location);
	            if (dist < bestDist && robots[i].type == type) {
	                bestDist = dist;
	                closest = robots[i];
	            }
	        }
	        return closest;
		}
	    
	    public static void removeIndexFromArray(Object[] array, int index, int size){
			for(int i = index; i < size - 1; i++){
				array[i] = array[i+1];
			}
		}
	    
	    public static RobotInfo[] getUnitsOfType(RobotInfo[] array, RobotType t) {
			int[] inds = new int[array.length];
			int s = 0;
			for(int i = array.length; i --> 0; ){
				if(array[i].type == t){
					inds[s++] = i;
				}
			}
			RobotInfo[] units = new RobotInfo[s];
			for(int i = s ; i --> 0 ; ){
				units[i] = array[inds[i]]; 
			}
			return units;
		}
	    
	    public static RobotInfo[] combineThreeRIArrays( RobotInfo[] array1, int a1size, RobotInfo[] array2, RobotInfo[] array3){
	    	RobotInfo[] combo = new RobotInfo[a1size + array2.length + array3.length];
	    	for (int i = 0; i < a1size; i++){
				combo[i] = array1[i];
			}
	    	for (int i = 0; i < array2.length; i++){
				combo[i + a1size] = array2[i];
			}
	    	for (int i = 0; i < array3.length; i++){
				combo[i + a1size + array2.length] = array3[i];
			}
	    	return combo;
	    }
	    
	    public static MapLocation getLocationOfType(RobotInfo[] array, RobotType t){
			for(RobotInfo ri : array)
				if(ri.type == t)
					return ri.location;
			return null;
		}
	    
	    public static int indexOfLocation(MapLocation[] array, int arraySize, MapLocation loc){
			for(int i = arraySize; i --> 0; )
				if(array[i] != null && array[i].equals(loc))
					return i;
			return -1;
		}
}