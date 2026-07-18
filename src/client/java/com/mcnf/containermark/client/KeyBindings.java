package com.mcnf.containermark.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyBinding markKey;

    public static void register() {
        markKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.containermark.mark",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            KeyBinding.Category.create(Identifier.of("containermark", "keys"))
        ));
    }
}
