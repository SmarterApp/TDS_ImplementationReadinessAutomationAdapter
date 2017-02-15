package org.cresst.sb.irp.automation.adapter.rollback;

/**
 * When errors occur or automation is complete, implementors will rollback data that was inserted into the vendor's system.
 */
public interface Rollbacker {
    /**
     * Rollback data that was inserted into the vendor's system.
     */
     void rollback();
}
