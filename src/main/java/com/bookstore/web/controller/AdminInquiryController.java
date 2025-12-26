package com.bookstore.web.controller;

import com.bookstore.dao.BookInquiryDao;
import com.bookstore.dto.BookInquiryRequestDto;
import com.bookstore.model.BookInquiryRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/inquiries")
@CrossOrigin(origins = "*")
public class AdminInquiryController {

    private final BookInquiryDao bookInquiryDao = new BookInquiryDao();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    public ResponseEntity<?> getAllInquiries() {
        try {
            List<BookInquiryRequest> list = bookInquiryDao.getAllInquiries();
            List<BookInquiryRequestDto> dtos = list.stream().map(this::convertToDto).collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "获取失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/quote")
    public ResponseEntity<?> quoteInquiry(@PathVariable Long id, @RequestBody BookInquiryRequestDto dto) {
        try {
            BookInquiryRequest inquiry = bookInquiryDao.getInquiryById(id);
            if (inquiry == null)
                return ResponseEntity.status(404).body(Map.of("message", "记录不存在"));

            inquiry.setStatus("QUOTED");
            inquiry.setQuotedPrice(dto.getQuotedPrice());
            inquiry.setAdminReply(dto.getAdminReply());
            inquiry.setReplyTime(new Timestamp(System.currentTimeMillis()));

            bookInquiryDao.updateInquiry(inquiry);
            return ResponseEntity.ok(Map.of("message", "已提交报价"));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "操作失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectInquiry(@PathVariable Long id, @RequestBody BookInquiryRequestDto dto) {
        try {
            BookInquiryRequest inquiry = bookInquiryDao.getInquiryById(id);
            if (inquiry == null)
                return ResponseEntity.status(404).body(Map.of("message", "记录不存在"));

            inquiry.setStatus("REJECTED");
            inquiry.setAdminReply(dto.getAdminReply());
            inquiry.setReplyTime(new Timestamp(System.currentTimeMillis()));

            bookInquiryDao.updateInquiry(inquiry);
            return ResponseEntity.ok(Map.of("message", "已拒绝请求"));
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "操作失败: " + e.getMessage()));
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
