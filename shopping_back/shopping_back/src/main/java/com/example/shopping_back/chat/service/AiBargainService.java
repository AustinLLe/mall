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
import org.springframework.stereotype.Service;

@Service
public class AiBargainService {
    private final ChatMessageService chatMessageService;
    private final ConversationService conversationService;
    private final ChatLanguageModel chatModel;
    private final ShopProductMapper shopProductMapper;

    public AiBargainService(ChatMessageService chatMessageService,
                            ConversationService conversationService,
                            ChatLanguageModel chatModel,
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

        try {
            String prompt = seller
                    ? buildSellerPrompt(product, history, myUserId)
                    : buildBuyerPrompt(product, history, myUserId);
            String answer = chatModel.generate(prompt);
            if (answer == null || answer.trim().isEmpty()) {
                return new AiBargainSuggestion(fallback, "fallback");
            }
            return new AiBargainSuggestion(answer.trim(), "ai");
        } catch (RuntimeException e) {
            return new AiBargainSuggestion(fallback, "fallback");
        }
    }

    private boolean isSeller(String role) {
        return role != null && "seller".equalsIgnoreCase(role.trim());
    }

    private String buildBuyerPrompt(ProductRecord product, List<ChatMessage> history, Integer myUserId) {
        String name = product == null ? "当前商品" : defaultText(product.getGoodsName(), "当前商品");
        String price = product == null || product.getPrice() == null ? "未知" : product.getPrice().toPlainString();
        String floorPrice = product == null || product.getFloorPrice() == null ? "未知" : product.getFloorPrice().toPlainString();

        StringBuilder sb = new StringBuilder();
        sb.append("你是二手/新品交易平台里的买家议价助手。").append("\n");
        sb.append("商品：").append(name).append("，标价：").append(price).append("，卖家底价：").append(floorPrice).append("。").append("\n");
        sb.append("请根据最近对话，生成一句可以直接发送给卖家的中文议价回复。").append("\n");
        sb.append("要求：礼貌、具体、不要压价过狠，不超过 45 个中文字符，不要表情。").append("\n");
        sb.append("最近对话：").append("\n");
        appendHistory(sb, history, myUserId);
        return sb.toString();
    }

    private String buildSellerPrompt(ProductRecord product, List<ChatMessage> history, Integer myUserId) {
        String name = product == null ? "当前商品" : defaultText(product.getGoodsName(), "当前商品");
        String price = product == null || product.getPrice() == null ? "未知" : product.getPrice().toPlainString();
        String floorPrice = product == null || product.getFloorPrice() == null ? "未知" : product.getFloorPrice().toPlainString();

        StringBuilder sb = new StringBuilder();
        sb.append("你是二手/新品交易平台里的卖家回复助手。").append("\n");
        sb.append("商品：").append(name).append("，标价：").append(price).append("，你能接受的底价：").append(floorPrice).append("。").append("\n");
        sb.append("请根据买家最近发言，生成一句可以直接发送的中文回复。").append("\n");
        sb.append("要求：友好专业、可小幅让步但不低于底价，强调成色和担保，不超过 45 个中文字符，不要表情。").append("\n");
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
