package com.example.shopping_back.chat.service;

import com.example.shopping_back.chat.dto.AiBargainSuggestion;
import com.example.shopping_back.chat.model.ChatMessage;
import com.example.shopping_back.chat.model.Conversation;
import com.example.shopping_back.shop.mapper.ShopProductMapper;
import com.example.shopping_back.shop.model.ProductRecord;
import dev.langchain4j.model.chat.ChatLanguageModel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AiBargainService {
    private final ChatMessageService chatMessageService;
    private final ConversationService conversationService;
    private final Optional<ChatLanguageModel> chatModel;
    private final ShopProductMapper shopProductMapper;

    public AiBargainService(ChatMessageService chatMessageService,
                            ConversationService conversationService,
                            Optional<ChatLanguageModel> chatModel,
                            ShopProductMapper shopProductMapper) {
        this.chatMessageService = chatMessageService;
        this.conversationService = conversationService;
        this.chatModel = chatModel;
        this.shopProductMapper = shopProductMapper;
    }

    public AiBargainSuggestion getBargainSuggestion(Integer covId, Integer myUserId, String role) {
        Conversation conversation = conversationService.getConversation(covId);
        ProductRecord product = conversation == null ? null : shopProductMapper.selectById(conversation.getGoodsId());
        List<ChatMessage> history = chatMessageService.getRecentMessages(covId, 5);
        boolean seller = isSeller(role);
        String fallback = seller
                ? fallbackSellerSuggestion(product, history)
                : fallbackBuyerSuggestion(product, history);

        if (chatModel.isEmpty()) {
            return new AiBargainSuggestion(fallback, "fallback");
        }

        try {
            String prompt = seller
                    ? buildSellerPrompt(product, history, myUserId)
                    : buildBuyerPrompt(product, history, myUserId);
            String answer = chatModel.get().generate(prompt);
            if (answer == null || answer.trim().isEmpty()) {
                return new AiBargainSuggestion(fallback, "fallback");
            }
            return new AiBargainSuggestion(answer.trim(), "ai");
        } catch (RuntimeException e) {
            return new AiBargainSuggestion(fallback, "fallback");
        }
    }

    /**
     * AI 自动回复：当买家发送消息后，以卖家身份自动生成回复。
     */
    public AiBargainSuggestion getAutoReply(Integer covId, Integer buyerId) {
        Conversation conversation = conversationService.getConversation(covId);
        ProductRecord product = conversation == null ? null : shopProductMapper.selectById(conversation.getGoodsId());
        List<ChatMessage> history = chatMessageService.getRecentMessages(covId, 5);
        String fallback = fallbackAutoReply(product, history);

        if (chatModel.isEmpty()) {
            return new AiBargainSuggestion(fallback, "fallback");
        }

        try {
            String prompt = buildAutoReplyPrompt(product, history, buyerId);
            String answer = chatModel.get().generate(prompt);
            if (answer == null || answer.trim().isEmpty()) {
                return new AiBargainSuggestion(fallback, "fallback");
            }
            return new AiBargainSuggestion(answer.trim(), "ai");
        } catch (RuntimeException e) {
            return new AiBargainSuggestion(fallback, "fallback");
        }
    }

    private String buildAutoReplyPrompt(ProductRecord product, List<ChatMessage> history, Integer buyerId) {
        String name = product == null ? "当前商品" : defaultText(product.getGoodsName(), "当前商品");
        String price = product == null || product.getPrice() == null ? "未知" : product.getPrice().toPlainString();
        String floorPrice = product == null || product.getFloorPrice() == null ? "未知" : product.getFloorPrice().toPlainString();

        StringBuilder sb = new StringBuilder();
        sb.append("你是松果集市二手交易平台上的AI客服助手，正在以卖家身份回复买家。").append("\n");
        sb.append("你卖的商品：").append(name).append("，标价：").append(price).append("元，你的底价：").append(floorPrice).append("元（不能低于这个价）。").append("\n");
        sb.append("你的任务是促成交易，根据买家消息灵活应对：").append("\n");
        sb.append("- 如果买家在问商品情况（成色、保修、配件等），热情介绍，突出实拍和平台担保").append("\n");
        sb.append("- 如果买家在砍价/问最低价，在底价之上适当让步，给出具体数字让对方觉得有诚意").append("\n");
        sb.append("- 如果买家在犹豫，强调性价比、售后保障这些卖点").append("\n");
        sb.append("要求：像真人卖家一样自然说话，可以用语气词（哈、哦、呀～），控制在80字以内，直接回复买家即可。").append("\n");
        sb.append("最近对话：").append("\n");
        appendHistory(sb, history, buyerId);
        return sb.toString();
    }

    private String fallbackAutoReply(ProductRecord product, List<ChatMessage> history) {
        String name = product == null ? "这件商品" : defaultText(product.getGoodsName(), "这件商品");
        if (hasPriceTalk(history)) {
            BigDecimal price = product == null ? null : product.getPrice();
            if (price != null) {
                return name + "目前标价 " + price.stripTrailingZeros().toPlainString() + " 元，品质有保障，支持平台担保交易。";
            }
        }
        if (hasShippingTalk(history)) {
            return name + "支持快递发货，下单后会尽快为您安排，有疑问可以随时沟通。";
        }
        return "您好，感谢关注" + name + "，有什么可以帮您的吗？";
    }

    private boolean hasShippingTalk(List<ChatMessage> history) {
        return history.stream()
                .map(ChatMessage::getContent)
                .filter(Objects::nonNull)
                .anyMatch(text -> text.contains("邮") || text.contains("快递") || text.contains("发货") || text.contains("物流"));
    }

    private boolean isSeller(String role) {
        return role != null && "seller".equalsIgnoreCase(role.trim());
    }

    private String buildBuyerPrompt(ProductRecord product, List<ChatMessage> history, Integer myUserId) {
        String name = product == null ? "当前商品" : defaultText(product.getGoodsName(), "当前商品");
        String price = product == null || product.getPrice() == null ? "未知" : product.getPrice().toPlainString();
        String floorPrice = product == null || product.getFloorPrice() == null ? "未知" : product.getFloorPrice().toPlainString();

        StringBuilder sb = new StringBuilder();
        sb.append("你是松果集市上买家的砍价助手，帮买家向卖家砍价。").append("\n");
        sb.append("目标商品：").append(name).append("，卖家标价：").append(price).append("元，估计卖家底价在 ").append(floorPrice).append(" 元左右。").append("\n");
        sb.append("根据聊天记录生成一句砍价话术，要求：").append("\n");
        sb.append("- 给出一个具体出价，在底价之上留点空间让卖家还嘴").append("\n");
        sb.append("- 礼貌但坚定，说明理由（学生党、看了很久、真心想要等）").append("\n");
        sb.append("- 像真人买家说话，自然一点，80字以内").append("\n");
        sb.append("最近对话：").append("\n");
        appendHistory(sb, history, myUserId);
        return sb.toString();
    }

    private String buildSellerPrompt(ProductRecord product, List<ChatMessage> history, Integer myUserId) {
        String name = product == null ? "当前商品" : defaultText(product.getGoodsName(), "当前商品");
        String price = product == null || product.getPrice() == null ? "未知" : product.getPrice().toPlainString();
        String floorPrice = product == null || product.getFloorPrice() == null ? "未知" : product.getFloorPrice().toPlainString();

        StringBuilder sb = new StringBuilder();
        sb.append("你是松果集市上的卖家助手，帮卖家回复买家。").append("\n");
        sb.append("你卖的是：").append(name).append("，标价：").append(price).append("元，你的底价是：").append(floorPrice).append("元。").append("\n");
        sb.append("根据聊天记录回复买家，策略：").append("\n");
        sb.append("- 买家砍价时，小幅度让步但守住底价，给出具体数字").append("\n");
        sb.append("- 强调商品成色、实拍图、平台担保这些卖点").append("\n");
        sb.append("- 像真卖家一样说话，自然热情，80字以内").append("\n");
        sb.append("最近对话：").append("\n");
        appendHistory(sb, history, myUserId);
        return sb.toString();
    }

    private void appendHistory(StringBuilder sb, List<ChatMessage> history, Integer myUserId) {
        for (ChatMessage msg : history) {
            String role = Objects.equals(myUserId, msg.getSenderId()) ? "我" : "对方";
            sb.append(role).append("：").append(defaultText(msg.getContent(), "")).append("\n");
        }
    }

    private String fallbackBuyerSuggestion(ProductRecord product, List<ChatMessage> history) {
        String name = product == null ? "这个商品" : defaultText(product.getGoodsName(), "这个商品");
        BigDecimal price = product == null ? null : product.getPrice();
        BigDecimal floor = product == null ? null : product.getFloorPrice();
        BigDecimal offer = suggestedOffer(price, floor);
        boolean hasPriceTalk = hasPriceTalk(history);
        if (offer != null) {
            if (hasPriceTalk) {
                return "如果方便的话，" + name + "按 " + offer.stripTrailingZeros().toPlainString() + " 元成交可以吗？";
            }
            return "我对" + name + "挺感兴趣，" + offer.stripTrailingZeros().toPlainString() + " 元可以出吗？";
        }
        return "我对" + name + "挺感兴趣，价格还能再优惠一点吗？";
    }

    private String fallbackSellerSuggestion(ProductRecord product, List<ChatMessage> history) {
        String name = product == null ? "这件商品" : defaultText(product.getGoodsName(), "这件商品");
        BigDecimal price = product == null ? null : product.getPrice();
        BigDecimal floor = product == null ? null : product.getFloorPrice();
        boolean hasPriceTalk = hasPriceTalk(history);
        if (hasPriceTalk && price != null && floor != null && floor.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal counter = floor.add(price.subtract(floor).multiply(new BigDecimal("0.6"))).setScale(0, RoundingMode.HALF_UP);
            return name + "成色很好，最低能到 " + counter.stripTrailingZeros().toPlainString() + " 元，支持平台担保。";
        }
        if (price != null) {
            return name + "标价 " + price.stripTrailingZeros().toPlainString() + " 元已经很有诚意，支持平台担保交易。";
        }
        return "您好，" + name + "成色很好，价格可以小幅商量，支持平台担保。";
    }

    private boolean hasPriceTalk(List<ChatMessage> history) {
        return history.stream()
                .map(ChatMessage::getContent)
                .filter(Objects::nonNull)
                .anyMatch(text -> text.contains("价") || text.contains("便宜") || text.contains("优惠") || text.contains("少"));
    }

    private BigDecimal suggestedOffer(BigDecimal price, BigDecimal floor) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        if (floor != null && floor.compareTo(BigDecimal.ZERO) > 0 && floor.compareTo(price) < 0) {
            return floor.add(price.subtract(floor).multiply(new BigDecimal("0.35"))).setScale(0, RoundingMode.HALF_UP);
        }
        return price.multiply(new BigDecimal("0.90")).setScale(0, RoundingMode.HALF_UP);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
