package com.susang.stellarVision.config.security.authentication;

import com.susang.stellarVision.entity.Member;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private Member member;

    public Member getMember() {
        return member;
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public boolean isEnabled() {
        return !member.getIsDeleted();
    }
}
