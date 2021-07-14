package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Lombok;
import lombok.NoArgsConstructor;

@Data // 기본 생성자를 안만들어준다
@NoArgsConstructor
public class MemberDto {

    private String username;
    private int age;

    @QueryProjection // + compileQuedsl 실행 -> DTO도 Q파일로 생성된다
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
