package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets; // <<< MODIFIED: Added import for Sets
import java.util.Set; // <<< MODIFIED: Added import for Set
import java.util.List; // Still needed for IChunkProvider interface, but chunkListing is now a Set
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkProviderClient implements IChunkProvider
{
    private static final Logger logger = LogManager.getLogger();

    /**
     * The completely empty chunk used by ChunkProviderClient when chunkMapping doesn't contain the requested
     * coordinates.
     */
    private Chunk blankChunk;
    private LongHashMap<Chunk> chunkMapping = new LongHashMap();
    // <<< MODIFIED: Changed from List<Chunk> to Set<Chunk> for better remove performance.
    // Original: private List<Chunk> chunkListing = Lists.<Chunk>newArrayList();
    private Set<Chunk> chunkListing = Sets.<Chunk>newLinkedHashSet();

    /** Reference to the World object. */
    private World worldObj;

    public ChunkProviderClient(World worldIn)
    {
        this.blankChunk = new EmptyChunk(worldIn, 0, 0);
        this.worldObj = worldIn;
    }

    /**
     * Checks to see if a chunk exists at x, z
     */
    public boolean chunkExists(int x, int z)
    {
        return true;
    }

    /**
     * Unload chunk from ChunkProviderClient's hashmap. Called in response to a Packet50PreChunk with its mode field set
     * to false
     */
    public void unloadChunk(int x, int z)
    {
        Chunk chunk = this.provideChunk(x, z);

        if (!chunk.isEmpty())
        {
            chunk.onChunkUnload();
        }

        this.chunkMapping.remove(ChunkCoordIntPair.chunkXZ2Int(x, z));
        // <<< MODIFIED: remove from Set is O(1) on average, was O(N) for ArrayList
        this.chunkListing.remove(chunk);
    }

    /**
     * loads or generates the chunk at the chunk location specified
     *
     * @param chunkX x coord of the chunk to load (block coord >> 4)
     * @param chunkZ z coord of the chunk to load (block coord >> 4)
     */
    public Chunk loadChunk(int chunkX, int chunkZ)
    {
        Chunk chunk = new Chunk(this.worldObj, chunkX, chunkZ);
        this.chunkMapping.add(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ), chunk);
        this.chunkListing.add(chunk); // Add to Set is O(1) on average
        chunk.setChunkLoaded(true);
        return chunk;
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    public Chunk provideChunk(int x, int z)
    {
        Chunk chunk = (Chunk)this.chunkMapping.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
        return chunk == null ? this.blankChunk : chunk;
    }

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
    public boolean saveChunks(boolean saveAllChunks, IProgressUpdate progressCallback)
    {
        return true;
    }

    /**
     * Save extra data not associated with any Chunk.  Not saved during autosave, only during world unload.  Currently
     * unimplemented.
     */
    public void saveExtraData()
    {
    }

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     */
    public boolean unloadQueuedChunks()
    {
        // <<< MODIFIED: Optimized timing logic
        long startTime = System.currentTimeMillis();

        for (Chunk chunk : this.chunkListing)
        {
            // Pass true if more than 5ms have elapsed *since startTime*
            chunk.func_150804_b(System.currentTimeMillis() - startTime > 5L);
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        if (elapsedTime > 100L)
        {
            // Original: logger.info("Warning: Clientside chunk ticking took {} ms", new Object[] {Long.valueOf(System.currentTimeMillis() - i)});
            logger.info("Warning: Clientside chunk ticking took {} ms", elapsedTime);
        }

        return false;
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    public boolean canSave()
    {
        return false;
    }

    /**
     * Populates chunk with ores etc etc
     */
    public void populate(IChunkProvider chunkProvider, int x, int z)
    {
    }

    public boolean populateChunk(IChunkProvider chunkProvider, Chunk chunkIn, int x, int z)
    {
        return false;
    }

    /**
     * Converts the instance data to a readable string.
     */
    public String makeString()
    {
        return "MultiplayerChunkCache: " + this.chunkMapping.getNumHashElements() + ", " + this.chunkListing.size();
    }

    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
    {
        return null;
    }

    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position)
    {
        return null;
    }

    public int getLoadedChunkCount()
    {
        return this.chunkListing.size();
    }

    public void recreateStructures(Chunk chunkIn, int x, int z)
    {
    }

    public Chunk provideChunk(BlockPos blockPosIn)
    {
        return this.provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
    }
}