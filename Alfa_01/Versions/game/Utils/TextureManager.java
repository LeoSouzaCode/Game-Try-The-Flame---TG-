package com.root.game.Utils;

import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;

public class TextureManager {


    private static final HashMap<String, Texture> cache = new HashMap<>();

    public static Texture get(String path) {

        if (!cache.containsKey(path)) {
            Texture tex = new Texture(com.badlogic.gdx.Gdx.files.internal(path));
            tex.setFilter(Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear);
            cache.put(path, tex);
        }

        return cache.get(path);

    }

    public static void disposeAll() {
        for (Texture tex : cache.values()) {
            tex.dispose();
        }
        cache.clear();
    }
}
