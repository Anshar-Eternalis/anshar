package com.lgmrszd.anshar.frequency;

import static com.lgmrszd.anshar.Anshar.MOD_ID;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.util.Identifier;

public interface IFrequencyIdentifierComponent extends Component {
    public static final ComponentKey<IFrequencyIdentifierComponent> KEY = ComponentRegistry.getOrCreate(
        new Identifier(MOD_ID, "frequency_identifier"), IFrequencyIdentifierComponent.class
    );

    public IFrequencyIdentifier get();
}