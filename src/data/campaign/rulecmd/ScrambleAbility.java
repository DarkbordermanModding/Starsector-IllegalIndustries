package data.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.econ.MarketCondition;


public class ScrambleAbility extends BaseDurationAbility
{
    @Override
    protected void activateImpl()
    {
        InteractionDialogPlugin dialog = new SystemDialog();
        Global.getSector().getCampaignUI().showInteractionDialog(dialog, null);
    }

    @Override
    public boolean isUsable()
    {
        if (Global.getSector().getPlayerFleet().isInHyperspaceTransition()) return false;
        return true;
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded)
    {
        if(Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            tooltip.addPara(
                "You can not use the ability now.",
                Misc.getNegativeHighlightColor(),
                10.0f
            );
        }
    }

    @Override
    public boolean isTooltipExpandable() {
        return false;
    }

    @Override
    protected void applyEffect(float v, float v1) { }

    @Override
    protected void deactivateImpl() { }

    @Override
    protected void cleanupImpl() { }
}