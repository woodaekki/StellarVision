package com.susang.stellarVision.application.member.service;

import com.susang.stellarVision.application.member.dto.MemberSearchInfoDTO;
import com.susang.stellarVision.application.member.dto.MemberSearchListDTO;
import com.susang.stellarVision.application.member.dto.MemberSearchListDTO.PageInfo;
import com.susang.stellarVision.application.member.dto.SignUpRequest;
import com.susang.stellarVision.application.member.exception.MemberAlreadyExistException;
import com.susang.stellarVision.application.member.exception.MemberNotFoundException;
import com.susang.stellarVision.application.member.repository.MemberRepository;
import com.susang.stellarVision.application.profile.service.ProfileService;
import com.susang.stellarVision.entity.Member;
import com.susang.stellarVision.entity.Profile;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileService profileService;

    @Override
    @Transactional(readOnly = true)
    public Member getMemberByEmail(String email) {
        Optional<Member> findMember = memberRepository.findByEmail(email);
        return findMember.orElseThrow(() -> new MemberNotFoundException(email));
    }

    @Override
    @Transactional
    public Long registerMember(SignUpRequest request) {
        String rawPassword = request.getPassword();
        String encoded = passwordEncoder.encode(rawPassword);

        Profile profile = Profile.builder()
                .build();

        Member member = Member.builder()
                .email(request.getEmail())
                .password(encoded)
                .name(request.getName())
                .birth(request.getBirth())
                .latestLogin(null)
                .followerCount(0L)
                .followingCount(0L)
                .isDeleted(false)
                .profile(profile)
                .build();

        try {
            memberRepository.save(member);
            return member.getId();
        } catch (DataIntegrityViolationException ex) {
            Throwable root = ex.getRootCause();
            if (root instanceof org.hibernate.exception.ConstraintViolationException
                    && root.getMessage()
                    .contains("UK_MEMBER_EMAIL")) {
                throw new MemberAlreadyExistException(request.getEmail());
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    @Transactional
    public void updateLatestLogin(Long memberId) {
        memberRepository.findById(memberId)
                .ifPresent(Member::updateLatestLogin);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberSearchListDTO searchMembers(String name, Long cursor, int limit) {
        int pageSize = Math.min(Math.max(limit, 0), 100);
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        List<Member> rows = memberRepository.findSliceByNameAndCursor(name, cursor,
                pageable);

        boolean hasNext = rows.size() == pageSize;
        Long nextCursor = hasNext ? rows.get(rows.size() - 1)
                .getId() : null;

        List<MemberSearchInfoDTO> result = rows.stream()
                .map(m -> new MemberSearchInfoDTO(
                        m.getId(),
                        m.getName(),
                        profileService.getProfileImageUrl(m.getProfile()
                                .getProfileS3Key()),
                        m.getCreatedAt()
                ))
                .toList();

        return new MemberSearchListDTO(
                result,
                new PageInfo(nextCursor, hasNext, pageSize)
        );
    }
}
