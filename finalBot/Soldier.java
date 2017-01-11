package team008.finalBot;
import battlecode.common.*;


public class Soldier extends Bot {
	MapLocation target;

    public Soldier(RobotController r){
        super(r);
        //anything else soldier specific
    }
    
	public void takeTurn() throws Exception{
		if(target != null){
			rc.setIndicatorDot(target, 255, 0, 0);
		}
		RobotInfo[] enemies = rc.senseNearbyRobots(-1,rc.getTeam().opponent());
		if(enemies.length > 0){
			RangedCombat.execute();
			return;
		}
		if(target == null && rc.getRoundNum() % 10 == 1){
			assignNewTarget();
		}
		else if (target != null && rc.getLocation().distanceTo(target) < 2 && enemies.length == 0){
			Messaging.removeEnemyArmyLocation(target);
			Messaging.removeEnemyUnitLocation(target);
			target = null;
			assignNewTarget();
		}
		if(target != null){
			goTo(target);
		}
		else{
			tryMoveDirection(Util.randomDirection());
		}
	
	public void assignNewTarget() throws GameActionException{
		target = Messaging.getClosestEnemyArmyLocation(rc.getLocation());
		if(target == null){
			target = Messaging.getClosestEnemyUnitLocation(rc.getLocation());
		}
	}
}