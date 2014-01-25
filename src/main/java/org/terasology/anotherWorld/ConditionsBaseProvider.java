package org.terasology.anotherWorld;

import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.Noise2D;
import org.terasology.utilities.procedural.SimplexNoise;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ConditionsBaseProvider {
    private final float minMultiplier = 0.0005f;
    private final float maxMultiplier = 0.01f;

    private float minTemperature;
    private float maxTemperature;
    private float minHumidity;
    private float maxHumidity;

    private final Noise2D temperatureNoise;
    private final Noise2D humidityNoise;

    private float noiseMultiplier;

    public ConditionsBaseProvider(String worldSeed, float conditionsDiversity, float minTemperature, float maxTemperature, float minHumidity, float maxHumidity) {
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.minHumidity = minHumidity;
        this.maxHumidity = maxHumidity;
        temperatureNoise = new SimplexNoise(worldSeed.hashCode() + 582374);
        humidityNoise = new SimplexNoise(worldSeed.hashCode() + 129534);
        noiseMultiplier = minMultiplier + (maxMultiplier - minMultiplier) * conditionsDiversity;
    }

    public float getBaseHumidity(int x, int z) {
        double result = humidityNoise.noise(x * noiseMultiplier, z * noiseMultiplier);
        return minHumidity + ((float) TeraMath.clamp((result + 1.0f) / 2.0f) * (maxHumidity - minHumidity));
    }

    public float getBaseTemperature(int x, int z) {
        double result = temperatureNoise.noise(x * noiseMultiplier, z * noiseMultiplier);
        return minTemperature + ((float) TeraMath.clamp((result + 1.0f) / 2.0f) * (maxTemperature - minTemperature));
    }
}
