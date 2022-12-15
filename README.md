# allOpen

처음 코틀린 리팩토링을 진행할 때 기존의 자바 프로젝트를 테스트 코드를 통해서 리팩토링하는 것을 먼저 시작했었다.

그래서 사실 그레이들 관련 세팅을 그대로 가져가는 경우가 있다.

물론 테스트를 하다보면 작동은 정말 깔끔하게 잘된다.

그래서 생각하기로는 '잘 되네?'하고 아무 생각없이 넘어가다가 로그에서 미묘한 점을 발견하게 되는데 바로 lazy loading이 안된다는 것을 발견하게 된것이다.

분명 나는 lazy로 설정했음에도 불구하고 로그를 보다보니 한번 날아가야 할 쿼리가 두번 날아가는 것이 눈에 보인것이다.

## JDK Dynamic Proxy vs CGLIB

지금은 스프링 부트를 많이 쓰지만 한 때 스프링프레임워크나 전자정부프레임워크가 대부분 차지했던 2010년 초중반에 스프링 커뮤니티에서 나온 이야기가 있다.

지금이야 성능에 대한 이슈 제기가 없는걸로 아는데 다음 글에 참 좋은 내용이 있어서 링크를 걸어본다.

[Spring AOP가 제공하는 두 가지 AOP Proxy](https://gmoon92.github.io/spring/aop/2019/04/20/jdk-dynamic-proxy-and-cglib.html)

뜬금없이 AOP얘기를 하지만 하이버네이트와도 관련이 있는 내용이다.

또한 이 내용은 단지 JPA에만 국한된 내용이 아니기 때문에 한번쯤 읽어보면 잘 모르셨던 분들이라면 많은 도움이 될 것이라 생각이 된다.

물론 아는 분들은 그냥 넘어가도 상관없다.

어쨰든 JPA Hibernate는 기본적으로 CGLib을 사용한다.

자 위에 내용을 읽어봐다면 CGLIB는 JDK Dynamic Proxy와는 다르게 클래스 기반으로 바이트코드를 조작한다는 내용을 알게 될 것이다.

이 말은 상속 방식을 이용해서 프록시를 만들 때 메서드를 오버라이딩하는 방식이라는 것을 알 수 있다.

**어라? 잠깐**

감이 오는가???

자바와는 달리 코틀린의 클래스나 함수, 프로퍼티는 기본적으로 final이다.

그래서 open키워드를 일일이 붙여줘서 '나 final아니다'라고 알려줘야 한다.

코틀린을 사용하는 이유중 하나가 간결함이다.

근데 이런 상황이면 그 장점이 무색해진다. 그래서 플러그인으로 allOpen을 제공한다.

결국 lazy loading을 위해서 프록시를 생성할 수 없어서 쿼리가 두번 날아가는 현상을 이 플러그인으로 해결할 수 있다.

* 마지막에 전체 세팅 코드를 남겨둘 생각이다.

# noArg

사실 딱히 이 부분은 없어도 개발하는데 크게 어떤 이슈를 만나지 않았다.

하지만 이런 생각을 해보면 어떨까?

```
hibernate의 Reflection API를 사용하는 라이브러리를 사용하면 어떤 일이 벌어질까?
```

자바를 오랫동안 해오다가 코틀린의 생성자에 대한 부분을 공부하다보면 좀 당황스러운 부분이 있다.

특히 롬복에 익숙해진 분들이라면 더 그러할 텐데 일단 data class의 경우에는 인자가 있는 생성자가 필수이다.

그리고 생성자를 생략한 class를 만들고 내부에 constructor를 이용해 인자없는 생성자를 만든다고 해도 그 녀석은 기본생성자가 아니다.!!!!

부 생성자에 지나지 않는다.

보통 위에서 언급한 내용은 인자없는 기본 생성자를 필수로 한다.

그렇다는 것은 만일 hibernate의 Reflection API를 사용하는 ModelMapper같은 라이브러리를 사용하게 되면 오류를 만나게 된다.

또한 내부적으로 어떤 특별한 이유로 이런 상황이 발생한다면 버젓이 뻘겋게 에러를 토해낼 것이다.

그래서 이것을 플러그인 제공을 통해 해결하도록 하고 있다.

다만 코틀린의 간결함을 유지하기 위해 플러그인 설정이 추가되긴 해지만 그래도 이렇게 플러그인을 제공하는게 어디냐!!!!

## allOpen과 noArg를 설정하지 않고 테스트하기

다음 2개의 엔티티를 빠르게 만들어 보자.

```kotlin
@Entity
@Table(name = "musician")
class Musician(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var name: String,

    @Enumerated(value = EnumType.STRING)
    var genre: GenreCode? = GenreCode.ETC,

)

@Entity
@Table(name = "album")
class Album(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var title: String,

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "musician_id")
    var musician: Musician,

)
```

repository도 아주 빠르게 초간단하게 만들어보자.

```kotlin
@NoRepositoryBean
interface BaseRepository<M, ID : Serializable?> : JpaRepository<M, ID>, JpaSpecificationExecutor<M>

interface AlbumRepository : BaseRepository<Album, Long>
```

나머지 코드는 프로젝트에 미리 만들어놨으니 참조하면 된다.

뮤지션과 음반의 연관관계는 단방향을 염두하고 Album에 @ManyToOne를 설정한다.

jpa에 대한 이론이 아니기 때문에 ERD를 그려보거나 뮤지션과 앨범의 관계를 살펴보면 쉽게 파악이 될 수 있다.


```
듀오 음반을 고려해보면 @ManyToMany도 가능한디??
```

아.... 그게 있네?

어째든 일단 기본적인 관점에서 살펴보면 그렇다.

이제 테스트를 빠르게 해보자.

```kotlin
@Transactional
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AlbumRepositoryTest @Autowired constructor(
    private val albumRepository: AlbumRepository,
) {

    @Test
    @Order(1)
    @DisplayName("save 테스트")
    @Rollback(false)
    fun save_Query_TEST() {
        // given
        val musician = Musician(
            name = "Chet Baker",
            genre = GenreCode.JAZZ
        )

        val album = Album(
            title = "As Time Goes By",
            musician = musician
        )
        // when
        val saved = albumRepository.save(album)
        assertTrue(saved.title == "As Time Goes By")
    }

    @Test
    @Order(2)
    @DisplayName("findByIdOrThrow 테스트")
    fun findById_Query_TEST() {
        // given
        val id: Long = 1

        // when
        val album = albumRepository.findByIdOrThrow(id)

        // then
        assertTrue(album.title == "As Time Goes By")
    }
}
```
테스트 클래스 전체에 @Transactional을 걸고 설정을 통해 앨범과 뮤지션 정보를 저장하고 다음 테스트에서 조회를 하는 테스트이다.


실제로 예상한 결과는 다음과 같을 것이다.

```
최초 뮤지션과 앨범에 대한 인서트 쿼리가 한번씩 나가고 앨범 조회를 하는 쿼리가 한번 나간다.

따라서 총 3번의 쿼리가 날아갈 것이다.
```

이것일 것이다.

실제로 코드 검증은 앨범의 타이틀만 보기 때문에 뮤지션 정보를 조회하지 않기 때문에 뮤지션을 조회하는 쿼리가 나가지 않을 것이라 예상된다.

하지만 실제 결과는 어떨까????

```
// result

// ------------------------- insert query -------------------------
Hibernate: 
    /* insert io.basquiat.refactoring.musician.model.entity.Musician
        */ insert 
        into
            musician
            (genre, name) 
        values
            (?, ?)
Hibernate: 
    /* insert io.basquiat.refactoring.album.domain.entity.Album
        */ insert 
        into
            album
            (musician_id, title) 
        values
            (?, ?)


// ------------------------- select query -------------------------

Hibernate: 
    select
        album0_.id as id1_0_0_,
        album0_.musician_id as musician3_0_0_,
        album0_.title as title2_0_0_ 
    from
        album album0_ 
    where
        album0_.id=?
Hibernate: 
    select
        musician0_.id as id1_1_0_,
        musician0_.genre as genre2_1_0_,
        musician0_.name as name3_1_0_ 
    from
        musician musician0_ 
    where
        musician0_.id=?

```

테스트는 정말 아주 깔~~끔하게 성공한다.

**로그를 보기 전까진!**

왜냐하면 뮤지션 정보를 조회할 이유가 없는데 뜬금없이 불필요한 쿼리가 한번 나갔기 때문이다.

처음에는 테스트 성공 여부만 확인했지 로그를 살펴볼 생각을 하지 않았다.

왜냐하면 자바에서 코틀린으로의 리팩토링에서 기존의 코드가 원하는 성공 결과를 보여주는데 집중했었기 때문이다.

## 플러그인 세팅

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.5"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	kotlin("jvm") version "1.7.22"
	kotlin("kapt") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
	kotlin("plugin.jpa") version "1.7.22"
	kotlin("plugin.noarg") version "1.7.22"
	kotlin("plugin.allopen") version "1.7.22"
}

