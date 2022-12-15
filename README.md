# 코틀린에서 JPA Entity를 다루는 방법

뭔가 거창한 말같아 보인다.

사실 우리가 자바에서 JPA를 하면서 항상 듣는 말이 있다.

```
getter는 열고 의미없는 setter를 닫는다.     

그 이유는 생각과 비판없이 setter를 통해서 값을 변경하고 하는데 이것은 여러 이슈를 발생시킨다.     

그 중에 하나는 dirty checking에 의한 이슈이다.       

따라서 setter를 닫고 값을 변경하다는 의미를 부여할 수 있도록 setter는 의미를 지닌 메서드로 정의해서 제공한다.
```

자바에서 우리는 롬복을 통해 이것을 쉽게 다룰 수 있다.

수많은 롬복의 애노테이션을 포함하는 `@Data`를 사용하기 보다는 필요한 부분만 열도록 작업을 할 수 있다.

`@Getter`만 사용하고 변경점이 필요한 부분은 의미있는 이름을 줘서 그 메서드를 사용하게 함으로서 행위에 대해 인식을 할 수 있도록 하는 것이다.

하지만 지금 우리가 이전 브랜치에서 작업했던 엔티티를 보면 `var`로 선언한 프로퍼티는 모든 세터에 대해서 다 열려있다는 것을 알 수 있다.

## 코드 컨벤션 룰을 정의한다.

가장 편한 방법은 코드 컨벤션에 대한 룰을 정의하는 것이다.

개발팀의 모든 인원들이 이런 부분을 인지하고 있고 정말 특별한 경우가 아니면 setter를 사용하지 않는다는 문화가 정착되었다면 좋을 것이다.

하지만 휴먼 미스테이크는 예기치 않는 상황에서 발생할 수 있다.


## 일단 이전 엔티티를 살펴보자

```kotlin
@Entity
@DynamicUpdate
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
```
이전 브랜치에서 우리가 작성했던 엔티티이다.

코드만으로 본다면 전혀 문제될 것이 없다.

음반의 경우에는 아이디는 val로 선언했기 때문에 변경할 수 없으며 음반명의 경우에는 잘못 입력된 경우 수정할 수 있으니 코드 자체는 좋아보인다.

뮤지션의 경우에는 val로 놓아도 상관없어 보이긴 한다.

뮤지션 정보가 없는 음반이란 있을 수 없으니깐!

```
아닌데? 여러 곡들을 모은 컴필레이션 음반의 경우에는 특정 뮤지션을 정의할 수 없잖아????
```

그런 경우가 있지만 보통 컴필레이션 음반의 경우에는 VA로 표현하는 경우가 있어 공통 정보를 만들어서 생성할 수 있다.

또한 뮤지션의 경우를 보면 앨범 저장시 뮤지션의 정보가 잘못되었다면 뮤지션의 정보를 조회해서 수정하면 그만이다.

따라서 그런 컨셉 자체만을 본다면 지금 작성한 엔티티는 위에서 말한 코드 컨벤션 문화만 잘 정착되었다면 전혀 문제가 없다.

하지만 어느 누군가가 개발도중 무의식적으로 다음과 같이 작성했다고 한다면 문제가 발생할 것이다.

예를 들면,

```kotlin
fun main() {
    // 이름이 잘못 저장된 아이디의 뮤지션의 정보를 가져와서 이름를 수정한다.
    val musician = muscianRepository.findByIdOrThrow(1L)
    musician.name = "John Coltrane"
    // do something
}
```

물론 코드는 아주 깔끔하다.

하지만 이름을 변경하는 행위가 쉽게 눈에 들어오지 않는다.

오히려 자바의 `setName(name)`이 주는 가시성이 돋보일 정도이다.

따라서 `musician.modifyName(name)`이라든가 `musician.updateName(name)`라든지 change같은 단어를 부여한다면 좀더 코드가 명확하지 않을까?

그렇다면 코드를 한번 수정해 보는 시간을 가져보자.

엔티티에만 국한해서 생각을 하지말고 클래스라는 개념에서 진행해 볼까 한다.

왜냐하면 꼭 엔티티가 아니더라도 이런 케이스를 만날 수 있기 때문이다.

## 변경불가능한 프로퍼티만 생성자로 받고 바디내에 프로퍼티를 정의하는 방식

