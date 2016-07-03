package es.nkmem.da.movefreq.packets;

import com.comphenix.packetwrapper.WrapperPlayClientLook;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(exclude={"initTime"})
@ToString
public class LookPacket {
    private long initTime;

    private float yaw;
    private float pitch;
    private boolean onGround;

    public LookPacket(WrapperPlayClientLook wrapper) {
        this.initTime = System.currentTimeMillis();
        this.yaw = wrapper.getYaw();
        this.pitch = wrapper.getPitch();
        this.onGround = wrapper.getOnGround();
    }

    public void apply(WrapperPlayClientLook wrapper) {
        wrapper.setYaw(yaw);
        wrapper.setPitch(pitch);
        wrapper.setOnGround(onGround);
    }
}
