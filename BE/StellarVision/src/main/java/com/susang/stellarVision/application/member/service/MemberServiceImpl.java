package com.susang.stellarVision.application.member.service;

import com.susang.stellarVision.application.member.dto.SignUpRequest;
import com.susang.stellarVision.application.member.exception.MemberAlreadyExistException;
import com.susang.stellarVision.application.member.exception.MemberNotFoundException;
import com.susang.stellarVision.application.member.repository.MemberRepository;
import com.susang.stellarVision.entity.Member;
import com.susang.stellarVision.entity.Profile;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Member getMemberByEmail(String email) {
        Optional<Member> findMember = memberRepository.findByEmail(email);
        return findMember.orElseThrow(() -> new MemberNotFoundException(email));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
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
                    && root.getMessage().contains("UK_MEMBER_EMAIL")) {
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
}
