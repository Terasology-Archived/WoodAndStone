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
package org.terasology.was.component;

import com.google.common.hash.HashCode;
import org.terasology.entitySystem.Component;
import org.terasology.logic.inventory.ItemDifferentiating;
import org.terasology.world.block.items.AddToBlockBasedItem;

@ItemDifferentiating
@AddToBlockBasedItem
public class TreeTypeComponent implements Component {
    public String treeType;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TreeTypeComponent that = (TreeTypeComponent) o;

        if (treeType != null ? !treeType.equals(that.treeType) : that.treeType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return HashCode.fromString(treeType).asInt();
    }
}
