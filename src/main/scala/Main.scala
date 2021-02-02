import java.io.{BufferedReader, BufferedWriter, FileWriter, InputStreamReader, PrintWriter}
import java.net.Socket
import java.nio.file.{Files, Paths}
import java.util.stream.Collectors


def readResponse(in: BufferedReader, until: Long): String = {
  val builder = StringBuilder("")

  try {
    while (System.currentTimeMillis() < until) {
      val line = in.readLine()
      if (line == null)
        println("line was null - this should not have happened")
      else
        builder ++= line + "\n"
    }
  } catch {
    case e: Exception => ()
  }

  builder.toString()
}

val regex = raw".+\s\s(.+)=(\S+)\s?(.*)".r

def parseResponse(response: String, includeHeader: Boolean): String = {
  val sep = ","

  val pairs = for {
    line <- response.linesIterator.toList
    if regex.matches(line)
  } yield {
    line match {
      case regex(name, value, "") => (name, value)
      case regex(name, value, unit) => (s"$name (in $unit)", value)
    }
  }
  
  def quote(string: String) = "\"" + string + "\""

  val nameSet = pairs.map(pair => quote(pair._1)).toSet
  try {
    val names = nameSet.mkString(",") + "\n"
    val values = pairs.map(pair => quote(pair._2)).grouped(nameSet.size).map(_.mkString(",")).mkString("\n") + "\n"
    return if(includeHeader) names + values else values
  } catch {
    case e: IllegalArgumentException =>
      println(s"response: $response")
      println(s"nameset size: ${nameSet.size}, ${pairs.map(pair => quote(pair._2)).toList}")
  }
  "Error"
}

@main def hello(ip: String, port: Int, interval: Int): Unit = {
  println("Hello world!")
  println(s"ip: $ip, port: $port, interval: $interval")
  
  val filename = "diagnose.csv"

  var first = !Files.exists(Paths.get(filename))
  val fw = new BufferedWriter(new FileWriter(filename, true))

  def readAndAppendResponse(out: PrintWriter, in: BufferedReader) = {
    out.println("t list")
    out.flush()

    val response = readResponse(in, System.currentTimeMillis() + 999)

    val csv = if (first) {
      first = false
      parseResponse(response, true)
    } else
      parseResponse(response, false)

    fw.write(csv)
  }

  try {
    val echoSocket = new Socket(ip, port)
    echoSocket.setSoTimeout(500)
    val out = new PrintWriter(echoSocket.getOutputStream(), true)
    val in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()))
    
    val start = System.currentTimeMillis()
    val secondsInFourWeeks = 60L * 60 * 24 * 28
    
    var currentIteration = 0
    var nextWrite = start
    
    val end = start + secondsInFourWeeks * 1000
    
    while(System.currentTimeMillis() < end) {
      val timeSlot = start + currentIteration * interval * 1000
      
      if(System.currentTimeMillis() < timeSlot) {
        readAndAppendResponse(out, in)
      }
      
      if(System.currentTimeMillis() > nextWrite) {
        nextWrite = nextWrite + 60 * 1000
        fw.flush()
      }
      
      currentIteration += 1
      val amountToSleepFor = timeSlot - System.currentTimeMillis()
      println(amountToSleepFor)
      
      if(amountToSleepFor > 0)
        Thread.sleep(amountToSleepFor)
    }
    
    fw.close()
  } catch {
    case e: Exception => 
      println("error: " + e)
      e.printStackTrace()
  } 
}