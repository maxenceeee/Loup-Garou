package ml.maxence.loupgarou.roles;

import ml.maxence.loupgarou.game.WinType;

public class SimpleVillager extends LGRole{


    public SimpleVillager() {
        super("villager", "Un", "&eSimple Villageois",  new String[]{"Un simple villageois, qui n'a rien de spécial..."});
    }

    @Override
    public boolean canPlayAtNight() {
        return false;
    }

    @Override
    public WinType getWinType() {
        return WinType.INNOCENT;
    }
}
