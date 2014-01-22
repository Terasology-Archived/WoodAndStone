package org.terasology.anotherWorld;

import org.terasology.math.TeraMath;
import org.terasology.utilities.procedural.BrownianNoise3D;
import org.terasology.utilities.procedural.Noise3D;
import org.terasology.utilities.procedural.PerlinNoise;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ConditionsBaseProvider {
    private final Noise3D temperatureNoise;
    private final Noise3D humidityNoise;

    public ConditionsBaseProvider(String worldSeed) {
        temperatureNoise = new BrownianNoise3D(new PerlinNoise(worldSeed.hashCode() + 582374));
        humidityNoise = new BrownianNoise3D(new PerlinNoise(worldSeed.hashCode() + 129534));
    }

    public float getHumidityAtseaLevel(int x, int z) {
        double result = humidityNoise.noise(x * 0.0005, 0, 0.0005 * z);
        return (float) TeraMath.clamp((result + 1.0f) / 2.0f);
    }

    public float getTemperatureAtseaLevel(int x, int z) {
        double result = temperatureNoise.noise(x * 0.0005, 0, 0.0005 * z);
        return (float) TeraMath.clamp((result + 1.0f) / 2.0f);
    }
}
