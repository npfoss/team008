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
    private static TreeInfo treeTarget;


    public static void execute() throws GameActionException{

        String firstAction = determineFirstAction();
        Direction destinationDir;
        RobotInfo target;
        RobotInfo[] robotsInSight = rc.senseNearbyRobots(-1,enemy);
        RobotInfo[] alliesNextToMe = rc.senseNearbyRobots(rc.getType().bodyRadius+2,us);
        RobotInfo[] alliesICanSee = rc.senseNearbyRobots(-1,us);
        BulletInfo[] bulletsInSight = rc.senseNearbyBullets();
        TreeInfo[] treesInSight = rc.senseNearbyTrees(-1,enemy);


            if(firstAction == MOVE_FIRST){
                //move to destination
                destinationDir = chooseMove(robotsInSight,bulletsInSight,treesInSight,alliesICanSee);
                tryMoveDirection(destinationDir);

                //shoot target
                target = chooseTargetAndShotType(robotsInSight,treesInSight,alliesNextToMe,alliesICanSee);
                shootIfWorth(target,treeTarget,shotType);
            } else {
                //shoot target
                target  = chooseTargetAndShotType(robotsInSight, treesInSight,alliesNextToMe,alliesICanSee);
                shootIfWorth(target,treeTarget,shotType);

                //move to destination
                destinationDir = chooseMove(robotsInSight, bulletsInSight, treesInSight, alliesICanSee);
                tryMoveDirection(destinationDir);

            }

    }

    private static String determineFirstAction() throws GameActionException{
        //check if our best shot is in the direction we're moving
        return MOVE_FIRST;
    }

    ///////////////////// Movement Micro/////////////////////
    private static Direction chooseMove(RobotInfo[] robotsInSight, BulletInfo[] bulletsInSight, TreeInfo[] treesInSight, RobotInfo[] alliesICanSee) throws GameActionException{
        //decide whether to engage
        RobotInfo closestBadGuy = Util.closestRobot(robotsInSight,here);
        if(robotsInSight.length>0) {
            if(robotsInSight.length ==1){
                if(canWin1v1(closestBadGuy)){
                    return here.directionTo(closestBadGuy.location);
                }
            } else {
                //will we have backup?
                if(numOtherAlliesInSightRange(closestBadGuy.location,alliesICanSee)<robotsInSight.length){
                    return here.directionTo(closestBadGuy.location);
                }
                return here.directionTo(closestBadGuy.location).opposite();
            }
        }

        //This should be where signals are read.

        //we dont have an objective so go for trees?
        TreeInfo closestTree = Util.closestTree(treesInSight,here);
        if(treesInSight.length>0){
            return rc.getLocation().directionTo(closestTree.getLocation());
        }


        return Util.randomDirection();
    }

    public static boolean canWin1v1(RobotInfo enemy) {
        if (enemy.type == RobotType.ARCHON || enemy.type == RobotType.GARDENER)
            return true;
        int turnsToKillEnemy = (int) (enemy.health / rc.getType().attackPower);
        int turnsForEnemyToKillUs = (int) (rc.getHealth() / enemy.getType().attackPower);
        return turnsToKillEnemy <= turnsForEnemyToKillUs;
    }

    public static int numOtherAlliesInSightRange(MapLocation loc, RobotInfo[] allies) {
        int ret = 0;
        for (RobotInfo ally : allies) {
            if (ally.getType().sensorRadius >= loc.distanceTo(ally.location) && (ally.type!=RobotType.ARCHON || ally.type!=RobotType.GARDENER ))
                ret++;
        }
        return ret;
    }



    ///////////////////// Shooting and Target Micro/////////////////////
    private static RobotInfo chooseTargetAndShotType(RobotInfo[] robotsInSight, TreeInfo[] treesInSight, RobotInfo[] alliesNextToMe, RobotInfo[] alliesICanSee) throws GameActionException{
        int score;
        int bestScore = -999999;
        RobotInfo bestTarget = null;
        for(RobotInfo robot: robotsInSight){
            //add other factors for choosing best bot
            //value based on num nearby bots including trees
            score = (int) (( -robot.getHealth()
                    + robot.getType().attackPower/300
                    + robot.getType().attackPower)
                    + numOtherAlliesInSightRange(robot.location,alliesICanSee) / robot.getHealth());

            if(score > bestScore && isDirectionSafe(robot.location,alliesNextToMe)){
                bestScore = score;
                bestTarget = robot;
            }
        }

            if(treesInSight.length>0) {
                float bestD = 0;
                float d;
                for(TreeInfo tree: treesInSight){
                    d = here.distanceTo(tree.location);
                    if(d < bestD && isDirectionSafe(tree.location, alliesNextToMe)) {
                        bestD = d;
                        treeTarget = tree;
                    }
                }
            }


        shotType = calculateShotType(robotsInSight);
        return bestTarget;
    }

    private static String calculateShotType(RobotInfo[] robotsInSight) throws GameActionException{
        //come up with some sort of formula for choosing the kind of shot
        if(robotsInSight.length>8){
            return PENTAD_SHOT;
        }
        if(robotsInSight.length>4) {
            return TRIAD_SHOT;
        }
        return SINGLE_SHOT;
    }

    private static void shootIfWorth(RobotInfo target, TreeInfo treeTarget, String shotType) throws GameActionException{
        switch (shotType){
            case SINGLE_SHOT: shootSingleShot(target,treeTarget);
            case TRIAD_SHOT: shootTriadShot(target,treeTarget);
            case PENTAD_SHOT: shootPentadShot(target,treeTarget);
                default: //do nothing, it isn't worth shooting.
        }
    }

    private static boolean isDirectionSafe(MapLocation target, RobotInfo[] alliesNestToMe) throws GameActionException{
        Direction intendedAttackDir = here.directionTo(target);
        for(RobotInfo friend: alliesNestToMe){
            if(intendedAttackDir.radiansBetween(here.directionTo(friend.location)) < Math.PI/6){
                return false;
            }
        }
        return true;
    }


    ///////////////////// These Might Belong in Util/////////////////////
    private static void shootSingleShot(RobotInfo target, TreeInfo treeTarget) throws GameActionException{
        if (rc.canFireSingleShot()) {
            if (target != null) {
                rc.fireSingleShot(rc.getLocation().directionTo(target.location));
            } else if(treeTarget != null) {
                rc.fireSingleShot(rc.getLocation().directionTo(treeTarget.location));

            }
        }

    }

    private static void shootTriadShot(RobotInfo target, TreeInfo treeTarget) throws GameActionException{
        if (rc.canFireTriadShot() && target!= null) {
            if (target != null) {
                rc.fireTriadShot(rc.getLocation().directionTo(target.location));
            } else if(treeTarget != null)  {
                rc.fireTriadShot(rc.getLocation().directionTo(treeTarget.location));

            }        }

    }

    private static void shootPentadShot(RobotInfo target, TreeInfo treeTarget) throws GameActionException{
        if (rc.canFirePentadShot() && target!= null) {
            if (target != null) {
                rc.firePentadShot(rc.getLocation().directionTo(target.location));
            } else if(treeTarget != null) {
                rc.firePentadShot(rc.getLocation().directionTo(treeTarget.location));

            }        }

    }

}
