package net.sujjsteam.tweaks.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public final class WindowIcon {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int[] SIZES = {16, 20, 24, 28, 32, 36, 40, 48, 56, 64, 72, 80, 96, 128, 256, 512};

    private WindowIcon() {
    }

    public static void apply() {
        if (Minecraft.ON_OSX) {
            return;
        }
        List<ByteBuffer> images = new ArrayList<>();
        List<int[]> dimensions = new ArrayList<>();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            for (int size : SIZES) {
                ByteBuffer file = readResource("/assets/sujjstweaks/icon/window_icon_" + size + ".png");
                if (file == null) {
                    continue;
                }
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);
                ByteBuffer image = STBImage.stbi_load_from_memory(file, width, height, channels, 4);
                MemoryUtil.memFree(file);
                if (image == null) {
                    continue;
                }
                images.add(image);
                dimensions.add(new int[]{width.get(0), height.get(0)});
            }
            if (images.isEmpty()) {
                LOGGER.warn("Window icon files were not found");
                return;
            }
            GLFWImage.Buffer icons = GLFWImage.malloc(images.size(), stack);
            for (int i = 0; i < images.size(); i++) {
                icons.position(i);
                icons.width(dimensions.get(i)[0]);
                icons.height(dimensions.get(i)[1]);
                icons.pixels(images.get(i));
            }
            icons.position(0);
            GLFW.glfwSetWindowIcon(Minecraft.getInstance().getWindow().getWindow(), icons);
        } catch (Exception exception) {
            LOGGER.warn("Could not set the window icon", exception);
        } finally {
            for (ByteBuffer image : images) {
                STBImage.stbi_image_free(image);
            }
        }
    }

    private static ByteBuffer readResource(String path) throws IOException {
        try (InputStream input = WindowIcon.class.getResourceAsStream(path)) {
            if (input == null) {
                return null;
            }
            byte[] bytes = input.readAllBytes();
            ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
            buffer.put(bytes).flip();
            return buffer;
        }
    }
}
