package com.example.mall.interaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:interaction;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.auth.base-url=http://127.0.0.1:9",
        "app.catalog.base-url=http://127.0.0.1:9"
})
class TopicServiceTest {
    @Autowired TopicService service;

    @Test
    void createsAndListsTopicsInInteractionDatabase() {
        TopicService.TopicView created = service.create(7, "校园二手数码交流");
        assertThat(service.find(created.topicId()).title()).isEqualTo("校园二手数码交流");
        assertThat(service.list(null, null)).isNotEmpty();
    }

    @Test
    void buyerCanPostCommentAndLike() {
        TopicService.CurrentUser buyer = new TopicService.CurrentUser(100, "alice", "buyer", "");
        TopicService.TopicView topic = service.createTopic(
                new TopicService.TopicCreateRequest("宿舍桌搭", "分享布置", "买家话题", "", java.util.List.of("宿舍")),
                buyer);
        service.createTopicPost(topic.id(), new TopicService.TopicPostRequest("这件很好用", java.util.List.of(), "", ""), buyer);
        var posts = service.topicPosts(topic.id(), buyer);
        assertThat(posts).hasSize(1);
        String postId = posts.get(0).id();
        service.createTopicComment(postId, new TopicService.TopicCommentRequest("同意"), buyer);
        service.toggleLike(postId, buyer);
        var after = service.topicPosts(topic.id(), buyer);
        assertThat(after.get(0).liked()).isTrue();
        assertThat(after.get(0).comments()).hasSize(1);
        assertThat(after.get(0).author()).isEqualTo("alice");
    }
}
