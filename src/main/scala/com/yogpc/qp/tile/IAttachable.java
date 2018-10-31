package com.yogpc.qp.tile;

import net.minecraft.util.EnumFacing;

public interface IAttachable {
    /**
     * Internal use only.
     *
     * @param attachments must have returned true by {@link IAttachable#isValidAttachment(IAttachment.Attachments)}.
     */
    boolean connectAttachment(final EnumFacing facing, final IAttachment.Attachments<? extends APacketTile> attachments);

    boolean isValidAttachment(final IAttachment.Attachments<? extends APacketTile> attachments);

    default boolean connect(final EnumFacing facing, final IAttachment.Attachments<? extends APacketTile> attachments) {
        return isValidAttachment(attachments) && connectAttachment(facing, attachments);
    }
}
