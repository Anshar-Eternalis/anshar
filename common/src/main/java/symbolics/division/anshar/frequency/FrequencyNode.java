package symbolics.division.anshar.frequency;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import org.apache.commons.lang3.ArrayUtils;
import symbolics.division.anshar.beacon.BeaconComponent;
import symbolics.division.anshar.serialization.AnsharCodecs;

public class FrequencyNode {

    public static final Codec<FrequencyNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(FrequencyNode::getPos),
            ExtraCodecs.COMPONENT.fieldOf("name").forGetter(FrequencyNode::getName),
            AnsharCodecs.FLOAT_COLOR_RGB.fieldOf("color").forGetter(node -> FloatArrayList.of(node.color[0], node.color[1], node.color[2])) // idk
    ).apply(instance, (pos, name, color) -> new FrequencyNode(pos, name, ArrayUtils.toPrimitive(color.toArray(Float[]::new)))));

    private final Component name;
    private final float[] color;
    private final BlockPos pos;

    public FrequencyNode(BeaconComponent beaconComponent) {
        this.pos = beaconComponent.getBeaconPos();
        this.name = beaconComponent.getName();
        float[] color = beaconComponent.topColor();
        if (color != null && color.length == 3){
            this.color = color;
        } else {
            this.color = new float[]{0, 0, 0};
        }
    }

    private FrequencyNode(BlockPos pos, Component name, float[] color) {
        this.pos = pos;
        this.name = name;
        this.color = color.clone();
    }

    public static FrequencyNode makeFake(BlockPos pos) {
        return new FrequencyNode(pos, Component.literal("?????"), new float[]{1, 1, 1});
    }

    public Component getName() {return name;}
    public float[] getColor() {return color;}
    public BlockPos getPos() {return pos;}
    public int getColorHex() {
        int rgb = (int)(getColor()[0] * 255);
        rgb = (rgb<<8) + (int)(getColor()[1] * 255);
        rgb = (rgb<<8) + (int)(getColor()[2] * 255);
        return rgb;
    }
}
