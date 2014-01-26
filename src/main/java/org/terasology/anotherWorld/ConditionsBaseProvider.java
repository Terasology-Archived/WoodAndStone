package org.terasology.anotherWorld;

import org.terasology.anotherWorld.util.AlphaFunction;
import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.Noise2D;
import org.terasology.utilities.procedural.SimplexNoise;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ConditionsBaseProvider {
    private final float minMultiplier = 0.0005f;
    private final float maxMultiplier = 0.01f;

    private final Noise2D temperatureNoise;
    private final Noise2D humidityNoise;

    private float noiseMultiplier;
    private AlphaFunction temperatureFunction;
    private AlphaFunction humidityFunction;

    public ConditionsBaseProvider(String worldSeed, float conditionsDiversity, AlphaFunction temperatureFunction, AlphaFunction humidityFunction) {
        this.temperatureFunction = temperatureFunction;
        this.humidityFunction = humidityFunction;
        temperatureNoise = new SimplexNoise(worldSeed.hashCode() + 582374);
        humidityNoise = new SimplexNoise(worldSeed.hashCode() + 129534);
        noiseMultiplier = minMultiplier + (maxMultiplier - minMultiplier) * conditionsDiversity;
    }

    public float getBaseTemperature(int x, int z) {
        double result = temperatureNoise.noise(x * noiseMultiplier, z * noiseMultiplier);
        return temperatureFunction.execute((float) TeraMath.clamp((result + 1.0f) / 2.0f));
    }

    public float getBaseHumidity(int x, int z) {
        double result = humidityNoise.noise(x * noiseMultiplier, z * noiseMultiplier);
        return humidityFunction.execute((float) TeraMath.clamp((result + 1.0f) / 2.0f));
    }
}
