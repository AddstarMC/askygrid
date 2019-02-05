package com.wasteofplastic.askygrid.generators;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeMap;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.block.data.type.GlassPane;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;

import com.wasteofplastic.askygrid.Settings;


@SuppressWarnings("deprecation")
public class SkyGridPop extends BlockPopulator {
    private final static boolean DEBUG = false;
    private final static HashMap<String, Double> endItems;
    private static RandomSeries slt = new RandomSeries(27);

    static {
        // Hard code these probabilities. TODO: make these config.yml settings.
        endItems = new HashMap<>();
        // double format - integer part is the quantity, decimal is the probability
        endItems.put("FIREWORK", 20.2); // for elytra
        endItems.put("EMERALD", 1.1);
        endItems.put("CHORUS_FRUIT", 3.2);
        endItems.put("ELYTRA", 1.1);
        endItems.put("PURPLE_SHULKER_BOX", 1.2);
    }
    
    private final int size;
    
    /**
     * @param size
     */
    public SkyGridPop(int size) {
        this.size = size;
        // Work out if SpawnEgg method is available
    }
    
    /**
     * Helper method to find out if a method exists in a class. Used for
     * backwards compatibility checking.
     *
     * @param name
     * @param clazz
     *
     * @return Method or null if it does not exist
     */
    private Method getMethod(String name, Class<?> clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }
    
