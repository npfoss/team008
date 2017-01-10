package team008.finalBot;
import battlecode.common.*;


public strictfp class RobotPlayer {
    static RobotController rc;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController theRc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = theRc;
        Bot bot;

        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                bot = new Archon(rc);
                break;
            case GARDENER:
                bot = new Gardener(rc);
                break;
            case SOLDIER:
                bot = new Soldier(rc);
                break;
            case TANK:
            	bot = new Tank(rc);
            	break;
            case LUMBERJACK:
                bot = new Lumberjack(rc);
                break;
            case SCOUT:
            	bot = new Scout(rc);
            	break;
            default:
                System.out.println("HOLY SHIT HOW DID THIS HAPPEN?! ABORT! ABORT! ABORT!!");
                bot = new Bot();
        }
        bot.loop();
	}
}