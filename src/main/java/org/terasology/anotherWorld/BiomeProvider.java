package org.terasology.anotherWorld;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BiomeProvider {
    private ConditionsBaseProvider conditions;
    private Collection<Biome> biomes;
    private int seeLevel;
    private int maxLevel;
    private Biome defaultBiome;

    public BiomeProvider(String worldSeed, Collection<Biome> biomes, int seeLevel, int maxLevel, Biome defaultBiome) {
        this.biomes = biomes;
        this.seeLevel = seeLevel;
        this.maxLevel = maxLevel;
        this.defaultBiome = defaultBiome;

        conditions = new ConditionsBaseProvider(worldSeed);
    }

    public Biome getBiomeAt(int x, int y, int z) {
        float temp = getTemperature(x, y, z);
        float hum = getHumidity(x, y, z);

        Set<Biome> biomesMatching = new HashSet<>();
        for (Biome biome : biomes) {
            if (biome.biomeAccepts(temp, hum)) {
                biomesMatching.add(biome);
            }
        }

        if (biomesMatching.size() == 1) {
            return biomesMatching.iterator().next();
        } else if (biomesMatching.size() == 0) {
            return defaultBiome;
        } else {
            // For now - fixed
            BiomeRandomizer biomeRandomizer = new BiomeRandomizer(biomesMatching);
            return biomeRandomizer.getRandomBiome(0f);
        }
    }

    public float getTemperature(int x, int y, int z) {
        float temperatureBase = conditions.getTemperatureAtSeeLevel(x, z);
        if (y == seeLevel) {
            return temperatureBase;
        }

        if (y > seeLevel) {
            return temperatureBase / ((y - seeLevel) / maxLevel + 1);
        } else {
            return temperatureBase * seeLevel / (seeLevel - y);
        }
    }

    public float getHumidity(int x, int y, int z) {
        float humidityBase = conditions.getHumidityAtSeeLevel(x, z);
        if (y == seeLevel) {
            return humidityBase;
        }

        if (y > seeLevel) {
            return humidityBase / ((y - seeLevel) / maxLevel + 1);
        } else {
            return humidityBase / ((seeLevel - y) / maxLevel + 1);
        }
    }
}
