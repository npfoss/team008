package team008.finalBot;
import battlecode.common.*;

/**
 * Created by jmac on 1/10/17.
 */
public class RangedCombat extends Bot {
    private static final String MOVE_FIRST = "move first";
    private static final String SHOOT_FIRST = "shoot first";
    private static final String SINGLE_SHOT = "single shot";
    private static final String TRIAD_SHOT = "triad shot";
    private static final String PENTAD_SHOT = "pentad shot";

    //forgive me for I have sinned
    private static String shotType = "";

    /**
     * Effectively "domicro"
     * @throws GameActionException
     */
    public static void execute() throws GameActionException{

        String firstAction = determineFirstAction();
        Direction destinationDir;
        BodyInfo target;

            if(firstAction == MOVE_FIRST){
                //move to destination
                destinationDir = chooseMove();
                goTo(destinationDir);

                //shoot target
                target = chooseTargetAndShotType();
                shootIfWorth(target,shotType);
            } else {
                //shoot target
                target  = chooseTargetAndShotType();
                shootIfWorth(target,shotType);

                //move to destination
                destinationDir = chooseMove();
                goTo(destinationDir);

            }

    }

    /**
     * Determines the first move.
     * @return What the first move should be
     * @throws GameActionException
     */
    private static String determineFirstAction() throws GameActionException{
        //check if our best shot is in the direction we're moving
        return MOVE_FIRST;
    }

    ///////////////////// Movement Micro/////////////////////

    /**
     * Decides what direction to move in.
     * @param robotsInSight
     * @param bulletsInSight
     * @param treesInSight
     * @param alliesICanSee
     * @return The direction to try and move in.
     * @throws GameActionException
     */
    private static Direction chooseMove() throws GameActionException{
        //decide whether to engage
        RobotInfo closestBadGuy = Util.closestRobot(nearbyEnemyRobots,here);
        if(nearbyEnemyRobots.length>0) {
            if(nearbyEnemyRobots.length ==1){
                if(canWin1v1(closestBadGuy)){
                    return here.directionTo(closestBadGuy.location);
                }
            } else {
                //will we have backup?
                if(numOtherAlliesInSightRange(closestBadGuy.location)>nearbyEnemyRobots.length){
                    return here.directionTo(closestBadGuy.location);
                }
                return here.directionTo(closestBadGuy.location).opposite();
            }
        }

        //This should be where signals are read.

        //we dont have an objective so go for trees?
        TreeInfo closestTree = Util.closestTree(nearbyEnemyTrees,here);
        if(nearbyEnemyTrees.length>0){
            return rc.getLocation().directionTo(closestTree.getLocation());
        }

        //TODO: go to where the scout tells them to go
        if(myRand.nextDouble() < .1){
        return here.directionTo(Util.rc.getInitialArchonLocations(enemy)[0]);
        }
        else{
        	return Util.randomDirection();
        }
    }

    /**
     * Checks if we can win the 1v1
     * @param enemy enemy to check against
     * @return true if we can win
     */
    public static boolean canWin1v1(RobotInfo enemy) {
        if (enemy.type == RobotType.ARCHON || enemy.type == RobotType.GARDENER)
            return true;
        int turnsToKillEnemy = (int) (enemy.health / rc.getType().attackPower);
        int turnsForEnemyToKillUs = (int) (rc.getHealth() / enemy.getType().attackPower);
        return turnsToKillEnemy <= turnsForEnemyToKillUs;
    }

    /**
     * finds the number of allies that can also see this loc.
     * @param loc place to check if they can see
     * @param allies all the allies we can see
     * @return number of allies who can see the loc
     */
    public static int numOtherAlliesInSightRange(MapLocation loc) {
        int ret = 0;
        for (RobotInfo ally : nearbyAlliedRobots) {
            if (ally.getType().sensorRadius > loc.distanceTo(ally.location) && (ally.type!=RobotType.ARCHON || ally.type!=RobotType.GARDENER ))
                ret++;
        }
        return ret;
    }



    ///////////////////// Shooting and Target Micro/////////////////////

