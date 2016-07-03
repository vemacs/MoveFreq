package es.nkmem.da.movefreq.packets;

import com.comphenix.packetwrapper.WrapperPlayClientPosition;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(exclude={"initTime"})
@ToString
public class PositionPacket {
    private long initTime;

    private double x;
    private double y;
    private double z;
    private boolean onGround;

    public PositionPacket(WrapperPlayClientPosition wrapper) {
        this.initTime = System.currentTimeMillis();
        this.x = wrapper.getX();
        this.y = wrapper.getY();
        this.z = wrapper.getZ();
        this.onGround = wrapper.getOnGround();
    }

    public void apply(WrapperPlayClientPosition wrapper) {
        wrapper.setX(x);
        wrapper.setY(y);
        wrapper.setZ(z);
        wrapper.setOnGround(onGround);
    }
}

