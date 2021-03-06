/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsplode.core.protocol;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Csaba Tamas
 */
@XmlType(name = "ExecStat")
@XmlEnum
public enum ExecutionStatus {

    @XmlEnumValue("ACK")
    ACKNOWLEDGED,
    @XmlEnumValue("POST")
    POSTED,
    @XmlEnumValue("DEC")
    DECLINED;

    /**
     *
     * @return true if the value of this enum is ACKNOWLEDGED, otherwise false
     */
    public boolean isAcknowledged() {
        if (this == ACKNOWLEDGED || this == POSTED) {
            return true;
        } else {
            return false;
        }
    }
}
