package team008.finalBot;
import battlecode.common.*;

/**
 * Created by jmac on 1/10/17.
 */
public class RangedCombat extends Bot {

    private static final String SINGLE_SHOT = "single shot";
    private static final String TRIAD_SHOT = "triad shot";
    private static final String PENTAD_SHOT = "pentad shot";

    //forgive me for I have sinned
    private static String shotType = "";
    private static int shotValue = 0;
    private static boolean movingSafely = true;


    /**
     * Effectively "domicro"
     * @throws GameActionException
     */
    public static void execute() throws GameActionException{
        Direction destinationDir;
        BodyInfo finalTarget = chooseTargetAndShotType();
        System.out.println("Picking First move" + shotValue);

        //check if we have a worthwhile attack
        if(shotValue>70){
            System.out.println("shooting first");
            shootIfWorth(finalTarget,shotType);
        }

        //move
        destinationDir = chooseMove();
        System.out.println("picked place to move"  + Clock.getBytecodeNum());
        if(destinationDir!=null) {
            if(movingSafely){
                goTo(destinationDir);
            }else{
                tryMoveDirectionDangerous(destinationDir);
            }
        }

        //if we havent shot try again
        if(!rc.hasAttacked()) {
            finalTarget = chooseTargetAndShotType();
            shootIfWorth(finalTarget, shotType);
        }


    }


    ///////////////////// Movement Micro/////////////////////

    /**
     * Decides what direction to move in.
     * @return The direction to try and move in.
     * @throws GameActionException
     */
    private static Direction chooseMove() throws GameActionException{
        //decide whether to engage
        if(nearbyEnemyRobots.length>0) {
            System.out.println("trying to pick best spot" + Clock.getBytecodeNum());
            movingSafely = false;
            return pickOptimalDir();
        }

        movingSafely = true;
        if(myRand.nextDouble() < .1){
            return here.directionTo(Util.rc.getInitialArchonLocations(enemy)[0]);
        }else{
        	return Util.randomDirection();
        }
    }

    /**
     * Finds the spot that we will gain the least damage by going to.
     * @return
     */
    private static Direction pickOptimalDir(){
        Direction bestDir = null;
        int score;
        int bestScore = hypotheticalDamageToSpot(here)+knownDamageToLoc(here) - numberOfUnitsWeBlock(here)/2;
        Direction dir = new Direction(0);
        MapLocation potentialLoc;

        for(int i = 0; i<8; i++){
            System.out.println("picking best spot" + Clock.getBytecodeNum());

            potentialLoc = here.add(dir,rc.getType().strideRadius);
            score = hypotheticalDamageToSpot(potentialLoc) + knownDamageToLoc(potentialLoc);
            score -= numberOfUnitsWeBlock(potentialLoc)/2;
            if(score < bestScore && rc.canMove(dir)){
                bestScore = score;
                bestDir = dir;
            }
            dir = dir.rotateRightDegrees(45);
        }
        System.out.println("picked best spot" + Clock.getBytecodeNum());

        return bestDir;
    }

    /**
     * We want to avoid getting in the way of our other offensive units.
     * @param loc the location to test
     * @return the number of units we could be blocking
     */
    private static int numberOfUnitsWeBlock(MapLocation loc){
        int num = 0;
        for(RobotInfo robot: nearbyAlliedRobots){
            if(robot.type == RobotType.TANK || robot.type == RobotType.SOLDIER){
                num+=1;
            }
            if(robot.type == RobotType.LUMBERJACK && loc.distanceTo(robot.location) <= 1){
                num+=1;
            }
        }
        return num;
    }

    /**
     * NOT USED
     * Checks if we can win the 1v1
     * @param enemy enemy to check against
     * @return true if we can win
     */
//    public static boolean canWin1v1(RobotInfo enemy) {
//        if (enemy.type == RobotType.ARCHON || enemy.type == RobotType.GARDENER)
//            return true;
//        int turnsToKillEnemy = (int) (enemy.health / rc.getType().attackPower);
//        int turnsForEnemyToKillUs = (int) (rc.getHealth() / enemy.getType().attackPower);
//        return turnsToKillEnemy <= turnsForEnemyToKillUs;
//    }

    /**
     * finds the number of allies that can also see this loc.
     * @param loc place to check if they can see
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
     * @return
     * @throws GameActionException
     */
    private static BodyInfo chooseTargetAndShotType() throws GameActionException{
        int score;
        int bestScore = -999999;
        RobotInfo bestRobot = null;
        int canWeHitThemValue;
        for(RobotInfo robot: nearbyEnemyRobots){

            canWeHitThemValue = canWeHitHeuristic(robot);
            if(canWeHitThemValue<60){
                continue;
            }
            score = (int) ( -robot.getHealth()
                    + robot.getType().attackPower
                    + numOtherAlliesInSightRange(robot.location) / robot.getHealth())
                    + canWeHitThemValue;

            if(score > bestScore && isDirectionSafe(robot)){
                bestScore = score;
                bestRobot = robot;
                shotValue = canWeHitThemValue;
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
        shotType = calculateShotType(bestRobot);
        return bestRobot;
    }

    /**
     * An attempt to take shots we can make
     * @param robot
     * @return
     */
    private static int canWeHitHeuristic(RobotInfo robot){
        int score = 100;
        float howFarAwayTheyCanGet =  here.distanceTo(robot.location) - type.bulletSpeed + robot.type.strideRadius;
        score -= 10* howFarAwayTheyCanGet;
        score += 20*nearbyEnemyRobots.length;
        return score;
    }


    /**
     * Picks whether to shot 1,3 or 5 bullets.
     * @return the shot type
     * @throws GameActionException
     */
    private static String calculateShotType(BodyInfo target) throws GameActionException{
        //come up with some sort of formula for choosing the kind of shot
        float score  = nearbyEnemyRobots.length - here.distanceTo(target.getLocation());
        if(score>4){
            return PENTAD_SHOT;
        }
        if(score>2) {
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
     * @return true if it could also hit a friend
     * @throws GameActionException
     */
    private static boolean isDirectionSafe(RobotInfo target) throws GameActionException{
    	Direction intendedAttackDir = here.directionTo(target.location);
        for(RobotInfo friend: nearbyAlliedRobots){
            if(friend.location.distanceTo(here) < here.distanceTo(target.location)- type.bodyRadius - target.type.bodyRadius && intendedAttackDir.radiansBetween(here.directionTo(friend.location)) < Math.PI/6 ){
                //rc.setIndicatorDot(here,1,1,1);
                return false;
            }
        }
        for(TreeInfo friend: nearbyAlliedTrees){
            if(friend.location.distanceTo(here) < here.distanceTo(target.location)- type.bodyRadius - target.type.bodyRadius && intendedAttackDir.radiansBetween(here.directionTo(friend.location)) < Math.PI/6 ){
                //rc.setIndicatorDot(here,1,1,1);
                return false;
            }
        }
        for(TreeInfo friend: nearbyNeutralTrees){
            if(friend.location.distanceTo(here) < here.distanceTo(target.location)- type.bodyRadius - target.type.bodyRadius && intendedAttackDir.radiansBetween(here.directionTo(friend.location)) < Math.PI/6 ){
                //rc.setIndicatorDot(here,1,1,1);
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
