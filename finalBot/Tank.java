package team008.finalBot;
import battlecode.common.*;


public class Tank extends Bot {
	private int turnsSinceSeenEnemy;

    public Tank(RobotController r) throws GameActionException{
        super(r);
    }
    
	public void takeTurn() throws Exception{
		if(nearbyEnemyRobots.length > 0){
            if((rc.getRoundNum() +rc.getID()) % 15 == 0 || target == null){
                notifyFriendsOfEnemies(nearbyEnemyRobots);
            }
            RangedCombat.execute();
            turnsSinceSeenEnemy = 0;
            return;
        }
        turnsSinceSeenEnemy++;
        if(target == null || (rc.getRoundNum() + rc.getID()) % 10 == 0){
            assignNewTarget();
        }
        if (rc.getMoveCount() == 0) {
        	if(nearbyBullets.length > 0){
        		Direction dirToMove = here.directionTo(nearbyBullets[0].location);
        		if(nearbyAlliedRobots.length > 0)
        			dirToMove = here.directionTo(nearbyAlliedRobots[0].location);
        		RangedCombat.bulletMove(here.add(dirToMove, type.strideRadius), true);
        	}
        	else if (target != null && (here.distanceTo(target) < 3 && turnsSinceSeenEnemy > 5)) {
				Message.ENEMY_ARMIES.removeLocation(target);
				Message.ISOLATED_ENEMIES.removeLocation(target);
				Message.DISTRESS_SIGNALS.removeLocation(target);
				assignNewTarget();
			}
			else if (target != null) {
				if (debug) {
		        	rc.setIndicatorLine(here, target, (us == Team.A ? 255: 0), (us == Team.A ? 0: 255), 0); 
				}
				goTo(target);
			} 
			else {
				if(nearbyAlliedRobots.length > 0 && here.distanceTo(nearbyAlliedRobots[0].location) > 4)
					tryMoveDirection(here.directionTo(nearbyAlliedRobots[0].location), true, true);
				else{
					tryMoveDirection(here.directionTo(MapAnalysis.center), true, true);
				}
			}
		}
	}
}
