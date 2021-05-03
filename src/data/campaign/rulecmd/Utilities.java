package data.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.intel.misc.WarningBeaconIntel;
import com.fs.starfarer.campaign.CustomCampaignEntity;
import com.fs.starfarer.campaign.JumpPoint;
import com.fs.starfarer.campaign.NascentGravityWell;

import java.util.*;

public class Utilities
{
    public static float getDistanceBetweenPoints(float x1, float y1, float x2, float y2)
    {
        return (float) Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    public static float getDistanceBetweenTokens(SectorEntityToken tokenA, SectorEntityToken tokenB)
    {
        return getDistanceBetweenPoints(tokenA.getLocation().x, tokenA.getLocation().y, tokenB.getLocation().x, tokenB.getLocation().y);
    }

    public static float getAngle(float focusX, float focusY, float playerX, float playerY)
    {
        float angle = (float) Math.toDegrees(Math.atan2(focusY - playerY, focusX - playerX));

        //Not entirely sure what math is going on behind the scenes but this works to get the station to spawn next to the player
        angle = angle + 180f;

        return angle;
    }

    public static float getAngleFromPlayerFleet(SectorEntityToken target)
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        return getAngle(target.getLocation().x, target.getLocation().y, playerFleet.getLocation().x, playerFleet.getLocation().y);
    }

    public static float getAngleFromEntity(SectorEntityToken entity, SectorEntityToken target)
    {
        return getAngle(target.getLocation().x, target.getLocation().y, entity.getLocation().x, entity.getLocation().y);
    }

    public static void surveyAll(MarketAPI market)
    {
        for (MarketConditionAPI condition : market.getConditions())
        {
            condition.setSurveyed(true);
        }
    }

    public static float getRandomOrbitalAngleFloat(float min, float max)
    {
        Random rand = new Random();
        return rand.nextFloat() * (max - min) + min;
    }

    public static boolean canScrambled(SectorEntityToken token){
        /**
         * Check object can be scrambled
         * @param SectorEntityToken token Object to be checked
         * @return bool Item can be scrambled or not
         */
        boolean scrambleNascentGravityWell = Global.getSettings().getBoolean("scrambleNascentGravityWell");
        boolean scrambleJumpPoint = Global.getSettings().getBoolean("scrambleJumpPoint");
        boolean scrambleWarningBeacon = Global.getSettings().getBoolean("scrambleWarningBeacon");
        boolean scrambleAbandonedStation = Global.getSettings().getBoolean("scrambleAbandonedStation");

        if(scrambleNascentGravityWell && token instanceof NascentGravityWell) return true;
        if(scrambleJumpPoint && token instanceof JumpPoint) return true;
        if(token instanceof CustomCampaignEntity){
            if(scrambleWarningBeacon && token.getCustomEntityType().equals("warning_beacon")) return true;

            System.out.println(".."+ token.getCustomEntityType() + "..");

            if(scrambleAbandonedStation && token.getCustomEntityType().contains("station")){
                if(token.getMarket() != null){
                    for(MarketConditionAPI condition: token.getMarket().getConditions()){
                        if(condition.getId().equals("abandoned_station")) return true;
                    }
                }
                else return true;
            }
        }
        return false;
    }

    public static void removeRelatedIntel(SectorEntityToken token){
        IntelManagerAPI manager = Global.getSector().getIntelManager();
        if(token instanceof CustomCampaignEntity){
            if(token.getCustomEntityType().equals("warning_beacon")){
                for(IntelInfoPlugin intel: manager.getIntel(WarningBeaconIntel.class)){
                    SectorEntityToken intelToken = intel.getMapLocation(null);
                    if(intelToken == token) manager.removeIntel(intel);
                }
            }
        }
    }

    public static List<SectorEntityToken> getEntitiesInRadius(SectorEntityToken source, double radius){
        /**
        * Return entities within the radius, exclude itself
        * Return null when not found
        *
        * @param  source Original entity to be compared with
        * @param  radius Radius, default is infinity if < 0
        * @return entities within radius
        */
        List<SectorEntityToken> tokens = source.getContainingLocation().getAllEntities();
        List<SectorEntityToken> responses = new ArrayList<SectorEntityToken>();
        if(radius < 0) radius = Double.POSITIVE_INFINITY;

        for(SectorEntityToken token: tokens){
            if(token == source) continue;
            else if(getDistanceBetweenTokens(token, source) < radius)
            {
                responses.add(token);
            }
        }
        return responses;
    }

    public static List<SectorEntityToken> getScrambleInRadius(SectorEntityToken source, double radius){
        List<SectorEntityToken> tokens = getEntitiesInRadius(source, radius);
        List<SectorEntityToken> responses = new ArrayList<SectorEntityToken>();

        for(SectorEntityToken token: tokens){
            System.out.println(".."+ token.getCustomEntityType() + "..");
            System.out.println(".."+ token + "..");
            if(canScrambled(token)) responses.add(token);
        }
        return responses;
    }

    public static SectorEntityToken getClosetEntity(SectorEntityToken source, double radius){
        /**
        * Return closet entity of source entity except token itself
        * Return null when not found
        *
        * @param  source original entity to be compared with
        * @param  radius within radius, default is infinity if < 0
        * @return closet entity within radius
        */
        List<SectorEntityToken> tokens = getEntitiesInRadius(source, radius);

        SectorEntityToken closet = null;
        if(radius < 0) radius = Double.POSITIVE_INFINITY;

        for(SectorEntityToken token: tokens){
            if(token == source) continue;
            else if(getDistanceBetweenTokens(token, source) < radius)
            {
                radius = getDistanceBetweenTokens(token, source);
                closet = token;
            }
        }
        return closet;
    }

    public static SectorEntityToken getClosetScramble(SectorEntityToken source, double radius){
        /**
        * Return closet entity of scramble source entity except token itself
        * Return null when not found
        *
        * @param  source original entity to be compared with
        * @param  radius within radius, default is infinity if < 0
        * @return closet entity within radius
        */
        List<SectorEntityToken> tokens = getScrambleInRadius(source, radius);

        SectorEntityToken closet = null;
        if(radius < 0) radius = Double.POSITIVE_INFINITY;

        for(SectorEntityToken token: tokens){
            if(token == source) continue;
            else if(getDistanceBetweenTokens(token, source) < radius)
            {
                radius = getDistanceBetweenTokens(token, source);
                closet = token;
            }
        }
        return closet;
    }
}
