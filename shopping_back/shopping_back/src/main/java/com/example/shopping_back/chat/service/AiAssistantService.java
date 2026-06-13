package com.example.shopping_back.chat.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AiAssistantService {

    private final Optional<ChatLanguageModel> chatModel;

    public AiAssistantService(Optional<ChatLanguageModel> chatModel) {
        this.chatModel = chatModel;
    }

    public String ask(String question) {
        if (question == null || question.isBlank()) {
            return "请告诉我你想了解什么～";
        }

        String prompt = buildPrompt(question.trim());

        if (chatModel.isEmpty()) {
            return fallbackAnswer(question.trim());
        }

        try {
            String answer = chatModel.get().generate(prompt);
            if (answer == null || answer.trim().isEmpty()) {
                return fallbackAnswer(question.trim());
            }
            return answer.trim();
        } catch (RuntimeException e) {
            return fallbackAnswer(question.trim());
        }
    }

    private String buildPrompt(String question) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是「松果集市」二手交易平台的官方AI助手，回答用户关于平台使用的问题。").append("\n");
        sb.append("你的知识范围：").append("\n");
        sb.append("- 买家如何浏览商品、发起咨询、下单购买、支付、收货确认").append("\n");
        sb.append("- 卖家如何发布商品、管理订单、发货、收款").append("\n");
        sb.append("- 平台担保交易流程：买家付款→平台托管→卖家发货→买家确认→平台打款").append("\n");
        sb.append("- 议价功能：买家可以跟卖家协商价格，AI议价助手可以辅助砍价").append("\n");
        sb.append("- 平台保障：实名认证、交易担保、售后纠纷处理").append("\n");
        sb.append("- 商品分类：新品/二手，发布时需填写成色、价格、实拍图").append("\n");
        sb.append("- 聊天功能：买家和卖家可以通过平台聊天沟通，AI自动回复辅助").append("\n");
        sb.append("- 转人工客服：如果AI无法解决问题，可以转接人工客服").append("\n");
        sb.append("\n");
        sb.append("回答要求：").append("\n");
        sb.append("- 简洁明了，用自然的中文回答，200字以内").append("\n");
        sb.append("- 根据用户身份（买家/卖家）给出针对性建议").append("\n");
        sb.append("- 如果问题超出平台范围，礼貌说明你不知道并建议联系人工客服").append("\n");
        sb.append("- 语气友好热情，像平台工作人员在耐心解答").append("\n");
        sb.append("- 不要使用markdown格式，不要用**加粗**，不要用列表符号，用纯文本和换行分段即可").append("\n");
        sb.append("\n");
        sb.append("用户问题：").append(question).append("\n");
        sb.append("请给出回答：").append("\n");
        return sb.toString();
    }

    private String fallbackAnswer(String question) {
        String q = question.toLowerCase();
        if (q.contains("买") || q.contains("下单") || q.contains("购物") || q.contains("支付")) {
            return "在松果集市购物很简单：浏览商品找到心仪的物品后，点击「去问问」与卖家沟通，确认无误后即可下单付款。平台会托管资金，等你确认收货后再打款给卖家，全程保障交易安全。";
        }
        if (q.contains("卖") || q.contains("发布") || q.contains("上架") || q.contains("出售")) {
            return "想成为松果集市的卖家？点击「发布」按钮，填写商品信息（标题、价格、成色、实拍图等）即可上架。平台提供AI议价助手帮您自动回复买家，交易成功后平台担保收款，安全省心。";
        }
        if (q.contains("担保") || q.contains("安全") || q.contains("保障") || q.contains("纠纷")) {
            return "松果集市提供全程担保交易：买家付款后资金由平台托管，待买家确认收货后平台再打款给卖家。如果出现纠纷，可以申请平台介入处理。所有用户需实名认证，保障交易安全。";
        }
        if (q.contains("运费") || q.contains("发货") || q.contains("物流") || q.contains("快递")) {
            return "卖家在发布商品时可以设置运费方式（包邮或按实际运费）。下单后卖家负责发货，买家可以在订单中查看物流信息。建议买卖双方在聊天中提前确认运费细节。";
        }
        if (q.contains("AI") || q.contains("议价") || q.contains("砍价") || q.contains("客服")) {
            return "松果集市内置AI议价助手：在聊天页面点击「AI议价」按钮，AI会根据商品底价自动生成砍价或回复话术。如果需要人工帮助，随时可以点击「转人工」切换到真人客服。";
        }
        return "你好！我是松果集市的AI助手，可以帮你解答购物、卖货、平台保障等方面的问题。请具体描述你想了解的内容，我会尽力帮你解答～";
    }
}
