package team008.finalBot;
import battlecode.common.*;
import sun.reflect.generics.tree.Tree;

import java.util.ArrayList;


/**
 * Created by Jonah on 1/13/2017.
 */
public class FastMethods extends Bot {
    public static float fastDistanceSquaredTo(MapLocation loc){
        return ((loc.x-here.x)*(loc.x - here.x)) + ((loc.y-here.y)*(loc.y - here.y));
    }


    public static void initializeNearbyEnemies(){
        ArrayList<RobotInfo> holder = new ArrayList<>();
        for(RobotInfo robot: nearbyRobots){
            if(robot.team == enemy){
                holder.add(robot);
            }
        }
        nearbyEnemyRobots = holder.toArray( new RobotInfo[holder.size()]);
    }

    public static void initializeNearbyFriends(){
        ArrayList<RobotInfo> holder = new ArrayList<>();
        for(RobotInfo robot: nearbyRobots){
            if(robot.team == us){
                holder.add(robot);
            }
        }
        nearbyAlliedRobots = holder.toArray( new RobotInfo[holder.size()]);

    }
    public static void initializeNearbyEnemyTrees(){
        ArrayList<TreeInfo> holder = new ArrayList<>();
        for(TreeInfo tree: nearbyTrees){
            if(tree.team == enemy){
                holder.add(tree);
            }
        }
        nearbyEnemyTrees = holder.toArray( new TreeInfo[holder.size()]);

    }
    public static void initializeNearbyNeutralTrees(){
        ArrayList<TreeInfo> holder = new ArrayList<>();
        for(TreeInfo tree: nearbyTrees){
            if(tree.team == Team.NEUTRAL){
                holder.add(tree);
            }
        }
        nearbyNeutralTrees = holder.toArray( new TreeInfo[holder.size()]);
    }
    public static void initializeNearbyFriendlyTrees(){
        ArrayList<TreeInfo> holder = new ArrayList<>();
        for(TreeInfo tree: nearbyTrees){
            if(tree.team == us){
                holder.add(tree);
            }
        }
        nearbyAlliedTrees = holder.toArray( new TreeInfo[holder.size()]);

    }


}