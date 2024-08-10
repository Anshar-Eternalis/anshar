package symbolics.division.anshar.beacon;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import symbolics.division.anshar.frequency.FrequencyIdentifier;
import symbolics.division.anshar.frequency.FrequencyNetwork;

import java.util.List;
import java.util.Optional;

public interface BeaconComponent {
    boolean isValid();
    boolean isActive();
    FrequencyIdentifier getEffectiveFrequencyID();

    Optional<FrequencyNetwork> getFrequencyNetwork();

    BlockPos getBeaconPos();

    Component getName();

    void tryPutPlayerIntoNetwork(ServerPlayer player);

    float[] topColor();

    List<EndCrystalComponent> getConnectedEndCrystals();
}
