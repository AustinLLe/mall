package com.example.shopping_back.shop;

import com.example.shopping_back.auth.dto.AuthUserView;
import com.example.shopping_back.shop.ShopDtos.AiAssistRequest;
import com.example.shopping_back.shop.ShopDtos.AiAssistResponse;
import com.example.shopping_back.shop.ShopDtos.AiPublishSuggestionRequest;
import com.example.shopping_back.shop.ShopDtos.AiPublishSuggestionResponse;
import com.example.shopping_back.shop.ShopDtos.AuditRequest;
import com.example.shopping_back.shop.ShopDtos.AuditResult;
import com.example.shopping_back.shop.ShopDtos.KeyValue;
import com.example.shopping_back.shop.ShopDtos.OrderView;
import com.example.shopping_back.shop.ShopDtos.ProductView;
import com.example.shopping_back.shop.ShopDtos.PublishRequest;
import com.example.shopping_back.shop.ShopDtos.ReviewView;
import com.example.shopping_back.shop.ShopDtos.StoreView;
import com.example.shopping_back.shop.ShopDtos.TimelineNode;
import com.example.shopping_back.shop.ShopDtos.TopicView;
import com.example.shopping_back.shop.mapper.ShopProductMapper;
import com.example.shopping_back.shop.model.ProductRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ShopService {
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private final List<ProductView> products = new ArrayList<>();
    private final List<StoreView> stores = new ArrayList<>();
    private final List<TopicView> topics = new ArrayList<>();
    private final ShopProductMapper productMapper;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String openAiApiKey;
    private final String openAiModel;
    private final String openAiResponsesUrl;
    private final String dashScopeApiKey;
    private final String dashScopeModel;
    private final String dashScopeChatUrl;

    public ShopService(
            ShopProductMapper productMapper,
            ObjectMapper objectMapper,
            @Value("${openai.api.key:}") String openAiApiKey,
            @Value("${openai.model:gpt-4.1-mini}") String openAiModel,
            @Value("${openai.responses.url:https://api.openai.com/v1/responses}") String openAiResponsesUrl,
            @Value("${dashscope.api.key:}") String dashScopeApiKey,
            @Value("${dashscope.model:qwen-plus}") String dashScopeModel,
            @Value("${dashscope.chat.url:https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions}") String dashScopeChatUrl
    ) {
        this.productMapper = productMapper;
        this.objectMapper = objectMapper;
        this.openAiApiKey = openAiApiKey == null ? "" : openAiApiKey.trim();
        this.openAiModel = openAiModel == null || openAiModel.isBlank() ? "gpt-4.1-mini" : openAiModel.trim();
        this.openAiResponsesUrl = openAiResponsesUrl == null || openAiResponsesUrl.isBlank()
                ? "https://api.openai.com/v1/responses"
                : openAiResponsesUrl.trim();
        this.dashScopeApiKey = dashScopeApiKey == null ? "" : dashScopeApiKey.trim();
        this.dashScopeModel = dashScopeModel == null || dashScopeModel.isBlank() ? "qwen-plus" : dashScopeModel.trim();
        this.dashScopeChatUrl = dashScopeChatUrl == null || dashScopeChatUrl.isBlank()
                ? "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"
                : dashScopeChatUrl.trim();
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        seed();
        ensureProductSchema();
    }

    public List<ProductView> products(String scene, String keyword) {
        try {
            List<ProductRecord> records = productMapper.selectApproved(scene, keyword);
            return records.stream().map(this::toView).toList();
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    public ProductView product(String id) {
        ProductRecord record = productMapper.selectById(parseDbId(id));
        if (record == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        return toView(record);
    }

    public List<ProductView> myProducts(AuthUserView user) {
        Integer userId = user == null ? null : user.getUserId();
        try {
            if (userId != null) {
                List<ProductRecord> records = productMapper.selectBySeller(userId);
                if (!records.isEmpty()) {
                    return records.stream().map(this::toView).toList();
                }
            }
        } catch (RuntimeException ignored) {
        }
        String username = user == null ? "" : user.getUsername();
        return products.stream()
                .filter(item -> item.publisherName() == null || username.isBlank() || username.equals(item.publisherName()))
                .toList();
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

    public ProductView publish(PublishRequest request, AuthUserView user) {
        validatePublish(request);
        ProductView dbCreated = publishToDatabase(request, user);
        if (dbCreated != null) {
            return dbCreated;
        }
        String scene = normalizeScene(request.scene());
        String sellerName = user == null ? "我的个人店铺" : user.getUsername();
        Integer sellerId = user == null ? null : user.getUserId();
        String now = DISPLAY_TIME.format(LocalDateTime.now());
        ProductView created = new ProductView(
                "published-" + (products.size() + 1),
                scene,
                request.category() == null ? "未分类" : request.category(),
                request.title(),
                "用户新发布 · 等待审核通过后上架",
                request.price() == null ? BigDecimal.ZERO : request.price(),
                request.price() == null ? BigDecimal.ZERO : request.price().add(new BigDecimal("80")),
                request.image(),
                "待审核",
                request.condition() == null ? "待补充" : request.condition(),
                user == null || user.getCredit() == null ? 96 : user.getCredit(),
                request.location() == null ? "未知地区" : request.location(),
                sellerName,
                "发布后由卖家设置配送方式",
                List.of("平台担保", "审核上架"),
                List.of("AI 已生成标题建议", "等待管理员审核"),
                request.story() == null ? request.description() : request.story(),
                buildParams(request, "待审核", ""),
                List.of(),
                buildTimeline(scene, request.story(), now, null),
                List.of("建议补充瑕疵照片", "建议设置最低可接受价"),
                "pending",
                sellerId,
                sellerName,
                now,
                "",
                request.floorPrice(),
                request.description()
        );
        products.add(0, created);
        return created;
    }

    public List<ProductView> pendingProducts() {
        List<ProductView> pending = new ArrayList<>();
        try {
            ensureProductSchema();
            List<ProductRecord> records = productMapper.selectPending();
            pending.addAll(records.stream().map(this::toView).toList());
        } catch (RuntimeException ignored) {
        }
        for (ProductView item : products) {
            if (!"pending".equals(item.status())) {
                continue;
            }
            if (pending.stream().noneMatch(current -> current.id().equals(item.id()))) {
                pending.add(item);
            }
        }
        return pending;
    }

    public AuditResult audit(String id, AuditRequest request) {
        String action = request.action() == null ? "" : request.action().trim().toLowerCase(Locale.ROOT);
        String status;
        if ("approve".equals(action)) {
            status = "approved";
        } else if ("reject".equals(action)) {
            status = "rejected";
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "action 只能是 approve 或 reject");
        }
        String reason = "rejected".equals(status) ? defaultText(request.reason(), "信息不完整，请补充后重新提交") : "";
        try {
            ensureProductSchema();
            int affected = productMapper.updateAuditStatus(parseDbId(id), auditStatusForDatabase(status), reason);
            if (affected > 0) {
                return new AuditResult(id, status, reason);
            }
        } catch (RuntimeException ignored) {
        }
        for (int i = 0; i < products.size(); i++) {
            ProductView item = products.get(i);
            if (item.id().equals(id)) {
                ProductView next = withAuditStatus(item, status, reason);
                products.set(i, next);
                return new AuditResult(id, status, reason);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
    }

    public AiAssistResponse assist(AiAssistRequest request) {
        ProductView product = product(request.productId());
        String answer = "我会先确认 " + product.title() + " 的成色、瑕疵、配件和发货方式，再根据卖家信用给出报价建议。";
        List<String> checklist = List.of("确认商品实拍图", "确认是否支持平台担保", "确认瑕疵和售后约定", "保留聊天中的交易共识");
        String consensus = "交易共识：按平台担保下单，卖家如实说明瑕疵，买家收货验货后确认。";
        return new AiAssistResponse(answer, checklist, consensus);
    }

    public AiPublishSuggestionResponse suggestPublish(AiPublishSuggestionRequest request) {
        if (!dashScopeApiKey.isBlank()) {
            try {
                return suggestPublishWithDashScope(request);
            } catch (RuntimeException e) {
                return mockPublishSuggestion(request, "mock_dashscope_failed");
            }
        }
        if (!openAiApiKey.isBlank()) {
            try {
                return suggestPublishWithOpenAi(request);
            } catch (RuntimeException e) {
                return mockPublishSuggestion(request, "mock_api_failed");
            }
        }
        return mockPublishSuggestion(request, "mock_missing_key");
    }

    private AiPublishSuggestionResponse suggestPublishWithDashScope(AiPublishSuggestionRequest request) {
        String scene = normalizeScene(request.scene());
        String category = defaultText(request.category(), "校园好物");
        String condition = defaultText(request.condition(), "9 成新");
        String keyword = defaultText(request.keyword(), category);
        Map<String, Object> payload = Map.of(
                "model", dashScopeModel,
                "messages", List.of(
                        Map.of("role", "system", "content", buildAiInstructions()),
                        Map.of("role", "user", "content", buildAiInput(scene, category, condition, keyword))
                ),
                "temperature", 0.7
        );

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(dashScopeChatUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + dashScopeApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("DashScope API HTTP " + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            JsonNode suggestion = objectMapper.readTree(extractJsonObject(content));
            return toAiSuggestionResponse(request, suggestion, "dashscope");
        } catch (IOException e) {
            throw new IllegalStateException("DashScope API 返回解析失败", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("DashScope API 调用被中断", e);
        }
    }

    private AiPublishSuggestionResponse suggestPublishWithOpenAi(AiPublishSuggestionRequest request) {
        String scene = normalizeScene(request.scene());
        String category = defaultText(request.category(), "校园好物");
        String condition = defaultText(request.condition(), "9 成新");
        String keyword = defaultText(request.keyword(), category);
        Map<String, Object> payload = Map.of(
                "model", openAiModel,
                "instructions", buildAiInstructions(),
                "input", buildAiInput(scene, category, condition, keyword),
                "max_output_tokens", 500
        );

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(openAiResponsesUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("OpenAI API HTTP " + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode suggestion = objectMapper.readTree(extractJsonObject(extractOutputText(root)));
            return toAiSuggestionResponse(request, suggestion, "openai");
        } catch (IOException e) {
            throw new IllegalStateException("OpenAI API 返回解析失败", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OpenAI API 调用被中断", e);
        }
    }

    private AiPublishSuggestionResponse toAiSuggestionResponse(AiPublishSuggestionRequest request, JsonNode suggestion, String source) {
        String scene = normalizeScene(request.scene());
        String category = defaultText(request.category(), "校园好物");
        String condition = defaultText(request.condition(), "9 成新");
        String keyword = defaultText(request.keyword(), category);
        AiPublishSuggestionResponse fallback = mockPublishSuggestion(request, "mock");
        return new AiPublishSuggestionResponse(
                defaultText(suggestion.path("title").asText(), keyword),
                parsePositivePrice(suggestion.path("price"), "used".equals(scene) ? estimateUsedPrice(category, condition) : estimateNewPrice(category)),
                defaultText(suggestion.path("description").asText(), fallback.description()),
                defaultText(suggestion.path("story").asText(), fallback.story()),
                source
        );
    }

    private String buildAiInstructions() {
        return """
                你是校园二手交易平台的商品发布助手。
                请根据用户提供的商品类型、分类、成色和关键词，生成适合发布页直接填充的中文内容。
                必须只输出 JSON，不要 Markdown，不要解释。
                JSON 字段必须为：title, price, description, story。
                price 必须是合理数字，description 60 到 120 字，story 40 到 100 字。
                二手商品要突出真实成色、瑕疵说明、验货方式和流转故事；新品要突出卖点、服务和适用场景。
                """;
    }

    private String buildAiInput(String scene, String category, String condition, String keyword) {
        return """
                商品类型：%s
                分类：%s
                成色：%s
                关键词：%s
                """.formatted("used".equals(scene) ? "二手" : "新品", category, condition, keyword);
    }

    private AiPublishSuggestionResponse mockPublishSuggestion(AiPublishSuggestionRequest request, String source) {
        String scene = normalizeScene(request.scene());
        String category = defaultText(request.category(), "校园好物");
        String condition = defaultText(request.condition(), "9 成新");
        String keyword = defaultText(request.keyword(), category);
        boolean used = "used".equals(scene);
        BigDecimal price = used ? estimateUsedPrice(category, condition) : estimateNewPrice(category);
        String title = (used ? "[二手] " : "[新品] ") + keyword + " · " + (used ? condition : "现货严选");
        String description = used
                ? "这件" + category + "整体为" + condition + "，功能正常，适合校园内当面验货。已尽量清洁整理，关键配件和瑕疵会在交易前补充实拍。"
                : "这款" + category + "适合学习、办公和日常使用，主打稳定、易用和售后省心。建议突出规格、质保和发货时效。";
        String story = used
                ? "它陪上一任主人完成了一段认真使用的日常，现在整理出来转给需要的人，希望继续被好好使用。"
                : "新品卖点可以围绕品质、服务和适用场景展开，让买家快速判断是否适合自己。";
        return new AiPublishSuggestionResponse(title, price, description, story, source);
    }

    private String extractOutputText(JsonNode root) {
        if (root.hasNonNull("output_text")) {
            return root.path("output_text").asText();
        }
        for (JsonNode output : root.path("output")) {
            for (JsonNode content : output.path("content")) {
                String type = content.path("type").asText();
                if ("output_text".equals(type) || "text".equals(type)) {
                    String text = content.path("text").asText();
                    if (!text.isBlank()) {
                        return text;
                    }
                }
            }
        }
        throw new IllegalStateException("OpenAI API 未返回文本内容");
    }

    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("OpenAI API 未返回 JSON 对象");
        }
        return text.substring(start, end + 1);
    }

    private BigDecimal parsePositivePrice(JsonNode node, BigDecimal fallback) {
        try {
            BigDecimal value = node.isNumber() ? node.decimalValue() : new BigDecimal(node.asText());
            return value.compareTo(BigDecimal.ZERO) > 0 ? value : fallback;
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    private void ensureProductSchema() {
        try {
            productMapper.widenStatusColumn();
            if (productMapper.countGoodsColumn("reject_reason") == 0) {
                productMapper.addRejectReasonColumn();
            }
            if (productMapper.countGoodsColumn("reviewed_at") == 0) {
                productMapper.addReviewedAtColumn();
            }
            productMapper.migrateApprovedStatus();
            productMapper.migratePendingStatus();
            productMapper.migrateTruncatedPendingStatus();
        } catch (RuntimeException ignored) {
        }
    }

    private boolean usesWideGoodsStatus() {
        try {
            Integer length = productMapper.statusColumnLength();
            return length != null && length >= 8;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private String pendingStatusForDatabase() {
        return usesWideGoodsStatus() ? "pending" : "2";
    }

    private String auditStatusForDatabase(String status) {
        if (!usesWideGoodsStatus()) {
            return "approved".equals(status) ? "0" : "3";
        }
        return status;
    }

    private ProductView publishToDatabase(PublishRequest request, AuthUserView user) {
        try {
            ensureProductSchema();
            ProductRecord record = new ProductRecord();
            record.setSellerId(user == null ? null : user.getUserId());
            record.setGoodsName(request.title());
            record.setCategory(request.category());
            record.setGoodsDesc(request.description());
            record.setGoodsCondition(defaultText(request.condition(), "待补充"));
            record.setStory(request.story());
            record.setPrice(request.price());
            record.setFloorPrice(request.floorPrice());
            record.setScene(normalizeScene(request.scene()));
            record.setAddress(defaultText(request.location(), "未知地区"));
            record.setImage(request.image());
            record.setStatus(pendingStatusForDatabase());
            productMapper.insert(record);
            ProductRecord selected = productMapper.selectById(record.getGoodsId());
            return toView(selected == null ? record : selected);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private ProductView productFromDatabase(String id) {
        try {
            ProductRecord record = productMapper.selectById(parseDbId(id));
            return record == null ? null : toView(record);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private ProductView toView(ProductRecord record) {
        String scene = normalizeScene(record.getScene());
        String status = normalizeStatus(record.getStatus());
        String title = defaultText(record.getGoodsName(), "未命名商品");
        String description = defaultText(record.getGoodsDesc(), "卖家暂未填写详细描述");
        String condition = defaultText(record.getGoodsCondition(), "待补充");
        BigDecimal price = record.getPrice() == null ? BigDecimal.ZERO : record.getPrice();
        BigDecimal originPrice = price.add("used".equals(scene) ? new BigDecimal("80") : new BigDecimal("120"));
        String publishedAt = formatTime(record.getCreateTime());
        String story = defaultText(record.getStory(), description);
        return new ProductView(
                String.valueOf(record.getGoodsId()),
                scene,
                defaultText(record.getCategory(), "未分类"),
                title,
                statusSubtitle(status, scene, condition),
                price,
                originPrice,
                defaultText(record.getImage(), "/static/goods/viewtop-monitor.jpg"),
                statusTag(status),
                condition,
                record.getSellerCredit() == null ? 96 : record.getSellerCredit(),
                defaultText(record.getAddress(), "未知地区"),
                defaultText(record.getSellerName(), "个人卖家"),
                "发布后由卖家设置配送方式",
                List.of("平台担保", statusTag(status)),
                List.of("真实描述", "pending".equals(status) ? "待审核" : "平台审核记录"),
                story,
                buildParamsFromRecord(record, status),
                List.of(),
                buildTimeline(scene, story, publishedAt, record.getReviewedAt()),
                List.of("可询问成色和配件", "可生成验货清单", "可根据最低价辅助议价"),
                status,
                record.getSellerId(),
                defaultText(record.getSellerName(), "个人卖家"),
                publishedAt,
                defaultText(record.getRejectReason(), ""),
                record.getFloorPrice(),
                description
        );
    }

    private void validatePublish(PublishRequest request) {
        if ("used".equals(normalizeScene(request.scene()))) {
            if (isBlank(request.condition())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "二手商品必须填写成色");
            }
            if (isBlank(request.story())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "二手商品必须填写故事");
            }
        }
    }

    private ProductView withAuditStatus(ProductView item, String status, String reason) {
        LocalDateTime reviewedAt = "approved".equals(status) ? LocalDateTime.now() : null;
        return new ProductView(
                item.id(), item.scene(), item.category(), item.title(), statusSubtitle(status, item.scene(), item.condition()),
                item.price(), item.originPrice(), item.cover(), statusTag(status), item.condition(), item.credit(),
                item.location(), item.shopName(), item.delivery(), List.of("平台担保", statusTag(status)), item.highlights(),
                item.story(), item.params(), item.reviews(), buildTimeline(item.scene(), item.story(), item.publishedAt(), reviewedAt),
                item.aiTips(), status, item.publisherId(), item.publisherName(), item.publishedAt(), reason,
                item.floorPrice(), item.description()
        );
    }

    private List<KeyValue> buildParams(PublishRequest request, String statusText, String rejectReason) {
        List<KeyValue> rows = new ArrayList<>();
        rows.add(new KeyValue("状态", statusText));
        rows.add(new KeyValue("分类", defaultText(request.category(), "未分类")));
        rows.add(new KeyValue("成色", defaultText(request.condition(), "待补充")));
        if (request.floorPrice() != null) {
            rows.add(new KeyValue("最低可接受价", "¥" + request.floorPrice()));
        }
        if (!isBlank(rejectReason)) {
            rows.add(new KeyValue("拒绝原因", rejectReason));
        }
        return rows;
    }

    private List<KeyValue> buildParamsFromRecord(ProductRecord record, String status) {
        List<KeyValue> rows = new ArrayList<>();
        rows.add(new KeyValue("状态", statusTag(status)));
        rows.add(new KeyValue("分类", defaultText(record.getCategory(), "未分类")));
        rows.add(new KeyValue("成色", defaultText(record.getGoodsCondition(), "待补充")));
        if (record.getFloorPrice() != null) {
            rows.add(new KeyValue("最低可接受价", "¥" + record.getFloorPrice()));
        }
        if (!isBlank(record.getRejectReason())) {
            rows.add(new KeyValue("拒绝原因", record.getRejectReason()));
        }
        return rows;
    }

    private List<TimelineNode> buildTimeline(String scene, String story, String publishedAt, LocalDateTime reviewedAt) {
        if (!"used".equals(scene)) {
            return List.of();
        }
        String publishTime = defaultText(publishedAt, "今天");
        String reviewTime = reviewedAt == null ? "待审核" : formatTime(reviewedAt);
        return List.of(
                new TimelineNode("首次购入", "首次购入", "上一任主人把它带进日常生活，开始认真使用。", "🛒"),
                new TimelineNode("使用经历", "使用经历", defaultText(story, "它经历过稳定使用，功能和状态会在交易前充分说明。"), "✨"),
                new TimelineNode(publishTime, "发布转让", "卖家完成清洁整理并提交平台审核。", "📮"),
                new TimelineNode(reviewTime, "审核通过", reviewedAt == null ? "管理员正在核验描述、图片和价格合理性。" : "平台审核通过，商品流转信息已记录。", "✅"),
                new TimelineNode("进行中", "等待新主人", "等待合适的买家接手，继续延长物品的使用价值。", "🏠")
        );
    }

    private BigDecimal estimateUsedPrice(String category, String condition) {
        BigDecimal base = category.contains("数码") ? new BigDecimal("680") : category.contains("图书") ? new BigDecimal("28") : new BigDecimal("180");
        if (condition.contains("全新") || condition.contains("99")) {
            return base.add(new BigDecimal("120"));
        }
        if (condition.contains("8") || condition.contains("瑕疵")) {
            return base.subtract(new BigDecimal("60")).max(new BigDecimal("10"));
        }
        return base;
    }

    private BigDecimal estimateNewPrice(String category) {
        if (category.contains("数码")) {
            return new BigDecimal("699");
        }
        if (category.contains("家居")) {
            return new BigDecimal("129");
        }
        return new BigDecimal("99");
    }

    private Integer parseDbId(String id) {
        try {
            return Integer.parseInt(id);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }
    }

    private String normalizeScene(String scene) {
        return "new".equals(scene) ? "new" : "used";
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "approved";
        }
        String normalized = status.trim();
        if ("0".equals(normalized)) return "approved";
        if ("2".equals(normalized)) return "pending";
        if ("3".equals(normalized)) return "rejected";
        if ("rejected".equals(normalized) || "pending".equals(normalized) || "approved".equals(normalized)) {
            return normalized;
        }
        if (normalized.startsWith("pe")) {
            return "pending";
        }
        return "approved";
    }

    private String statusTag(String status) {
        if ("pending".equals(status)) return "待审核";
        if ("rejected".equals(status)) return "已拒绝";
        return "审核通过";
    }

    private String statusSubtitle(String status, String scene, String condition) {
        String type = "new".equals(scene) ? "新品" : "二手";
        return type + " " + defaultText(condition, "待补充") + " · " + statusTag(status);
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : DISPLAY_TIME.format(time);
    }

    private String defaultText(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
