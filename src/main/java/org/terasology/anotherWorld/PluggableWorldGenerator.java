/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.anotherWorld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.ChunkView;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.WorldGenerator;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class PluggableWorldGenerator implements WorldGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PluggableWorldGenerator.class);

    private String seed;

    private Vector3i chunkSize = new Vector3i(16, 256, 16);

    private List<ChunkDecorator> chunkDecorators = new LinkedList<>();
    private List<FeatureGenerator> featureGenerators = new LinkedList<>();

    private BiomeProvider biomeProvider;
    private int seaLevel = 32;
    private int maxLevel = 220;

    private LandscapeGenerator landscapeGenerator;
    private SimpleUri uri;
    private float biomeSize = 0.7f;

    public PluggableWorldGenerator(SimpleUri uri) {
        this.uri = uri;
    }

    public void setLandscapeGenerator(LandscapeGenerator landscapeGenerator) {
        this.landscapeGenerator = landscapeGenerator;
    }

    public void addChunkDecorator(ChunkDecorator chunkGenerator) {
        chunkDecorators.add(chunkGenerator);
    }

    public void addFeatureGenerator(FeatureGenerator featureGenerator) {
        featureGenerators.add(featureGenerator);
    }

    public void setSeaLevel(int seaLevel) {
        this.seaLevel = seaLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    /**
     * 0=very small, 1=very large
     *
     * @param biomeSize
     */
    public void setBiomeSize(float biomeSize) {
        this.biomeSize = biomeSize;
    }

    @Override
    public final void initialize() {
    }

    @Override
    public void setWorldSeed(String seed) {
        this.seed = seed;

        biomeProvider = new BiomeProvider(seed, seaLevel, maxLevel, biomeSize);

        appendGenerators();

        landscapeGenerator.initializeWithSeed(seed);

        for (ChunkDecorator chunkDecorator : chunkDecorators) {
            chunkDecorator.initializeWithSeed(seed);
        }

        for (FeatureGenerator featureGenerator : featureGenerators) {
            featureGenerator.initializeWithSeed(seed);
        }
    }

    protected abstract void appendGenerators();

    @Override
    public void applySecondPass(Vector3i chunkPos, ChunkView view) {
        for (FeatureGenerator featureGenerator : featureGenerators) {
            featureGenerator.generateInChunk(chunkPos, view, biomeProvider);
        }
    }

    @Override
    public SimpleUri getUri() {
        return uri;
    }

    @Override
    public void createChunk(Chunk chunk) {
        ChunkInformation chunkInformation = new ChunkInformation();

        landscapeGenerator.generateInChunk(chunk, chunkInformation, seaLevel, 255);

        for (ChunkDecorator chunkDecorator : chunkDecorators) {
            chunkDecorator.generateInChunk(chunk, chunkInformation, biomeProvider, seaLevel);
        }
    }

    @Override
    public float getFog(float x, float y, float z) {
        return biomeProvider.getBaseBiomeAt(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(z + 0.5f)).getFog();
    }

    @Override
    public float getTemperature(float x, float y, float z) {
        return biomeProvider.getTemperature(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }

    @Override
    public float getHumidity(float x, float y, float z) {
        return biomeProvider.getHumidity(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }
}
