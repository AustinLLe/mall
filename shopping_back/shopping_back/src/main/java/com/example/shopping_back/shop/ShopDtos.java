package com.example.shopping_back.shop;

import java.math.BigDecimal;
import java.util.List;

public final class ShopDtos {
    private ShopDtos() {
    }

    public record ProductView(
            String id,
            String scene,
            String category,
            String title,
            String subtitle,
            BigDecimal price,
            BigDecimal originPrice,
            String cover,
            String tag,
            String condition,
            int credit,
            String location,
            String shopName,
            String delivery,
            List<String> service,
            List<String> highlights,
            String story,
            List<KeyValue> params,
            List<ReviewView> reviews,
            List<TimelineNode> timeline,
            List<String> aiTips
    ) {
    }

    public record KeyValue(String key, String value) {
    }

    public record ReviewView(String user, String text, String score, List<String> tags) {
    }

    public record TimelineNode(String date, String title, String text) {
    }

    public record StoreView(String id, String name, String score, String fans, String desc, String badge) {
    }

    public record TopicView(String id, String type, String title, String desc, String heat, String author, String cover, List<String> tags) {
    }

    public record OrderView(String id, String shop, String status, String title, String cover, String type, String service, BigDecimal amount) {
    }

    public record PublishRequest(String title, String scene, String category, BigDecimal price, String condition, String description, String story, String location) {
    }

    public record AiAssistRequest(String productId, String question, BigDecimal offer) {
    }

    public record AiAssistResponse(String answer, List<String> checklist, String consensus) {
    }
}
