package team008.finalBot;

import battlecode.common.*;

public class Messaging extends Bot{
	
	public void updateMinX(int min) throws GameActionException{
		rc.broadcast(0, min*10);
		
	}
	public float getMinX() throws GameActionException{
		return (float) (rc.readBroadcast(0)/10.0);
	}
	public void updateMaxX(int max) throws GameActionException{
		rc.broadcast(1, max*10);
		
	}
	public float getMaxX() throws GameActionException{
		return (float) (rc.readBroadcast(1)/10.0);
	}
	public void updateMinY(int min) throws GameActionException{
		rc.broadcast(2, min*10);
		
	}
	public float getMinY() throws GameActionException{
		return (float) (rc.readBroadcast(2)/10.0);
	}
	public void updateMaxY(int max) throws GameActionException{
		rc.broadcast(3, max*10);
		
	}
	public float getMaxY() throws GameActionException{
		return (float) (rc.readBroadcast(3)/10.0);
	}

	
	public static boolean duplicateInRange(MapLocation loc, int start, int end) throws GameActionException{
		for(int i = start; i <= end; i++){
			int code = rc.readBroadcast(i);
			if(code == 0)
				continue;
			MapLocation decoded = new MapLocation((float)((code/6000)/10.0),(float)((code % 6000)/10.0));
			if(decoded != null && loc.distanceTo(decoded) < 10){
				return true;
			}
		}
		return false;
	}

	public static void setStrategy(int strat) throws GameActionException{
		rc.broadcast(4, strat);
	}
	public static int getStrategy() throws GameActionException{
		return rc.readBroadcast(4);
	}
	public void updateNeutralUnitTreeLocation(MapLocation loc) throws GameActionException{
		int index = rc.readBroadcast(50);
		if(duplicateInRange(loc,51,50+index))
			return;
		int x = (int) (loc.x * 10);
		int y = (int) (loc.y*10);
		rc.broadcast(50 + index + 1, (x*6000 + y));
		rc.broadcast(50, index+1);
	}
	
	public MapLocation getClosestNeutralUnitTreeLocation(MapLocation toHere) throws GameActionException{
		MapLocation ret = null;
		float dist = 999999;
		for(int i = 1; i <= rc.readBroadcast(50); i++){
			int code = rc.readBroadcast(50 + i);
			if(code == 0){
				continue;
			}
			MapLocation decoded = new MapLocation((float)((code/6000)/10.0),(float)((code % 6000)/10.0));
			if(decoded != null && toHere.distanceTo(decoded) < dist){
				dist = toHere.distanceTo(decoded);
				ret = decoded;
			}
		}
		return ret;
	}
	
	public boolean removeNeutralUnitTreeLocation(MapLocation loc) throws GameActionException{
		int code = (int)(loc.x*10)*6000 + (int)(loc.y*10);
		for(int i = 1; i <= rc.readBroadcast(50); i++){
			if(rc.readBroadcast(50 + i) == code){
				rc.broadcast(50 + i, 0);
				return true;
			}
		}
		return false;
	}
	
	public static void updateNeutralTreeLocation(MapLocation loc) throws GameActionException{
		int index = rc.readBroadcast(100);
		if(duplicateInRange(loc,101,100+index))
			return;
		int x = (int) (loc.x * 10);
		int y = (int) (loc.y*10);
		rc.broadcast(100 + index + 1, (x*6000 + y));
		rc.broadcast(100, index+1);
	}
	
	public MapLocation getClosestNeutralTreeLocation(MapLocation toHere) throws GameActionException{
		MapLocation ret = null;
		float dist = 999999;
		for(int i = 1; i <= rc.readBroadcast(100); i++){
			int code = rc.readBroadcast(100 + i);
			if(code == 0){
				continue;
			}
			MapLocation decoded = new MapLocation((float)((code/6000)/10.0),(float)((code % 6000)/10.0));
			if(decoded != null && toHere.distanceTo(decoded) < dist){
				dist = toHere.distanceTo(decoded);
				ret = decoded;
			}
		}
		return ret;
	}
	
	public boolean removeNeutralTreeLocation(MapLocation loc) throws GameActionException{
		int code = (int)(loc.x*10)*6000 + (int)(loc.y*10);
		for(int i = 1; i <= rc.readBroadcast(100); i++){
			if(rc.readBroadcast(100 + i) == code){
				rc.broadcast(100 + i, 0);
				return true;
			}
		}
		return false;
	}
	
