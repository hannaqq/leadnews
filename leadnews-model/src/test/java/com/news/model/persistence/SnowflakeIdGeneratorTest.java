package com.news.model.persistence;

import org.hibernate.generator.EventType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SnowflakeIdGeneratorTest {

    @Test
    void generatesUniqueIdentifiersForConfiguredNode() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(3, 4);

        long first = (long) generator.generate(null, null, null, EventType.INSERT);
        long second = (long) generator.generate(null, null, null, EventType.INSERT);

        assertNotEquals(first, second);
    }

    @Test
    void preservesAnExplicitlyAssignedIdentifier() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(0, 0);

        assertEquals(42L, generator.generate(null, null, 42L, EventType.INSERT));
    }

    @Test
    void rejectsInvalidConfiguredCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(32, 0));
    }

    @Test
    void convertsStringAndNumericConfigurationValues() {
        assertEquals(3L, SnowflakeIdGenerator.asLong("3"));
        assertEquals(4L, SnowflakeIdGenerator.asLong(4));
    }
}
