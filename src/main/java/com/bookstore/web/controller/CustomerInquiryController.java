package com.bookstore.web.controller;

import com.bookstore.dao.BookInquiryDao;
import com.bookstore.dto.BookInquiryRequestDto;
import com.bookstore.model.BookInquiryRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer/{customerId}/inquiries")
@CrossOrigin(origins = "*")
public class CustomerInquiryController {

    private final BookInquiryDao bookInquiryDao = new BookInquiryDao();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @PostMapping
    public ResponseEntity<?> createInquiry(@PathVariable Long customerId,
            @RequestBody BookInquiryRequestDto requestDto) {
        try {
            BookInquiryRequest inquiry = new BookInquiryRequest();
            inquiry.setCustomerId(customerId);
            inquiry.setBookTitle(requestDto.getBookTitle());
            inquiry.setBookAuthor(requestDto.getBookAuthor());
            inquiry.setPublisher(requestDto.getPublisher());
            inquiry.setIsbn(requestDto.getIsbn());
            inquiry.setQuantity(requestDto.getQuantity());
            inquiry.setCustomerNote(requestDto.getCustomerNote());

            bookInquiryDao.createInquiry(inquiry);

            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "询价请求已提交");
            return ResponseEntity.ok(resp);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "提交失败: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getInquiries(@PathVariable Long customerId) {
        try {
            List<BookInquiryRequest> inquiries = bookInquiryDao.getCustomerInquiries(customerId);
            List<BookInquiryRequestDto> dtos = inquiries.stream().map(this::convertToDto).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "获取失败"));
        }
    }

    private BookInquiryRequestDto convertToDto(BookInquiryRequest inquiry) {
        BookInquiryRequestDto dto = new BookInquiryRequestDto();
        dto.setInquiryId(inquiry.getInquiryId());
        dto.setCustomerId(inquiry.getCustomerId());
        dto.setBookTitle(inquiry.getBookTitle());
        dto.setBookAuthor(inquiry.getBookAuthor());
        dto.setPublisher(inquiry.getPublisher());
        dto.setIsbn(inquiry.getIsbn());
        dto.setQuantity(inquiry.getQuantity());
        dto.setCustomerNote(inquiry.getCustomerNote());
        dto.setInquiryTime(sdf.format(inquiry.getInquiryTime()));
        dto.setStatus(inquiry.getStatus());
        dto.setAdminReply(inquiry.getAdminReply());
        dto.setQuotedPrice(inquiry.getQuotedPrice());
        if (inquiry.getReplyTime() != null) {
            dto.setReplyTime(sdf.format(inquiry.getReplyTime()));
        }
        return dto;
    }
}
