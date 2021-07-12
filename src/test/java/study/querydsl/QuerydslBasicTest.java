package study.querydsl;


import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {


    @Autowired
    EntityManager em;


    JPAQueryFactory qf;


    @BeforeEach
    public void before(){
        //given
        qf = new JPAQueryFactory(em);

        Team teamA= new Team("teamA");
        Team teamB= new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",20,teamA);
        Member member3 = new Member("member3",30,teamB);
        Member member4 = new Member("member4",40,teamB);
        Member member5 = new Member("member5",50,teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
    }

    // 런타임 오류가 일어나기 쉽다. 오류가 런타임 실행 후 잡히기 떄문이다.
    @Test
    public void startJPQL(){
        Member findByJPQL = em.createQuery(
                "select m from Member m " +
                        "where m.username =:username "
                , Member.class)
                .setParameter("username", "member1")
                .getSingleResult();


        Assertions.assertThat(findByJPQL.getUsername()).isEqualTo("member1");

    }


    @Test
    public void startQuerydsl(){


        // 어떤 큐멤버인지 구분하기 위해 이름을 준다.
//        QMember m = new QMember("m");


//         같은 테이블을 조인할 경우 아니면 기본클래스이름으로 사용하기 기본클래스 : member1
//        QMember m1 = new QMember("m1");


        //dsl 은 파라미터를 자동으로 바인디 해준다.
        Member findMember = qf
                .select(member)
                .from(member)
                .where(member.username.eq("member1")) // 파라미터 바인딩
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }


    @Test
    public void search(){
        Member findMember = qf
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");

    }


    @Test //Search 매소드랑 같은역할
    public void searchAndParam(){
        Member findMember = qf
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10) // and 일 경우 ,만 써도된다.
                )
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void resultFetch(){
        List<Member> fetch = qf
                .selectFrom(member)
                .fetch();


        Member fetchOne = qf
                .selectFrom(member)
//                .where(member.age.eq(10))
                .limit(1)
                .fetchOne();



        Member fetchFirst = qf
                .selectFrom(member)
                .fetchFirst();
//                .limit(1).fetchOne()
        
        
        //페이징 기능을 한다 - > 쿼리문 2개 실행 count함수, 조회함수
        QueryResults<Member> results = qf
                .selectFrom(member)
                .fetchResults();// 페이징 정보 보함 total count 쿼리 추가실행

        long total = results.getTotal();
        List<Member> content = results.getResults();

        System.out.println("content ==> " + content + "total == " + total);
//        results.get
    }


    /**
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort(){
        em.persist(new Member(null,100));
        em.persist(new Member("member5",100));
        em.persist(new Member("member6",100));

        List<Member> result = qf
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member membernull = result.get(2);

        Assertions.assertThat(member5.getUsername()).isEqualTo("member5");
        Assertions.assertThat(member6.getUsername()).isEqualTo("member6");
        Assertions.assertThat(membernull.getUsername()).isNull();



    }


    @Test
    public void paging1(){
        List<Member> result = qf
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        Assertions.assertThat(result.size()).isEqualTo(2);
    }

    @Test // 총갯수도 같이조회
    public void paging2(){
        
        // fetchresults는 웬만하면 사용xx 성능안나옴
        QueryResults<Member> results = qf
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        Assertions.assertThat(results.getTotal()).isEqualTo(4);
        Assertions.assertThat(results.getOffset()).isEqualTo(1);
        Assertions.assertThat(results.getLimit()).isEqualTo(2);
        Assertions.assertThat(results.getResults()).isEqualTo(2);

    }


    @Test
    public void aggregation(){
        List<Tuple> result = qf
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);

        Assertions.assertThat(tuple.get(member.count())).isEqualTo(5);
        Assertions.assertThat(tuple.get(member.age.sum())).isEqualTo(150);
        Assertions.assertThat(tuple.get(member.age.avg())).isEqualTo(30);
        Assertions.assertThat(tuple.get(member.age.max())).isEqualTo(50);
        Assertions.assertThat(tuple.get(member.age.min())).isEqualTo(10);


    }


    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() throws Exception{
        List<Tuple> result = qf
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        Assertions.assertThat(teamA.get(team.name)).isEqualTo("teamA");
        Assertions.assertThat(teamA.get(member.age.avg())).isEqualTo(15);


        Assertions.assertThat(teamB.get(team.name)).isEqualTo("teamB");
        Assertions.assertThat(teamB.get(member.age.avg())).isEqualTo(40);
    }


    /**
     * Left Outer Join  - > teamA에 속하지 않아도 멤버객체는 다 나온다
     */
    // join(조인 대상, 별칭으로 사용할 Q타입)
    @Test
    public void join(){

        List<Member> result = qf
                .select(member)
                .from(member)
                .leftJoin(member.team, team) //QMember, QTeam
                .where(team.name.eq("teamA"))
                .fetch();

        Assertions.assertThat(result)
                .extracting("username")// 필드 추출 member의name 필드 추출
                .containsExactly("member1","member2","member3");

    }

    /**
     * 세타 조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join(){

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = qf
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        Assertions.assertThat(result)
                .extracting("username")
                .containsExactly("teamA","teamB");


    }



    /**
     * 조인 대상 필터링
     *
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조인
     *  JPQL : select m, t from Member m left join m.team t on t.name = 'teamA'
     */
    @Test
    public void join_on_filtering(){

        List<Tuple> result = qf
                .select(member, team) // 여러가지 타입이라 튜플로 조회됨
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();
        System.out.println("==== "+ result.get(0).get(team.name));

        // iter + tap
        for (Tuple tuple : result) {
            System.out.println("tuple"+tuple);
        }
    }


    /**
     *  연관관계 없는 엔티티 외부 조인
     *
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     */
    @Test
    public void join_on_no_relation(){

        // join할 때 member.team 으로 조인하면 쿼리문이 날라갈 때 id값으로 조인이 걸린다.

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        List<Tuple> result = qf
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name)) // Team 객체와 외부조인 member.team 과 조인하는게 아님
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple == "+ tuple);
        }

