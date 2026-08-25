package com.example.shopping_back.center;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shopping_back.auth.AuthService;
import com.example.shopping_back.auth.dto.AuthUserView;
import com.example.shopping_back.center.CenterDtos.InteractionCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class CenterServiceTest {

    private CenterMapper mapper;
    private AuthService authService;
    private CenterService centerService;

    @BeforeEach
    void setUp() {
        mapper = mock(CenterMapper.class);
        authService = mock(AuthService.class);
        when(mapper.countColumn(anyString(), anyString())).thenReturn(1);
        when(authService.me("token")).thenReturn(user());
        when(mapper.creditRecordCount(7)).thenReturn(1);
        when(mapper.favoriteCount(7)).thenReturn(0);
        when(mapper.browseCount(7)).thenReturn(0);
        when(mapper.followCount(7)).thenReturn(0);
        when(mapper.followTopicCount(7)).thenReturn(0);
        centerService = new CenterService(mapper, authService);
    }

    @Test
    void addFavoriteStoresActualGoodsId() {
        centerService.addBuyerItem("token", "favorite", new InteractionCreateRequest("42", "显示器", "松果小店"));

        verify(mapper).addFavorite(7, 42, "显示器");
    }

    @Test
    void addBrowseStoresActualGoodsId() {
        centerService.addBuyerItem("token", "history", new InteractionCreateRequest("18", "二手相机", "松果小店"));

        verify(mapper).addBrowse(7, 18, "二手相机");
    }

    @Test
    void addFavoriteRejectsMissingGoodsId() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> centerService.addBuyerItem("token", "favorite", new InteractionCreateRequest("", "显示器", "松果小店")));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    private static AuthUserView user() {
        return new AuthUserView(7, "alice", "138****8000", 100, "buyer", "买家", false, "normal", "");
    }
}
