/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsplode.core.protocol.cmdrequest;

import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.thingsplode.core.entities.Configuration;
import org.thingsplode.core.protocol.AbstractCommandRequest;

/**
 *
 * @author Csaba Tamas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SyncCmd")
public class SynchronizationCmd extends AbstractCommandRequest {

    
    private Collection<Configuration> configuration;

    /**
     * @return the configuration
     */
    public Collection<Configuration> getConfiguration() {
        return configuration;
    }

    /**
     * @param configuration the configuration to set
     */
    public void setConfiguration(Collection<Configuration> configuration) {
        this.configuration = configuration;
    }
    
}
