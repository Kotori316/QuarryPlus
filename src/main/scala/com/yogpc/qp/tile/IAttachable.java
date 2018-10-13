package com.yogpc.qp.tile;

import net.minecraft.util.EnumFacing;

public interface IAttachable {
    /**
     * Internal use only.
     *
     * @param attachments must have returned true by {@link IAttachable#isValidAttachment(IAttachment.Attachments)}.
     */
    boolean connectAttachment(final EnumFacing facing, final IAttachment.Attachments attachments);

    boolean isValidAttachment(final IAttachment.Attachments attachments);

    default boolean connect(final EnumFacing facing, final IAttachment.Attachments attachments) {
        return isValidAttachment(attachments) && connectAttachment(facing, attachments);
    }
}