거두절미하고

```kotlin
class Musician(
    val id: Long? = null,
) {

    var name: String? = null
        private set

    var genre = "ETC"
        private set

    fun modifyName(_name: String) {
        this.name = _name
    }

    fun modifyGenre(_genre: String) {
        this.genre = _genre
    }
}
```

코틀린에서 바디내의 프로퍼티는 초기값을 가져야 한다.

따라서 위와 같이 초기값을 주고 private set을 통해서 setter를 닫고 함수를 통해 변경할 수 있도록 작업한 것이다.

이렇게 작성하게 되면 원하는 방식으로 작동할 것이다.

다만 뮤지션 정보를 생성할 때 골치아프다.

```kotlin
fun main() {

    val musician = Musician()
    // 일단 생성자에서는 id외에는 받는 부분이 없다.
    // setter가 닫혀있기때문에 밑에 코드는 컴파일 에러가 발생한다.
    //musician.name = "John Coltrane"
    //musician.genre = "Jazz"

    musician.modifyName("John Coltrane")
    musician.modifyGenre("Jazz")

}

class Musician(
    val id: Long? = null,
) {

    var name: String? = null
        private set

    var genre = "ETC"
        private set

    fun modifyName(_name: String) {
        this.name = _name
    }

    fun modifyGenre(_genre: String) {
        this.genre = _genre
    }
}
```

자 위의 코드에 문제점이 무엇일까?

초기 뮤지션 정보를 생성하는데 이름과 장르에 대한 값을 세팅할 때 modify가 붙은 함수를 사용해야 한다.

의미가 이상해지는 것이다.

그렇다고 일일히 초기화하는 함수를 만든다는 것은 더 짜증나는 일이다.

이것을 해결하는 방법은 부 생성자를 만들어서 사용하는 것이다.

이제부터는 코틀린의 간결함이 조금씩 사라진다.

```kotlin
fun main() {

    val musician = Musician(name = "John Coltrane", genre = "Jazz")
    with(musician) {
        println("$id")
        println("$name")
        println("$genre")
    }

}

class Musician(
    val id: Long? = null,
) {

    constructor(name: String, genre: String): this() {
        this.name = name
        this.genre = genre
    }

    var name: String? = null
        private set

    var genre = "ETC"
        private set

    fun modifyName(_name: String) {
        this.name = _name
    }

    fun modifyGenre(_genre: String) {
        this.genre = _genre
    }
}

```
뭔가 추가가 되긴 했지만 원하는 방식으로 작동하기 시작했다.

그럼 이전 브랜치에서 작업했던 엔티티를 전보 저렇게 바꿔보자.

```kotlin
@Entity
@Table(name = "musician")
class Musician(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    ) {

    constructor(name: String, genre: GenreCode): this() {
        this.name = name
        this.genre = genre
    }

    var name: String? = null
        protected set

    @Enumerated(value = EnumType.STRING)
    var genre: GenreCode? = GenreCode.ETC
        protected set

    fun modifyName(_name: String) {
        this.name = _name
    }

    fun modifyGenre(_genre: GenreCode) {
        this.genre = _genre
    }
}

@Entity
@DynamicUpdate
@Table(name = "album")
class Album(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    ) {

    constructor(title: String, musician: Musician): this() {
        this.title = title
        this.musician = musician
    }

    var title: String? = null
        protected set

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "musician_id")
    var musician: Musician? = null
        protected set

    fun modifyTitle(_title: String) {
        this.title = _title
    }

}
```
여기서 `private set`이 아니고 `protected set`으로 작성한 것이 궁금할 것이다.

그도 그럴것이 allOpen 플로그인으로 인한 변경사항이기 때문이다.

allOpen이 적용되게 되면 정의된 애노테이션이 붙은 클래스에 대해 open를 열어주게 되는데 이때 프로퍼티 역시 open이 된다.

코틀린에서는 이 경우에는 private를 사용할 수 없다.

아마도 여러분의 IDE에서는 뻘겋게 에러가 날 것이다.

```
Private setters are not allowed for open properties
```

따라서 `protected set`로 세팅하게 된다.

이렇게 되면 외부에서는 접근할 수 없고 제공된 함수로만 변경할 수 있도록 작업이 완료된다.

어째든 이렇게 해서 테스트를 한번 진행해 보자.

