package team008.defendBot00;

import battlecode.common.*;

public class Util extends Bot {
	public static TreeInfo leastHealth(TreeInfo[] trees, boolean canWater) {
        TreeInfo ret = null;
        double minHealth = 1e99;
        for (TreeInfo tree : trees) {
            if ((!canWater || rc.canWater(tree.ID)) && tree.health < minHealth) {
                minHealth = tree.health;
                ret = tree;
            }
        }
        return ret;
    }
	 public static Direction randomDirection() {
	        return new Direction( myRand.nextFloat() * 2 * (float) Math.PI);
	    }
}