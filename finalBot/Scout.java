package team008.finalBot;

import battlecode.common.*;

public class Scout extends Bot {

	public Scout(RobotController r){
		super(r);
	}
	
	public void takeTurn(TreeInfo[] nearbyNeutralTrees) throws Exception{
	   if(dealWithNearbyTrees()){
		   return;
	   }  
	   explore();
       return;
	}
	
	public static boolean dealWithNearbyTrees() throws GameActionException{
		TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
		TreeInfo[] bulletTrees = new TreeInfo[trees.length];
		int i = 0;
		for(TreeInfo tree: trees){
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