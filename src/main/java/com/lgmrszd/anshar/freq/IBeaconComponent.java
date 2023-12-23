package com.lgmrszd.anshar.freq;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.world.World;

public interface IBeaconComponent extends Component {
    void rescanPyramid();
    int arraysHashCode();
}
