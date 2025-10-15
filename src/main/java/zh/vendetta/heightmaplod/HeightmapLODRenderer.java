package zh.vendetta.heightmaplod;

import net.minecraft.client.Minecraft;

public class HeightmapLODRenderer {

    public static void render(Minecraft mc, boolean solidPass) {
        if (mc.world == null || mc.player == null) return;

        // Рендерим все кэшированные мешы
        for (HeightmapLODMesh mesh : HeightmapLODManager.getMeshes()) {
            mesh.render(solidPass);
        }
    }
}
