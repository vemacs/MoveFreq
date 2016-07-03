package es.nkmem.da.movefreq.packets;

import com.comphenix.packetwrapper.WrapperPlayClientPositionLook;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(exclude={"initTime"})
@ToString
public class PositionLookPacket {
    private long initTime;

    private double x;
    private double y;
    private double z;

    private float yaw;
    private float pitch;

    private boolean onGround;

    public PositionLookPacket(WrapperPlayClientPositionLook wrapper) {
        this.initTime = System.currentTimeMillis();
        this.x = wrapper.getX();
        this.y = wrapper.getY();
        this.z = wrapper.getZ();

        this.yaw = wrapper.getYaw();
        this.pitch = wrapper.getPitch();
        this.onGround = wrapper.getOnGround();
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - initTime) > 50;
    }

    public void apply(WrapperPlayClientPositionLook wrapper) {
        wrapper.setX(x);
        wrapper.setY(y);
        wrapper.setZ(z);

        wrapper.setYaw(yaw);
        wrapper.setPitch(pitch);
        wrapper.setOnGround(onGround);
    }
}
