package mod.illegalindustries.impl;


import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.util.Pair;


public class DrugProduction extends BaseIndustry {

    public void apply() {
        super.apply(true);

        int size = market.getSize();
        demand(Commodities.ORGANICS, size);
        demand(Commodities.FOOD, size);
        supply(Commodities.DRUGS, size + 1);

        Pair<String, Integer> deficit = getMaxDeficit(Commodities.ORGANICS, Commodities.FOOD);
        applyDeficitToProduction(1, deficit,Commodities.DRUGS);

        if (!isFunctional()) {
            supply.clear();
        }
    }

    @Override
    public void unapply() {
        super.unapply();
    }

    @Override
    public RaidDangerLevel adjustCommodityDangerLevel(String commodityId, RaidDangerLevel level) {
        return level.next();
    }

    @Override
    public RaidDangerLevel adjustItemDangerLevel(String itemId, String data, RaidDangerLevel level) {
        return level.next();
    }

    @Override
    protected boolean canImproveToIncreaseProduction() {
        return true;
    }
}
