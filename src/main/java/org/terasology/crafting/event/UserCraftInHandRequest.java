// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.crafting.event;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@ServerEvent
public class UserCraftInHandRequest implements Event {
    private String recipeId;
    private List<String> parameters;
    private int count;

    public UserCraftInHandRequest() {
    }

    public UserCraftInHandRequest(String recipeId, List<String> parameters, int count) {
        this.recipeId = recipeId;
        this.parameters = parameters;
        this.count = count;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public int getCount() {
        return count;
    }
}
