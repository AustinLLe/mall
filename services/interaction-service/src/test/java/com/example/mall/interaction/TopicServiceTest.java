package com.example.mall.interaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:interaction;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password="})
class TopicServiceTest {
    @Autowired TopicService service;

    @Test
    void createsAndListsTopicsInInteractionDatabase() {
        TopicService.TopicView created = service.create(7, "校园二手数码交流");
        assertThat(service.find(created.topicId()).title()).isEqualTo("校园二手数码交流");
        assertThat(service.list()).isNotEmpty();
    }
}
