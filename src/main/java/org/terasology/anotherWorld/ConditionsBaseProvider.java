package org.terasology.anotherWorld;

import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.BrownianNoise2D;
import org.terasology.utilities.procedural.Noise2D;
import org.terasology.utilities.procedural.SimplexNoise;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ConditionsBaseProvider {
    private float minTemperature;
    private float maxTemperature;
    private float minHumidity;
    private float maxHumidity;

    private final double noiseScale;
    private final Noise2D temperatureNoise;
    private final Noise2D humidityNoise;

    private float noiseMultiplier;

    public ConditionsBaseProvider(String worldSeed, float biomeSize, float minTemperature, float maxTemperature, float minHumidity, float maxHumidity) {
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.minHumidity = minHumidity;
        this.maxHumidity = maxHumidity;
        BrownianNoise2D brownianNoise2D = new BrownianNoise2D(new SimplexNoise(worldSeed.hashCode() + 582374), 2);
        noiseScale = brownianNoise2D.getScale();
        temperatureNoise = brownianNoise2D;
        humidityNoise = new BrownianNoise2D(new SimplexNoise(worldSeed.hashCode() + 129534), 2);
        noiseMultiplier = 0.0005f + (0.005f - 0.0005f) * biomeSize;
    }

    public float getHumidityAtSeaLevel(int x, int z) {
        double result = humidityNoise.noise(x * noiseMultiplier, z * noiseMultiplier) / noiseScale;
        return minHumidity + ((float) TeraMath.clamp((result + 1.0f) / 2.0f) * (maxHumidity - minHumidity));
    }

    public float getTemperatureAtSeaLevel(int x, int z) {
        double result = temperatureNoise.noise(x * noiseMultiplier, z * noiseMultiplier) / noiseScale;
        return minTemperature + ((float) TeraMath.clamp((result + 1.0f) / 2.0f) * (maxTemperature - minTemperature));
    }
}
