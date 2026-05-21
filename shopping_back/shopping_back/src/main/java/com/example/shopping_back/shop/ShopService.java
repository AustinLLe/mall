package com.example.shopping_back.shop;

import com.example.shopping_back.shop.ShopDtos.AiAssistRequest;
import com.example.shopping_back.shop.ShopDtos.AiAssistResponse;
import com.example.shopping_back.shop.ShopDtos.KeyValue;
import com.example.shopping_back.shop.ShopDtos.OrderView;
import com.example.shopping_back.shop.ShopDtos.ProductView;
import com.example.shopping_back.shop.ShopDtos.PublishRequest;
import com.example.shopping_back.shop.ShopDtos.ReviewView;
import com.example.shopping_back.shop.ShopDtos.StoreView;
import com.example.shopping_back.shop.ShopDtos.TimelineNode;
import com.example.shopping_back.shop.ShopDtos.TopicView;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShopService {
    private final List<ProductView> products = new ArrayList<>();
    private final List<StoreView> stores = new ArrayList<>();
    private final List<TopicView> topics = new ArrayList<>();

    public ShopService() {
        seed();
    }

    public List<ProductView> products(String scene, String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        return products.stream()
                .filter(item -> scene == null || scene.isBlank() || "all".equals(scene) || item.scene().equals(scene))
                .filter(item -> kw.isBlank() || (item.title() + item.subtitle() + item.category() + item.shopName()).toLowerCase().contains(kw))
                .toList();
    }

    public ProductView product(String id) {
        return products.stream().filter(item -> item.id().equals(id)).findFirst().orElse(products.get(0));
    }

    public List<StoreView> stores() {
        return stores;
    }

    public List<TopicView> topics() {
        return topics;
    }

    public List<OrderView> orders() {
        return List.of(
                new OrderView("o1", "松果严选数码", "待收货", "AirWave Pro 降噪耳机", "🎧", "新品", "平台担保", new BigDecimal("699")),
                new OrderView("o2", "阿洛的桌面仓库", "待评价", "ViewTop 27 英寸 2K 显示器", "🖥️", "二手", "同城验货", new BigDecimal("680"))
        );
    }

    public ProductView publish(PublishRequest request) {
        ProductView created = new ProductView(
                "published-" + (products.size() + 1),
                request.scene() == null ? "used" : request.scene(),
                request.category() == null ? "未分类" : request.category(),
                request.title(),
                "用户新发布 · 等待审核通过后上架",
                request.price() == null ? BigDecimal.ZERO : request.price(),
                request.price() == null ? BigDecimal.ZERO : request.price().add(new BigDecimal("80")),
                "📦",
                "待审核",
                request.condition() == null ? "待补充" : request.condition(),
                96,
                request.location() == null ? "未知地区" : request.location(),
                "我的个人店铺",
                "发布后由卖家设置配送方式",
                List.of("平台担保", "审核上架"),
                List.of("AI 已生成标题建议", "等待管理员审核"),
                request.story() == null ? request.description() : request.story(),
                List.of(new KeyValue("状态", "待审核")),
                List.of(),
                List.of(new TimelineNode("今天", "提交审核", "商品进入管理员审核队列。")),
                List.of("建议补充瑕疵照片", "建议设置最低可接受价")
        );
        products.add(0, created);
        return created;
    }

    public AiAssistResponse assist(AiAssistRequest request) {
        ProductView product = product(request.productId());
        String answer = "我会先确认 " + product.title() + " 的成色、瑕疵、配件和发货方式，再根据卖家信用给出报价建议。";
        List<String> checklist = List.of("确认商品实拍图", "确认是否支持平台担保", "确认瑕疵和售后约定", "保留聊天中的交易共识");
        String consensus = "交易共识：按平台担保下单，卖家如实说明瑕疵，买家收货验货后确认。";
        return new AiAssistResponse(answer, checklist, consensus);
    }

    private void seed() {
        stores.add(new StoreView("store-1", "松果严选数码", "4.9", "1.2w", "新品数码与官方配件，售后响应快。", "官方严选"));
        stores.add(new StoreView("store-2", "南湖旧书摊", "4.8", "6.4k", "课程教材、考研资料和学长笔记流转地。", "校园认证"));
        stores.add(new StoreView("store-3", "榕树下的小店", "4.7", "4.1k", "家居生活闲置为主，重视真实描述。", "信用卖家"));

        topics.add(new TopicView("topic-1", "好物清单", "宿舍桌面升级，哪些二手数码最值得淘？", "整理低预算但体验提升明显的桌面清单。", "2.3w 浏览", "松果编辑部", "🧩", List.of("宿舍桌搭", "二手数码")));
        topics.add(new TopicView("topic-2", "避雷经验", "买二手大件前，最好确认这 5 件事", "验货、物流、瑕疵、售后协商和平台担保。", "1.8w 讨论", "同城交易观察", "🛡️", List.of("交易保障", "验货清单")));

        products.add(new ProductView(
                "new-headphone", "new", "数码影音", "AirWave Pro 降噪耳机", "新品正品 · 48h 发货 · 支持七天无理由",
                new BigDecimal("699"), new BigDecimal("899"), "🎧", "官方新品", "全新", 100, "上海", "松果严选数码",
                "顺丰包邮，48 小时内发货", List.of("平台担保", "七天无理由", "官方质保"),
                List.of("45dB 主动降噪", "38 小时续航", "低延迟游戏模式"),
                "适合通勤、学习和长时间在线会议的轻量耳机。",
                List.of(new KeyValue("品牌", "AirWave"), new KeyValue("连接方式", "蓝牙 5.4")),
                List.of(new ReviewView("晨光买家", "降噪效果很稳。", "4.9", List.of("降噪好"))),
                List.of(), List.of("可回答保修政策", "可比较二手替代品")
        ));
        products.add(new ProductView(
                "used-monitor", "used", "数码影音", "ViewTop 27 英寸 2K 显示器", "二手 9 成新 · 无坏点 · 支持当面验货",
                new BigDecimal("680"), new BigDecimal("1099"), "🖥️", "同城自提", "9 成新", 97, "广州大学城", "阿洛的桌面仓库",
                "同城自提 / 到付快递", List.of("平台担保", "当面验货", "48 小时售后协商"),
                List.of("2K 分辨率", "接口齐全", "办公游戏都够用"),
                "陪前任主人完成了毕业设计和第一份实习作品集。",
                List.of(new KeyValue("品牌", "ViewTop"), new KeyValue("分辨率", "2560 x 1440")),
                List.of(new ReviewView("桌搭玩家", "卖家说明很细，现场验货顺利。", "4.9", List.of("描述真实"))),
                List.of(new TimelineNode("2024.09", "入手第一天", "用于设计作业和剪辑练习。"), new TimelineNode("2026.05", "准备流转", "已清洁打包，支持同城验货。")),
                List.of("可帮你砍价到 620-650", "可生成验货清单")
        ));
    }
}
