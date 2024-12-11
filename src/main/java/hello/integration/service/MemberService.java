package hello.integration.service;

import hello.integration.domain.Member;
import hello.integration.domain.MemberRepository;
import hello.integration.repository.JoinRequestDTO;
import hello.integration.repository.JoinResponsDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // 닉네임 중복 체크
    public boolean isNicknameExists(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    public boolean isNicknameAvailable(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return false;
        }

        // 닉네임 길이 체크 (예: 2~20자)
        if (nickname.length() < 2 || nickname.length() > 20) {
            return false;
        }

        // 특수문자 체크 (선택적)
        if (!nickname.matches("^[a-zA-Z0-9가-힣]+$")) {
            return false;
        }

        // 데이터베이스에서 중복 체크
        return !memberRepository.existsByNickname(nickname);
    }

    // 멤버 저장
    public Member saveMember(String nickname, int characterId, String modelPath) {
        Member member = Member.builder()
                .nickname(nickname)
                .characterId(characterId)
                .modelPath(modelPath)
                .build();
        return member;
    }

    // 닉네임으로 멤버 조회
    public String getNickname(String nickname) {
        Member member = memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("멤버를 찾을 수 없습니다."));
        return member.getNickname();
    }

    @Transactional
    public void deleteMember(String nickname) {
        try {
            memberRepository.deleteByNickname(nickname);
            log.info("Member deleted successfully: {}", nickname);
        } catch (Exception e) {
            log.error("Error deleting member: {}", nickname, e);
            throw new RuntimeException("Failed to delete member", e);
        }
    }

    public List<JoinResponsDTO> getAllConnectedUsers() {
        return memberRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private JoinResponsDTO convertToDTO(Member member) {
        return JoinResponsDTO.builder()
                .nickname(member.getNickname())
                .characterId(member.getCharacterId())
                .modelPath(member.getModelPath())
                .build();
    }
}
