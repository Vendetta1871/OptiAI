package zh.vendetta.heightmaplod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.Map;

public class HeightmapLODManager {

    private static final Map<ChunkPos, HeightmapLODMesh> meshCache = new HashMap<>();
    private static Entity lastViewEntity;
    private static ICamera lastCamera;

    public static void prepare(Minecraft mc, Entity viewEntity, ICamera camera) {
        if (mc.world == null) return;

        // Сброс кэша, если камера или игрок сильно изменились
        if (lastViewEntity != viewEntity || lastCamera != camera) {
            meshCache.clear();
        }
        lastViewEntity = viewEntity;
        lastCamera = camera;

        int playerChunkX = (int) Math.floor(viewEntity.posX / 16.0);
        int playerChunkZ = (int) Math.floor(viewEntity.posZ / 16.0);

        // Генерируем меш только для чанков вне обычного рендера (8–32 чанков)
        for (int cx = playerChunkX - 32; cx <= playerChunkX + 32; cx++) {
            for (int cz = playerChunkZ - 32; cz <= playerChunkZ + 32; cz++) {
                ChunkPos pos = new ChunkPos(cx, cz);
                double dx = (cx * 16 + 8) - viewEntity.posX;
                double dz = (cz * 16 + 8) - viewEntity.posZ;
                double dist = Math.sqrt(dx * dx + dz * dz);

                if (dist < 128 || dist > 512) {
                    meshCache.remove(pos);
                    continue;
                }

                // Frustum culling (OptiFine уже сделал, но на всякий)
                if (!camera.isBoundingBoxInFrustum(new net.minecraft.util.math.AxisAlignedBB(cx * 16, 0, cz * 16, cx * 16 + 16, 256, cz * 16 + 16))) {
                    meshCache.remove(pos);
                    continue;
                }

                if (!meshCache.containsKey(pos)) {
                    Chunk chunk = mc.world.getChunkProvider().getLoadedChunk(cx, cz);
                    if (chunk != null && chunk.isLoaded()) {
                        HeightmapLODMesh mesh = HeightmapLODMesh.fromChunk(chunk);
                        if (mesh != null) {
                            meshCache.put(pos, mesh);
                        }
                    }
                }
            }
        }
    }

    public static Iterable<HeightmapLODMesh> getMeshes() {
        return meshCache.values();
    }
}
