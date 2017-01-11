package team008.finalBot;
import battlecode.common.*;


public class Soldier extends Bot {

    public Soldier(RobotController r){
        super(r);
        //anything else soldier specific
    }

	public void takeTurn(TreeInfo[] nearbyNeutralTrees) throws Exception{
        RangedCombat.execute();
    }
}