	public static void updateEnemyTreeLocation(MapLocation loc) throws GameActionException{
		int index = rc.readBroadcast(200);
		if(duplicateInRange(loc,201,200+index))
			return;
		int x = (int) (loc.x * 10);
		int y = (int) (loc.y*10);
		rc.broadcast(200 + index + 1, (x*6000 + y));
		rc.broadcast(200, index+1);
	}
	
	public MapLocation getClosestEnemyTreeLocation(MapLocation toHere) throws GameActionException{
		MapLocation ret = null;
		float dist = 999999;
		for(int i = 1; i <= rc.readBroadcast(200); i++){
			int code = rc.readBroadcast(200 + i);
			if(code == 0){
				continue;
			}
			MapLocation decoded = new MapLocation((float)((code/6000)/10.0),(float)((code % 6000)/10.0));
			if(decoded != null && toHere.distanceTo(decoded) < dist){
				dist = toHere.distanceTo(decoded);
				ret = decoded;
			}
		}
		return ret;
	}
	
	public boolean removeEnemyTreeLocation(MapLocation loc) throws GameActionException{
		int code = (int)(loc.x*10)*6000 + (int)(loc.y*10);
		for(int i = 1; i <= rc.readBroadcast(200); i++){
			if(rc.readBroadcast(200 + i) == code){
				rc.broadcast(200 + i, 0);
				return true;
			}
		}
		return false;
	}
	
	public static void updateEnemyArmyLocation(MapLocation loc) throws GameActionException{
		int index = rc.readBroadcast(300);
		if(duplicateInRange(loc,301,300+index))
			return;
		int x = (int) (loc.x * 10);
		int y = (int) (loc.y* 10);
		rc.broadcast(300 + index + 1, (x*6000 + y));
		rc.broadcast(300, index+1);
	}
	
	public static MapLocation getClosestEnemyArmyLocation(MapLocation toHere) throws GameActionException{
		MapLocation ret = null;
		float dist = 999999;
		for(int i = 1; i <= rc.readBroadcast(300); i++){
			int code = rc.readBroadcast(300 + i);
			if(code == 0){
				continue;
			}
			MapLocation decoded = new MapLocation((float)((code/6000)/10.0),(float)((code % 6000)/10.0));
			if(decoded != null && toHere.distanceTo(decoded) < dist){
				dist = toHere.distanceTo(decoded);
				ret = decoded;
			}
		}
		return ret;
	}
	
	public static boolean removeEnemyArmyLocation(MapLocation loc) throws GameActionException{
		int code = (int)(loc.x*10)*6000 + (int)(loc.y*10);
		for(int i = 1; i <= rc.readBroadcast(300); i++){
			if(rc.readBroadcast(300 + i) == code){
				rc.broadcast(300 + i, 0);
				return true;
			}
		}
		return false;
	}
	
	public static void updateEnemyUnitLocation(MapLocation loc) throws GameActionException{
		int index = rc.readBroadcast(700);
		if(duplicateInRange(loc,701,700+index))
			return;
		int x = (int) (loc.x * 10);
		int y = (int) (loc.y * 10);
		rc.broadcast(700 + index + 1, (x*6000 + y));
		rc.broadcast(700, index+1);
	}
	
	public static MapLocation getClosestEnemyUnitLocation(MapLocation toHere) throws GameActionException{
		MapLocation ret = null;
		float dist = 999999;
		for(int i = 1; i <= rc.readBroadcast(700); i++){
			int code = rc.readBroadcast(700 + i);
			if(code == 0){
				continue;
			}
			MapLocation decoded = new MapLocation((float)((code/6000)/10.0),(float)((code % 6000)/10.0));
			if(decoded != null && toHere.distanceTo(decoded) < dist){
				dist = toHere.distanceTo(decoded);
				ret = decoded;
			}
		}
		return ret;
	}
	
	public static boolean removeEnemyUnitLocation(MapLocation loc) throws GameActionException{
		int code = (int)(loc.x*10)*6000 + (int)(loc.y*10);
		for(int i = 1; i <= rc.readBroadcast(700); i++){
			if(rc.readBroadcast(700 + i) == code){
				rc.broadcast(700 + i, 0);
				return true;
			}
		}
		return false;
	}
	
}