적동 방식은 변경이 없기때문에 테스트 코드를 그대로 사용할 수 있다.

다만 lazy loading 이후 album에서 뮤지션 정보를 꺼낼 때 Muscian를 nullable하게 설정했기 때문에 null safe call을 해야한다.

```
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
        assertTrue(album.musician?.name == "Chet Baker")
    }

}
```
자 여기서 여러분들은 이런 생각이 들것이다.

```
앨범의 뮤지션 정보가 nullable한게 맞는거야????

아까 말했듯이 컴필레이션 음반이라고 해도 VA라는 공통 뮤지션 객체로 대체한다면 앨범은 무조건 뮤지션을 가져야지???
```

또한 이런 생각도 들것이다.

```
음반 명이 없는 녀석들이 있을 수 있나???
```

좀 더 엔티티를 명시적으로 작업을 해보자.

컬럼이랑 프로퍼티명이랑 같으면 `@Column`을 생략했는데 좀 더 명확하게 엔티티를 정의해 보자.


```kotlin
@Entity
@Table(name = "musician")
class Musician(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,


) {

    constructor(name: String, genre: GenreCode): this() {
        this.name = name
        this.genre = genre
    }
    
    @Column(name = "name", nullable = false)
    var name: String? = null
        protected set

    @Enumerated(value = EnumType.STRING)
    var genre: GenreCode = GenreCode.ETC
        protected set

    fun modifyName(_name: String) {
        this.name = _name
    }

    fun modifyGenre(_genre: GenreCode) {
        this.genre = _genre
    }
}

@Entity
@DynamicUpdate
@Table(name = "album")
class Album(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

) {

    constructor(title: String, musician: Musician): this() {
        this.title = title
        this.musician = musician
    }

    @Column(name = "title", nullable = false)
    var title: String = ""
        protected set

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "musician_id")
    lateinit var musician: Musician
        protected set

    fun modifyTitle(_title: String) {
        this.title = _title
    }

}
```

뭔가 허술해 보이긴 하지만 명시적으로 `nullable = false`를 두고 초기값을 null이 아닌 빈값으로 세팅을 하면 그나마 좋아보인다.

또한 lateinit으로 선언하므로써 musician에 대한 초기화를 뒤로 미루는 방식으로 처리할 수 있다.

하지만 코드를 간결하게 하면서도 좋아보이게 할 방법이 없을까???

왜냐하면 생성자 코드를 작성하는게 불편하기 때문이다.

게다가 지금이야 프로퍼티가 몇개 없어서 그렇지 많으면 저것도 보기가 불편해진다.

사실 해결책은 일종의 boilerplate code를 활용하는 방식이 가장 무난할 것이다.

예를 들면,

```kotlin
class Test(
    val name: String,
)

class Test1(
    name: String,
) {
    val name = name
}
```

이 두개의 코드는 분명 똑같이 작동하지만 Test1클래스는 마치 자바의 RequiredArg생성자같은 느낌을 준다.

즉, 어떻게 보면 boilerplate code의 향기가 나는 것이다.

자 이것을 다음과 같이 수정을 해보자.

```kotlin
@Entity
@DynamicUpdate
@Table(name = "album")
class Album(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    title: String,
    musician: Musician,

) {
    @Column(name = "title", nullable = false)
    var title = title
        protected set

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var musician = musician
        protected set

    fun modifyTitle(_title: String) {
        this.title = _title
    }

}

@Entity
@Table(name = "musician")
class Musician(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    name: String,
    genre: GenreCode? = GenreCode.ETC,

) {

    @Column(name = "name", nullable = false)
    var name = name
        protected set

    @Enumerated(value = EnumType.STRING)
    var genre = genre
        protected set

    fun modifyName(_name: String) {
        this.name = _name
    }

    fun modifyGenre(_genre: GenreCode) {
        this.genre = _genre
    }
}

```
부 생성자를 따로 작성하는 부분이 전부 사라졌다.

또한 nullable하게 적용하지 않았기 때문에 엔티티를 생성할 때 저 프로퍼티에는 값이 존재해야 한다.

그러면서도 꼴보기 싫었던 부 생성자가 없으니 마음이 편안해진다.

