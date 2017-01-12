package team008.finalBot;

import battlecode.common.*;

public class Scout extends Bot {

	public Scout(RobotController r){
		super(r);
	}
	
	public void takeTurn() throws Exception{
		nearbyAlliedRobots = rc.senseNearbyRobots(-1, us);
	   dealWithNearbyTrees();
	   explore();
	  // rc.setIndicatorDot(here,0,255,0);
	   RobotInfo[] enemies = rc.senseNearbyRobots(-1,rc.getTeam().opponent());
		if(enemies.length > 0 && rc.getRoundNum() % 10 == 0){
			//rc.setIndicatorDot(enemies[0].location, 255, 0, 0);
			Util.notifyFriendsOfEnemies(enemies);
		}
       return;
	}
	
	public static boolean dealWithNearbyTrees() throws GameActionException{
		TreeInfo[] bulletTrees = new TreeInfo[nearbyNeutralTrees.length];
		int i = 0;
		for(TreeInfo tree: nearbyNeutralTrees){
			if(tree.containedBullets > 0){
				bulletTrees[i] = tree;
				i++;
			}
		}
		if(i == 0){
			return false;
		}
		TreeInfo closestBulletTree = Util.closestTree(bulletTrees, rc.getLocation(), i);
		goTo(closestBulletTree.location);
		rc.setIndicatorDot(here,0,0,255);

		return true;
	}
}