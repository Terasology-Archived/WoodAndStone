/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.herbalism.system;

import com.google.common.collect.ImmutableSet;
import org.terasology.gestalt.assets.AssetDataProducer;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetDataProducer;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureData;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.engine.rendering.assets.texture.TextureUtil;
import org.terasology.gestalt.naming.Name;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterAssetDataProducer
public class HerbIconAssetResolver implements AssetDataProducer<TextureData>  {
    private static final Name HERBALISM_MODULE = new Name("herbalism");

    private AssetManager assetManager;

    public HerbIconAssetResolver(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public static String getHerbUri(String iconUri, float[] hueValues) {
        StringBuilder sb = new StringBuilder();
        sb.append("Herbalism:Herb(");
        sb.append(iconUri);
        for (float hueValue : hueValues) {
            sb.append(",").append(String.valueOf(hueValue));
        }
        sb.append(")");

        return sb.toString();
    }


    @Override
    public Set<ResourceUrn> getAvailableAssetUrns() {
        return Collections.emptySet();
    }

    @Override
    public Set<Name> getModulesProviding(Name resourceName) {
        if (!resourceName.toLowerCase().startsWith("herb(")) {
            return Collections.emptySet();
        }
        return ImmutableSet.of(HERBALISM_MODULE);
    }

    @Override
    public ResourceUrn redirect(ResourceUrn urn) {
        return urn;
    }

    @Override
    public Optional<TextureData> getAssetData(ResourceUrn urn) throws IOException {
        final String assetName = urn.getResourceName().toString().toLowerCase();
        if (!HERBALISM_MODULE.equals(urn.getModuleName())
                || !assetName.startsWith("herb(")) {
            return Optional.empty();
        }
        String[] split = assetName.split("\\(");

        String parameters = split[1].substring(0, split[1].length() - 1);
        String[] parameterValues = parameters.split(",");
        String textureResourceUri = parameterValues[0];

        Optional<TextureRegionAsset> resourceImageAsset = assetManager.getAsset(textureResourceUri,
                TextureRegionAsset.class);
        BufferedImage resourceImage = TextureUtil.convertToImage(resourceImageAsset.get());
        int imageSize = resourceImage.getHeight();

        int frameCount = resourceImage.getWidth() / imageSize;

        if (frameCount != parameterValues.length - 1) {
            return Optional.empty();
        }

        BufferedImage resultImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);

        float[] hsv = new float[3];

        for (int i = 0; i < frameCount; i++) {
            float hue = Float.parseFloat(parameterValues[i + 1]);
            for (int y = 0; y < imageSize; y++) {
                for (int x = 0; x < imageSize; x++) {
                    int argb = resourceImage.getRGB(x + i * imageSize, y);
                    int a = (argb >> 24) & 0xFF;
                    if (a > 0) {
                        Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, hsv);

                        int resultRgb = Color.HSBtoRGB(hue, hsv[1], hsv[2]);
                        int resultArgb = (a << 24) | (resultRgb & 0x00FFFFFF);
                        resultImage.setRGB(x, y, resultArgb);
                    }
                }
            }
        }

        final ByteBuffer byteBuffer = TextureUtil.convertToByteBuffer(resultImage);
        return Optional.of(new TextureData(resultImage.getWidth(), resultImage.getHeight(),
                new ByteBuffer[]{byteBuffer}, Texture.WrapMode.REPEAT, Texture.FilterMode.NEAREST));
    }

}
