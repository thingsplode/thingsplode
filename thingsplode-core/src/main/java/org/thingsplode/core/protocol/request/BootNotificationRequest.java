/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsplode.core.protocol.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.thingsplode.core.entities.Device;
import org.thingsplode.core.protocol.AbstractRequest;

/**
 *
 * @author Csaba Tamas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BootNotificationReq")
public class BootNotificationRequest extends AbstractRequest {

    private Device device;

    public BootNotificationRequest() {
        super();
    }

    public BootNotificationRequest(Device device) {
        super();
        this.device = device;
    }

    public BootNotificationRequest(String deviceId, Long timeStamp, Device device) {
        super(deviceId, timeStamp);
        this.device = device;
    }

    /**
     * @return the device
     */
    public Device getDevice() {
        return device;
    }

    /**
     * @param device the device to set
     */
    public void setDevice(Device device) {
        this.device = device;
    }

    @Override
    public String toString() {
        return "BootNotificationRequest{ " + super.toString() + " device=" + device + '}';
    }
}
