package team008.finalBot;
import battlecode.common.*;


/**
 * Created by jmac on 1/16/17.
 */
public class DefenseMicro extends Bot{
private static final int loopLimit = 8;
    /**
     * This is effectively "domicro"
     */
    public static void defend(RobotInfo friend) throws GameActionException {
        //find the most threatening target to us
        RobotInfo enemy = findMostThreateningTarget(friend);
        //if we can damage or fire a scary shoot do so
        if(!couldDamage(enemy)){
        	Direction bestDefensiveSpot = calculateBestDefensiveDirection(enemy,friend);
	        if(enemy.type == RobotType.SCOUT){
	        	if(here.distanceTo(enemy.location) > 4){
	        		goTo(enemy.location);
	        	}
	        	else{
	        		tryMoveDirectionDangerous(bestDefensiveSpot, enemy.location);
	        	}
	        }
	        else{
	        	if(here.distanceTo(enemy.location) > 4){
	        		goTo(enemy.location);
	        	}
	        	else{
	        		tryMoveDirection(bestDefensiveSpot, true);
	        	}	
	        }
        }
        if(couldDamage(enemy)){
        	System.out.println("pew pew");
            String shotType = "single shot";
            RangedCombat.parseShotTypeAndShoot(enemy, shotType);
        }
        //move to either scare away or defend
    }
    
    public static void defendL(RobotInfo friend) throws GameActionException{
    	RobotInfo enemy = findMostThreateningTarget(friend);
        //if we can damage or fire a scary shoot do so
    	if(!couldDamageL(enemy)){
    		Direction bestDefensiveSpot = calculateBestDefensiveDirection(enemy,friend);
	        if(enemy.type == RobotType.SCOUT){
	        	tryMoveDirectionDangerous(bestDefensiveSpot, enemy.location.add(enemy.location.directionTo(here),(float)(2.0049)));
	        }
	        else{
	        	tryMoveDirection(bestDefensiveSpot, true);
	        }
    	}
        if(couldDamageL(enemy)){
        	rc.strike();
        }
        //move to either scare away or defend
    }
    
    private static boolean couldDamageL(RobotInfo attack){
    	return here.distanceTo(attack.location) < 3;
    }

    private static RobotInfo findMostThreateningTarget(RobotInfo friend) {
        return Util.closestRobot(nearbyEnemyRobots,friend.getLocation());
    }
    private static  boolean couldDamage(RobotInfo attack) throws GameActionException{
    	if(attack.type == RobotType.SCOUT && rc.canSenseLocation(attack.location) && rc.isLocationOccupiedByTree(attack.location)){
    		if(here.distanceTo(attack.location) < 2.005)
    			return true;
    		return false;
    	}
        float bestShotD = (rc.getType().bodyRadius + rc.getType().bulletSpeed + attack.getType().bodyRadius)*(rc.getType().bodyRadius + rc.getType().bulletSpeed + attack.getType().bodyRadius);
        if(Util.distanceSquaredTo(here,attack.getLocation()) > bestShotD){
            return false;
        }
        return true;

    }
    private static int canWeHitAtLocationHeuristic(RobotInfo robot, MapLocation loc) {
        int score = 50;
        float howFarAwayTheyCanGet = loc.distanceTo(robot.location) - type.bulletSpeed - type.bodyRadius
                - robot.type.bodyRadius + robot.type.strideRadius;
        score -= 25 * howFarAwayTheyCanGet / (nearbyTrees.length + 1);
        score += 10 * nearbyEnemyRobots.length * nearbyEnemyRobots.length;
        return score;
    }

    private static int defensiveHeuristicAtLocation(RobotInfo enemy, RobotInfo friend, MapLocation loc){
        float score = -1 * Util.distanceSquaredTo(loc,enemy.getLocation());
        score-= 1 * Util.distanceSquaredTo(loc,friend.getLocation());
        return (int) score;
    }

    private static Direction calculateBestDefensiveDirection(RobotInfo enemy, RobotInfo friend){
    	if(enemy.location.distanceTo(friend.location) < 5){
    		return here.directionTo(enemy.location);
    	}
    	else{
    		return here.directionTo(friend.location.add(friend.location.directionTo(enemy.location), (float)(3.5)));
    	}
    	/*Direction bestDir = null;
        float bestScore = -999;

        float score;
        Direction dir = new Direction(0);
        for(int i = 0; i < loopLimit ; i++){
            score = canWeHitAtLocationHeuristic(enemy,here.add(dir))
            + defensiveHeuristicAtLocation(enemy,friend,here.add(dir));

            if(score>bestScore){
                bestScore = score;
                bestDir = dir;
            }
            dir.rotateRightDegrees(360/loopLimit);
        }
        return bestDir;*/
    }
}
