package hello.integration.controller;

import hello.integration.domain.Member;
import hello.integration.domain.MemberRepository;
import hello.integration.repository.JoinRequestDTO;
import hello.integration.repository.JoinResponsDTO;
import hello.integration.service.MemberService;
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
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5000"}) // React 앱의 주소
//@CrossOrigin(origins = {"http://gogolckh.ddns.net:3000", "http://gogolckh.ddns.net:5000"}) // React 앱의 주소
@CrossOrigin(origins = {"http://gogolckh.ddns.net:8010","http://gogolckh.ddns.net:10"}) // React 앱의 주소
@Slf4j
public class MemberController {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinOrLogin(@RequestBody Member member) {
        try {
            // 닉네임 존재 여부 확인
            if (memberRepository.existsByNickname(member.getNickname())) {
                // 닉네임이 존재하면 로그인 시도
                Optional<Member> existingMember = memberRepository.findByNicknameAndPassword(
                        member.getNickname(),
                        member.getPassword()
                );

                if (existingMember.isPresent()) {
                    // 로그인 성공
                    Map<String, String> response = new HashMap<>();
                    response.put("nickname", member.getNickname());
                    return ResponseEntity.ok(response);
                } else {
                    // 비밀번호 불일치
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body("중복된 닉네임입니다.");
                }
            } else {
                // 닉네임이 존재하지 않으면 새로 가입
                Member newMember = memberRepository.save(member);
                Map<String, String> response = new HashMap<>();
                response.put("nickname", newMember.getNickname());
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("회원가입/로그인 처리 중 오류 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("서버 오류가 발생했습니다.");
        }
    }
}