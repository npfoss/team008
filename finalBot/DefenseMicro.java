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
        if(couldDamage(enemy)){
        	String shotType = RangedCombat.calculateShotType(enemy);
            RangedCombat.parseShotTypeAndShoot(enemy, shotType);
        }
        //move to either scare away or defend
        Direction bestDefensiveSpot = calculateBestDefensiveDirection(enemy,friend);
        if(enemy.type == RobotType.SCOUT){
        	tryMoveDirectionDangerous(bestDefensiveSpot);
        }
        else{
        	tryMoveDirection(bestDefensiveSpot, true);
        }
    }
    
    public static void defendL(RobotInfo friend) throws GameActionException{
    	RobotInfo enemy = findMostThreateningTarget(friend);
        //if we can damage or fire a scary shoot do so
        if(couldDamageL(enemy)){
        	rc.strike();
        }
        //move to either scare away or defend
        Direction bestDefensiveSpot = calculateBestDefensiveDirection(enemy,friend);
        if(enemy.type == RobotType.SCOUT){
        	tryMoveDirectionDangerous(bestDefensiveSpot);
        }
    }
    
    private static boolean couldDamageL(RobotInfo attack){
    	return here.distanceTo(attack.location) < 2;
    }

    private static RobotInfo findMostThreateningTarget(RobotInfo friend) {
        return Util.closestRobot(nearbyEnemyRobots,friend.getLocation());
    }
    private static  boolean couldDamage(RobotInfo attack){
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
