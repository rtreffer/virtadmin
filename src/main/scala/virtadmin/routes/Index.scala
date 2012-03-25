package virtadmin.routes

object Index extends Index

class Index extends virtadmin.Layout {
  def index = GET {
    respondView()
  }
}
