package com.automarket.marketplace.inquiry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InquiryReplyRepository extends JpaRepository<InquiryReply, UUID> {
    List<InquiryReply> findByInquiryIdOrderByCreatedAtAsc(UUID inquiryId);
}

