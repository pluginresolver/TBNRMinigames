package net.tbnr.commerce.items.definitions;

import net.tbnr.commerce.items.CommerceItem;
import net.tbnr.commerce.items.CommerceItemAPI;
import net.tbnr.commerce.items.CommerceItemMeta;
import net.tbnr.commerce.items.Tier;
import net.tbnr.gearz.GearzException;
import net.tbnr.gearz.player.GearzPlayer;

@CommerceItemMeta(
        tier = Tier.Awesome,
        key = "ender_pearls",
        humanName = "Ender Pearls"
)
public final class EnderPearls extends CommerceItem {
    public EnderPearls(GearzPlayer player, CommerceItemAPI api) throws GearzException {
        super(player, api);
    }
}