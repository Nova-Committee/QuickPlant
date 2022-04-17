package committee.nova.quickplant.api;

import net.minecraft.block.PlantBlock;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

public class ItemPlantMap {
    private static final Map<Item, PlantBlock> plantMap = new HashMap<>();

    public static Map<Item, PlantBlock> getPlantMap() {
        return plantMap;
    }

    public static void add(Item item, PlantBlock plantBlock) {
        plantMap.put(item, plantBlock);
    }

    public static void remove(Item item) {
        plantMap.remove(item);
    }
}
