package com.wasteofplastic.askygrid.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import com.wasteofplastic.askygrid.ASkyGrid;

/**
 * Provides different results for different world types: normal, nether, etc.
 *
 */
public class WorldStyles {
	private static final Map<World.Environment, WorldStyles> map = new HashMap<>();

    private final BlockProbability prob;
    private final TreeMap<Integer,EntityType> spawns;

    private WorldStyles(BlockProbability prob, TreeMap<Integer,EntityType> spawns) {
	this.prob = prob;
	this.spawns = spawns;
    }

    static {
	map.put(World.Environment.NORMAL, new WorldStyles(normalWorldProbabilities(), normalSpawns()));
	map.put(World.Environment.NETHER, new WorldStyles(netherWorldProbabilities(), netherSpawns()));
	map.put(World.Environment.THE_END, new WorldStyles(endWorldProbabilities(), endSpawns()));
    }

    public static WorldStyles get(World.Environment style) {
	if (!map.containsKey(style))
	    throw new Error("ASkyGrid can only generate The Overworld and The Nether");
	return map.get(style);
    }

    /**
     * @return the block probability
     */
    public BlockProbability getProb() {
	return prob;
    }

    /**
     * @return the spawns
     */
    public TreeMap<Integer,EntityType> getSpawns() {
	return spawns;
    }

    /**
     * Set up the block probabilities for the normal world
     * @return Block Probabilities
     */
    private static BlockProbability normalWorldProbabilities() {
	BlockProbability blockProbability = new BlockProbability();
	FileConfiguration config = ASkyGrid.getPlugin().getConfig();
	int count = 0;
	for (String material: config.getConfigurationSection("world.blocks").getValues(false).keySet()) {
	    try {
		Material blockMaterial = Material.valueOf(material.toUpperCase());
		//Bukkit.getLogger().info("DEBUG: read in material " + blockMaterial + " value " + config.getInt("world.blocks." + material));
		blockProbability.addBlock(blockMaterial, config.getInt("world.blocks." + material));
		count++;
	    } catch (Exception e) {
		Bukkit.getLogger().severe("Do not know what " + material + " is so skipping...");
	    }
	}
	Bukkit.getLogger().info("Loaded " + count + " block types for ASkyGrid over world");
	if (count == 0) {
	    blockProbability.addBlock(Material.STONE, 100);
	    Bukkit.getLogger().severe("Using default stone as only block (fix/update config.yml)");
	}
	return blockProbability;
    }

    /**
     * Nether world probabilities
     * @return
     */
    private static BlockProbability netherWorldProbabilities() {
	BlockProbability blockProbability = new BlockProbability();
	FileConfiguration config = ASkyGrid.getPlugin().getConfig();
	int count = 0;
	for (String material: config.getConfigurationSection("world.netherblocks").getValues(false).keySet()) {
	    try {
		Material blockMaterial = Material.valueOf(material.toUpperCase());
		blockProbability.addBlock(blockMaterial, config.getInt("world.netherblocks." + material));
		count++;
	    } catch (Exception e) {
		Bukkit.getLogger().severe("Do not know what " + material + " is so skipping...");
	    }
	}
	Bukkit.getLogger().info("Loaded " + count + " block types for ASkyGrid nether");
	if (count == 0) {
	    blockProbability.addBlock(Material.NETHERRACK, 100);
		blockProbability.addBlock(Material.LAVA, 300);
	    blockProbability.addBlock(Material.GRAVEL, 30);
		blockProbability.addBlock(Material.SPAWNER, 2);
	    blockProbability.addBlock(Material.CHEST, 1);
	    blockProbability.addBlock(Material.SOUL_SAND, 100);
	    blockProbability.addBlock(Material.GLOWSTONE, 1);
	    blockProbability.addBlock(Material.NETHER_BRICK, 30);
		blockProbability.addBlock(Material.NETHER_BRICK_FENCE, 10);
	    blockProbability.addBlock(Material.NETHER_BRICK_STAIRS,15);
		blockProbability.addBlock(Material.NETHER_WART_BLOCK, 30);
		blockProbability.addBlock(Material.NETHER_QUARTZ_ORE, 15);
	    Bukkit.getLogger().warning("Using default nether blocks (fix/update config.yml)");
	}
	return blockProbability;
    }
    