![속이 편안](https://github.com/basquiat78/r2dbc-proxy-and-mysql/blob/main/pyunan.jpeg)

어째든 기존의 테스트 코드는 변경하지 않아도 그대로 진행가능하다.

테스트를 해본다면 원하는 결과를 얻을 수 있는 것이다.

## createdAt/updatedAt 공통화

어떤 데이터가 생성될 때의 시간과 해당 데이터가 변경될 때의 변경 시점을 기록해야 하는 경우가 빈번하게 발생한다.

분명 어떤 엔티티는 단순 코드의 정보이기 때문에 이런 시간을 기록하지 않기도 할 것이고 요구사항에 따라 변경하기도 한다.

따라서 공통 코드가 반복될 상황이 올수 있다.

일반적으로 이런 방법을 공통화 하기 위해서 JPA의 Auditing을 활용하는 경우가 상당히 많다.

main클래스에 `@EnableJpaAuditing`를 붙여서 해당 기능을 사용하겠다고 먼저 알려줘야 한다.

```kotlin
@EnableJpaAuditing
@SpringBootApplication
class RefactoringApplication

fun main(args: Array<String>) {
    runApplication<RefactoringApplication>(*args)
}


@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = now()
        protected set

    @LastModifiedDate
    @Column(name = "updated_at", nullable = true, updatable = false)
    var updatedAt: LocalDateTime = now()
        protected set

}
```

코틀린이나 자바를 떠나서 이런 공통화를 묶은 엔티티의 경우에는 그 자체가 엔티티가 되면 안된다.

따라서 추상 클래스로 작성을 하게 되고 `@MappedSuperclass`를 통해 그 관계를 설정하게 된다.

이 방법이 좋은 점은 그냥 엔티티에서 저걸 상속하면 끝이다.

하지만 어떤 요구사항이 다음과 같다면 이 코드를 그대로 사용할 수 없다.

```
최초 데이터 생성시에는 생성된 날짜만 찍히고 업데이트 된 적이 없으니 해당 컬럼은 null로 비워둔다.

그러다가 데이터의 변경이 생기면 그때 변경된 날짜를 채운다.
```
위의 저 코드는 일단 두 컬럼에 현재 데이터가 생성된 시점의 시간이 찍히게 된다.

`@LastModifiedDate`입장에서는 아마도 새로 생성된 시점의 데이터를 같은 관점에서 보는게 아닌가 생각이 든다.

지금같은 상황이 문제가 없다면 이 방식을 고수해도 무방하지만 위와 같은 요구사항이 오게 되면 약간 변경해야 한다.

```kotlin
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeWithPreUpdateEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = now()
        protected set

    @Column(name = "updated_at", nullable = true, updatable = true)
    lateinit var updatedAt: LocalDateTime
        protected set

    @PreUpdate
    fun updated() {
        updatedAt = now()
    }

}
```

updatedAt컬럼 부분만 lateinit으로 지연 초기화를 시키고 업데이트가 될 때만 해당 날짜를 찍을 수 있게 처리하는 방식이다.

이렇게 하게 되면 최초 데이터 생성시에는 updatedAt컬럼은 null로 들어가고 실제 데이터의 변경 시점에는 해당 날짜가 찍히게 된다.

간혹 엔티티에는 생성된 날짜만 가지는 경우도 있다.

당연히 생각해보면 업데이트된 날짜만 가지는 엔티티는 존재할 수 없을 것이다.

코틀린/자바를 떠나서 JPA를 사용하다보면 간혹 다음과 같이 분리해서 사용하는 경우도 봤다.

```kotlin
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class CreatedAtTimeEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = now()
        protected set

}
```

나름대로 상당히 좋은 방식이라는 생각을 해봤다.

사실 저게 엔티티 작업을 많이 하다보면 은근히 귀찮은 감이 있다.

아니 어떤 엔티티는 두개를 다 가지고 있는데 어떤 넘은 생성 날짜만 가지고 있는거여!!!!!!!!

그럴 때만 생성 날짜만 따로 작업을 해줘야 하니깐 이게 귀찮다는 것이다.

그런데 저렇게 분리하면 필요에 따라 상속해서 사용할 수 있어 반복되는 코드를 줄일 수 있는 최선의 방법일 것이다.

아마 더 좋은 방법은 애초에 데이터베이스 설계시 저 두개를 무조건 묶어서 설계하는 방법이 최선일 것이다.

그래야 일관성도 좀 있어보이니깐!

## Primary Key id 정의

primary key를 정의할 때 몇가지 방법이 존재한다.

그 중 하나가 sequence를 활용한 방식, mySql이라면 `auto_increment`를 활용하는 방식일 것이다.

아니면 UUID를 활용한 방식이 채택되는데 사실 여기에는 장단점이 있다.

내 경우에는 경험한적이 없는 부분이긴 한데 예를 들면 분산 환경의 경우에는 `auto_increment`를 활용하는 방식이 이슈를 불러 일으킨다는 것이다.

하나의 DB에 여러 개의 서버가 분산되었다면 사실 일어나지 않는 케이스겠지만 만일 DB까지 여러개로 분산되었다면 이 아이디값이 충돌할 수 있다는 것이다.

또한 이 방식은 DB를 통해 채번을 하는 방식이다.

oracle, mySql, mariaDB, PostgreSQL - `auto_increment`를 지원하지만 내부적으로 살펴보면 sequence 방식에 가깝다 - 같은 방식은 결국 DB를 통해서 채번을 한다.

시퀀스나 시퀀스 테이블이나 어째든 DB에 책임을 부여하고 이런 부분이 부하를 발생시킨다는 글도 봤다.

아마도 대용량 트래픽과 관련된 내용일 것이다.

게다가 `/api/member/1`같은 API가 있다고 해보자.

여러분들은 해커가 될 수 있다.

```
어 그럼 /api/member/2, /api/member/3 이렇게 호출해 보면 되겠는데?????
```
브루털하게 공격하다보면 해당 사이트의 사용자가 몇 명인지도 알 수 있을지도 모른다!

예전에 nate가 이렇게 털렸었다!

반면에 UUID같은 타입을 생성하는 방식은 랜덤 방식이라 추정이 불가능한 대신 해당 길이만큼의 컬럼의 공간을 차지한다.

게다가 랜덤 방식이라 정렬이 단점으로 꼽힐 것이다.

사용해본 적은 없지만 이것도 ULID라는 방식을 사용하면 정렬을 할 수 있다고 한다.

## UUID 사용하기

이런거 말고

```
class Test(
    val id: String? = UUID.randomUUID().toString(),
)

fun main() {

    val test = Test(UUID.randomUUID().toString())

}

```
외부로부터 넣어주는 방식이 아닌 UUID자체를 사용하는 방식에 대해서 알아보고자 하는 것이다.

게다가 위 방법은 컬럼 자체가 vachar로 설정되기 때문에 확실히 성능에 영향을 줄것이라는 것이 일반적인 견해인거 같다.

게다가 엔티티 식별자를 직접 생성해서 활당하는 방식에는 몇가지 이슈가 있다는 글을 자주 접하게 된다.

예를 들면 우리가 JPA를 사용하다보면 insert/update를 위해 save를 사용한다.

내부적으로 식별자가 없다면 insert, 즉 persist 그리고 있다면 merge를 하는 방식이다.

merge방식은 해당 식별자가 있는지 일단 DB에서 알아봐야 하기 때문에 식별자를 조회하는 행위가 발생한다.

근데 mySql의 `auto_increment`방식이 아닌 위와 같은 방식을 사용하게 되면 엔티티 생성시에 무조건 식별자가 존재하게 된다.

즉 merge방식으로 동작한다는 것이다.

그래서 이것을 해결하기 위해 spring-data에서 제공하는 Persistable를 활용하면 된다.

`@GeneratedValue(strategy = GenerationType.IDENTITY)`를 사용할 때의 save와 동일한 기능을 제공한다고 하니 사용안 할 이유가 없다.

하지만 이 방식을 쓸려면 손이 많이 간다.

Persistable이 제공하는 인터페이스를 구현해야하기 때문이다.

예를 들면 기존 Musician를 Long이 아닌 UUID로 바꿀려면

```kotlin
@Entity
@Table(name = "musician")
class Musician(
    id: UUID? = null,

    name: String,
    genre: GenreCode? = GenreCode.ETC,

): CreatedAtTimeEntity(), Persistable<UUID> {

    @Id
    @Column(length = 16, unique = true, nullable = false)
    private val id: UUID = id ?: UUID.randomUUID()

    @Transient
    private var persisted: Boolean = id != null
    override fun getId(): UUID = id

    override fun isNew() = !persisted

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            other !is Musician -> false
            else -> getId() == other.id
        }
    }

    @PostPersist
    @PostLoad
    protected fun load() {
        persisted = true
    }

    @Column(name = "name", nullable = false)
    var name = name
        protected set

    @Enumerated(value = EnumType.STRING)
    var genre = genre
        protected set

    fun modifyName(_name: String) {
        this.name = _name
    }

    fun modifyGenre(_genre: GenreCode) {
        this.genre = _genre
    }

}
```

저렇게 약간 아스트랄한 방식으로 작업을 해야 한다.

저런 반복될 코드가 각 엔티티마다 전부들어간다면???

그래서 실제로 UUID로 엔티티의 아이디를 정한다면 이 부분을 따로 빼놓고 사용해야 한다.

```kotlin
@MappedSuperclass
abstract class UUIDPrimaryKeyEntity(
    id: UUID? = UUID.randomUUID(),
): Persistable<UUID>, BaseTimeEntity() {

    @Id
    @Column(length = 16, unique = true, nullable = false)
    private val id: UUID = id ?: UUID.randomUUID()

    @Transient
    private var persisted: Boolean = id != null
    override fun getId(): UUID = id

    override fun isNew() = !persisted

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other == null -> false
            other !is HibernateProxy -> false
            other !is UUIDPrimaryKeyEntity -> false
            else -> getId() == this.anyIdentifier(other)
        }
    }

    private fun anyIdentifier(other: Any?): Serializable {
        return if (other is HibernateProxy) {
            other.hibernateLazyInitializer.identifier
        } else {
            (other as UUIDPrimaryKeyOnlyPreUpdateEntity).id
        }
    }

    @PostPersist
    @PostLoad
    protected fun load() {
        persisted = true
    }

}
```

추상 클래스라도 클래스이다.

JVM기반의 언어에서는 클래스는 다중 상속을 허용하지 않는다.

따라서 둘 중 하나이다.

생성한 `UUIDPrimaryKeyEntity`에 `BaseTimeEntity`를 상속해서 사용하던지 해당 로직을 `UUIDPrimaryKeyEntity`로 옮기는 방법이다.

재사용의 입장에서 상속을 하는 것이 가장 시간을 절약하는 방식이다.

따라서 다음과 같이 작성을 하고 Album과 Musician에 위 추상 클래스를 상속해서 사용해 보자.

여기 소스에서는 두가지 방식으로 만든 `BaseTimeEntity`를 기본으로 해보자.

요구사항이 많거나 한다면 각각 분리해서 여러개를 만들어 볼 수 있을수도 있고 인터페이스를 활용할 수도 있겠지만 여기서는 UUID를 사용하는데 집중해보자.

```kotlin
@Entity
@DynamicUpdate
@Table(name = "album")
class Album(

    id: UUID? = null,
    title: String,
    musician: Musician,

): UUIDPrimaryKeyOnlyPreUpdateEntity(id) {

    @Column(name = "title", nullable = false)
    var title = title
        protected set

    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var musician = musician
        protected set

    fun modifyTitle(_title: String) {
        this.title = _title
    }

}

@Entity
@Table(name = "musician")
class Musician(

    id: UUID? = null,
    name: String,
    genre: GenreCode? = GenreCode.ETC,

): UUIDPrimaryKeyOnlyPreUpdateEntity(id) {

    @Column(name = "name", nullable = false)
    var name = name
        protected set

    @Enumerated(value = EnumType.STRING)
    var genre = genre
        protected set

    fun modifyName(_name: String) {
        this.name = _name
    }

    fun modifyGenre(_genre: GenreCode) {
        this.genre = _genre
    }

}


interface MusicianRepository: BaseRepository<Musician, UUID>

interface AlbumRepository: BaseRepository<Album, UUID>
```

Repository부분도 수정을 해주자.

이렇게 공통화 함으로써 코드가 간결해지고 다른 엔티티에서도 쉽게 사용할 수 있게 준비가 되었다.

다만 테스트는 방식을 바꿔야 한다. UUID이기 때문에 값을 추정할 수 없기 때문이다.

게다가 DB를 보게되면 컬럼 data type이 `binary(16)`로 생성된 것을 알 수 있다.

따라서 일반적인 방식으로는 UUID의 스트링값을 알 방법이 없다.

물론 println으로 찍어서 `UUID.fromString()`같은 함수로 UUID로 다시 변환해서 사용할 수 있겠으나 전체 코드 테스트를 한사이클로 실행해보자.

```kotlin
@SpringBootTest
@Transactional
@Rollback(false)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AlbumRepositoryTest @Autowired constructor(
    private val em: EntityManager,
    private val albumRepository: AlbumRepository,
    private val musicianRepository: MusicianRepository,
) {

    lateinit var album: Album

    @BeforeEach
    fun init() {
        // given
        val musician = Musician(
            name = "Chet Baker",
            genre = GenreCode.JAZZ
        )

        val album = Album(
            title = "As Time Goes By",
            musician = musician
        )

        // then 저장전에는 true
        assertTrue(album.isNew)

        // when
        em.persist(album)
        em.flush()

        // then 저장이후에는 false
        assertTrue(!album.isNew)

        this.album = album
        em.detach(this.album)
    }

    @Test
    @Order(1)
    @DisplayName("findByIdOrThrow 테스트")
    fun findByIdOrThrow_Query_TEST() {
        // given
        val id: UUID = album.id

        // when
        val selected = albumRepository.findByIdOrThrow(id)

        // then 조회된 엔티티는 isNew가 false여야 한다.
        assertTrue(!selected.isNew)
        assertTrue(selected.title == album.title)

        // lazy loding
        println("============================= lazy loading =========================")
        // when
        val musician = selected.musician

        val selectedMusician = musicianRepository.findByIdOrThrow(musician.id)

        // then lazy loading으로 가져온 뮤지션과 실제 조회한 뮤지션이 같은지 체크
        assertTrue(musician == selectedMusician)
        // then 조회된 엔티티므로 false
        assertTrue(!musician.isNew)
        assertTrue(musician.name == "Chet Baker")

    }

}
```

`@BeforeEach`를 통해서 미리 저장을 하고 EntityManager를 통해서 flush로 DB에 저장이후 album를 내부 album에 치환해서 준영속성화를 시킨다.

그래야 해당 아이디로 다시 조회를 하는 테스트 코드에서 쿼리가 제대로 나가는지 확인할 수 있다.

그 이후 검증 및 lazy loading까지 확인해 보자

```
Hibernate: 
    /* insert io.basquiat.refactoring.musician.model.entity.Musician
        */ insert 
        into
            musician
            (created_at, updated_at, genre, name, id) 
        values
            (?, ?, ?, ?, ?)
Hibernate: 
    /* insert io.basquiat.refactoring.album.domain.entity.Album
        */ insert 
        into
            album
            (created_at, updated_at, musician_id, title, id) 
        values
            (?, ?, ?, ?, ?)
Hibernate: 
    select
        album0_.id as id1_0_0_,
        album0_.created_at as created_2_0_0_,
        album0_.updated_at as updated_3_0_0_,
        album0_.musician_id as musician5_0_0_,
        album0_.title as title4_0_0_ 
    from
        album album0_ 
    where
        album0_.id=?
============================= lazy loading =========================
Hibernate: 
    select
        musician0_.id as id1_1_0_,
        musician0_.created_at as created_2_1_0_,
        musician0_.updated_at as updated_3_1_0_,
        musician0_.genre as genre4_1_0_,
        musician0_.name as name5_1_0_ 
    from
        musician musician0_ 
    where
        musician0_.id=?
```
테스트는 예상대로 잘 통과하는 것을 확인할 수 있다.

# At a Glance

현재 다뤘던 주제는 코틀린에 특화된 부분도 있고 그와는 무관한 주제를 같이 다뤄봤다.

게다가 이것이 정답은 아닐 것이다.

각자 다른 관점이 있을 수 있기 때문에 다양한 관점에서 바라보는 게 중요한게 아닐까 싶다.

단순하게 코틀린의 간결함을 극한으로 추구한다면 회사내의 코드 컨벤션 문화를 잘 정립하면 될 것이고 좀 더 엄격하게 코드레벨에서 다룰 수도 있다.

어디까지나 상황에 따른 부분이기 때문에 팀 또는 개인적으로 잘 맞는 방식을 찾는게 중요하다.

