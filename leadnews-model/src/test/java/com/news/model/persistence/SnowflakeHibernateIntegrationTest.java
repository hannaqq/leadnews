package com.news.model.persistence;

import com.news.model.article.pojos.ApArticle;
import com.news.model.article.pojos.ApArticleConfig;
import com.news.model.article.pojos.ApArticleContent;
import com.news.model.article.pojos.ApCollection;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SnowflakeHibernateIntegrationTest {

    @Test
    void hibernateLoadsGeneratorAndUsesConfiguredNodeCoordinates() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.driver_class", "org.h2.Driver")
                .applySetting("hibernate.connection.url", "jdbc:h2:mem:snowflake;MODE=MySQL")
                .applySetting("hibernate.hbm2ddl.auto", "create-drop")
                .applySetting(SnowflakeIdGenerator.WORKER_ID_PROPERTY, "3")
                .applySetting(SnowflakeIdGenerator.DATACENTER_ID_PROPERTY, "4")
                .build();

        try (SessionFactory sessionFactory = new MetadataSources(registry)
                .addAnnotatedClass(ApArticle.class)
                .addAnnotatedClass(ApArticleConfig.class)
                .addAnnotatedClass(ApArticleContent.class)
                .addAnnotatedClass(ApCollection.class)
                .buildMetadata()
                .buildSessionFactory()) {
            ApArticle article = new ApArticle();
            article.setTitle("Snowflake integration test");
            ApArticleConfig articleConfig = new ApArticleConfig();
            ApArticleContent articleContent = new ApArticleContent();
            ApCollection collection = new ApCollection();

            sessionFactory.inTransaction(session -> {
                session.persist(article);
                session.persist(articleConfig);
                session.persist(articleContent);
                session.persist(collection);
            });

            assertConfiguredNode(article.getId());
            assertConfiguredNode(articleConfig.getId());
            assertConfiguredNode(articleContent.getId());
            assertConfiguredNode(collection.getId());
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private static void assertConfiguredNode(Long id) {
        assertNotNull(id);
        assertEquals(3L, (id >> 12) & 31);
        assertEquals(4L, (id >> 17) & 31);
    }
}
