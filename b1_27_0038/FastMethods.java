package team008.b1_27_0038;

import battlecode.common.*;
import java.util.ArrayList;
import java.util.TreeMap;

public class FastMethods extends Bot {

//    public static void initializeNearbyEnemyRobots(){
//        ArrayList<RobotInfo> holder = new ArrayList<>();
//        for(RobotInfo robot: nearbyRobots){
//            if(robot.team == enemy){
//                holder.add(robot);
//            }
//        }
//        nearbyEnemyRobots = holder.toArray( new RobotInfo[holder.size()]);
//    }
//
//    public static void initializeNearbyAlliedRobots(){
//        ArrayList<RobotInfo> holder = new ArrayList<>();
//        for(RobotInfo robot: nearbyRobots){
//            if(robot.team == us){
//                holder.add(robot);
//            }
//        }
//        nearbyAlliedRobots = holder.toArray( new RobotInfo[holder.size()]);
//    }
//
//    public static void initializeNearbyEnemyTrees(){
//        ArrayList<TreeInfo> holder = new ArrayList<>();
//        for(TreeInfo tree: nearbyTrees){
//            if(tree.team == enemy){
//                holder.add(tree);
//            }
//        }
//        nearbyEnemyTrees = holder.toArray( new TreeInfo[holder.size()]);
//
//    }
//    public static void initializeNearbyNeutralTrees(){
//        ArrayList<TreeInfo> holder = new ArrayList<>();
//        for(TreeInfo tree: nearbyTrees){
//            if(tree.team == Team.NEUTRAL){
//                holder.add(tree);
//            }
//        }
//        nearbyNeutralTrees = holder.toArray( new TreeInfo[holder.size()]);
//    }
//    public static void initializeNearbyAlliedTrees(){
//        ArrayList<TreeInfo> holder = new ArrayList<>();
//        for(TreeInfo tree: nearbyTrees){
//            if(tree.team == us){
//                holder.add(tree);
//            }
//        }
//        nearbyAlliedTrees = holder.toArray( new TreeInfo[holder.size()]);
//    }
    public static void initializeAllTrees(){

        ArrayList<TreeInfo> enemyTrees = new ArrayList<>();
        ArrayList<TreeInfo> friendlyTrees = new ArrayList<>();
        ArrayList<TreeInfo> neutralTrees = new ArrayList<>();
        for(TreeInfo tree: nearbyTrees){
            if(tree.team == Team.NEUTRAL) {
                neutralTrees.add(tree);
            }else if(tree.team == enemy){
                enemyTrees.add(tree);
            }else if(tree.team == us){
                friendlyTrees.add(tree);
            }
        }
        nearbyEnemyTrees = enemyTrees.toArray( new TreeInfo[enemyTrees.size()]);
        nearbyNeutralTrees = neutralTrees.toArray( new TreeInfo[neutralTrees.size()]);
        nearbyAlliedTrees = friendlyTrees.toArray( new TreeInfo[friendlyTrees.size()]);

    }
    public static void initializeAllRobots(){
        ArrayList<RobotInfo> enemyBots = new ArrayList<>();
        ArrayList<RobotInfo> friendlyBots = new ArrayList<>();
        for(RobotInfo bot: nearbyRobots){
            if(bot.team == enemy){
                enemyBots.add(bot);
            }else{
                friendlyBots.add(bot);
            }
        }
        nearbyEnemyRobots = enemyBots.toArray( new RobotInfo[enemyBots.size()]);
        nearbyAlliedRobots = friendlyBots.toArray( new RobotInfo[friendlyBots.size()]);

    }


}