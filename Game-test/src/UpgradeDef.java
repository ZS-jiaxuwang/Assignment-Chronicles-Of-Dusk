// Jiaxu Wang (24009377)  Jiaheng Liu (24009483)  Angze Song (24009333)   Xiao Wu (24009458)
public class UpgradeDef {
    public interface UpgradeAction {
        void apply(SurvivalGame game);
    }

    public final String title;
    public final String description;
    public final UpgradeAction action;

    public UpgradeDef(String title, String description, UpgradeAction action) {
        this.title = title;
        this.description = description;
        this.action = action;
    }

    public void apply(SurvivalGame game) {
        action.apply(game);
    }
}
