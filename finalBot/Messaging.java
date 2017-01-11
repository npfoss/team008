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
	
	public void updateNeutralUnitTreeLocation(MapLocation loc) throws GameActionException{
		int index = rc.readBroadcast(50);
		int x = (int) (loc.x * 10);
		int y = (int) (loc.y*10);
		rc.broadcast(index+1, (x*6000 + y));
		rc.broadcast(50, index+1);
	}
	public void updateNeutralTreeLocation(MapLocation loc) throws GameActionException{
		int index = rc.readBroadcast(100);
		int x = (int) (loc.x * 10);
		int y = (int) (loc.y*10);
		rc.broadcast(index+1, (x*6000 + y));
		rc.broadcast(100, index+1);
	}
	public void updateEnemyTreeLocation(MapLocation loc) throws GameActionException{
		int index = rc.readBroadcast(200);
		int x = (int) (loc.x * 10);
		int y = (int) (loc.y*10);
		rc.broadcast(index+1, (x*6000 + y));
		rc.broadcast(200, index+1);
	}
	public void updateEnemyArmyLocation(MapLocation loc) throws GameActionException{
		int index = rc.readBroadcast(300);
		int x = (int) (loc.x * 10);
		int y = (int) (loc.y*10);
		rc.broadcast(index+1, (x*6000 + y));
		rc.broadcast(300, index+1);
	}
	public void updateEnemyUnitLocation(MapLocation loc) throws GameActionException{
		int index = rc.readBroadcast(400);
		int x = (int) (loc.x * 10);
		int y = (int) (loc.y*10);
		rc.broadcast(index+1, (x*6000 + y));
		rc.broadcast(400, index+1);
	}
	
}