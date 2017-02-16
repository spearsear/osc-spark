import java.net.{ServerSocket, Socket}
import java.io._
import resource._

abstract class FetchIterator[T] extends Iterator[T] {
  var fetched = false
  var nextItem : Option[T] = None
  protected def fetchNext(): Option[T]
  override def hasNext = {
    if (!fetched) {
      nextItem = fetchNext()
      fetched = true
    }
    nextItem.isDefined
  }
  override def next() = {
    if (!hasNext) throw new NoSuchElementException("EOF")
      fetched = false
      nextItem.get
  }
}

/** This class creates an iterator from a buffered iterator.  Used to test toTraverable method */
class JavaBufferedReaderLineIterator(br : java.io.BufferedReader) extends FetchIterator[String] {
  override def fetchNext() = br.readLine() match {
    case null => None
    case s => Some(s)
  }
}

class EchoServer extends Thread {
  override def run() : Unit = {
    import resource._
    for {
      server <- managed(new ServerSocket(8007))
      connection <- managed(server.accept)
      outStream <- managed(new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream))))
      input <- managed(new BufferedReader(new InputStreamReader(connection.getInputStream)))
      line <- new JavaBufferedReaderLineIterator(input)
    } {
      outStream.println(line)
      outStream.flush()
    }
  }
}

object EchoClient {
  def main(args : Array[String]) : Unit = {
    for { connection <- managed(new Socket("localhost", 8007))
      outStream <- managed(connection.getOutputStream)
      val out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outStream)))
      inStream <- managed(new InputStreamReader(connection.getInputStream))
      val in = new BufferedReader(inStream)
    } {
      out.println("Test Echo Server!")
      out.flush()
      println("Client Received: " + in.readLine)
    }
  }
}