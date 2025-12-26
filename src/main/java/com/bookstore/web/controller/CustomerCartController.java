package com.bookstore.web.controller;

import com.bookstore.dao.ShoppingCartDao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * 客户购物车 REST 控制器
 */
@RestController
@RequestMapping("/api/customer")
@CrossOrigin
public class CustomerCartController {

    private final ShoppingCartDao cartDao = new ShoppingCartDao();

    /**
     * 获取购物车列表
     */
    @GetMapping("/{customerId}/cart")
    public ResponseEntity<?> getCart(@PathVariable("customerId") long customerId) {
        try {
            List<ShoppingCartDao.CartItem> items = cartDao.findByCustomerId(customerId);
            List<CartItemResp> resp = items.stream().map(i -> {
                CartItemResp r = new CartItemResp();
                r.bookId = i.getBookId();
                r.title = i.getBookTitle();
                r.quantity = i.getQuantity();
                r.unitPrice = i.getUnitPrice();
                return r;
            }).toList();
            return ResponseEntity.ok(resp);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ErrorResp("数据库错误: " + e.getMessage()));
        }
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/{customerId}/cart")
    public ResponseEntity<?> addToCart(@PathVariable("customerId") long customerId,
            @RequestBody AddToCartReq req) {
        try {
            if (req.getBookId() == null || req.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body(new ErrorResp("参数错误"));
            }
            cartDao.upsert(customerId, req.getBookId(), req.getQuantity());
            return ResponseEntity.ok().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ErrorResp("数据库错误: " + e.getMessage()));
        }
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/{customerId}/cart/{bookId}")
    public ResponseEntity<?> updateCartItem(@PathVariable("customerId") long customerId,
            @PathVariable("bookId") String bookId,
            @RequestBody UpdateCartReq req) throws SQLException {
        if (req.getQuantity() <= 0) {
            cartDao.delete(customerId, bookId);
        } else {
            cartDao.updateQuantity(customerId, bookId, req.getQuantity());
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 删除购物车中的商品
     */
    @DeleteMapping("/{customerId}/cart/{bookId}")
    public ResponseEntity<?> removeFromCart(@PathVariable("customerId") long customerId,
            @PathVariable("bookId") String bookId) throws SQLException {
        cartDao.delete(customerId, bookId);
        return ResponseEntity.ok().build();
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/{customerId}/cart")
    public ResponseEntity<?> clearCart(@PathVariable("customerId") long customerId) throws SQLException {
        cartDao.clearCart(customerId);
        return ResponseEntity.ok().build();
    }

    // --- DTOs ---
    public static class CartItemResp {
        public String bookId;
        public String title;
        public int quantity;
        public BigDecimal unitPrice;
    }

    public static class AddToCartReq {
        private String bookId;
        private int quantity;

        public String getBookId() {
            return bookId;
        }

        public void setBookId(String bookId) {
            this.bookId = bookId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public static class UpdateCartReq {
        private int quantity;

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public static class ErrorResp {
        public String message;

        public ErrorResp(String message) {
            this.message = message;
        }
    }
}
