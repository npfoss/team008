package team008.finalBot;

import battlecode.common.*;
import java.util.ArrayList;

public class FastMethods extends Bot {

    public static void initializeNearbyEnemeyRobots(){
        ArrayList<RobotInfo> holder = new ArrayList<>();
        for(RobotInfo robot: nearbyRobots){
            if(robot.team == enemy){
                holder.add(robot);
            }
        }
        nearbyEnemyRobots = holder.toArray( new RobotInfo[holder.size()]);
    }

    public static void initializeNearbyAlliedRobots(){
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
    public static void initializeNearbyAlliedTrees(){
        ArrayList<TreeInfo> holder = new ArrayList<>();
        for(TreeInfo tree: nearbyTrees){
            if(tree.team == us){
                holder.add(tree);
            }
        }
        nearbyAlliedTrees = holder.toArray( new TreeInfo[holder.size()]);

    }


}