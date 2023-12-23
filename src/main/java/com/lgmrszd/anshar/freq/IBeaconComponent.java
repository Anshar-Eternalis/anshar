package com.lgmrszd.anshar.freq;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;

public interface IBeaconComponent extends Component, ServerTickingComponent {
    void rescanPyramid();

    IBeaconFrequency getFrequency();
}
