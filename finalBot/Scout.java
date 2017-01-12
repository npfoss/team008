package team008.finalBot;

import battlecode.common.*;

public class Scout extends Bot {

	public Scout(RobotController r){
		super(r);
	}
	
	public void takeTurn() throws Exception{
		nearbyAlliedRobots = rc.senseNearbyRobots(-1, us);
	   if(dealWithNearbyTrees()){
		   return;
	   }  
	   explore();
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
		if(rc.canShake(closestBulletTree.ID)){
			rc.shake(closestBulletTree.ID);
		}
		else{
			goTo(closestBulletTree.location);
		}
		return true;
	}
}