package com.yogpc.qp.machines;

import net.minecraft.util.EnumFacing;

public interface IAttachable {
    /**
     * Internal use only.
     *
     * @param attachments must have returned true by {@link IAttachable#isValidAttachment(IAttachment.Attachments)}.
     */
    boolean connectAttachment(final EnumFacing facing, final IAttachment.Attachments<? extends APacketTile> attachments);

    /**
     * @param attachments that you're trying to add.
     * @return whether this machine can accept the attachment.
     */
    boolean isValidAttachment(final IAttachment.Attachments<? extends APacketTile> attachments);

    /**
     * Connect the attachment to this machine.
     *
     * @param facing      the direction of this block.
     * @param attachments you want to add.
     * @return true if the attachment was connected successfully.
     */
    default boolean connect(final EnumFacing facing, final IAttachment.Attachments<? extends APacketTile> attachments) {
        return isValidAttachment(attachments) && connectAttachment(facing, attachments);
    }
}
