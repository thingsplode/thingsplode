/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsplode.server.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.thingsplode.core.domain.entities.Component;
import org.thingsplode.core.domain.entities.Event;

/**
 *
 * @author tamas.csaba@gmail.com
 */
public interface EventRepository extends PagingAndSortingRepository<Event, Long> {

    Page<Event> findByComponent(Component component, Pageable pageable);
}