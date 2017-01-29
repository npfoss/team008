package team008.b1_28_2024;

import battlecode.common.*;

public enum Message {
    // see communications gdoc for most up to date and detailed descriptions of each
    // TODO: not sure if these are all used
    MIN_X(0, 0),
    MAX_X(1, 0),
    MIN_Y(2, 0),
    MAX_Y(3, 0),
    NUM_ARCHONS(4, 0),
    NUM_GARDENERS(5, 0),
    NUM_SOLDIERS(6, 0),
    NUM_TANKS(7,0),
    NUM_SCOUTS(8, 0),
    NUM_LUMBERJACKS(9,0),
    DECISION_MAKER(10, 0),
    GENETICS(11, 0),
    ADAPTATION(12, 0),
    ARCHON_BUILD_NUM(13, 0),
    GARDENER_BUILD_ORDERS(14, 0),
    GARDENER_BUILD_NUM(15, 0),
    MAP_SIZE(16, 0),
//<<<<<<< HEAD
//    // wtf does "scout stuff for map size" mean in the gdoc?
//    NUM_DISTRESS_SIGNALS_SENT(21, 0),
//    GARDENERS_BUILT(22, 0),
//    SOLDIER_IS_DEFENDER(23, 0),
//=======
    //don't need to use this doc for scout edge stuff
    ARCHON_DISTRESS_NUM(21,0),
    //>>>>>>> origin/dev
    DIST_TO_CENTER(22, 0),
    NEUTRAL_TREES(100, 99),
    ENEMY_TREES(200, 99),
    ENEMY_ARMIES(300, 399),
    ISOLATED_ENEMIES(700, 299),
    DISTRESS_SIGNALS(1000, 199),
    ENEMY_ARCHONS(1200, 50),
    TREES_WITH_UNITS(1250, 99),
    CLEAR_TREES_PLEASE(1350, 49), // make this location the center of the clearing ideally
    GARDENER_BUILD_LOCS(1400,100)
    ;

    /** Ok great, but how tf do I use this?
     *
     * Examples:
     *  say you have just found the left edge of the map. you'd call:
     *      Message.MIN_X.setValue(edgeLoc.x);
     *
     *  what is the size of the map? if we've calculated it, you can find out with:
     *      Message.MAP_SIZE.getValue();
     *
     *  you found a tree with a unit in it and want to alert the team (checks for duplicates):
     *      Message.TREES_WITH_UNITS.addLocation(treeLoc);
     *
     *  you're a soldier looking for the nearest enemy to kill:
     *      target = Message.ENEMY_ARMIES.getClosestLocation(toHere);
     *
     *  you get to your target but the enemies are no longer there. don't let other people waste their time:
     *      Message.ENEMY_ARMIES.removeLocation(nothingHereLoc);
     *
     */

    private float FLOAT_MULTIPLIER =   10; // how much precision we want to preserve when truncating
    private int   CODE_OFFSET_AMT  = 600 * (int)(FLOAT_MULTIPLIER + 1); // max x,y val is 600, but we multiply them to be more precise so it has to be more than that
    private float SAME_LOC_THRESH  =    4;

    private int bandStart; // these values are taken from the "communications" gdoc.
    //    make sure they always match
    private int bandWidth; // if you may use 800-899, it's 99. I know, it's number of spaces - 1. done so bandStart+bandWidth is the last index

    Message(int start, int width){
        bandStart = start;
        bandWidth = width;
    }

    public int getLength() throws GameActionException{
    	return getValue();
    }
    
    public void setValue(int x) throws GameActionException {
        Bot.rc.broadcast(bandStart, x);
    }

    public void setValue(float f) throws GameActionException {
        Bot.rc.broadcastFloat(bandStart, f);
        //System.out.println("broadcasting " + f);
    }

    public void setValue(int channel, int x) throws GameActionException {
        Bot.rc.broadcast(channel, x);
    }

    public void setValue(int channel, float f) throws GameActionException {
        Bot.rc.broadcastFloat(channel, f);
    }

    public int getValue() throws GameActionException {
        return Bot.rc.readBroadcast(bandStart);
    }

    public int getValue(int channel) throws GameActionException {
        return Bot.rc.readBroadcast(channel);
    }

    public float getFloatValue() throws GameActionException {
        return Bot.rc.readBroadcastFloat(bandStart);
    }

    public float getFloatValue(int channel) throws GameActionException {
        return Bot.rc.readBroadcastFloat(channel);
    }

    public boolean containsLocation(MapLocation loc) throws GameActionException{
    	//System.out.println("bandStart = " + bandStart);
    	return duplicateLocInRange(loc,bandStart+1,bandStart + getValue());
    }
    
    public void addLocation(MapLocation loc) throws GameActionException {
        int size = getValue(); // for ranges, the number of elements is stored in the first spot, so the list is 1-indexed (sorry)
        if (size > bandWidth || duplicateLocInRange(loc,bandStart+1,bandStart+size)) return;
        // full!             or it's already in there
        setValue(bandStart + size + 1, (int)(loc.x * FLOAT_MULTIPLIER) * CODE_OFFSET_AMT + (int)(loc.y * FLOAT_MULTIPLIER));
        setValue(size+1);
    }

    public boolean removeLocation(MapLocation loc) throws GameActionException{
        int code = (int)(loc.x*FLOAT_MULTIPLIER)*CODE_OFFSET_AMT + (int)(loc.y*FLOAT_MULTIPLIER);
        int size = getValue();
//        System.out.println("trying to remove " + bandStart);
        for(int i = 1; i <= size; i++){
            if(getValue(bandStart + i) == code){
//                System.out.println("FOUND! at channel " + (bandStart + i));
                setValue(bandStart + i, getValue(bandStart+size));
                setValue(size-1);
                return true;
            }
        }
        return false;
    }

    public MapLocation getClosestLocation(MapLocation toHere) throws GameActionException{
        MapLocation ret = null;
        float dist = 999999;
        for(int i = 1; i <= getValue(); i++){
            int code = getValue(bandStart + i);
            if(code == 0) continue;
            MapLocation decoded = new MapLocation((code/CODE_OFFSET_AMT)/FLOAT_MULTIPLIER,(code % CODE_OFFSET_AMT)/FLOAT_MULTIPLIER);
            if( toHere.distanceTo(decoded) < dist){
                dist = toHere.distanceTo(decoded);
                ret = decoded;
            }
        }
        return ret;
    }

    public boolean duplicateLocInRange(MapLocation loc, int start, int end) throws GameActionException{
        for(int i = start; i <= end; i++){
            int code = getValue(i);
            if(code == 0) continue;
            if(loc.distanceTo(new MapLocation((code/CODE_OFFSET_AMT)/FLOAT_MULTIPLIER,(code % CODE_OFFSET_AMT)/FLOAT_MULTIPLIER)) < SAME_LOC_THRESH){
                return true;
            }
        }
        return false;
    }
}