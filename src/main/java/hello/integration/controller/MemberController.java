package hello.integration.controller;

import hello.integration.domain.Member;
import hello.integration.domain.MemberRepository;
import hello.integration.repository.JoinRequestDTO;
import hello.integration.repository.JoinResponsDTO;
import hello.integration.repository.NicknameCheckRequestDTO;
import hello.integration.repository.NicknameCheckResponseDTO;
import hello.integration.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import lombok.Data;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/member")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5000"})

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
        boolean isAvailable = false;
        if(memberService.isNicknameAvailable(request.getNickname()) && !request.getNickname().equals("관리자")){
            isAvailable = true;
        }
        System.out.println("결과 : " + isAvailable);
        return ResponseEntity.ok(new NicknameCheckResponseDTO(isAvailable));
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinOrLogin(@RequestBody Member member, HttpSession session) {
        try {
            // 캐릭터가 이미 사용 중인지 다시 한번 확인
            boolean isCharacterInUse = memberRepository.existsByCharacterId(member.getCharacterId());
            if (isCharacterInUse) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("이미 사용 중인 캐릭터입니다.");
            }

            // 닉네임이 이미 사용 중인지 다시 한번 확인
            if (!memberService.isNicknameAvailable(member.getNickname()) && member.getNickname().equals("관리자")) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("이미 사용 중인 닉네임입니다.");
            }

            Member newMember = memberRepository.save(member);

            // 세션에 정보 저장
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

    @GetMapping("/character-status")
    public ResponseEntity<Map<Integer, Boolean>> getCharacterStatus() {
        Map<Integer, Boolean> status = new HashMap<>();

        // 기본적으로 모든 캐릭터는 사용 가능 상태로 초기화
        for (int i = 1; i <= 6; i++) {
            status.put(i, false);  // false = 사용 가능
        }

        // DB에 있는 캐릭터들은 사용 중 상태로 변경
        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            status.put(member.getCharacterId(), true);  // true = 사용 중
        }

        return ResponseEntity.ok(status);
    }


}