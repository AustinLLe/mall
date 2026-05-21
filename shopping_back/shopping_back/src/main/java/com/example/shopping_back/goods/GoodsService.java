package com.example.shopping_back.goods;

import com.example.shopping_back.goods.dto.PublishGoodsRequest;
import com.example.shopping_back.goods.model.Goods;
import com.example.shopping_back.goods.mapper.GoodsMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Date;

@Service
public class GoodsService {

    private final GoodsMapper goodsMapper;

    public GoodsService(GoodsMapper goodsMapper) {
        this.goodsMapper = goodsMapper;
    }

    public List<Goods> searchGoods(String keyword, String sortBy, String sortOrder) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "create_time";
        } else if ("price".equals(sortBy)) {
            sortBy = "price"; 
        } else if ("createTime".equals(sortBy)) {
            sortBy = "create_time"; 
        }
        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = "DESC";
        }
        return goodsMapper.searchGoodsGlobal(keyword, sortBy, sortOrder);
    }

    // 发布商品业务逻辑改造
    public Goods publish(PublishGoodsRequest request, Integer sellerId) {
        Goods goods = new Goods();
        goods.setSellerId(sellerId);
        goods.setGoodsName(request.getGoodsName());
        goods.setGoodsDesc(request.getGoodsDesc());
        goods.setPrice(request.getPrice());
        goods.setScene(request.getScene()); 
        goods.setAddress(request.getAddress());
        goods.setImage(request.getImage());
        goods.setStatus("0"); 
        goods.setCreateTime(new Date());
        goods.setCategory(request.getCategory());               // 商品分类
        goods.setGoodsCondition(request.getGoodsCondition());   // 成色/状态
        goods.setStory(request.getStory());                     // 二手故事/核心卖点
        goods.setFloorPrice(request.getFloorPrice());           // 最低接受价（AI议价保底线）

        // 4. 执行持久化
        goodsMapper.insertGoods(goods);
        return goods;
    }

    public List<Goods> getActiveGoodsList() {
        return goodsMapper.selectAllActiveGoods();
    }

    public Goods getGoodsDetail(Integer goodsId) {
        return goodsMapper.selectGoodsById(goodsId);
    }
}