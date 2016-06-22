package  tools

import slick.jdbc.JdbcBackend.Database
import slick.codegen.SourceCodeGenerator
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global

object CodeGenMySQL{

  def create() = {
    val driver: JdbcProfile = slick.driver.MySQLDriver

    val db = Database.forURL("jdbc:mysql://localhost:3306/testdb", driver="com.mysql.jdbc.Driver", user="root", password="")

    val modelAction = driver.createModel(Some(driver.defaultTables))
    val modelFuture = db.run(modelAction)
    val codeGenFuture = modelFuture.map(model => new SourceCodeGenerator(model) {
      override def code = "import com.github.tototoshi.slick.MySQLJodaSupport._\n" + "import org.joda.time.DateTime\n" + super.code

      override def Table = new Table(_) {
        override def Column = new Column(_) {
          override def rawType = model.tpe match {
            case "java.sql.Timestamp" => "DateTime" // kill j.s.Timestamp
            case _ => {
              super.rawType
            }
          }
        }
      }
    })

    codeGenFuture.onSuccess {
      case codeGen =>
        println("success")
        codeGen.writeToFile("slick.driver.MySQLDriver", "app/", "models")
    }
    codeGenFuture.onFailure {
      case e =>
        println("error")
        e.printStackTrace()
    }
  }
}
