package study.querydsl.entity;


import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id","username","age"}) // Team을 넣을 경우 연관관계 때문에 무한루프가 돈다.
public class Member {

    @Id @GeneratedValue
    @Column(name="member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="team_id")
    private Team team;


    public Member(String username){
        this(username,0);
    }




    public Member(String username,int age){
        this(username,age,null);

    }

    public Member(String username,int age, Team team){
        this.username = username;
        this.age=age;
        if (team!=null){
            changeTeam(team);
        }
    }

    private void changeTeam(Team team) {
        this.team = team;
        team.getMember().add(this);
    }

}
