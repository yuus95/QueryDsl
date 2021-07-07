package study.querydsl.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
//기본생성자를 만들어준다
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

    @Id @GeneratedValue
    @Column(name = "team_id")
    private Long id;

    private String name ;

    @OneToMany(mappedBy = "team")
    private List<Member>  member = new ArrayList<>();


    public Team(String name){
        this.name= name;
    }


}
