package com.communitysurvivalgames.thesurvivalgames.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import com.communitysurvivalgames.thesurvivalgames.exception.ArenaNotFoundException;
import com.communitysurvivalgames.thesurvivalgames.kits.Kit;
import com.communitysurvivalgames.thesurvivalgames.kits.KitItem;
import com.communitysurvivalgames.thesurvivalgames.util.IconMenu;
import com.communitysurvivalgames.thesurvivalgames.util.ItemSerialization;

public class KitManager {
	private List<Kit> kits = new ArrayList<Kit>();
	private IconMenu menu;

	public void loadKits() {
		readKitsFromFiles();

		if (kits.size() == 0)
			saveDefaultKits();

		menu = new IconMenu("Select Your Kit", 18, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				event.getPlayer().sendMessage("You have chosen the " + event.getName() + " kit!");
				try {
					SGApi.getArenaManager().getArena(event.getPlayer()).setPlayerKit(event.getPlayer(), getKit(event.getName()));
				} catch (ArenaNotFoundException e) {
					Bukkit.getLogger().warning(event.getPlayer() + " attempted to select kit " + getKit(event.getName() + " but was not in a game lobby!"));
				}
				event.setWillClose(true);
			}
		}, SGApi.getPlugin());
		int index = 0;
		for (Kit k : kits) {
			menu.setOption(index, k.getIcon(), k.getName(), k.getIconLore());
			index++;
		}
	}

	public void readKitsFromFiles() {
		String[] files = SGApi.getPlugin().getDataFolder().list();

		for (String file : files) {
			if (file.startsWith("kit_")) {
				FileConfiguration kitData = YamlConfiguration.loadConfiguration(new File(SGApi.getPlugin().getDataFolder(), file));

				String kitName = kitData.getString("name");
				String type = kitData.getString("type");
				String[] iconU = kitData.getString("icon").split(":");
				ItemStack icon = null;
				if (iconU.length > 1) {
					if (iconU[0].equalsIgnoreCase("@p")) {
						icon = new ItemStack(Material.POTION);
						Potion potion = new Potion(1);
						potion.setType(PotionType.valueOf(iconU[1]));
						potion.setSplash(Boolean.valueOf(iconU[2]));
					}
				} else {
					icon = new ItemStack(Material.getMaterial(iconU[0]));
				}
				String iconLore = kitData.getString("iconLore");
				String serializedInventory = kitData.getString("items.lvl1.inventory");

				Bukkit.getServer().getLogger().info("Attempting to read inventory string of " + kitName + ". If it errors here, its a problem with this kit.");

				Inventory inventory = ItemSerialization.stringToInventory(serializedInventory); // TODO Not a temp solution, this is awesome!
				List<KitItem> list = new ArrayList<>();
				for (ItemStack itemStack : inventory) {
					KitItem ki = new KitItem();
					ki.setItem(itemStack);
					list.add(ki);
				}

				List<Integer> abilityIds = kitData.getIntegerList("ability-ids");

				kits.add(new Kit(kitName, list, icon, iconLore, abilityIds));
			}
		}
	}

	void saveDefaultKits() {
		SGApi.getPlugin().saveResource("kit_archer.yml", false);
		SGApi.getPlugin().saveResource("kit_crafter.yml", false);
		SGApi.getPlugin().saveResource("kit_enchanter.yml", false);
		SGApi.getPlugin().saveResource("kit_knight.yml", false);
		SGApi.getPlugin().saveResource("kit_notch.yml", false);
		SGApi.getPlugin().saveResource("kit_pacman.yml", false);
		SGApi.getPlugin().saveResource("kit_skeleton.yml", false);
		SGApi.getPlugin().saveResource("kit_toxicologist.yml", false);
		SGApi.getPlugin().saveResource("kit_zelda.yml", false);

		readKitsFromFiles();
	}

	public Kit getKit(String name) {
		for (Kit k : kits) {
			if (k.getName().equalsIgnoreCase(name))
				return k;
		}
		return kits.get(0);
	}

	public List<Kit> getKits() {
		return kits;
	}

	public void displayKitSelectionMenu(Player p) {
		menu.open(p);
	}
}
