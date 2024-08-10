package symbolics.division.anshar.storage;

import com.mojang.serialization.Codec;

public class EmbeddedStorage {
    public static final Codec<EmbeddedStorage> CODEC = Codec.unit(EmbeddedStorage::new);
}
