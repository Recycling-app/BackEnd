package com.example.recycling_app.controller.market;

import com.example.recycling_app.dto.market.ProductDto;
import com.example.recycling_app.service.market.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products") // API 경로의 공통 부분
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 상품 등록
    @PostMapping
    public ResponseEntity<Map<String, Object>> registerProduct(@RequestBody ProductDto product) {
        Map<String, Object> response = new HashMap<>();
        try {
            String productId = productService.registerProduct(product);
            response.put("status", "success");
            response.put("productId", productId);
            response.put("message", "상품이 성공적으로 등록되었습니다.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 모든 상품 조회
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ProductDto> products = productService.getAllProducts();
            response.put("status", "success");
            response.put("products", products);
            response.put("count", products.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 상품명 검색
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(@RequestParam String keyword) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ProductDto> products = productService.searchProductsByName(keyword);
            response.put("status", "success");
            response.put("products", products);
            response.put("count", products.size());
            response.put("keyword", keyword);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 상품 삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> deleteProduct(
            @PathVariable String productId,
            @RequestParam String uid) {
        Map<String, Object> response = new HashMap<>();
        try {
            productService.deleteProduct(productId, uid);
            response.put("status", "success");
            response.put("message", "상품이 성공적으로 삭제되었습니다.");
            response.put("productId", productId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}