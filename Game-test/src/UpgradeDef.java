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
