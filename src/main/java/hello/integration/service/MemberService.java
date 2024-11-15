package hello.integration.service;

import hello.integration.domain.Member;
import hello.integration.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // 닉네임 중복 체크
    public boolean isNicknameExists(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    // 멤버 저장
    public Member saveMember(String nickname, String password) {
        Member member = Member.builder()
                .nickname(nickname)
                .password(password)
                .build();

        //Member savedMember = memberRepository.save(member);
        return member;
    }

    // 닉네임으로 멤버 조회
    public String getNickname(String nickname) {
        Member member = memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("멤버를 찾을 수 없습니다."));
        return member.getNickname();
    }
}
