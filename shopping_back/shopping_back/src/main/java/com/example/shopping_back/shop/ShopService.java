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
                new OrderView("o1", "松果严选数码", "待收货", "AirWave Pro 降噪耳机", "/static/goods/airwave-pro.jpg", "新品", "平台担保", new BigDecimal("699")),
                new OrderView("o2", "阿洛的桌面仓库", "待评价", "ViewTop 27 英寸 2K 显示器", "/static/goods/viewtop-monitor.jpg", "二手", "同城验货", new BigDecimal("680"))
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
            "airwave-pro", "new", "数码影音", "AirWave Pro 降噪耳机", "全新正品 · 48 小时发货 · 支持七天无理由",
            new BigDecimal("699"), new BigDecimal("899"), "/static/goods/airwave-pro.jpg", "官方新品", "全新", 100, "上海", "松果严选数码",
                "顺丰包邮，48 小时内发货", List.of("平台担保", "七天无理由", "官方质保"),
                List.of("45dB 主动降噪", "38 小时续航", "低延迟游戏模式"),
            "适合通勤、学习和线上会议的轻量耳机，主打稳定、舒适和清晰通话。",
            List.of(new KeyValue("品牌", "AirWave"), new KeyValue("连接方式", "蓝牙 5.4"), new KeyValue("续航", "38 小时"), new KeyValue("质保", "一年官方质保")),
            List.of(new ReviewView("晨光买家", "降噪很稳，佩戴一下午也不夹耳。", "4.9", List.of("降噪好", "发货快"))),
            List.of(), List.of("可询问保修政策", "可比较同价位耳机", "可查看发票与质保")
        ));
        products.add(new ProductView(
            "songuo-pad", "new", "数码影音", "松果 Pad 11 学习平板", "新品首发 · 学习办公两用 · 赠保护套",
            new BigDecimal("2299"), new BigDecimal("2599"), "/static/goods/songuo-pad.jpg", "新品首发", "全新", 100, "杭州", "松果严选数码",
            "次日达覆盖核心城市", List.of("平台担保", "官方质保", "学生优惠"),
            List.of("2.5K 护眼屏", "8300mAh 电池", "手写笔低延迟"),
            "面向课程笔记、网课和轻办公场景，兼顾屏幕素质与续航。",
            List.of(new KeyValue("内存", "8GB + 256GB"), new KeyValue("屏幕", "11 英寸 2.5K"), new KeyValue("重量", "485g"), new KeyValue("网络", "Wi-Fi")),
            List.of(new ReviewView("期末冲刺中", "做笔记很顺手，续航够一天课。", "4.7", List.of("学习友好"))),
            List.of(), List.of("可生成学习设备清单", "可估算分期预算")
        ));
        products.add(new ProductView(
            "viewtop-monitor", "used", "数码影音", "ViewTop 27 英寸 2K 显示器", "二手 9 成新 · 无坏点 · 支持当面验货",
            new BigDecimal("680"), new BigDecimal("1099"), "/static/goods/viewtop-monitor.jpg", "同城自提", "9 成新", 97, "广州大学城", "阿洛的桌面仓库",
                "同城自提 / 到付快递", List.of("平台担保", "当面验货", "48 小时售后协商"),
                List.of("2K 分辨率", "接口齐全", "办公游戏都够用"),
            "陪前任主人完成了毕业设计和第一份实习作品集，现在桌面升级，等待下一位使用者。",
            List.of(new KeyValue("品牌", "ViewTop"), new KeyValue("分辨率", "2560 x 1440"), new KeyValue("接口", "HDMI / DP"), new KeyValue("成色", "9 成新")),
                List.of(new ReviewView("桌搭玩家", "卖家说明很细，现场验货顺利。", "4.9", List.of("描述真实"))),
            List.of(new TimelineNode("2024.09", "入手第一天", "用于设计作业和剪辑练习。"), new TimelineNode("2025.06", "完成毕业项目", "屏幕一直稳定，无亮点坏点。"), new TimelineNode("2026.05", "准备流转", "已清洁打包，支持同城验货。")),
                List.of("可帮你砍价到 620-650", "可生成验货清单")
        ));
        products.add(new ProductView(
            "software-book", "used", "图书文创", "软件工程导论与项目管理笔记", "二手教材 · 含重点标注 · 适合课程复习",
            new BigDecimal("18"), new BigDecimal("69"), "/static/goods/software-book.jpg", "学长笔记", "8.5 成新", 99, "武汉", "南湖旧书摊",
            "校园面交 / 普通快递", List.of("真实笔记", "可拍内页", "平台担保"),
            List.of("重点页有标记", "附课程项目清单", "适合期末复习"),
            "上一任主人用它完成了一次软工大作业，夹着需求评审清单和测试用例模板。",
            List.of(new KeyValue("版本", "第 3 版"), new KeyValue("语言", "中文"), new KeyValue("成色", "8.5 成新"), new KeyValue("附赠", "复习提纲")),
            List.of(new ReviewView("赶 ddl 的同学", "笔记很实用，重点划得很清楚。", "4.8", List.of("内容实用"))),
            List.of(new TimelineNode("2025.03", "开始软工课程", "第一章写下需求分析重点。"), new TimelineNode("2025.06", "项目答辩通过", "附带的用例模板帮了大忙。"), new TimelineNode("2026.05", "转给下一届", "希望继续发挥作用。")),
            List.of("可提取重点页", "可生成复习计划")
        ));
        products.add(new ProductView(
            "ergo-chair", "used", "家居生活", "人体工学椅 Pro", "二手 9 成新 · 腰托完整 · 适合宿舍/工位",
            new BigDecimal("420"), new BigDecimal("899"), "/static/goods/ergo-chair.jpg", "大件同城", "9 成新", 95, "成都", "榕树下的小店",
            "同城搬运可协商", List.of("平台担保", "线下验货", "议价空间"),
            List.of("腰托可调", "坐垫回弹正常", "无明显破损"),
            "陪伴过很多个赶项目的夜晚，椅背和扶手状态良好，适合继续服役。",
            List.of(new KeyValue("材质", "网布 + 金属脚"), new KeyValue("功能", "升降 / 后仰 / 腰托"), new KeyValue("成色", "9 成新"), new KeyValue("配送", "同城优先")),
            List.of(new ReviewView("新工位用户", "坐感不错，卖家帮忙叫了车。", "4.6", List.of("服务好"))),
            List.of(new TimelineNode("2024.11", "入驻工作室", "成为第一把正式办公椅。"), new TimelineNode("2025.12", "陪伴项目冲刺", "坐垫和腰托依旧稳定。"), new TimelineNode("2026.05", "搬家出闲置", "同城优先，欢迎试坐。")),
            List.of("可协商同城运费", "可询问坐垫塌陷情况")
        ));
        products.add(new ProductView(
            "desk-lamp", "new", "家居生活", "折叠护眼台灯", "新品 · 宿舍桌面友好 · 三档色温",
            new BigDecimal("89"), new BigDecimal("129"), "/static/goods/desk-lamp.jpg", "宿舍好物", "全新", 100, "深圳", "松果生活馆",
            "满 59 包邮", List.of("七天无理由", "一年质保", "平台担保"),
            List.of("无频闪", "USB-C 供电", "可折叠收纳"),
            "为宿舍、书桌和夜间阅读设计的小型台灯，亮度柔和，收纳方便。",
            List.of(new KeyValue("供电", "USB-C"), new KeyValue("光源", "LED"), new KeyValue("色温", "三档调节"), new KeyValue("功率", "8W")),
            List.of(new ReviewView("夜读党", "光线柔和，不占桌面。", "4.7", List.of("护眼"))),
            List.of(), List.of("可推荐宿舍桌搭组合", "可计算顺手买优惠")
        ));
    }
}
