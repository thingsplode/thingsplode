/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsplode.core.entities;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.thingsplode.core.EnabledState;
import org.thingsplode.core.Location;
import org.thingsplode.core.StatusInfo;

/**
 *
 * @author tamas.csaba@gmail.com
 */
@Entity
@XmlAccessorType(XmlAccessType.FIELD)
@DiscriminatorValue(value = Device.MAIN_TYPE)
@XmlRootElement
public class Device extends Component<Device> {

    @XmlTransient
    public final static String MAIN_TYPE = "DEVICE";

    private String deviceId;
    @XmlTransient
    private Calendar startupDate;
    @XmlTransient
    private Calendar lastHeartBeat;
    private Location location;
    private String hostAddress;

    /**
     * @return the deviceId
     */
    @Column(nullable = false, unique = true)
    @NotNull
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * @param deviceId the deviceId to set
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * @return the lastHeartBeat
     */
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Calendar getLastHeartBeat() {
        return lastHeartBeat;
    }

    /**
     * @param lastHeartBeat the lastHeartBeat to set
     */
    public void setLastHeartBeat(Calendar lastHeartBeat) {
        this.lastHeartBeat = lastHeartBeat;
    }

    /**
     * @return the location
     */
    @Embedded
    public Location getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * @return the ipAddress
     */
    public String getHostAddress() {
        return hostAddress;
    }

    /**
     * @param host the ipAddress to set
     */
    public void setHostAddress(String host) {
        this.hostAddress = host;
    }

    /**
     * @return the startupDate
     */
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Calendar getStartupDate() {
        return startupDate;
    }

    /**
     * @param startupDate the startupDate to set
     */
    public void setStartupDate(Calendar startupDate) {
        this.startupDate = startupDate;
    }

    public static Device create() {
        return new Device();
    }

    public static Device create(String deviceId, String deviceName, EnabledState enabledState, StatusInfo statusInfo) {
        Device d = Device.create();
        d.setDeviceId(deviceId);
        d.setName(deviceName);
        d.setEnabledState(enabledState);
        d.setStatus(statusInfo);
        return d;
    }

    public Device putDeviceId(String deviceId) {
        this.setDeviceId(deviceId);
        return this;
    }

    public Device putStartupDate(Calendar startupDate) {
        this.setStartupDate(startupDate);
        return this;
    }

    public Device putLastHeartbeat(Calendar heartbeatDate) {
        this.setLastHeartBeat(heartbeatDate);
        return this;
    }

    public Device putLocation(Location location) {
        this.setLocation(location);
        return this;
    }

    public Device putIpAddress(String addr) {
        this.setHostAddress(addr);
        return this;
    }

    public Device putIpAddress(InetAddress addr) {
        this.setHostAddress(addr.getHostAddress());
        return this;
    }

    @Override
    protected void setTresholdsScope(Collection<Treshold> tresholds) {
        if (tresholds != null) {
            tresholds.stream().forEach((t) -> {
                t.setScope(Treshold.Scope.DEVICE);
            });
        }
    }
}
