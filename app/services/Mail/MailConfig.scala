package services.Mail

import javax.mail.search.SearchTerm
import javax.mail.{Folder, Message, Session, Store}

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by Shota on 2016/06/23.
  */

object MailConfig{
  def default(): MailConfig = {
    val session = Session.getDefaultInstance(System.getProperties)
    val store = session.getStore("imaps")
    new MailConfig(
      store
      ,ConfigFactory.load
    )
  }
}

class MailConfig private(s: Store, c: Config){
  protected var folder: Folder = null
  protected val store : Store = s
  protected val config : Config = c

  def open() = {
    this.store.connect("imap.gmail.com", config.getString("gmail.address"), config.getString("gmail.password"))
    this.folder = store.getFolder(config.getString("gmail.folder"))
    this.folder.open(Folder.READ_ONLY)
  }
  def close() = {
    this.folder.close(true)
    this.store.close()
  }
  def search(term: SearchTerm): Seq[Message] = {
    this.folder.search(term)
  }
}