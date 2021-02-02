
import org.junit.Test
import org.junit.Assert._

class Test1 {
  
  val line1 = raw"T  032:21:56  0001  SO2 STB=0.167 PPB"
  val line2 = raw"T  032:21:56  0001  TIME=21:56:3"
  
  val header = "\"SO2 STB (in PPB)\",\"TIME\"\n"
  val csv = "\"0.167\",\"21:56:3\"\n"
  
  @Test def testRegexMatch(): Unit = {
    assert(regex.matches(line1))
    assert(regex.matches(line2))
  }

  @Test def testRegexGroup(): Unit = {
    assertEquals("SO2 STB", regex.findFirstMatchIn(line1).get.group(1))
    assertEquals("TIME", regex.findFirstMatchIn(line2).get.group(1))

    assertEquals("0.167", regex.findFirstMatchIn(line1).get.group(2))
    assertEquals("21:56:3", regex.findFirstMatchIn(line2).get.group(2))

    assertEquals("PPB", regex.findFirstMatchIn(line1).get.group(3))
    assertEquals("", regex.findFirstMatchIn(line2).get.group(3))
  }

  @Test def testParseResponse(): Unit = {
    assertEquals(csv, parseResponse(line1 + "\n" + line2, false))
    assertEquals(header + csv, parseResponse(line1 + "\n" + line2, true))
    assertEquals(header + csv + csv, parseResponse(line1 + "\n" + line2 +"\n" + line1 + "\n" + line2 , true))
  }
}