package mod.illegalindustries.impl;

import java.awt.Color;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;



public class AutomatedPortComplex extends BaseIndustry implements MarketImmigrationModifier {

    public static float OFFICER_PROB_MOD_MEGA = 0.2f;

    public static float UPKEEP_MULT_PER_DEFICIT = 0.1f;

    public static final float GROUND_DEFENSES_FLAT = 50f;
    public static final float HAZARD_FLAT = -0.50f;
    public static final float STATIC_ACCESSIBILITY = 1.00f;

    public static final float IMRPOVE_FLEET_SIZE_MULT = 0.20f;
    public static final float ALPHA_CORE_GROUND_DEFENSES_MULT = 0.5f;

    public void apply() {
        super.apply(true);

        int size = market.getSize();

        demand(Commodities.FUEL, size + 1);
        demand(Commodities.SUPPLIES, size + 1);
        demand(Commodities.SHIPS, size + 1);

        supply(Commodities.CREW, size + 2);
        supply(Commodities.MARINES, size);
        supply(Commodities.DRUGS, size);
        supply(Commodities.ORGANS, size - 1);

        String desc = getNameForModifier();

        Pair<String, Integer> deficit = getUpkeepAffectingDeficit();

        if (deficit.two > 0) {
            float loss = getUpkeepPenalty(deficit);
            getUpkeep().modifyMult("deficit", 1f + loss, getDeficitText(deficit.one));
        } else {
            getUpkeep().unmodifyMult("deficit");
        }

        market.setHasSpaceport(true);

        float total = 0f;
        for(Map.Entry<String, StatMod> entry: market.getAccessibilityMod().getFlatBonuses().entrySet()) {
            if(!entry.getKey().equals("automatedportcomplex")){
                total += entry.getValue().value;
            }
        }
        market.getAccessibilityMod().modifyFlat(getModId(0), STATIC_ACCESSIBILITY - total, desc);
        market.getHazard().modifyFlat(getModId(0), HAZARD_FLAT, desc);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyFlat(getModId(0), GROUND_DEFENSES_FLAT, desc);

        if(isImproved()){
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat(
                getModId(0), IMRPOVE_FLEET_SIZE_MULT, desc
            );
        }

        float officerProb = OFFICER_PROB_MOD_MEGA;
        market.getStats().getDynamic().getMod(Stats.OFFICER_PROB_MOD).modifyFlat(getModId(0), officerProb);

        if (!isFunctional()) {
            supply.clear();
            unapply();
            market.setHasSpaceport(true);
        }
    }

    @Override
    public void unapply() {
        super.unapply();

        market.setHasSpaceport(false);
        market.getAccessibilityMod().unmodifyFlat(getModId(0));
        market.getAccessibilityMod().unmodifyFlat(getModId(1));
        market.getAccessibilityMod().unmodifyFlat(getModId(2));

        market.getStats().getDynamic().getMod(Stats.OFFICER_PROB_MOD).unmodifyFlat(getModId(0));
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyMult(getModId(0));
        market.getHazard().unmodifyFlat(getModId(0));
    }

    protected float getUpkeepPenalty(Pair<String, Integer> deficit) {
        float loss = deficit.two * UPKEEP_MULT_PER_DEFICIT;
        if (loss < 0) loss = 0;
        return loss;
    }

    protected Pair<String, Integer> getUpkeepAffectingDeficit() {
        return getMaxDeficit(Commodities.FUEL, Commodities.SUPPLIES, Commodities.SHIPS);
    }

    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        return mode != IndustryTooltipMode.NORMAL || isFunctional();
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
            Color h = Misc.getHighlightColor();
            float opad = 10f;
            float bonus = getPopulationGrowthBonus();
            tooltip.addPara("Population growth: %s", opad, h, "+" + (int)bonus);
            tooltip.addPara("Accessibility is lock to: %s", opad, h, (int)(STATIC_ACCESSIBILITY * 100) + "%");
            tooltip.addPara("Ground defense: %s", opad, h, "+" + (int)GROUND_DEFENSES_FLAT);
            tooltip.addPara("Hazard rating: %s", opad, h, (int)(HAZARD_FLAT * 100f) + "%");
        }
    }

    public float getPopulationGrowthBonus() {
        return market.getSize();
    }

    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        float bonus = getPopulationGrowthBonus();
        incoming.getWeight().modifyFlat(getModId(), bonus, getNameForModifier());
    }

    @Override
    protected void applyAlphaCoreModifiers() {
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
        .modifyMult(getModId(1), 1f + ALPHA_CORE_GROUND_DEFENSES_MULT, getNameForModifier());
    }

    @Override
    protected void applyNoAICoreModifiers() {
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD)
        .unmodifyMult(getModId(1));
    }

    @Override
    protected void applyAlphaCoreSupplyAndDemandModifiers() {
        demandReduction.modifyFlat(getModId(0), DEMAND_REDUCTION, "Alpha core");
    }

    protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10f;
        Color highlight = Misc.getHighlightColor();

        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. Ground defense strength: %s", 0f, highlight,
                "" + (int)((1f - UPKEEP_MULT) * 100f) + "%",
                "" + DEMAND_REDUCTION,
                "x" + (1 + ALPHA_CORE_GROUND_DEFENSES_MULT)
            );
            tooltip.addImageWithText(opad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. Ground defense strength: %s", 0f, highlight,
            "" + (int)((1f - UPKEEP_MULT) * 100f) + "%",
            "" + DEMAND_REDUCTION,
            "x" + (1 + ALPHA_CORE_GROUND_DEFENSES_MULT)
        );
    }

    @Override
    public boolean canImprove() {
        return true;
    }

    @Override
    protected boolean canImproveToIncreaseProduction() {
        return false;
    }

    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
        float opad = 10f;
        Color highlight = Misc.getHighlightColor();

        String str = "" + (int)Math.round(IMRPOVE_FLEET_SIZE_MULT * 100f);

        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
            info.addPara("Incrase %s%% combat fleet size.", 0f, highlight, str);
        } else {
            info.addPara("Incrase %s%% combat fleet size.", 0f, highlight, str);
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }
}
