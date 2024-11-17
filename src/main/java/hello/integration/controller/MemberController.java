package hello.integration.controller;

import hello.integration.domain.Member;
import hello.integration.domain.MemberRepository;
import hello.integration.repository.JoinRequestDTO;
import hello.integration.repository.JoinResponsDTO;
import hello.integration.repository.NicknameCheckRequestDTO;
import hello.integration.repository.NicknameCheckResponseDTO;
import hello.integration.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import lombok.Data;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/member")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5000"}) // React 앱의 주소
//@CrossOrigin(origins = {"http://gogolckh.ddns.net:3000", "http://gogolckh.ddns.net:5000"}) // React 앱의 주소
//@CrossOrigin(origins = {"http://gogolckh.ddns.net:8010","http://gogolckh.ddns.net:10"}) // React 앱의 주소
@Slf4j
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @Autowired
    public MemberController(MemberRepository memberRepository, MemberService memberService) {
        this.memberRepository = memberRepository;
        this.memberService = memberService;
    }

    @PostMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestBody NicknameCheckRequestDTO request) {
        boolean isAvailable = memberService.isNicknameAvailable(request.getNickname());
        return ResponseEntity.ok(new NicknameCheckResponseDTO(isAvailable));
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinOrLogin(@RequestBody Member member, HttpSession session) {  // HttpSession 추가
        try {
            Member newMember = memberRepository.save(member);

            // 세션에 사용자 정보 저장
            session.setAttribute("nickname", newMember.getNickname());
            session.setAttribute("characterId", newMember.getCharacterId());

            Map<String, Object> response = new HashMap<>();
            response.put("nickname", newMember.getNickname());
            response.put("characterId", newMember.getCharacterId());
            response.put("modelPath", newMember.getModelPath());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("회원가입/로그인 처리 중 오류 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류가 발생했습니다.");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<JoinResponsDTO> getCurrentMember(@RequestParam String nickname, HttpSession session) {
        System.out.println("/me 에는 옴  세션아이디는? : " + session.toString());
        log.info("GET /me 요청 받음");
        log.info("요청 닉네임: {}", nickname);

        Optional<Member> member = memberRepository.findByNickname(nickname);
        if (member.isEmpty()) {
            log.info("DB에서 멤버를 찾을 수 없음");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        JoinResponsDTO response = JoinResponsDTO.builder()
                .nickname(member.get().getNickname())
                .characterId(member.get().getCharacterId())
                .modelPath(member.get().getModelPath())
                .build();

        log.info("응답 데이터: {}", response);
        return ResponseEntity.ok(response);
    }
}