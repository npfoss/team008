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


    public static void execute() throws GameActionException{

        String firstAction = determineFirstAction();
        MapLocation destination;
        RobotInfo target;
        RobotInfo[] robotsInSight = rc.senseNearbyRobots();
        BulletInfo[] bulletsInSight = rc.senseNearbyBullets();

            if(firstAction == MOVE_FIRST){
                //move to destination
                destination = chooseMove(robotsInSight,bulletsInSight);

                //shoot target
                target = chooseTargetAndShotType(robotsInSight);
                shootIfWorth(target, shotType);
            } else {
                //move to destination
                destination = chooseMove(robotsInSight, bulletsInSight);

                //shoot target
                target  = chooseTargetAndShotType(robotsInSight);
                shootIfWorth(target, shotType);
            }

    }

    private static String determineFirstAction() throws GameActionException{
        //check if our best shot is in the direction we’re moving
        return MOVE_FIRST;
    }

    private static MapLocation chooseMove(RobotInfo[] robotsInSight, BulletInfo[] bulletsInSight) throws GameActionException{

        if(bulletsInSight.length > 0){
            //evade bullets
            for(BulletInfo bullet: bulletsInSight){
                if(willCollideWithMe(bullet))
            }

        }

        //decide whether to engage
        if(robotsInSight.length>0) {
            //can we win 1v1?
            //will we have backup?
        }

        //move to assist someone else
        //move to put us in best spot
        return null;
    }

    ///////////////////// Shooting and Target Micro/////////////////////
    private static RobotInfo chooseTargetAndShotType(RobotInfo[] robotsInSight) throws GameActionException{
        int score;
        int bestScore = 999999;
        RobotInfo bestTarget = null;
        for(RobotInfo robot: robotsInSight){
            //add other factors for choosing best bot
            //value based on num nearby bots including trees
            score = (robot.getType().maxHealth - (int)robot.getHealth());

            if(score < bestScore){
                bestScore = score;
                bestTarget = robot;
            }
        }

        shotType = calculateShotType(bestTarget,bestScore);
        return bestTarget;
    }

    private static String calculateShotType(RobotInfo target, int score) throws GameActionException{
        //come up with some sort of formula for choosing the kind of shot
        return SINGLE_SHOT;
    }

    private static void shootIfWorth(RobotInfo target, String shotType) throws GameActionException{
        switch (shotType){
            case SINGLE_SHOT: shootSingleShot(target);
            case TRIAD_SHOT: shootTriadShot(target);
            case PENTAD_SHOT: shootPentadShot(target);
                default: //do nothing, it isn't worth shooting.
        }
    }



    ///////////////////// These Might Belong in Util/////////////////////
    private static void shootSingleShot(RobotInfo target) throws GameActionException{
        if (rc.canFireSingleShot()) {
            rc.fireSingleShot(rc.getLocation().directionTo(target.location));
        }

    }
    private static void shootTriadShot(RobotInfo target) throws GameActionException{
        if (rc.canFireTriadShot()) {
            rc.fireSingleShot(rc.getLocation().directionTo(target.location));
        }

    }
    private static void shootPentadShot(RobotInfo target) throws GameActionException{
        if (rc.canFirePentadShot()) {
            rc.fireSingleShot(rc.getLocation().directionTo(target.location));
        }

    }

}
