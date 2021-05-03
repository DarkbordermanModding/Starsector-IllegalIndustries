package data.campaign.rulecmd;

import java.awt.Color;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.util.Misc;

public class SystemDialog implements InteractionDialogPlugin {

    private InteractionDialogAPI dialog;
    private OptionPanelAPI options;
    private TextPanelAPI textPanel;
    private final double radius=500f;

    @Override
    public void advance(float arg0) {}

    @Override
    public void backFromEngagement(EngagementResultAPI arg0) {}

    @Override
    public void optionMousedOver(String text, Object optionData) {}

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return null;
    }

    @Override
    public void init(InteractionDialogAPI d) {
        dialog = d;
        options = dialog.getOptionPanel();
        textPanel = dialog.getTextPanel();

        Color colorHighlight = Misc.getHighlightColor();
        textPanel.addPara("Scramble", colorHighlight);

        SectorEntityToken player = Global.getSector().getPlayerFleet();
        SectorEntityToken token = Utilities.getClosetScramble(player, radius);
        if(token != null){
            textPanel.addPara("Your fleet detect objects can be scrambled");
        }
        else textPanel.addPara("Your fleet doesn't detect anything can be scrambled");
        showMenu(token);
    }

    @Override
    public void optionSelected(String text, Object optionData) {
        System.out.println(text);
        System.out.println(optionData);
        SectorEntityToken token = (SectorEntityToken)optionData;
        if(text == "Leave")dialog.dismiss();

        if (text == "Scramble"){
            Utilities.removeRelatedIntel(token);
            LocationAPI location = token.getContainingLocation();
            location.removeEntity(token);
            options.clearOptions();
            textPanel.addPara("Your fleet scrambled stuffs");
            options.addOption("Leave", null);
        }
    }

    public void showMenu(SectorEntityToken token) {
        options.addOption("Go back", null);
        options.clearOptions();
        if(token != null) options.addOption("Scramble", token);
        options.addOption("Leave", null);
    }
}
