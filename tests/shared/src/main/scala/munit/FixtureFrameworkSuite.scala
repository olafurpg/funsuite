package munit

class FixtureFrameworkSuite extends FunSuite {
  def println(msg: String): Unit = TestingConsole.out.println(msg)
  private def fixture(name: String) = new Fixture[Int](name) {
    def apply(): Int = 1
    override def beforeAll(): Unit = {
      println(s"beforeAll($name)")
    }
    override def beforeEach(context: BeforeEach): Unit = {
      println(s"beforeEach($name, ${context.test.name})")
    }
    override def afterEach(context: AfterEach): Unit = {
      println(s"afterEach($name, ${context.test.name})")
    }
    override def afterAll(): Unit = {
      println(s"afterAll($name)")
    }
  }
  private val a = fixture("a")
  private val b = fixture("b")
  private val adhoc = fixture("ad-hoc")
  override val munitFixtures: List[Fixture[Int]] = List(a, b)

  override def beforeAll(): Unit = {
    adhoc.beforeAll()
  }
  override def beforeEach(context: BeforeEach): Unit = {
    adhoc.beforeEach(context)
  }
  override def afterEach(context: AfterEach): Unit = {
    adhoc.afterEach(context)
  }
  override def afterAll(): Unit = {
    adhoc.afterAll()
  }

  1.to(3).foreach { i =>
    test(i.toString()) {
      println(s"test($i)")
    }
  }
}

object FixtureFrameworkSuite
    extends FrameworkTest(
      classOf[FixtureFrameworkSuite],
      """|munit.FixtureFrameworkSuite:
         |beforeAll(ad-hoc)
         |beforeAll(a)
         |beforeAll(b)
         |beforeEach(ad-hoc, 1)
         |beforeEach(a, 1)
         |beforeEach(b, 1)
         |test(1)
         |afterEach(a, 1)
         |afterEach(b, 1)
         |afterEach(ad-hoc, 1)
         |+ 1 <elapsed time>
         |beforeEach(ad-hoc, 2)
         |beforeEach(a, 2)
         |beforeEach(b, 2)
         |test(2)
         |afterEach(a, 2)
         |afterEach(b, 2)
         |afterEach(ad-hoc, 2)
         |+ 2 <elapsed time>
         |beforeEach(ad-hoc, 3)
         |beforeEach(a, 3)
         |beforeEach(b, 3)
         |test(3)
         |afterEach(a, 3)
         |afterEach(b, 3)
         |afterEach(ad-hoc, 3)
         |+ 3 <elapsed time>
         |afterAll(a)
         |afterAll(b)
         |afterAll(ad-hoc)
         |""".stripMargin,
      format = StdoutFormat
    )
