package com.automarket.marketplace.inquiry;

import com.automarket.marketplace.auth.AuthException;
import com.automarket.marketplace.common.PagedResponse;
import com.automarket.marketplace.common.ResourceNotFoundException;
import com.automarket.marketplace.inquiry.dto.CreateInquiryRequest;
import com.automarket.marketplace.inquiry.dto.InquiryReplyDto;
import com.automarket.marketplace.inquiry.dto.InquirySummaryDto;
import com.automarket.marketplace.inquiry.dto.InquiryThreadDto;
import com.automarket.marketplace.inquiry.dto.ReplyInquiryRequest;
import com.automarket.marketplace.listing.CarListing;
import com.automarket.marketplace.listing.CarListingRepository;
import com.automarket.marketplace.security.UserPrincipal;
import com.automarket.marketplace.user.User;
import com.automarket.marketplace.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final CarListingRepository carListingRepository;
    private final UserRepository userRepository;

    @Transactional
    public InquirySummaryDto create(CreateInquiryRequest request, UserPrincipal principal) {
        User buyer = requireUser(principal);
        CarListing listing = carListingRepository.findById(request.listingId())
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        User seller = listing.getSeller();
        if (seller == null) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Listing seller not found");
        }
        if (seller.getId().equals(buyer.getId())) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Cannot inquire on your own listing");
        }

        Inquiry inquiry = new Inquiry();
        inquiry.setListing(listing);
        inquiry.setBuyer(buyer);
        inquiry.setSeller(seller);
        inquiry.setMessage(request.message());
        inquiry.setRead(false);
        Inquiry saved = inquiryRepository.save(inquiry);

        listing.setInquiryCount((listing.getInquiryCount() == null ? 0 : listing.getInquiryCount()) + 1);
        carListingRepository.save(listing);

        return toSummary(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<InquirySummaryDto> received(UserPrincipal principal, int page, int size) {
        User user = requireUser(principal);
        Page<Inquiry> result = inquiryRepository.findBySellerIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size));
        return toPagedSummary(result);
    }

    @Transactional(readOnly = true)
    public PagedResponse<InquirySummaryDto> sent(UserPrincipal principal, int page, int size) {
        User user = requireUser(principal);
        Page<Inquiry> result = inquiryRepository.findByBuyerIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size));
        return toPagedSummary(result);
    }

    @Transactional(readOnly = true)
    public InquiryThreadDto thread(UUID inquiryId, UserPrincipal principal) {
        User user = requireUser(principal);
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found"));
        ensureParticipant(inquiry, user);

        List<InquiryReplyDto> replies = inquiryReplyRepository.findByInquiryIdOrderByCreatedAtAsc(inquiryId)
            .stream()
            .map(this::toReply)
            .toList();

        return toThread(inquiry, replies);
    }

    @Transactional
    public InquiryReplyDto reply(UUID inquiryId, ReplyInquiryRequest request, UserPrincipal principal) {
        User user = requireUser(principal);
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found"));
        ensureParticipant(inquiry, user);

        InquiryReply reply = new InquiryReply();
        reply.setInquiry(inquiry);
        reply.setSender(user);
        reply.setMessage(request.message());
        InquiryReply saved = inquiryReplyRepository.save(reply);

        if (inquiry.getBuyer().getId().equals(user.getId())) {
            inquiry.setRead(false);
            inquiryRepository.save(inquiry);
        }

        return toReply(saved);
    }

    @Transactional
    public boolean markRead(UUID inquiryId, UserPrincipal principal) {
        User user = requireUser(principal);
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found"));

        if (!inquiry.getSeller().getId().equals(user.getId())) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Only seller can mark inquiry as read");
        }

        inquiry.setRead(true);
        inquiryRepository.save(inquiry);
        return true;
    }

    private User requireUser(UserPrincipal principal) {
        if (principal == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void ensureParticipant(Inquiry inquiry, User user) {
        UUID userId = user.getId();
        if (!inquiry.getBuyer().getId().equals(userId) && !inquiry.getSeller().getId().equals(userId)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "You are not part of this inquiry thread");
        }
    }

    private InquirySummaryDto toSummary(Inquiry inquiry) {
        return new InquirySummaryDto(
            inquiry.getId(),
            inquiry.getListing().getId(),
            inquiry.getListing().getTitle(),
            inquiry.getBuyer().getId(),
            inquiry.getBuyer().getFullName(),
            inquiry.getSeller().getId(),
            inquiry.getSeller().getFullName(),
            inquiry.getMessage(),
            inquiry.isRead(),
            inquiry.getCreatedAt()
        );
    }

    private InquiryReplyDto toReply(InquiryReply reply) {
        return new InquiryReplyDto(
            reply.getId(),
            reply.getSender().getId(),
            reply.getSender().getFullName(),
            reply.getMessage(),
            reply.getCreatedAt()
        );
    }

    private InquiryThreadDto toThread(Inquiry inquiry, List<InquiryReplyDto> replies) {
        return new InquiryThreadDto(
            inquiry.getId(),
            inquiry.getListing().getId(),
            inquiry.getListing().getTitle(),
            inquiry.getBuyer().getId(),
            inquiry.getBuyer().getFullName(),
            inquiry.getSeller().getId(),
            inquiry.getSeller().getFullName(),
            inquiry.getMessage(),
            inquiry.isRead(),
            inquiry.getCreatedAt(),
            replies
        );
    }

    private PagedResponse<InquirySummaryDto> toPagedSummary(Page<Inquiry> result) {
        List<InquirySummaryDto> items = result.getContent().stream().map(this::toSummary).toList();
        return new PagedResponse<>(
            items,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.isFirst(),
            result.isLast()
        );
    }
}

