package com.example.shopping_back.goods;

import com.example.shopping_back.common.dto.ApiResult;
import com.example.shopping_back.auth.AuthService;
import com.example.shopping_back.auth.dto.AuthUserView;
import com.example.shopping_back.goods.dto.PublishGoodsRequest;
import com.example.shopping_back.goods.model.Goods;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    private final GoodsService goodsService;
    private final AuthService authService; 

    public GoodsController(GoodsService goodsService, AuthService authService) {
        this.goodsService = goodsService;
        this.authService = authService;
    }

    @PostMapping("/publish")
    public ApiResult<Goods> publish(
            @Valid @RequestBody PublishGoodsRequest body,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        String token = bearerToken(authorization);
    
        AuthUserView currentUser = authService.me(token);
        Goods publishedGoods = goodsService.publish(body, currentUser.getUserId());
        
        return ApiResult.ok(publishedGoods);
    }

    @GetMapping("/list")
    public ApiResult<List<Goods>> list() {
        return ApiResult.ok(goodsService.getActiveGoodsList());
    }

    @GetMapping("/detail/{id}")
    public ApiResult<Goods> detail(@PathVariable("id") final Integer goodsId) {
        return ApiResult.ok(goodsService.getGoodsDetail(goodsId));
    }

    @GetMapping("/search")
    public ApiResult<List<Goods>> search(@RequestParam("keyword") String keyword, 
                                         @RequestParam(value = "sortBy", required = false) String sortBy,
                                         @RequestParam(value = "sortOrder", required = false) String sortOrder) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ApiResult.ok(new java.util.ArrayList<>());
        }
        List<Goods> searchResult = goodsService.searchGoods(keyword.trim(), sortBy, sortOrder);
        return ApiResult.ok(searchResult);
    }

    private static String bearerToken(String authorization) {
        if (authorization == null) {
            return null;
        }
        String v = authorization.trim();
        if (v.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return v.substring(7).trim();
        }
        return v;
    }

}