//        System.out.println("tuple[6] ==> 1" + result.get(6).get(team.name));
    }



    /**
     * 페치 조인
     */

    @PersistenceUnit // EntityManagerFactory 가져올 때사용
    EntityManagerFactory emf;


    @Test
    public void fetchJoinNo(){
        em.flush();
        em.clear();

        Member findMember = qf
                .selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        Assertions.assertThat(loaded).as("페치 조인 미적용").isFalse(); // False이기떄문에 오류가 나지 않는다.


    }


    @Test //페치조인사용
    public void fetchJoinUse(){
        em.flush();
        em.clear();


        //연관된 객체를 한번에 끌고온다.
        Member findMember = qf
                .selectFrom(QMember.member)
                .join(member.team,team).fetchJoin()
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        Assertions.assertThat(loaded).as("페치 조인 적용").isFalse();  // 실패 True값이 나온다.


    }


    /**
     * 서브쿼리 사용 - 나이가 가장많은 사람 조회
     */
    @Test
    public void use_subQuery(){
        QMember sub_m = new QMember("sub_m");

        List<Member> member = qf
                .selectFrom(QMember.member)
                .where(QMember.member.age.eq(
                        JPAExpressions
                                .select(sub_m.age.max())
                                .from(sub_m)
                ))
                .fetch();

        Assertions.assertThat(member.get(0).getAge()).isEqualTo(50);


    }



    /**
     * 서브쿼리 사용 - 나이가 10살 이상인사람
     */
    @Test
    public void use_subQuery_goe(){
        QMember sub_m = new QMember("sub_m");

        List<Member> member = qf
                .selectFrom(QMember.member)
                .where(QMember.member.age.in(
                        JPAExpressions
                                .select(sub_m.age)
                                .from(sub_m)
                                .where(sub_m.age.gt(10))
                ))
                .fetch();
        Assertions.assertThat(member.size()).isEqualTo(3);

    }


    /**
     * select -subQuery
     */
    @Test
    public void Select_Suq_Query(){
        QMember sub_m = new QMember("sub_m");


        List<Tuple> Tuple = qf
                .select(
                        member.username,
                        JPAExpressions
                                .select(sub_m.age.avg())
                                .from(sub_m))
                .from(member)
                .fetch();

        for (com.querydsl.core.Tuple tuple : Tuple) {
            System.out.println("Tuple ==> "+tuple);
        }
    }




    /**
     * case문 기본 문법
     */
    @Test
    public void basicCase(){
        List<String> members = qf
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        for (String s : members) {
            System.out.println("member ==>  "+s);
        }
    }


    /**
     * 복잡한 case문  - caseBuilder() 사용하기
     */
    @Test
    public void caseBuildUse(){

        List<String> caseMember = qf
                .select(
                        new CaseBuilder()
                                .when(member.age.between(0, 20)).then("0~20살")
                                .when(member.age.between(21, 30)).then("21~30")
                                .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : caseMember) {
            System.out.println("caseMember ==> " + s);
        }
    }

    /**
     * OrderBy에서 Case문 함계 사용하기
     */
    @Test
    public void OrderByCase(){
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);
        List<Tuple> result = qf
                .select(member.username,
                        member.age,
                        rankPath)
                .from(member)
                .orderBy(rankPath.desc())
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = "  + username + " age = " + age + " rank = " + rank);
        }
    }


    /**
     * 상수 표시
     */
    @Test
    public void constant(){

        List<Tuple> members = qf
                .select(QMember.member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : members) {
            System.out.println("tuple ==> " + tuple);
        }
    }


    /**
     * 문자더하기 {member.username}_{member.age}
     */
    @Test
    public void str_plus(){

        List<String> members = qf
                .select(
                        member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        for (String s : members) {
            System.out.println("s ==> " + s );
        }

    }


}



