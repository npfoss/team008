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
        Direction destinationDir;
        RobotInfo target;
        RobotInfo[] robotsInSight = rc.senseNearbyRobots();
        BulletInfo[] bulletsInSight = rc.senseNearbyBullets();

            if(firstAction == MOVE_FIRST){
                //move to destination
                destinationDir = chooseMove(robotsInSight,bulletsInSight);
                tryMoveDirection(destinationDir);

                //shoot target
                target = chooseTargetAndShotType(robotsInSight);
                shootIfWorth(target, shotType);
            } else {
                //move to destination
                destinationDir = chooseMove(robotsInSight, bulletsInSight);

                //shoot target
                target  = chooseTargetAndShotType(robotsInSight);
                shootIfWorth(target, shotType);
            }

    }

    private static String determineFirstAction() throws GameActionException{
        //check if our best shot is in the direction we’re moving
        return MOVE_FIRST;
    }

    ///////////////////// Movement Micro/////////////////////
    private static Direction chooseMove(RobotInfo[] robotsInSight, BulletInfo[] bulletsInSight) throws GameActionException{
        Direction moveDir = null;
        if(bulletsInSight.length > 0){
            //evade bullets
            for(BulletInfo bullet: bulletsInSight){
                if(willCollideWithMe(bullet)){
                    return bullet.dir.rotateLeftDegrees((float)90.0);
                }
            }

        }

        //decide whether to engage
        if(robotsInSight.length>0) {
            if(robotsInSight.length ==1){
                if(canWin1v1(robotsInSight[0])){
                    return rc.getLocation().directionTo(robotsInSight[0].getLocation());
                }
            }
            //will we have backup?
        }

        //move to assist someone else
        //move to put us in best spot
        return moveDir;
    }
    public static boolean canWin1v1(RobotInfo enemy) {
        if (enemy.type == RobotType.ARCHON )
            return true;
        int turnsToKillEnemy = (int) (enemy.health / rc.getType().attackPower);
        int turnsForEnemyToKillUs = (int) (rc.getHealth() / enemy.getType().attackPower);
        return turnsToKillEnemy <= turnsForEnemyToKillUs;
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
