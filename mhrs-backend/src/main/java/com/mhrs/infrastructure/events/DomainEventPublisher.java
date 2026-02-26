package com.mhrs.infrastructure.events;

public interface DomainEventPublisher {

    void publish(Object event);
}
