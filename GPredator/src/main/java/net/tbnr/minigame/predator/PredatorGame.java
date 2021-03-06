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

package net.tbnr.minigame.predator;

import lombok.Getter;
import net.tbnr.gearz.GearzPlugin;
import net.tbnr.gearz.arena.Arena;
import net.tbnr.gearz.effects.EnderBar;
import net.tbnr.gearz.game.GameCountdown;
import net.tbnr.gearz.game.GameCountdownHandler;
import net.tbnr.gearz.game.GameMeta;
import net.tbnr.gearz.game.kits.GearzKit;
import net.tbnr.gearz.game.kits.GearzKitItem;
import net.tbnr.gearz.network.GearzPlayerProvider;
import net.tbnr.manager.TBNRMinigame;
import net.tbnr.manager.TBNRPlayer;
import net.tbnr.manager.classes.TBNRAbstractClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by George on 11/01/14.
 * <p/>
 * Purpose Of File: To be The Predator Game Class
 * <p/>
 * Latest Change: Added it
 */
@GameMeta(
        longName = "Predator",
        // I'm not letting you take credit twister ;P
        //:(
        author = "pokuit",
        shortName = "PR",
        version = "1.0",
        description = "In Predator, one Player is marked the Predator at the beginning of the game. The Predator has speed 2, nobody else can sprint at all." +
                "The Prey may choose 3 items to carry with them." +
                "The Predator chooses a weapon from a list." +
                "Every 30 seconds, if a Prey is alive, they gain 1 point. (max total 15 points over 8 minutes)." +
                "When the time expires, all surviving Prey win the game." +
                "If the Predator kills all the Prey, the game ends early and the Predator wins.",
        key = "predator",
        minPlayers = 2,
        maxPlayers = 20,
        mainColor = ChatColor.YELLOW,
        secondaryColor = ChatColor.GRAY)
public class PredatorGame extends TBNRMinigame implements GameCountdownHandler {

    // predator gives 200 points
    // prey get 240 / prey length
    // predator gets 2 speed boost
    //
    private GameCountdown countdown;

    private static final String PREDATOR_FILE = "predators.json";
    private static final String PREY_FILE = "prey.json";

	private static final String PREDATOR_MENU_TITLE = "Predator Menu!";
	private static final String PREY_MENU_TITLE = "Prey Menu!";

	@Getter private ArrayList<GearzKitItem> preyItems;
	@Getter private ArrayList<GearzKitItem> predatorItems;

	@Getter private HashMap<TBNRPlayer, Inventory> preyInventories;
	@Getter private HashMap<TBNRPlayer, Inventory> predatorInventories;

    @Getter private TBNRPlayer predator;

    @Getter private PRState currentState;
    private PredatorArena pArena;


    protected static enum PRState {
        /**
         * State (How long you want it to last)
         * e.g. for Choosing how long in seconds you want the player
         * to be choosing
         */
        CHOOSING(60),
        IN_GAME(8*60);

        Integer time = 0;

        PRState(Integer time) {
            this.time = time;
        }

	    public Integer getTime() {
		    return time;
	    }
    }

    public PredatorGame(List<TBNRPlayer> players, Arena arena, GearzPlugin<TBNRPlayer, TBNRAbstractClass> plugin, GameMeta meta, Integer id, GearzPlayerProvider<TBNRPlayer> playerProvider) {
        super(players, arena, plugin, meta, id, playerProvider);
        if (!(arena instanceof PredatorArena)) throw new RuntimeException("Invalid game class");
        this.pArena = (PredatorArena) arena;
    }

    @Override
    protected void gamePreStart() {
		this.predatorItems = new ArrayList<>();
		this.preyItems = new ArrayList<>();
		this.predatorInventories = new HashMap<>();
		this.preyInventories = new HashMap<>();
        this.registerExternalListeners(new PredatorListener(this));
		this.setupItems();
    }


    @Override
    protected void gameStarting() {
        giveJobs();
		updateScoreboard();
		this.currentState = PRState.CHOOSING;
		this.countdown = new GameCountdown(PRState.CHOOSING.getTime(), this, this);
		countdown.start();
		openChoosingMenu();
	}

    @Override
    protected void gameEnding() {

    }

    @Override
    protected boolean canBuild(TBNRPlayer player) {
        return false;
    }

    @Override
    protected boolean canPvP(TBNRPlayer attacker, TBNRPlayer target) {
        return currentState != PRState.CHOOSING;
    }

    @Override
    protected boolean canUse(TBNRPlayer player) {
        return true;
    }

