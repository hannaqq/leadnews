package com.news.model.persistence;

import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.factory.spi.CustomIdGeneratorCreationContext;

import java.lang.reflect.Member;
import java.util.EnumSet;

public final class SnowflakeIdGenerator implements BeforeExecutionGenerator {

    static final String WORKER_ID_PROPERTY = "leadnews.snowflake.worker-id";
    static final String DATACENTER_ID_PROPERTY = "leadnews.snowflake.datacenter-id";

    private final SnowflakeSequence sequence;

    public SnowflakeIdGenerator(
            SnowflakeId annotation,
            Member member,
            CustomIdGeneratorCreationContext context) {
        this(readSetting(context, WORKER_ID_PROPERTY),
                readSetting(context, DATACENTER_ID_PROPERTY));
    }

    SnowflakeIdGenerator(long workerId, long datacenterId) {
        this.sequence = new SnowflakeSequence(workerId, datacenterId);
    }

    @Override
    public Object generate(
            SharedSessionContractImplementor session,
            Object owner,
            Object currentValue,
            EventType eventType) {
        return currentValue != null ? currentValue : sequence.nextId();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }

    @Override
    public boolean allowAssignedIdentifiers() {
        return true;
    }

    private static long readSetting(GeneratorCreationContext context, String property) {
        ConfigurationService configuration = context.getServiceRegistry()
                .getService(ConfigurationService.class);
        return configuration.getSetting(property, SnowflakeIdGenerator::asLong, 0L);
    }

    static Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }
}