    /**
     * End world probabilities
     * @return
     */
    private static BlockProbability endWorldProbabilities() {
	BlockProbability blockProbability = new BlockProbability();
	FileConfiguration config = ASkyGrid.getPlugin().getConfig();
	int count = 0;
	for (String material: config.getConfigurationSection("world.endblocks").getValues(false).keySet()) {
	    try {
		Material blockMaterial = Material.valueOf(material.toUpperCase());
		blockProbability.addBlock(blockMaterial, config.getInt("world.endblocks." + material));
		count++;
	    } catch (Exception e) {
		Bukkit.getLogger().severe("Do not know what " + material + " is so skipping...");
	    }
	}
	Bukkit.getLogger().info("Loaded " + count + " block types for ASkyGrid End");
	if (count == 0) {
		blockProbability.addBlock(Material.END_STONE, 300);
	    blockProbability.addBlock(Material.OBSIDIAN, 10);
		blockProbability.addBlock(Material.SPAWNER, 2);
	    blockProbability.addBlock(Material.CHEST, 1);
	    Bukkit.getLogger().warning("Using default end settings for blocks (fix/update config.yml)");
	}
	return blockProbability;
    }

    /**
     * What will come out of spawners
     * @return
     */
    private static TreeMap<Integer,EntityType> normalSpawns() {	
	// Use strings to enable backwards compatibility
		TreeMap<Integer, EntityType> s = new TreeMap<>();
		List<String> types = new ArrayList<>();
	types.add("CREEPER");
	types.add("SKELETON");
	types.add("SPIDER");
	types.add("CAVE_SPIDER");
	types.add("ZOMBIE");
	types.add("SLIME");
	types.add("PIG");
	types.add("SHEEP");
	types.add("COW");
	types.add("CHICKEN");
	types.add("SQUID");
	types.add("WOLF");
	types.add("ENDERMAN");
	types.add("SILVERFISH");
	types.add("VILLAGER");
	types.add("RABBIT");
	types.add("GUARDIAN");
	types.add("HORSE");
	types.add("WITCH");
	types.add("LLAMA");
	types.add("POLAR_BEAR");
	types.add("BAT");
	types.add("HUSK");
	types.add("MULE");
	types.add("MUSHROOM_COW");
	types.add("OCELOT");
	types.add("VINDICATOR");
	types.add("ZOMBIE_HORSE");
	types.add("ZOMBIE_VILLAGER");

	int step = 1000 / types.size();
	int i = step;
	for (EntityType type: EntityType.values()) {
	    if (types.contains(type.toString())) {
		s.put(i, type);
		i += step;
	    }
	}
	//Bukkit.getLogger().info("DEBUG: spawner list = " + s);
	return s;
    }

    /**
     * What will come out of spawners in the nether
     * @return
     */
    private static TreeMap<Integer,EntityType> netherSpawns() {
		TreeMap<Integer, EntityType> s = new TreeMap<>();
		HashMap<String, Integer> types = new HashMap<>();
	types.put("BLAZE", 25);
	types.put("MAGMA_CUBE", 50);
	types.put("SKELETON", 75);
	types.put("WITHER_SKELETON", 20);
	types.put("PIG_ZOMBIE", 75);
	types.put("SKELETON_HORSE", 15);
	
	for (EntityType type: EntityType.values()) {
	    if (types.containsKey(type.toString())) {
		s.put(types.get(type.toString()), type);
	    }
	}
	return s;
    }
    
    /**
     * What will come out of spawners in the end
     * @return
     */
    private static TreeMap<Integer,EntityType> endSpawns() {
		TreeMap<Integer, EntityType> s = new TreeMap<>();
		HashMap<String, Integer> types = new HashMap<>();
	types.put("ENDERMAN", 50);
	types.put("ENDERMITE", 55);
	types.put("SHULKER", 60);
	for (EntityType type: EntityType.values()) {
	    if (types.containsKey(type.toString())) {
		s.put(types.get(type.toString()), type);
	    }
	}

	return s;
    }

}