    /**
     * Picks the target
     * @param robotsInSight
     * @param treesInSight
     * @param alliesNextToMe
     * @param alliesICanSee
     * @return
     * @throws GameActionException
     */
    private static BodyInfo chooseTargetAndShotType() throws GameActionException{
        int score;
        int bestScore = -999999;
        RobotInfo bestRobot = null;
        TreeInfo bestTree = null;
        for(RobotInfo robot: nearbyEnemyRobots){
            //add other factors for choosing best bot
            //value based on num nearby bots including trees
            score = (int) ( -robot.getHealth()
                    + robot.getType().attackPower
                    + numOtherAlliesInSightRange(robot.location) / robot.getHealth());

            if(score > bestScore && isDirectionSafe(robot)){
                bestScore = score;
                bestRobot = robot;
            }
        }

//            if(nearbyEnemyTrees.length>0) {
//                float bestD = 0;
//                float d;
//                for(TreeInfo tree: nearbyEnemyTrees){
//                    d = here.distanceTo(tree.location);
//                    if(d < bestD && isDirectionSafe(tree)) {
//                        bestD = d;
//                        bestTree = tree;
//                    }
//                }
//            }


        shotType = calculateShotType();
        return ( bestRobot!= null ) ? bestRobot:bestTree;
    }

    /**
     * Picks whether to shot 1,3 or 5 bullets.
     * @param robotsInSight
     * @return the shot type
     * @throws GameActionException
     */
    private static String calculateShotType() throws GameActionException{
        //come up with some sort of formula for choosing the kind of shot
        if(nearbyEnemyRobots.length>4){
            return PENTAD_SHOT;
        }
        if(nearbyEnemyRobots.length>2) {
            return TRIAD_SHOT;
        }
        return SINGLE_SHOT;
    }

    /**
     * Determines if it is worth shooting, checks for friendly fire.
     * @param target
     * @param shotType
     * @throws GameActionException
     */
    private static void shootIfWorth(BodyInfo target, String shotType) throws GameActionException{
        switch (shotType){
            case SINGLE_SHOT: shootSingleShot(target);
            case TRIAD_SHOT: shootTriadShot(target);
            case PENTAD_SHOT: shootPentadShot(target);
                default: //do nothing, it isn't worth shooting.
        }
    }

    /**
     * Determines if shooting at a target will cause friendly fire.
     * @param target
     * @param alliesICouldHit
     * @return true if it could also hit a friend
     * @throws GameActionException
     */
    private static boolean isDirectionSafe(RobotInfo target) throws GameActionException{
    	Direction intendedAttackDir = here.directionTo(target.location);
        for(RobotInfo friend: nearbyAlliedRobots){
            if(friend.location.distanceTo(here) < here.distanceTo(target.location)- type.bodyRadius - target.type.bodyRadius && intendedAttackDir.radiansBetween(here.directionTo(friend.location)) < Math.PI/6 ){
                rc.setIndicatorDot(here,1,1,1);
                return false;
            }
        }
        for(TreeInfo friend: nearbyAlliedTrees){
            if(friend.location.distanceTo(here) < here.distanceTo(target.location)- type.bodyRadius - target.type.bodyRadius && intendedAttackDir.radiansBetween(here.directionTo(friend.location)) < Math.PI/6 ){
                rc.setIndicatorDot(here,1,1,1);
                return false;
            }
        }
        return true;
    }



    ///////////////////// These Might Belong in Util/////////////////////
    public static void shootSingleShot(BodyInfo target) throws GameActionException{
        if (rc.canFireSingleShot() && target!=null) {
                rc.fireSingleShot(rc.getLocation().directionTo(target.getLocation()));
        }

    }

    public static void shootTriadShot(BodyInfo target) throws GameActionException{
        if (rc.canFireTriadShot() && target!= null) {
                rc.fireTriadShot(rc.getLocation().directionTo(target.getLocation()));
        }

    }

    public static void shootPentadShot(BodyInfo target) throws GameActionException{
        if (rc.canFirePentadShot() && target!= null) {
                rc.firePentadShot(rc.getLocation().directionTo(target.getLocation()));
        }

    }

}
