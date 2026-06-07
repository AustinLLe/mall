package com.example.shopping_back.chat.service;

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import dev.langchain4j.model.chat.ChatLanguageModel;
import com.example.shopping_back.shop.model.ProductRecord;
import com.example.shopping_back.chat.model.ChatMessage;
import com.example.shopping_back.chat.model.Conversation;
import com.example.shopping_back.shop.mapper.ShopProductMapper;

@Service
public class AiBargainService {
    private final ChatMessageService chatMessageService;
    private final ConversationService conversationService;
    private final ChatLanguageModel chatModel; 
    private final ShopProductMapper shopProductMapper;
    public AiBargainService(ChatMessageService chatMessageService, ConversationService conversationService, ChatLanguageModel chatModel, ShopProductMapper shopProductMapper) {
        this.chatMessageService = chatMessageService;
        this.conversationService = conversationService;
        this.chatModel = chatModel;
        this.shopProductMapper = shopProductMapper;
    }

    public String getBargainSuggestion(Integer covId, Integer myUserId) {
        List<ChatMessage> history = chatMessageService.getRecentMessages(covId, 3);
        
        Conversation conv = conversationService.getConversation(covId);
        ProductRecord product = shopProductMapper.selectById(conv.getGoodsId());
        String productInfo = String.format("商品: %s, 定价: %s元, 最低接受价: %s元。", 
                                            product.getGoodsName(), product.getPrice(), product.getFloorPrice());

        // 3. 构建包含商品上下文的 System Prompt
        StringBuilder sb = new StringBuilder();
        sb.append("System: 销售专家。").append("\n");
        sb.append("商品: ").append(product.getGoodsName()).append(", 目标价:").append(product.getPrice()).append(", 底价:").append(product.getFloorPrice()).append("\n");
        sb.append("规则: 友好拒绝低价，强调品质，每句≤10字，总≤40字，不带表情。").append("\n");
        sb.append("对话历史:\n");
        for (ChatMessage msg : history) {
            String role = Objects.equals(myUserId, msg.getSenderId()) ? "我" : "对方";
            sb.append(role).append(": ").append(msg.getContent()).append("\n");
        }
        sb.append("请作为我的AI助手，根据以上对话给出高情商的回复建议。");
        /*sb.append("这是二手交易的对话记录，请分析后给出议价回复建议：\n");

        for (ChatMessage msg : history) {

            String role = Objects.equals(myUserId, msg.getSenderId()) ? "我" : "对方";

            sb.append(role).append(": ").append(msg.getContent()).append("\n");

        }

        sb.append("请作为我的AI助手，根据以上对话给出一条高情商的回复建议。");*/
        return chatModel.generate(sb.toString());
    }
}