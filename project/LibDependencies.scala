import sbt.{ModuleID, _}
import PlayCrossCompilation._
import play.core.PlayVersion

object LibDependencies {

  val compile: Seq[ModuleID] = dependencies(
    shared = Seq()
  )

  val test: Seq[ModuleID] = dependencies(
    shared = Seq()
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
