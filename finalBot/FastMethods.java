package team008.finalBot;
import battlecode.common.*;


/**
 * Created by Jonah on 1/13/2017.
 */
public class FastMethods extends Bot {
    public static float fastDistanceSquaredTo(MapLocation loc){
        return ((loc.x-here.x)*(loc.x - here.x)) + ((loc.y-here.y)*(loc.y - here.y));
    }
}
