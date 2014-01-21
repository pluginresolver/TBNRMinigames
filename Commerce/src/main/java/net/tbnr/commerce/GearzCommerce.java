package net.tbnr.commerce;

import lombok.Getter;
import net.tbnr.commerce.items.CommerceItemManager;
import net.tbnr.util.TPlugin;

/**
 * Created by Joey on 1/9/14.
 */
public final  class GearzCommerce extends TPlugin {
    @Getter
    private static GearzCommerce instance;

    @Getter
    private CommerceItemManager manager;

    @Override
    public void enable() {
        GearzCommerce.instance = this;
        this.manager = new CommerceItemManager();
        this.manager.activateCommerce();
    }

    @Override
    public void disable() {

    }

    @Override
    public String getStorablePrefix() {
        return null;
    }
}
