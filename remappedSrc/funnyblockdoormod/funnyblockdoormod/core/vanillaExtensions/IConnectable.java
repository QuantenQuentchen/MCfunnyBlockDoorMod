package funnyblockdoormod.funnyblockdoormod.core.vanillaExtensions;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

public interface IConnectable {
    boolean canConnect(Direction direction, BlockState state);
}