    @Override
    protected boolean canMove(TBNRPlayer player) {
        return currentState != PRState.CHOOSING;
    }

    @Override
    protected boolean canDrawBow(TBNRPlayer player) {
        return currentState != PRState.CHOOSING;
    }

    @Override
    protected void playerKilled(TBNRPlayer dead, TBNRPlayer killer) {

    }

    @Override
    protected Location playerRespawn(TBNRPlayer player) {
        //IF player is predator
        return player.equals(this.predator) ?
                //if TRUE
                getArena().pointToLocation(pArena.predatorSpawn.next()) :
                //if FALSE
                getArena().pointToLocation(pArena.spawnPoints.next());
    }

    @Override
    protected boolean canPlayerRespawn(TBNRPlayer player) {
        return player.equals(this.predator);
    }

    @Override
    protected int xpForPlaying() {
        return 180;
    }

    @Override
    protected void activatePlayer(TBNRPlayer player) {

    }

    @Override
    protected boolean allowHunger(TBNRPlayer player) {
        return false;
    }

    @Override
    protected boolean allowInventoryChange() {
        return true;
    }

    @Override
    protected boolean canDropItem(TBNRPlayer player, ItemStack itemToDrop) {
        return false;
    }

    @Override
    protected void mobKilled(LivingEntity killed, TBNRPlayer killer) {

    }

    @Override
    protected void playerKilled(TBNRPlayer dead, LivingEntity killer) {

    }

    @Override
    protected boolean canPlace(TBNRPlayer player, Block block) {
        return false;
    }

    @Override
    protected boolean canBreak(TBNRPlayer player, Block block) {
        return false;
    }

    @Override
    public void onCountdownStart(Integer max, GameCountdown countdown) {
        broadcast(getPluginFormat("formats.game-started", false, new String[]{"<time>", max + ""}));
    }

    @Override
    public void onCountdownChange(Integer seconds, Integer max, GameCountdown countdown) {
        updateEnderBar();
    }

    @Override
    public void onCountdownComplete(GameCountdown countdown) {
        if(this.currentState == PRState.CHOOSING) {
            this.currentState = PRState.IN_GAME;
            this.countdown = new GameCountdown(PRState.IN_GAME.getTime(), this, this);
            this.countdown.start();
        } else {
            if(this.currentState == PRState.IN_GAME) {
                finishGame();
                broadcast(getPluginFormat("formats.win", true, new String[]{"<winner>", getWinner().getTPlayer().getPlayerName()}));
            }
        }
    }

    private void updateEnderBar() {
        for (TBNRPlayer player : getPlayers()) {
            if(!player.isValid()) continue;
            EnderBar.setTextFor(player, getPluginFormat("formats.time", false, new String[]{"<time>", formatInt(countdown.getSeconds() - countdown.getPassed())}));
            EnderBar.setHealthPercent(player, ((float) countdown.getSeconds() - countdown.getPassed()) / (float) countdown.getSeconds());
        }
    }

    private String formatInt(Integer integer) {
        if (integer < 60) return String.format("%02d", integer);
        else return String.format("%02d:%02d", (integer / 60), (integer % 60));
    }

    private void updateScoreboard() {
        for (TBNRPlayer player : getPlayers()) {
            if(!player.isValid()) continue;
            player.getTPlayer().resetScoreboard();
            player.getTPlayer().setScoreboardSideTitle(getPluginFormat("formats.scoreboard-title", false));
            /*for (TBNRPlayer player1 : points.keySet()) {
                if(!player1.isValid()) continue;
                player.getTPlayer().setScoreBoardSide(player1.getUsername(), points.get(player1));
            }*/
        }
    }

    public void giveJobs() {
        predator = (TBNRPlayer) getPlayers().toArray()[new Random().nextInt(getPlayers().size())];
    }

	public TBNRPlayer[] getPrey() {
		HashSet<TBNRPlayer> prey = getPlayers();
		prey.remove(predator);
		return (TBNRPlayer[]) prey.toArray();
	}

