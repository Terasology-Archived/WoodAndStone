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

import org.terasology.asset.AssetFactory;
import org.terasology.asset.AssetResolver;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.assets.texture.TextureUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class HerbIconAssetResolver implements AssetResolver<Texture, TextureData> {
    @Override
    public AssetUri resolve(String partialUri) {
        String[] parts = partialUri.split("\\(", 2);
        if (parts.length > 1) {
            AssetUri uri = Assets.resolveAssetUri(AssetType.TEXTURE, parts[0]);
            if (uri != null) {
                return new AssetUri(AssetType.TEXTURE, uri.getModuleName(), partialUri);
            }
        }
        return null;
    }

    @Override
    public Texture resolve(AssetUri uri, AssetFactory<TextureData, Texture> factory) {
        if (!"herbalism".equals(uri.getNormalisedModuleName())
                || !uri.getNormalisedAssetName().startsWith("herb(")) {
            return null;
        }
        String assetName = uri.getAssetName();
        String[] split = assetName.split("\\(");

        String textureResourceUri = split[1].substring(0, split[1].length() - 1);
        String[] hueValuesStr = split[2].substring(0, split[2].length() - 1).split(",");

        BufferedImage resourceImage = TextureUtil.convertToImage(Assets.getTextureRegion(textureResourceUri));
        int imageSize = resourceImage.getHeight();

        int frameCount = resourceImage.getWidth() / imageSize;

        if (frameCount != hueValuesStr.length) {
            return null;
        }

        BufferedImage resultImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);

        float[] hsv = new float[3];

        for (int i = 0; i < frameCount; i++) {
            float hue = Float.parseFloat(hueValuesStr[i]);
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
        return factory.buildAsset(uri, new TextureData(resultImage.getWidth(), resultImage.getHeight(), new ByteBuffer[]{byteBuffer}, Texture.WrapMode.REPEAT, Texture.FilterMode.NEAREST));
    }


}