    @Override
    public void populate(World world, Random random, Chunk chunk) {
        if (DEBUG) {
            Bukkit.getLogger().info("DEBUG: populate chunk");
        }
        boolean chunkHasPortal = false;
        int r = 0;
        for (int x = 0; x < 16; x += 4) {
            for (int y = 0; y < size; y += 4) {
                for (int z = 0; z < 16; z += 4) {
                    Block b = chunk.getBlock(x, y, z);
                    // Do an end portal check
                    if (Settings.createEnd && world.getEnvironment().equals(Environment.NORMAL)
                            && x == 0 && z == 0 && y == 0 && !chunkHasPortal) {
                        if (random.nextDouble() < Settings.endPortalProb) {
                            chunkHasPortal = true;
                            for (int xx = 0; xx < 5; xx++) {
                                for (int zz = 0; zz < 5; zz++) {
                                    if (xx == zz || (xx == 0 && zz == 4) || (xx == 4 && zz == 0)) {
                                        continue;
                                    }
                                    if (xx > 0 && xx < 4 && zz > 0 && zz < 4) {
                                        continue;
                                    }
                                    Block frame = chunk.getBlock(xx, 0, zz);
                                    frame.setType(Material.END_PORTAL_FRAME);
                                    EndPortalFrame data = (EndPortalFrame) frame.getBlockData();
                                    // Add the odd eye of ender
                                    if (random.nextDouble() < 0.1) {
                                        data.setEye(true);
                                    }
                                    if (zz == 0) {
                                        data.setFacing(BlockFace.SOUTH);
                                    } else if (zz == 4) {
                                        // Face North
                                        data.setFacing(BlockFace.NORTH);
                                    } else if (xx == 0) {
                                        // Face East
                                        data.setFacing(BlockFace.EAST);
                                    } else if (xx == 4) {
                                        // Face West
                                        data.setFacing(BlockFace.WEST);
                                    }
                                }
                            }
                        }
                    }
                    if (b.getType().equals(Material.AIR)) {
                        continue;
                    }
                    // Alter blocks
                    switch (b.getType()) {
                        case CHEST:
                            if (DEBUG) {
                                Bukkit.getLogger().info("DEBUG: chest");
                            }
                            setChest(b, random);
                            break;
                        case SPAWNER:
                            if (DEBUG) {
                                Bukkit.getLogger().info("DEBUG: mob spawner");
                            }
                            setSpawner(b, random);
                            break;
                        case STONE:
                            if (DEBUG) {
                                Bukkit.getLogger().info("DEBUG: stone");
                            }
                            double type = random.nextDouble();
                            if (type < 0.03) {
                                b.setType(Material.GRANITE);
                            } else if (type < 0.06) {
                                b.setType(Material.DIORITE);// Diorite
                            } else if (type < 0.09) {
                                b.setType(Material.ANDESITE); // Andesite
                            }
                            break;
                        case DIRT:
                            if (DEBUG) {
                                Bukkit.getLogger().info("DIRT");
                            }
                            if (b.getRelative(BlockFace.UP).getType().equals(Material.LEGACY_SAPLING)) {
                                if (Settings.growTrees && random.nextBoolean()) {
                                    // Get biome
                                    Biome biome = b.getBiome();
                                    switch (biome) {
                                        case DESERT:
                                            // never used because dirt = sand in desert
                                            b.getRelative(BlockFace.UP).setType(Material.DEAD_BUSH);
                                            break;
                                        case FOREST:
                                            b.getRelative(BlockFace.UP).setType(Material.AIR);
                                            world.generateTree(b.getRelative(BlockFace.UP).getLocation(), TreeType.BIRCH);
                                            break;
                                        case SWAMP:
                                            b.getRelative(BlockFace.UP).setType(Material.AIR);
                                            world.generateTree(b.getRelative(BlockFace.UP).getLocation(), TreeType.SWAMP);
                                            break;
                                        case GRAVELLY_MOUNTAINS:
                                            b.getRelative(BlockFace.UP).setType(Material.AIR);
                                            world.generateTree(b.getRelative(BlockFace.UP).getLocation(), TreeType.REDWOOD);
                                            break;
                                        case SAVANNA:
                                            b.getRelative(BlockFace.UP).setType(Material.AIR);
                                            world.generateTree(b.getRelative(BlockFace.UP).getLocation(), TreeType.ACACIA);
                                            break;
                                        case JUNGLE:
                                            b.getRelative(BlockFace.UP).setType(Material.AIR);
                                            //b.getRelative(BlockFace.UP).setData((byte)3);
                                            world.generateTree(b.getRelative(BlockFace.UP).getLocation(), TreeType.JUNGLE);
                                            break;
                                        case PLAINS:
                                        default:
                                            b.getRelative(BlockFace.UP).setType(Material.AIR);
                                            world.generateTree(b.getRelative(BlockFace.UP).getLocation(), TreeType.TREE);
                                            break;
                                    }
                                } else {
                                    // Set sapling type
                                    switch (b.getBiome()) {
                                        case JUNGLE:
                                            b.setType(Material.JUNGLE_SAPLING);
                                            break;
                                        case PLAINS:
                                            if (random.nextBoolean()) {
                                                b.setType(Material.BIRCH_SAPLING);
                                                // Birch
                                            }
                                            // else Oak
                                            break;
                                        case TAIGA:
                                        case GRAVELLY_MOUNTAINS:
                                            b.setType(Material.SPRUCE_SAPLING);
                                            break;
                                        case SWAMP:
                                            break;
                                        case DESERT:
                                        case DESERT_HILLS:
                                            b.setType(Material.DEAD_BUSH);
                                            break;
                                        case SAVANNA:
                                            b.setType(Material.ACACIA_SAPLING);
                                            // Acacia
                                            break;
                                        case FOREST:
                                        default:
                                            switch (random.nextInt(6)) {
                                                case 0:
                                                    b.setType(Material.OAK_SAPLING);
                                                    break;
                                                case 1:
                                                    b.setType(Material.JUNGLE_SAPLING);
                                                    break;
                                                case 2:
                                                    b.setType(Material.ACACIA_SAPLING);
                                                    break;
                                                case 3:
                                                    b.setType(Material.SPRUCE_SAPLING);
                                                    break;
                                                case 4:
                                                    b.setType(Material.BIRCH_SAPLING);
                                                    break;
                                                case 5:
                                                    b.setType(Material.DARK_OAK_SAPLING);
                                                    break;
                                            }
                                    }
                                }
                            } else if (b.getRelative(BlockFace.UP).getType().equals(Material.AIR)) {
                                // Randomize the dirt type
                                switch (random.nextInt(3)) {
                                    case 0:
                                        b.setType(Material.DIRT);
                                        break;
                                
                                    case 1:
                                        b.setType(Material.COARSE_DIRT);
                                        break;
                                
                                    case 2:
                                    case 3:
                                        b.setType(Material.PODZOL);
                                        break;
                                }
                            }
                            break;
                        case SAND:
                            if (DEBUG) {
                                Bukkit.getLogger().info("DEBUG SAND");
                            }
                            switch (random.nextInt(2)) {
                                case 0:
                                    b.setType(Material.SAND);
                                    break;
                                case 1:
                                    b.setType(Material.RED_SAND);
                                    break;
                            
                            }
                        case LEGACY_LOG:
                        case LEGACY_LOG_2:
                            if (DEBUG) {
                                Bukkit.getLogger().info("DEBUG: LOG");
                            }
                        
                            if (Settings.createBiomes) {
                                switch (b.getBiome()) {
                                    case JUNGLE:
                                        b.setType(Material.JUNGLE_LOG);
                                        break;
                                    case PLAINS:
                                        if (random.nextBoolean()) {
                                            b.setType(Material.BIRCH_LOG);
                                        }
                                        // else Oak
                                        break;
                                    case TAIGA:
                                    case GRAVELLY_MOUNTAINS:
                                        b.setType(Material.SPRUCE_LOG);
                                        break;
                                    case SWAMP:
                                        break;
                                    case DESERT:
                                    case DESERT_HILLS:
                                        b.setType(Material.SANDSTONE);
                                        switch (random.nextInt(3)) {
                                            case 0:
                                                b.setType(Material.SANDSTONE);
                                                break;
                                            case 1:
                                                b.setType(Material.CHISELED_SANDSTONE);
                                                break;
                                        
                                            case 2:
                                                b.setType(Material.SMOOTH_SANDSTONE);
                                                break;
                                        
                                        }
                                        break;
                                    case SAVANNA:
                                        b.setType(Material.ACACIA_LOG);
                                        break;
                                    case FOREST:
                                    default:
                                        switch (random.nextInt(6)) {
                                            case 0:
                                                b.setType(Material.OAK_LOG);
                                                break;
                                            case 1:
                                                b.setType(Material.JUNGLE_LOG);
                                                break;
                                            case 2:
                                                b.setType(Material.ACACIA_LOG);
                                                break;
                                            case 3:
                                                b.setType(Material.SPRUCE_LOG);
                                                break;
                                            case 4:
                                                b.setType(Material.BIRCH_LOG);
                                                break;
                                            case 5:
                                                b.setType(Material.DARK_OAK_LOG);
                                                break;
                                        }
                                }
                            } else {
                                switch (random.nextInt(6)) {
                                    case 0:
                                        b.setType(Material.OAK_LOG);
                                        break;
                                    case 1:
                                        b.setType(Material.JUNGLE_LOG);
                                        break;
                                    case 2:
                                        b.setType(Material.ACACIA_LOG);
                                        break;
                                    case 3:
                                        b.setType(Material.SPRUCE_LOG);
                                        break;
                                    case 4:
                                        b.setType(Material.BIRCH_LOG);
                                        break;
                                    case 5:
                                        b.setType(Material.DARK_OAK_LOG);
                                        break;
                                }
                            }
                            break;
                        case ACACIA_LEAVES:
                        case OAK_LEAVES:
                        case JUNGLE_LEAVES:
                        case DARK_OAK_LEAVES:
                        case SPRUCE_LEAVES:
                        case BIRCH_LEAVES:
                            if (DEBUG) {
                                Bukkit.getLogger().info("DEBUG: leaves");
                            }
                            switch (random.nextInt(6)) {
                                case 0:
                                    b.setType(Material.OAK_LEAVES);
                                    break;
                                case 1:
                                    b.setType(Material.JUNGLE_LEAVES);
                                    break;
                                case 2:
                                    b.setType(Material.ACACIA_LEAVES);
                                    break;
                            
                                case 3:
                                    b.setType(Material.SPRUCE_LEAVES);
                                    break;
                            
                                case 4:
                                    b.setType(Material.BIRCH_LEAVES);
                                    break;
                                case 5:
                                    b.setType(Material.DARK_OAK_LEAVES);
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                    Block up = b.getRelative(BlockFace.UP);
                    switch (up.getType()) {
                        case LEGACY_LONG_GRASS:
                            if (DEBUG) {
                                Bukkit.getLogger().info("DEBUG: LONG grass");
                            }
                            if (Settings.createBiomes && b.getBiome().toString().contains("DESERT")) {
                                b.getRelative(BlockFace.UP).setType(Material.DEAD_BUSH);
                            } else {
                                switch (random.nextInt(3)) {
                                    case 0:
                                        b.setType(Material.TALL_GRASS);
                                        break;
                                    case 1:
                                        b.setType(Material.LARGE_FERN);
                                        break;
                                    case 2:
                                        b.setType(Material.GRASS);
                                        break;
                                }
                            }
                            break;
                        case LEGACY_RED_ROSE:
                            if (DEBUG) {
                                Bukkit.getLogger().info("DEBUG: red rose");
                            }
                            if (Settings.createBiomes && b.getBiome().toString().contains("DESERT")) {
                                b.getRelative(BlockFace.UP).setType(Material.DEAD_BUSH);
                            } else {
                                switch (random.nextInt(9)) {
                                    case 0:
                                        up.setType(Material.RED_TULIP);
                                        break;
                                    case 1:
                                        up.setType(Material.DANDELION);
                                    case 2:
                                        up.setType(Material.POPPY);
                                    case 3:
                                        up.setType(Material.ORANGE_TULIP);
                                        break;
                                    case 4:
                                        up.setType(Material.PINK_TULIP);
                                        break;
                                    case 5:
                                        up.setType(Material.AZURE_BLUET);
                                        break;
                                    case 6:
                                        up.setType(Material.OXEYE_DAISY);
                                        break;
                                    case 7:
                                        up.setType(Material.ALLIUM);
                                        break;
                                    case 8:
                                    default:
                                        up.setType(Material.POPPY);
                                        break;
                                }
                            }
                            break;
                        case LEGACY_DOUBLE_PLANT:
                            if (DEBUG) {
                                Bukkit.getLogger().info("DEBUG: Double plant");
                            }
                            if (Settings.createBiomes && b.getBiome().toString().contains("DESERT")) {
                                b.getRelative(BlockFace.UP).setType(Material.DEAD_BUSH);
                            } else {
                                switch (random.nextInt(6)) {
                                    case 0:
                                        up.setType(Material.ROSE_BUSH);
                                        break;
                                    case 1:
                                        up.setType(Material.LILAC);
                                        break;
                                    case 2:
                                        up.setType(Material.TALL_GRASS);
                                        break;
                                    case 3:
                                        up.setType(Material.SUNFLOWER);
                                        break;
                                    case 4:
                                        up.setType(Material.LARGE_FERN);
                                        break;
                                    case 5:
                                        up.setType(Material.PEONY);
                                        break;
                                }
                                up.getRelative(BlockFace.UP).setType(up.getType());
                            }
                            break;
                        default:
                            break;
                    }
                    // Nether
                    if (b.getWorld().getEnvironment().equals(Environment.NETHER)) {
                        if (b.getType().equals(Material.STONE)) {
                            b.setType(Material.NETHER_QUARTZ_ORE);
                        }
                    }
                    // End
                    if (b.getWorld().getEnvironment().equals(Environment.THE_END)) {
                        if (DEBUG) {
                            Bukkit.getLogger().info("DEBUG the end " + b);
                        }
                        BlockData d = b.getBlockData();
                        if (d instanceof GlassPane) {
                            b.setType(Material.PURPLE_STAINED_GLASS_PANE);
                        }
                        if (b.getRelative(BlockFace.UP).getType().toString().equals("CHORUS_PLANT")) {
                            if (DEBUG) {
                                Bukkit.getLogger().info("DEBUG Chorus Plant");
                            }
                            world.generateTree(b.getRelative(BlockFace.UP).getLocation(), TreeType.CHORUS_PLANT);
                        }
                        // End crystal becomes hay block in the generator - leave lighting calcs crash server
			/*
			if (b.getRelative(BlockFace.UP).getType().equals(Material.HAY_BLOCK)) {
			    b.getRelative(BlockFace.UP).setType(Material.AIR);
			    b.getWorld().spawn(b.getRelative(BlockFace.UP).getLocation(), EnderCrystal.class);
			}
			 */
                    }
                }
            }
        }
    }
    
    private void setChest(Block b, Random random) {
        //Bukkit.getLogger().info("DEBUG: setChest");
        Chest chest = (Chest) b.getState();
        Inventory inv = chest.getBlockInventory();
        HashSet<ItemStack> set = new HashSet<>();
        // Overworld
        switch (b.getWorld().getEnvironment()) {
            case NETHER:
                if (random.nextDouble() < 0.7) {
                    set.add(itemInRange(256, 294, random)); //weapon/random
                }
                if (random.nextDouble() < 0.7) {
                    ItemStack armor = itemInRange(298, 317, random); //armor
                    if (armor.getType().toString().endsWith("BOOTS")) {
                        if (random.nextDouble() < 0.5) {
                            armor.addEnchantment(Enchantment.PROTECTION_FALL, random.nextInt(4) + 1);
                        }
                    }
                    set.add(armor); //armor
                }
                
                if (random.nextDouble() < 0.9) {
                    // ghast, pigman, enderman
                    set.add(damageInRange(383, 56, 58, random)); //spawn eggs
                } else if (random.nextDouble() < 0.9) {
                    // Blaze, Magma Cube
                    set.add(damageInRange(383, 61, 62, random)); //spawn eggs
                }
                if (random.nextDouble() < 0.3) {
                    Double rand1 = random.nextDouble();
                    if (rand1 < 0.1) {
                        set.add(new ItemStack(Material.CLOCK)); // clock
                    } else if (rand1 < 0.5) {
                        set.add(new ItemStack(Material.BLAZE_ROD));
                    } else if (rand1 < 0.6) {
                        set.add(new ItemStack(Material.SADDLE));
                    } else if (rand1 < 0.7) {
                        set.add(new ItemStack(Material.IRON_HORSE_ARMOR));
                    } else if (rand1 < 0.8) {
                        set.add(new ItemStack(Material.GOLDEN_HORSE_ARMOR));
                    } else if (rand1 < 0.9) {
                        set.add(new ItemStack(Material.DIAMOND_HORSE_ARMOR));
                    } else {
                        set.add(new ItemStack(Material.GHAST_TEAR));
                    }
                }
                break;
            case NORMAL:
                if (random.nextDouble() < 0.7) {
                    set.add(itemInRange(256, 294, random)); //weapon/random
                }
                
                if (random.nextDouble() < 0.7) {
                    set.add(itemInRange(298, 317, random)); //armor
                }
                
                if (random.nextDouble() < 0.7) {
                    set.add(itemInRange(318, 350, random)); //food/tools
                }
                if (random.nextDouble() < 0.3) {
                    // Creeper, skeleton, spider
                    set.add(damageInRange(383, 50, 52, random)); //spawn eggs
                } else if (random.nextDouble() < 0.9) {
                    // Zombie, slime
                    set.add(damageInRange(383, 54, 55, random)); //spawn eggs
                } else if (random.nextDouble() < 0.9) {
                    // Enderman, cave spider, silverfish
                    set.add(damageInRange(383, 58, 60, random)); //spawn eggs
                }
                if (random.nextDouble() < 0.4) {
                    // Sheep, Cow, chicken, squid, wolf, mooshroom
                    set.add(damageInRange(383, 91, 96, random)); //spawn eggs
                }
                if (random.nextDouble() < 0.1) {
                    // Ocelot
                    set.add(new ItemStack(Material.OCELOT_SPAWN_EGG, 1));//ocelot spawn egg
                }
                if (random.nextDouble() < 0.1) {
                    set.add(new ItemStack(Material.VILLAGER_SPAWN_EGG, 1)); //villager spawn egg
                }
                
                if (random.nextDouble() < 0.1) {
                    Double rand = random.nextDouble();
                    if (rand < 0.25) {
                        set.add(new ItemStack(Material.HORSE_SPAWN_EGG, 1)); //horse spawn egg
                    } else if (rand < 0.5) {
                        set.add(new ItemStack(Material.RABBIT_SPAWN_EGG, 1)); //rabbit spawn egg
                    } else if (rand < 0.75) {
                        set.add(new ItemStack(Material.POLAR_BEAR_SPAWN_EGG, 1)); //polar bear spawn egg
                    } else {
                        set.add(new ItemStack(Material.GUARDIAN_SPAWN_EGG, 1)); //guardian spawn egg
                    }
                }
                if (random.nextDouble() < 0.7)
                // Stone, Grass, Dirt, Cobblestone, Planks
                {
                    set.add(itemMas(1, 5, 10, 64, random)); //materials
                }
                
                set.add(damageInRange(6, 0, 5, random)); //sapling
                
                if (random.nextDouble() < 0.1)
                // Prismarine
                {
                    set.add(itemInRange(409, 410, random));
                }
                
                //for dyes
                if (random.nextDouble() < 0.3) {
                    set.add(damageInRange(351, 0, 15, random));
                }
                
                break;
            case THE_END:
                set.add(itemInRange(318, 350, random)); //food/tools
                if (random.nextDouble() < 0.2) {
                    set.add(new ItemStack(Material.ENDERMAN_SPAWN_EGG, 1)); //enderman spawn egg
                }
                if (random.nextDouble() < 0.4) {
                    set.add(itemInRange(256, 294, random)); //weapon/random
                }
                for (Material mat : Material.values()) {
                    if (endItems.containsKey(mat.toString())) {
                        int qty = (int) ((double) endItems.get(mat.toString()));
                        double probability = endItems.get(mat.toString()) - qty;
                        if (random.nextDouble() < probability) {
                            set.add(new ItemStack(mat, qty));
                        }
                    }
                }
                if (random.nextDouble() < 0.2) {
                    set.add(new ItemStack(Material.SHULKER_SPAWN_EGG, 1)); //shulker spawn egg
                }
                break;
            default:
                break;
            
        }
        
        for (ItemStack i : set) {
            inv.setItem(slt.next(random), i);
        }
        slt.reset();
    }
    
    private void setSpawner(Block b, Random random) {
        CreatureSpawner spawner = (CreatureSpawner) b.getState();
        TreeMap<Integer, EntityType> spawns = WorldStyles.get(b.getWorld().getEnvironment()).getSpawns();
        int randKey = random.nextInt(spawns.lastKey());
        //Bukkit.getLogger().info("DEBUG: spawner rand key = " + randKey + " out of " + spawns.lastKey());
        EntityType type = spawns.ceilingEntry(randKey).getValue();
        //Bukkit.getLogger().info("DEBUG: spawner type = " + type);
        spawner.setDelay(120);
        spawner.setSpawnedType(type);
        spawner.update(true);
    }
    
    private ItemStack itemInRange(int min, int max, Random random) {
        ItemType type = LegacyMapper.getInstance().getItemFromLegacy(random.nextInt(max - min + 1) + min);
        return new ItemStack(BukkitAdapter.adapt(type), 1);
    }
    
    private ItemStack damageInRange(int id, int min, int max, Random random) {
        ItemType type = LegacyMapper.getInstance().
                getItemFromLegacy(id, (short) (random.nextInt(max - min + 1) + min));
        return new ItemStack(BukkitAdapter.adapt(type), 1);
    }
    
    
    private ItemStack itemMas(int min, int max, int sm, int lg, Random random) {
        ItemStack stack = itemInRange(min, max, random);
        stack.setAmount(random.nextInt(lg - sm + 1) + sm);
        return stack;
        
    }
}