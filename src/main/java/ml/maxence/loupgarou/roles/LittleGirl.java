package ml.maxence.loupgarou.roles;

import ml.maxence.loupgarou.game.WinType;

public class LittleGirl extends LGRole {

    public LittleGirl() {
        super("littlegirl", "La", "&9Petite-Fille", new String[]{"Durant la nuit, espionnez les loups-garous"});
    }

    @Override
    public boolean canPlayAtNight()
    {
        return false;
    }

    @Override
    public WinType getWinType()
    {
        return WinType.INNOCENT;
    }

    @Override
    public String getTextAtNight()
    {
        return "Ecoutez les loups-garous durant la nuit";
    }
}