group = "io.basquiat"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	// mysql
	runtimeOnly("mysql:mysql-connector-java")

	testImplementation("org.springframework.boot:spring-boot-starter-test")

}

allOpen {
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.MappedSuperclass")
	annotation("javax.persistence.Embeddable")
}

noArg {
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.MappedSuperclass")
	annotation("javax.persistence.Embeddable")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

```

현재 그레이들 버전은 7.5.1을 사용하고 있으며 스프링 부트 버전은 설정 파일에서 볼 수 있듯이 2.7.5를 사용하고 있다.

이제는 플러그인과 관련 설정을 했으니 테스트를 다시 한번 해보면 원하는 결과를 얻을 수 있다.

lazy loading까지 테스트해본다면

```kotlin
@Transactional
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AlbumRepositoryTest @Autowired constructor(
    private val albumRepository: AlbumRepository,
) {

    @Test
    @Order(1)
    @DisplayName("save 테스트")
    @Rollback(false)
    fun save_Query_TEST() {
        // given
        val musician = Musician(
            name = "Chet Baker",
            genre = GenreCode.JAZZ
        )

        val album = Album(
            title = "As Time Goes By",
            musician = musician
        )

        // when
        val saved = albumRepository.save(album)

        // then
        assertTrue(saved.title == "As Time Goes By")
    }

    @Test
    @Order(2)
    @DisplayName("findByIdOrThrow 테스트")
    fun findByIdOrThrow_Query_TEST() {
        // given
        val id: Long = 1

        // when
        val album = albumRepository.findByIdOrThrow(id)

        // then
        assertTrue(album.title == "As Time Goes By")

        println("======================lazy loading===========================")
        // then musician.name == Chet Baker, when lazy loading
        assertTrue(album.musician.name == "Chet Baker")
    }

}

```

예상대로라면 위에 콘솔로 찍은 부분 이후에 뮤지션 정보를 조회하는 쿼리가 나가야 한다.

```
Hibernate: 
    /* insert io.basquiat.refactoring.musician.model.entity.Musician
        */ insert 
        into
            musician
            (genre, name) 
        values
            (?, ?)
Hibernate: 
    /* insert io.basquiat.refactoring.album.domain.entity.Album
        */ insert 
        into
            album
            (musician_id, title) 
        values
            (?, ?)


Hibernate: 
    select
        album0_.id as id1_0_0_,
        album0_.musician_id as musician3_0_0_,
        album0_.title as title2_0_0_ 
    from
        album album0_ 
    where
        album0_.id=?

======================lazy loading===========================
Hibernate: 
    select
        musician0_.id as id1_1_0_,
        musician0_.genre as genre2_1_0_,
        musician0_.name as name3_1_0_ 
    from
        musician musician0_ 
    where
        musician0_.id=?

```
예상대로 결과가 잘 나온 것을 알 수 있다.

# At a Glance

자바와 코틀린이 호환된다고 해도 아무 의심없이 진행하다보면 놓칠 수 있는 부분이다.

사실 이 내용은 너무나 많은 블로그들이나 글들이 올라와 있기 때문에 비슷한 글들속에 하나 더 추가하는 상황이긴 하다.     

그럼에도 분명 필요한 정보를 얻는데 도움이 될것이라 생각한다.      

다음 내용은 엔티티에 대한 부분이 될 것이다.