	private void setupItems() {
		JSONObject prey = GearzKit.getJSONResource(PREY_FILE, getPlugin());
		JSONObject predator = GearzKit.getJSONResource(PREDATOR_FILE, getPlugin());
		try {
			this.preyItems.addAll(GearzKit.classFromJsonObject(prey).getItems());
			this.predatorItems.addAll(GearzKit.classFromJsonObject(predator).getItems());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected Inventory getChooser(TBNRPlayer player) {
		if(this.predator.equals(player)) {
			if(!predatorInventories.containsKey(player)) { predatorInventories.put(player, createInventory(player, predatorItems, PREDATOR_MENU_TITLE)); }
			return predatorInventories.get(player);
		} else {
			if(!preyInventories.containsKey(player)) { preyInventories.put(player, createInventory(player, preyItems, PREY_MENU_TITLE)); }
			return preyInventories.get(player);
		}
	}

	private Inventory createInventory(TBNRPlayer player, ArrayList<GearzKitItem> items, String name) {
		Inventory inventory = Bukkit.createInventory(player.getPlayer(), 36, name);
		for(GearzKitItem item : items) {
			inventory.addItem(item.getItemStack());
		}
		return inventory;
	}

	/*
    public void getChoosingMenu() {
        this.preyItems.clear();
        this.preyMenu = null;
        this.preyGUIOpen.clear();

        this.predatorItems.clear();
        this.predatorMenu = null;
        this.predatorGUIOpen.clear();

        JSONObject prey = GearzClassSelector.getJSONResource(PREY_FILE, getPlugin());
        try {
            this.preyItems.addAll(GearzKit.classFromJsonObject(prey).getItems());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<InventoryGUI.InventoryGUIItem> inventoryPreyItems = new ArrayList<>();
        for(GearzKitItem item : this.preyItems) {
            inventoryPreyItems.add(new InventoryGUI.InventoryGUIItem(item.getItemStack(), item.getItemMeta().getTitle()));
        }

	    Gearz.getInstance().getLogger().severe(getPluginFormat("formats.prey-inventory-title", false));

        this.preyMenu = new InventoryGUI(
                inventoryPreyItems,
                getPluginFormat("formats.prey-inventory-title", false),
                new InventoryGUI.InventoryGUICallback() {
                    @Override
                    public void onItemSelect(InventoryGUI gui, InventoryGUI.InventoryGUIItem item, Player player) {
                        player.getInventory().addItem(item.getItem());
                        ArrayList<InventoryGUI.InventoryGUIItem> items = gui.getItems();
                        ItemStack air = new ItemStack(Material.AIR);
                        items.set(items.indexOf(item), new InventoryGUI.InventoryGUIItem(air, ""));
                        gui.updateContents(items);
                        preyGUIOpen.add(TBNRPlayer.playerFromPlayer(player));
                    }

                    @Override
                    public void onGUIOpen(InventoryGUI gui, Player player){

                    }

                    @Override
                    public void onGUIClose(final InventoryGUI gui, final Player player){
	                    new BukkitRunnable() {
		                    @Override
		                    public void run() {
			                    if(currentState == PRState.CHOOSING) gui.open(player);
		                    }
	                    }.runTaskLater(Gearz.getInstance(), 20);
                    }
                },
                true
        );

        JSONObject predator = GearzClassSelector.getJSONResource(PREDATOR_FILE, getPlugin());
        try {
            this.predatorItems.addAll(GearzKit.classFromJsonObject(predator).getItems());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<InventoryGUI.InventoryGUIItem> inventoryPredatorItems = new ArrayList<>();
        for(GearzKitItem item : this.predatorItems) {
            inventoryPreyItems.add(new InventoryGUI.InventoryGUIItem(item.getItemStack(), item.getItemMeta().getTitle()));
        }

        this.predatorMenu = new InventoryGUI(
                inventoryPredatorItems,
                getPluginFormat("formats.predator-inventory-title", false),
                new InventoryGUI.InventoryGUICallback() {
                    @Override
                    public void onItemSelect(InventoryGUI gui, InventoryGUI.InventoryGUIItem item, Player player) {
                        player.getInventory().addItem(item.getItem());
                        ArrayList<InventoryGUI.InventoryGUIItem> items = gui.getItems();
                        ItemStack air = new ItemStack(Material.AIR);
                        items.set(items.indexOf(item), new InventoryGUI.InventoryGUIItem(air, ""));
                        gui.updateContents(items);
                        predatorGUIOpen.add(TBNRPlayer.playerFromPlayer(player));
                    }

                    @Override
                    public void onGUIOpen(InventoryGUI gui, Player player){

                    }

                    @Override
                    public void onGUIClose(InventoryGUI gui, Player player){
                        if(currentState == PRState.CHOOSING) gui.open(player);
                    }
                },
                true
        );
    }*/

    public void openChoosingMenu() {
        for(TBNRPlayer player : getPlayers()) {
			player.getPlayer().openInventory(getChooser(player));
		}
    }


    public TBNRPlayer getWinner() {
        return null;
    }
}
