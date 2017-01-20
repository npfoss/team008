package team008.defendBot00;
import battlecode.common.*;


public strictfp class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController theRc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        Bot bot;

        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (theRc.getType()) {
            case ARCHON:
                bot = new Archon(theRc);
                break;
            case GARDENER:
                bot = new Gardener(theRc);
                break;
            case SOLDIER:
                bot = new Soldier(theRc);
                break;
            case TANK:
            	bot = new Tank(theRc);
            	break;
            case LUMBERJACK:
                bot = new Lumberjack(theRc);
                break;
            case SCOUT:
            	bot = new Scout(theRc);
            	break;
            default:
                System.out.println("HOLY SHIT HOW DID THIS HAPPEN?! ABORT! ABORT! ABORT!! (got to default switch statement in RobotPlayer.java)");
                bot = new Bot();
        }
        bot.loop();
	}
}