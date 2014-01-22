package org.terasology.anotherWorld;

import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.BrownianNoise2D;
import org.terasology.utilities.procedural.Noise2D;
import org.terasology.utilities.procedural.SimplexNoise;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ConditionsBaseProvider {
    private final Noise2D temperatureNoise;
    private final Noise2D humidityNoise;

    public ConditionsBaseProvider(String worldSeed) {
        temperatureNoise = new BrownianNoise2D(new SimplexNoise(worldSeed.hashCode() + 582374), 2);
        humidityNoise = new BrownianNoise2D(new SimplexNoise(worldSeed.hashCode() + 129534), 2);
    }

    public float getHumidityAtSeaLevel(int x, int z) {
        double result = humidityNoise.noise(x * 0.0005, 0.0005 * z);
        return (float) TeraMath.clamp((result + 1.0f) / 2.0f);
    }

    public float getTemperatureAtSeaLevel(int x, int z) {
        double result = temperatureNoise.noise(x * 0.0005, 0.0005 * z);
        return (float) TeraMath.clamp((result + 1.0f) / 2.0f);
    }
}
