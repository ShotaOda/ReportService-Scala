package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.MySQLDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(Jobs.schema, Mailfetchlog.schema, Recipienttype.schema, Report.schema, Reportasset.schema, Reportbody.schema, ReportbodyType.schema, Reportheader.schema, User.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Jobs
   *  @param jobCode Database column Job_Code SqlType(VARCHAR), PrimaryKey, Length(3,true)
   *  @param jobDescription Database column Job_Description SqlType(VARCHAR), Length(100,true) */
  case class JobsRow(jobCode: String, jobDescription: String)
  /** GetResult implicit for fetching JobsRow objects using plain SQL queries */
  implicit def GetResultJobsRow(implicit e0: GR[String]): GR[JobsRow] = GR{
    prs => import prs._
    JobsRow.tupled((<<[String], <<[String]))
  }
  /** Table description of table Jobs. Objects of this class serve as prototypes for rows in queries. */
  class Jobs(_tableTag: Tag) extends Table[JobsRow](_tableTag, "Jobs") {
    def * = (jobCode, jobDescription) <> (JobsRow.tupled, JobsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(jobCode), Rep.Some(jobDescription)).shaped.<>({r=>import r._; _1.map(_=> JobsRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column Job_Code SqlType(VARCHAR), PrimaryKey, Length(3,true) */
    val jobCode: Rep[String] = column[String]("Job_Code", O.PrimaryKey, O.Length(3,varying=true))
    /** Database column Job_Description SqlType(VARCHAR), Length(100,true) */
    val jobDescription: Rep[String] = column[String]("Job_Description", O.Length(100,varying=true))
  }
  /** Collection-like TableQuery object for table Jobs */
  lazy val Jobs = new TableQuery(tag => new Jobs(tag))

  /** Entity class storing rows of table Mailfetchlog
   *  @param mailfetchlogId Database column MailFetchLog_ID SqlType(INT), PrimaryKey
   *  @param mailfetchDate Database column MailFetch_Date SqlType(DATETIME) */
  case class MailfetchlogRow(mailfetchlogId: Int, mailfetchDate: java.sql.Timestamp)
  /** GetResult implicit for fetching MailfetchlogRow objects using plain SQL queries */
  implicit def GetResultMailfetchlogRow(implicit e0: GR[Int], e1: GR[java.sql.Timestamp]): GR[MailfetchlogRow] = GR{
    prs => import prs._
    MailfetchlogRow.tupled((<<[Int], <<[java.sql.Timestamp]))
  }
  /** Table description of table MailFetchLog. Objects of this class serve as prototypes for rows in queries. */
  class Mailfetchlog(_tableTag: Tag) extends Table[MailfetchlogRow](_tableTag, "MailFetchLog") {
    def * = (mailfetchlogId, mailfetchDate) <> (MailfetchlogRow.tupled, MailfetchlogRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(mailfetchlogId), Rep.Some(mailfetchDate)).shaped.<>({r=>import r._; _1.map(_=> MailfetchlogRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column MailFetchLog_ID SqlType(INT), PrimaryKey */
    val mailfetchlogId: Rep[Int] = column[Int]("MailFetchLog_ID", O.PrimaryKey)
    /** Database column MailFetch_Date SqlType(DATETIME) */
    val mailfetchDate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("MailFetch_Date")

    /** Uniqueness Index over (mailfetchDate) (database name MailFetch_Date_UNIQUE) */
    val index1 = index("MailFetch_Date_UNIQUE", mailfetchDate, unique=true)
  }
  /** Collection-like TableQuery object for table Mailfetchlog */
  lazy val Mailfetchlog = new TableQuery(tag => new Mailfetchlog(tag))

  /** Entity class storing rows of table Recipienttype
   *  @param recipienttypeCode Database column RecipientType_Code SqlType(VARCHAR), PrimaryKey, Length(3,true) */
  case class RecipienttypeRow(recipienttypeCode: String)
  /** GetResult implicit for fetching RecipienttypeRow objects using plain SQL queries */
  implicit def GetResultRecipienttypeRow(implicit e0: GR[String]): GR[RecipienttypeRow] = GR{
    prs => import prs._
    RecipienttypeRow(<<[String])
  }
  /** Table description of table RecipientType. Objects of this class serve as prototypes for rows in queries. */
  class Recipienttype(_tableTag: Tag) extends Table[RecipienttypeRow](_tableTag, "RecipientType") {
    def * = recipienttypeCode <> (RecipienttypeRow, RecipienttypeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = Rep.Some(recipienttypeCode).shaped.<>(r => r.map(_=> RecipienttypeRow(r.get)), (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column RecipientType_Code SqlType(VARCHAR), PrimaryKey, Length(3,true) */
    val recipienttypeCode: Rep[String] = column[String]("RecipientType_Code", O.PrimaryKey, O.Length(3,varying=true))
  }
  /** Collection-like TableQuery object for table Recipienttype */
  lazy val Recipienttype = new TableQuery(tag => new Recipienttype(tag))

  /** Entity class storing rows of table Report
   *  @param reportId Database column Report_ID SqlType(INT), AutoInc
   *  @param userId Database column User_ID SqlType(INT)
   *  @param reportTitle Database column Report_Title SqlType(VARCHAR), Length(200,true)
   *  @param reportSentdate Database column Report_SentDate SqlType(DATETIME) */
  case class ReportRow(reportId: Int, userId: Int, reportTitle: String, reportSentdate: java.sql.Timestamp)
  /** GetResult implicit for fetching ReportRow objects using plain SQL queries */
  implicit def GetResultReportRow(implicit e0: GR[Int], e1: GR[String], e2: GR[java.sql.Timestamp]): GR[ReportRow] = GR{
    prs => import prs._
    ReportRow.tupled((<<[Int], <<[Int], <<[String], <<[java.sql.Timestamp]))
  }
  /** Table description of table Report. Objects of this class serve as prototypes for rows in queries. */
  class Report(_tableTag: Tag) extends Table[ReportRow](_tableTag, "Report") {
    def * = (reportId, userId, reportTitle, reportSentdate) <> (ReportRow.tupled, ReportRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(reportId), Rep.Some(userId), Rep.Some(reportTitle), Rep.Some(reportSentdate)).shaped.<>({r=>import r._; _1.map(_=> ReportRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column Report_ID SqlType(INT), AutoInc */
    val reportId: Rep[Int] = column[Int]("Report_ID", O.AutoInc)
    /** Database column User_ID SqlType(INT) */
    val userId: Rep[Int] = column[Int]("User_ID")
    /** Database column Report_Title SqlType(VARCHAR), Length(200,true) */
    val reportTitle: Rep[String] = column[String]("Report_Title", O.Length(200,varying=true))
    /** Database column Report_SentDate SqlType(DATETIME) */
    val reportSentdate: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("Report_SentDate")

    /** Primary key of Report (database name Report_PK) */
    val pk = primaryKey("Report_PK", (reportId, userId))

    /** Foreign key referencing User (database name fk_Report_User1) */
    lazy val userFk = foreignKey("fk_Report_User1", userId, User)(r => r.userId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Report */
  lazy val Report = new TableQuery(tag => new Report(tag))

  /** Entity class storing rows of table Reportasset
   *  @param reportassetId Database column ReportAsset_ID SqlType(INT), AutoInc, PrimaryKey
   *  @param reportassetCid Database column ReportAsset_CID SqlType(VARCHAR), Length(50,true)
   *  @param reportbodyId Database column ReportBody_ID SqlType(INT)
   *  @param reportasset Database column ReportAsset SqlType(BLOB), Default(None)
   *  @param reportassetExtention Database column ReportAsset_Extention SqlType(VARCHAR), Length(10,true), Default(None) */
  case class ReportassetRow(reportassetId: Int, reportassetCid: String, reportbodyId: Int, reportasset: Option[java.sql.Blob] = None, reportassetExtention: Option[String] = None)
  /** GetResult implicit for fetching ReportassetRow objects using plain SQL queries */
  implicit def GetResultReportassetRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[java.sql.Blob]], e3: GR[Option[String]]): GR[ReportassetRow] = GR{
    prs => import prs._
    ReportassetRow.tupled((<<[Int], <<[String], <<[Int], <<?[java.sql.Blob], <<?[String]))
  }
  /** Table description of table ReportAsset. Objects of this class serve as prototypes for rows in queries. */
  class Reportasset(_tableTag: Tag) extends Table[ReportassetRow](_tableTag, "ReportAsset") {
    def * = (reportassetId, reportassetCid, reportbodyId, reportasset, reportassetExtention) <> (ReportassetRow.tupled, ReportassetRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(reportassetId), Rep.Some(reportassetCid), Rep.Some(reportbodyId), reportasset, reportassetExtention).shaped.<>({r=>import r._; _1.map(_=> ReportassetRow.tupled((_1.get, _2.get, _3.get, _4, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ReportAsset_ID SqlType(INT), AutoInc, PrimaryKey */
    val reportassetId: Rep[Int] = column[Int]("ReportAsset_ID", O.AutoInc, O.PrimaryKey)
    /** Database column ReportAsset_CID SqlType(VARCHAR), Length(50,true) */
    val reportassetCid: Rep[String] = column[String]("ReportAsset_CID", O.Length(50,varying=true))
    /** Database column ReportBody_ID SqlType(INT) */
    val reportbodyId: Rep[Int] = column[Int]("ReportBody_ID")
    /** Database column ReportAsset SqlType(BLOB), Default(None) */
    val reportasset: Rep[Option[java.sql.Blob]] = column[Option[java.sql.Blob]]("ReportAsset", O.Default(None))
    /** Database column ReportAsset_Extention SqlType(VARCHAR), Length(10,true), Default(None) */
    val reportassetExtention: Rep[Option[String]] = column[Option[String]]("ReportAsset_Extention", O.Length(10,varying=true), O.Default(None))

    /** Foreign key referencing Reportbody (database name fk_ReportAsset_ReportBody1) */
    lazy val reportbodyFk = foreignKey("fk_ReportAsset_ReportBody1", reportbodyId, Reportbody)(r => r.reportbodyId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Reportasset */
  lazy val Reportasset = new TableQuery(tag => new Reportasset(tag))

  /** Entity class storing rows of table Reportbody
   *  @param reportbodyId Database column ReportBody_ID SqlType(INT), AutoInc
   *  @param reportId Database column Report_ID SqlType(INT)
   *  @param reportbodyTypeCode Database column ReportBody_Type_Code SqlType(VARCHAR), Length(10,true)
   *  @param reportbody Database column ReportBody SqlType(TEXT) */
  case class ReportbodyRow(reportbodyId: Int, reportId: Int, reportbodyTypeCode: String, reportbody: String)
  /** GetResult implicit for fetching ReportbodyRow objects using plain SQL queries */
  implicit def GetResultReportbodyRow(implicit e0: GR[Int], e1: GR[String]): GR[ReportbodyRow] = GR{
    prs => import prs._
    ReportbodyRow.tupled((<<[Int], <<[Int], <<[String], <<[String]))
  }
  /** Table description of table ReportBody. Objects of this class serve as prototypes for rows in queries. */
  class Reportbody(_tableTag: Tag) extends Table[ReportbodyRow](_tableTag, "ReportBody") {
    def * = (reportbodyId, reportId, reportbodyTypeCode, reportbody) <> (ReportbodyRow.tupled, ReportbodyRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(reportbodyId), Rep.Some(reportId), Rep.Some(reportbodyTypeCode), Rep.Some(reportbody)).shaped.<>({r=>import r._; _1.map(_=> ReportbodyRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ReportBody_ID SqlType(INT), AutoInc */
    val reportbodyId: Rep[Int] = column[Int]("ReportBody_ID", O.AutoInc)
    /** Database column Report_ID SqlType(INT) */
    val reportId: Rep[Int] = column[Int]("Report_ID")
    /** Database column ReportBody_Type_Code SqlType(VARCHAR), Length(10,true) */
    val reportbodyTypeCode: Rep[String] = column[String]("ReportBody_Type_Code", O.Length(10,varying=true))
    /** Database column ReportBody SqlType(TEXT) */
    val reportbody: Rep[String] = column[String]("ReportBody")

    /** Primary key of Reportbody (database name ReportBody_PK) */
    val pk = primaryKey("ReportBody_PK", (reportbodyId, reportId, reportbodyTypeCode))

    /** Foreign key referencing Report (database name fk_ReportBody_Report1) */
    lazy val reportFk = foreignKey("fk_ReportBody_Report1", reportId, Report)(r => r.reportId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing ReportbodyType (database name fk_ReportBody_ReportBody_TYPE1) */
    lazy val reportbodyTypeFk = foreignKey("fk_ReportBody_ReportBody_TYPE1", reportbodyTypeCode, ReportbodyType)(r => r.reportbodyTypeCode, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Reportbody */
  lazy val Reportbody = new TableQuery(tag => new Reportbody(tag))

  /** Entity class storing rows of table ReportbodyType
   *  @param reportbodyTypeCode Database column ReportBody_Type_Code SqlType(VARCHAR), PrimaryKey, Length(10,true) */
  case class ReportbodyTypeRow(reportbodyTypeCode: String)
  /** GetResult implicit for fetching ReportbodyTypeRow objects using plain SQL queries */
  implicit def GetResultReportbodyTypeRow(implicit e0: GR[String]): GR[ReportbodyTypeRow] = GR{
    prs => import prs._
    ReportbodyTypeRow(<<[String])
  }
  /** Table description of table ReportBody_TYPE. Objects of this class serve as prototypes for rows in queries. */
  class ReportbodyType(_tableTag: Tag) extends Table[ReportbodyTypeRow](_tableTag, "ReportBody_TYPE") {
    def * = reportbodyTypeCode <> (ReportbodyTypeRow, ReportbodyTypeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = Rep.Some(reportbodyTypeCode).shaped.<>(r => r.map(_=> ReportbodyTypeRow(r.get)), (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ReportBody_Type_Code SqlType(VARCHAR), PrimaryKey, Length(10,true) */
    val reportbodyTypeCode: Rep[String] = column[String]("ReportBody_Type_Code", O.PrimaryKey, O.Length(10,varying=true))
  }
  /** Collection-like TableQuery object for table ReportbodyType */
  lazy val ReportbodyType = new TableQuery(tag => new ReportbodyType(tag))

  /** Entity class storing rows of table Reportheader
   *  @param reportId Database column Report_ID SqlType(INT)
   *  @param recipienttypeCode Database column RecipientType_Code SqlType(VARCHAR), Length(3,true)
   *  @param recipientAddress Database column Recipient_Address SqlType(VARCHAR), Length(45,true) */
  case class ReportheaderRow(reportId: Int, recipienttypeCode: String, recipientAddress: String)
  /** GetResult implicit for fetching ReportheaderRow objects using plain SQL queries */
  implicit def GetResultReportheaderRow(implicit e0: GR[Int], e1: GR[String]): GR[ReportheaderRow] = GR{
    prs => import prs._
    ReportheaderRow.tupled((<<[Int], <<[String], <<[String]))
  }
  /** Table description of table ReportHeader. Objects of this class serve as prototypes for rows in queries. */
  class Reportheader(_tableTag: Tag) extends Table[ReportheaderRow](_tableTag, "ReportHeader") {
    def * = (reportId, recipienttypeCode, recipientAddress) <> (ReportheaderRow.tupled, ReportheaderRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(reportId), Rep.Some(recipienttypeCode), Rep.Some(recipientAddress)).shaped.<>({r=>import r._; _1.map(_=> ReportheaderRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column Report_ID SqlType(INT) */
    val reportId: Rep[Int] = column[Int]("Report_ID")
    /** Database column RecipientType_Code SqlType(VARCHAR), Length(3,true) */
    val recipienttypeCode: Rep[String] = column[String]("RecipientType_Code", O.Length(3,varying=true))
    /** Database column Recipient_Address SqlType(VARCHAR), Length(45,true) */
    val recipientAddress: Rep[String] = column[String]("Recipient_Address", O.Length(45,varying=true))

    /** Primary key of Reportheader (database name ReportHeader_PK) */
    val pk = primaryKey("ReportHeader_PK", (reportId, recipienttypeCode, recipientAddress))

    /** Foreign key referencing Recipienttype (database name fk_ReportHeader_RecipientType1) */
    lazy val recipienttypeFk = foreignKey("fk_ReportHeader_RecipientType1", recipienttypeCode, Recipienttype)(r => r.recipienttypeCode, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing Report (database name fk_ReportHeader_Report1) */
    lazy val reportFk = foreignKey("fk_ReportHeader_Report1", reportId, Report)(r => r.reportId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Reportheader */
  lazy val Reportheader = new TableQuery(tag => new Reportheader(tag))

  /** Entity class storing rows of table User
   *  @param userId Database column User_ID SqlType(INT), AutoInc
   *  @param userName Database column User_Name SqlType(VARCHAR), Length(20,true)
   *  @param userPassword Database column User_Password SqlType(VARCHAR), Length(200,true)
   *  @param userAddress Database column User_Address SqlType(VARCHAR), Length(200,true)
   *  @param jobsJobCode Database column Jobs_Job_Code SqlType(VARCHAR), Length(3,true) */
  case class UserRow(userId: Int, userName: String, userPassword: String, userAddress: String, jobsJobCode: String)
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Int], e1: GR[String]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[Int], <<[String], <<[String], <<[String], <<[String]))
  }
  /** Table description of table User. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends Table[UserRow](_tableTag, "User") {
    def * = (userId, userName, userPassword, userAddress, jobsJobCode) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(userName), Rep.Some(userPassword), Rep.Some(userAddress), Rep.Some(jobsJobCode)).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column User_ID SqlType(INT), AutoInc */
    val userId: Rep[Int] = column[Int]("User_ID", O.AutoInc)
    /** Database column User_Name SqlType(VARCHAR), Length(20,true) */
    val userName: Rep[String] = column[String]("User_Name", O.Length(20,varying=true))
    /** Database column User_Password SqlType(VARCHAR), Length(200,true) */
    val userPassword: Rep[String] = column[String]("User_Password", O.Length(200,varying=true))
    /** Database column User_Address SqlType(VARCHAR), Length(200,true) */
    val userAddress: Rep[String] = column[String]("User_Address", O.Length(200,varying=true))
    /** Database column Jobs_Job_Code SqlType(VARCHAR), Length(3,true) */
    val jobsJobCode: Rep[String] = column[String]("Jobs_Job_Code", O.Length(3,varying=true))

    /** Primary key of User (database name User_PK) */
    val pk = primaryKey("User_PK", (userId, jobsJobCode))

    /** Foreign key referencing Jobs (database name fk_User_Jobs) */
    lazy val jobsFk = foreignKey("fk_User_Jobs", jobsJobCode, Jobs)(r => r.jobCode, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)

    /** Uniqueness Index over (userAddress) (database name User_Address_UNIQUE) */
    val index1 = index("User_Address_UNIQUE", userAddress, unique=true)
    /** Uniqueness Index over (userName) (database name User_Name_UNIQUE) */
    val index2 = index("User_Name_UNIQUE", userName, unique=true)
  }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))
}
