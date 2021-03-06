/*
 * Copyright (c) 2014.
 * CogzMC LLC USA
 * All Right reserved
 *
 * This software is the confidential and proprietary information of Cogz Development, LLC.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with Cogz LLC.
 */

package net.tbnr.commerce.items.definitions;

import net.tbnr.commerce.items.CommerceItem;
import net.tbnr.commerce.items.CommerceItemAPI;
import net.tbnr.commerce.items.CommerceItemMeta;
import net.tbnr.commerce.items.Tier;
import net.tbnr.gearz.GearzException;
import net.tbnr.manager.TBNRPlayer;

@CommerceItemMeta(
        tier = Tier.Epic,
        key = "firework_sparkle",
        humanName = "Firework Sparkle"
)
@Deprecated
public final class FireworkSparkle extends CommerceItem {
    public FireworkSparkle(TBNRPlayer player, CommerceItemAPI api) throws GearzException {
        super(player, api);
    }
}
