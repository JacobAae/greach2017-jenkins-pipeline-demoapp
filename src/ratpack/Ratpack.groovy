import com.greachconf.DemoHandler
import ratpack.groovy.template.MarkupTemplateModule

import static ratpack.groovy.Groovy.groovyMarkupTemplate
import static ratpack.groovy.Groovy.ratpack

ratpack {
  bindings {
    module MarkupTemplateModule
    add(new DemoHandler())
  }

  handlers {
    prefix("greach") {
        get {
            render("Awesome Conference")
        }
    }

      prefix("greeter") {
        get(DemoHandler)
    }
    get {
      render groovyMarkupTemplate("index.gtpl", title: "My Ratpack App")
    }

    files { dir "public" }
  }
}
