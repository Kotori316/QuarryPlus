package com.yogpc.qp.machines.base;

import net.minecraft.util.Direction;

public interface IAttachable {
    /**
     * @param attachments must have returned true by {@link IAttachable#isValidAttachment(IAttachment.Attachments)}.
     * @param simulate    true to avoid having side effect.
     * @return true if the attachment is (will be) successfully connected.
     */
    boolean connectAttachment(final Direction facing, final IAttachment.Attachments<? extends APacketTile> attachments, boolean simulate);

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
    default boolean connect(final Direction facing, final IAttachment.Attachments<? extends APacketTile> attachments) {
        return isValidAttachment(attachments) && connectAttachment(facing, attachments, true);
    }

    IAttachable dummy = new IAttachable() {
        @Override
        public boolean connectAttachment(Direction facing, IAttachment.Attachments<? extends APacketTile> attachments, boolean simulate) {
            return false;
        }

        @Override
        public boolean isValidAttachment(IAttachment.Attachments<? extends APacketTile> attachments) {
            return false;
        }

        @Override
        public String toString() {
            return "IAttachable Dummy";
        }
    };
}
